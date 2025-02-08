package net.deanly.solana.sdk.rpc.client.websocket.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.rpc.client.RpcClient;
import net.deanly.solana.sdk.rpc.client.adapter.*;
import net.deanly.solana.sdk.rpc.client.exception.RpcException;
import net.deanly.solana.sdk.rpc.client.exception.RpcWebSocketException;
import net.deanly.solana.sdk.rpc.client.websocket.NotificationListener;
import net.deanly.solana.sdk.rpc.client.websocket.WebsocketMethodApi;
import net.deanly.solana.sdk.rpc.request.RpcRequest;
import net.deanly.solana.sdk.rpc.request.config.AccountSubscriptionConfig;
import net.deanly.solana.sdk.rpc.response.*;
import net.deanly.solana.sdk.types.*;
import net.deanly.solana.sdk.rpc.response.ResValueProgram;
import okhttp3.*;
import com.google.common.cache.Cache;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MoshiWebsocketMethodApiImpl implements WebsocketMethodApi {
    private final Cache<SubscriptionId, SubscriptionContext<?>> listeners = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(); // Concurrency Safe
    private final Cache<Long, CompletableFuture<SubscriptionId>> pendingSubscriptions = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(); // Concurrency Safe

    private final RpcClient.ClientConfig config;
    private OkHttpClient httpClient;
    private WebSocket webSocket;

    private final Moshi moshi = new Moshi.Builder()
            .add(MoshiFilterCriteriaJsonAdapter.FACTORY)
            .add(UnsignedLong.class, new MoshiUnsignedLongJsonAdapter())
            .add(StateData.class, new MoshiEncodedDataJsonAdapter())
            .add(PublicKey.class, new MoshiPublicKeyJsonAdapter())
            .add(Blockhash.class, new MoshiBlockhashJsonAdapter())
            .add(GenesisHash.class, new MoshiGenesisHashJsonAdapter())
            .add(Signature.class, new MoshiSignatureJsonAdapter())
            .add(EpochCredits.class, new MoshiEpochCreditsJsonAdapter())
            .add(ValidatorIdentityInfo.class, new MoshiValidatorIdentityInfoJsonAdapter())
            .add(SubscriptionId.class, new MoshiSubscriptionIdJsonAdapter())
            .build();

    private JsonAdapter<RpcRequest> rpcRequestJsonAdapter = moshi.adapter(RpcRequest.class);

    @RequiredArgsConstructor
    private static class SubscriptionContext<T> {
        final Type type;
        final NotificationListener<T> listener;
        final String method;
        final List<Object> params;
    }

    public MoshiWebsocketMethodApiImpl(RpcClient.ClientConfig config) {
        this.config = config;
        this.httpClient = createHttpClient();
        this.webSocket = connectWebSocket();
    }

    protected OkHttpClient createHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(this.config.getReadTimeoutMs(), TimeUnit.MICROSECONDS);

        if (this.config.getWriteTimeoutMs() != null) {
            builder.readTimeout(this.config.getWriteTimeoutMs(), TimeUnit.MICROSECONDS);
        }
        if (this.config.getConnectTimeoutMs() != null) {
            builder.connectTimeout(this.config.getConnectTimeoutMs(), TimeUnit.MICROSECONDS);
        }
        if (this.config.getProxyHost() != null && this.config.getProxyPort() != null) {
            builder.proxy(new Proxy(
                    Proxy.Type.SOCKS,
                    new InetSocketAddress(
                            this.config.getProxyHost(),
                            this.config.getProxyPort()
                    )
            ));
        }
        if (this.config.getUserAgent() != null) {
            builder.addNetworkInterceptor(chain -> chain.proceed(
                    chain.request().newBuilder().header("User-Agent", this.config.getUserAgent()).build()));
        }

        return builder.build();
    }

    protected WebSocket connectWebSocket() {
        try {
            URI endpointURI = new URI(config.getEndpoint());
            String scheme = "https".equals(endpointURI.getScheme()) ? "wss" : "ws";
            String endpointURL = (new URI(scheme + "://" + endpointURI.getHost())).toString();

            Request request = new Request.Builder().url(endpointURL).build();

            return this.httpClient.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                    log.info("WebSocket connected");
                }

                @Override
                public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                    handleMessage(text); // 메시지 핸들링
                }

                @Override
                public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                    log.warn("WebSocket closed: " + reason);
                    // 재연결 처리
                    reconnectWebSocket();
                }

                @Override
                public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
                    log.error("WebSocket failure: " + t.getMessage());
                    // 재연결 처리
                    reconnectWebSocket();
                }
            });
        } catch (Exception e) {
            throw new RpcWebSocketException("Failed to connect to WebSocket", e);
        }
    }

    private int reconnectDelay = 1000; // 초기 1초

    private void reconnectWebSocket() {
        log.info("Reconnecting WebSocket... (delay: {} ms)", reconnectDelay);
        try {
            Thread.sleep(reconnectDelay);
            connectWebSocket();
            resubscribeAll();
            reconnectDelay = 1000; // 성공 시 초기화
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // 지수 백오프 방식
            reconnectDelay = Math.min(reconnectDelay * 2, 30000); // 최대 30초
            reconnectWebSocket();
        }
    }


    @Getter
    public class IdSubscriptionTuple {
        private final Long requestId;
        private final SubscriptionId subscription;
        public IdSubscriptionTuple(Long requestId, Long subscription) {
            this.requestId = requestId;
            this.subscription = subscription != null ? SubscriptionId.of(subscription) : null;
        }
    }

    protected void handleMessage(String message) {
        try {
            // "id"와 "subscription" 값을 한 번에 추출
            IdSubscriptionTuple tuple = extractIdAndSubscription(message);

            if (tuple.getRequestId() != null) {
                handleSubscriptionResponse(message, tuple.getRequestId()); // id와 함께 처리
                return;
            }

            if (tuple.getSubscription() != null) {
                handleNotification(message, tuple.getSubscription()); // subscription과 함께 처리
                return;
            }

            log.warn("Unrecognized message format: {}", message);
        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage());
        }
    }

    /**
     * JSON의 "id"는 1-depth에서, "subscription"은 2-depth("params.subscription")에서 값을 추출.
     */
    private IdSubscriptionTuple extractIdAndSubscription(String json) throws IOException {
        JsonReader reader = JsonReader.of(new okio.Buffer().writeUtf8(json));
        reader.beginObject(); // JSON 객체의 시작

        Long id = null;
        Long subscription = null;

        while (reader.hasNext()) {
            String key = reader.nextName();
            switch (key) {
                case "id" -> id = reader.nextLong(); // "id"는 1-depth
                case "params" -> subscription = extractSubscriptionFromParams(reader); // "params.subscription"은 2-depth
                default -> reader.skipValue(); // 다른 값은 스킵
            }

            // 둘중 하나만 추출되면 더 이상 순회할 필요 없음
            if (id != null || subscription != null) {
                break;
            }
        }

        reader.endObject();
        return new IdSubscriptionTuple(id, subscription);
    }

    /**
     * "params.subscription"에서 subscription 값을 추출.
     */
    private Long extractSubscriptionFromParams(JsonReader reader) throws IOException {
        reader.beginObject(); // "params" 객체의 시작
        Long subscription = null;

        while (reader.hasNext()) {
            String key = reader.nextName();
            if ("subscription".equals(key)) {
                subscription = reader.nextLong(); // "subscription" 값 추출
            } else {
                reader.skipValue(); // 다른 값은 스킵
            }

            // subscription 값이 추출되면 더 이상 순회할 필요 없음
            if (subscription != null) {
                break;
            }
        }

        reader.endObject();
        return subscription;
    }

    private void handleSubscriptionResponse(String message, Long id) throws IOException {
        JsonAdapter<RpcResponse<SubscriptionId>> responseAdapter = this.getCachedAdapter(Types.newParameterizedType(RpcResponse.class, SubscriptionId.class));
        RpcResponse<SubscriptionId> response = responseAdapter.fromJson(message);

        if (response != null) {
            Long requestId = response.getId();
            CompletableFuture<SubscriptionId> future = getPendingSubscription(requestId);
            removePendingSubscription(requestId);

            if (future != null) {
                if (response.getError() != null) {
                    future.completeExceptionally(new RpcWebSocketException(response.getError().getMessage()));
                    log.error("Subscription error: {}", response.getError().getMessage());
                } else {
                    future.complete(response.getResult());
                }
            } else {
                log.warn("No pending subscription found for request ID: {}", requestId);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void handleNotification(String message, SubscriptionId id) throws IOException {
        SubscriptionContext<?> context = getListener(id);
        if (context == null) {
            log.warn("No listener found for subscription ID: {}", id);
            return;
        }
        JsonAdapter<RpcNotification<Object>> adapter = getCachedAdapter(context.type);
        RpcNotification<Object> notification = adapter.fromJson(message);

        if (notification != null && notification.getParams() != null) {
            SubscriptionId subscriptionId = notification.getParams().getSubscription();
            if (subscriptionId != null && !id.equals(subscriptionId)) {
                log.warn("Subscription ID mismatch: expected: {}, actual: {}", id, subscriptionId);
                return;
            }

            RpcNotification<Object> typedNotification = (RpcNotification<Object>) this.getCachedAdapter(context.type).fromJson(message);
            NotificationListener<Object> listener = (NotificationListener<Object>) context.listener;
            listener.onNotification(typedNotification);
        }
    }

    private <T> RpcNotification<T> parseTypedNotification(String message, Type valueType) throws IOException {
        JsonAdapter<RpcNotification<T>> adapter = moshi.adapter(
                Types.newParameterizedType(RpcNotification.class, valueType)
        );
        return adapter.fromJson(message);
    }

    private void resubscribeAll() {
        log.info("Resubscribing to all active subscriptions...");

        listeners.asMap().forEach((subscriptionId, context) -> {
            try {
                RpcRequest request = buildResubscribeRequest(subscriptionId, context);
                webSocket.send(toJson(request, context.type)); // 재구독 요청 전송
                log.info("Resubscribed to subscription ID: {}", subscriptionId);
            } catch (Exception e) {
                log.error("Failed to resubscribe for ID: {}", subscriptionId, e);
            }
        });
    }

    private RpcRequest buildResubscribeRequest(SubscriptionId subscriptionId, SubscriptionContext<?> context) {
        String method = context.method;
        List<Object> params = context.params;

//        return new RpcRequest(method, params);
        return new RpcRequest(method, params, subscriptionId.getValue());
    }

    private SubscriptionContext<?> getListener(SubscriptionId id) {
        return listeners.getIfPresent(id);  // 없으면 null 반환
    }

    private CompletableFuture<SubscriptionId> getPendingSubscription(Long id) {
        return pendingSubscriptions.getIfPresent(id);
    }

    private void removeListener(SubscriptionId id) {
        listeners.invalidate(id);
    }

    private void removePendingSubscription(Long id) {
        pendingSubscriptions.invalidate(id);
    }

    public Long countListeners() {
        return this.listeners.size();
    }

    public void cleanupListeners() {
        this.listeners.cleanUp();
    }

    /// Moshi Adapter Cache
    private final Map<Type, JsonAdapter<?>> adapterCache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private <T> JsonAdapter<T> getCachedAdapter(Type responseType) {
        return (JsonAdapter<T>) adapterCache.computeIfAbsent(responseType, moshi::adapter);
    }

    private String toJson(Object obj, Type type) {
        try {
            JsonAdapter<Type> adapter = this.getCachedAdapter(type);
            return adapter.toJson((Type) obj);
        } catch (Exception e) {
            throw new RpcWebSocketException("JSON serialization failed", e);
        }
    }

    private List<Object> getParams(Object... params) {
        if (params == null || params.length == 0) {
            return null;
        }

        List<Object> result = new ArrayList<>(Arrays.asList(params));

        if (result.stream().allMatch(Objects::isNull)) {
            return null;
        }

        for (int i = result.size() - 1; i >= 0; i--) {
            if (result.get(i) != null) {
                break;
            }
            result.remove(i);
        }

        return result;
    }

    private <T> T call(String method, List<Object> params, Type responseType, Type errorDataType) throws RpcException {
        RpcRequest rpcRequest = createRpcRequest(method, params);

        JsonAdapter<RpcResponse<T>> resultAdapter = getCachedAdapter(responseType);

        Request request = new Request.Builder().url(this.config.getEndpoint())
                .post(RequestBody.create(rpcRequestJsonAdapter.toJson(rpcRequest), this.config.getMediaType()))
                .build();

        try {
            Response response = httpClient.newCall(request).execute();
            final String result = Objects.requireNonNull(response.body()).string();
            RpcResponse<T> rpcResponse = resultAdapter.fromJson(result);

            if (rpcResponse == null) {
                throw new RpcException("Failed to parse RpcResponse: Response is null");
            }

            if (rpcResponse.getError() != null) {
                RpcResponse.Error error = rpcResponse.getError();

                Map<String, Object> errorData = null;
                if (error.getData() != null) {
                    try {
                        if (errorDataType != null) {
                            JsonAdapter<Map<String, Object>> mapAdapter = getCachedAdapter(errorDataType);
                            errorData = mapAdapter.fromJsonValue(error.getData());
                        } else {
                            Type mapType = Types.newParameterizedType(Map.class, String.class, Object.class);
                            JsonAdapter<Map<String, Object>> mapAdapter = getCachedAdapter(mapType);
                            errorData = mapAdapter.fromJsonValue(error.getData());
                        }
                    } catch (Exception e) {
                        throw new RpcException(
                                "Failed to parse error data: " + e.getMessage(),
                                (int) error.getCode(),
                                null
                        );
                    }
                }

                throw new RpcException(error.getMessage(), (int) error.getCode(), errorData);
            }

            return rpcResponse.getResult();

        } catch (SSLHandshakeException e) {
            this.httpClient = this.createHttpClient();
            throw new RpcException("SSL Handshake failed: " + e.getMessage());
        } catch (IOException e) {
            throw new RpcException("IO error during RPC call: " + e.getMessage());
        }
    }

    protected RpcRequest createRpcRequest(String method, List<Object> params) {
        return new RpcRequest(method, params);
    }

    ///  공통 Subscribe
    private <T> RpcResponse<SubscriptionId> subscribe(
            String method,
            List<Object> params,
            Type type,
            NotificationListener<T> listener) {
        RpcRequest request = createRpcRequest(method, params);

        Long id = request.getId();
        CompletableFuture<SubscriptionId> subscriptionFuture = new CompletableFuture<>();
        this.pendingSubscriptions.put(id, subscriptionFuture);

        Type requestType = Types.newParameterizedType(RpcRequest.class, RpcRequest.class);
        JsonAdapter<RpcRequest> requestJsonAdapter = this.getCachedAdapter(requestType);
        this.webSocket.send(requestJsonAdapter.toJson(request));

        try {
            SubscriptionId subscriptionId = subscriptionFuture.get(5, TimeUnit.SECONDS);

            this.listeners.put(subscriptionId, new SubscriptionContext<>(type, listener, method, params));
            return RpcResponse.<SubscriptionId>builder().result(subscriptionId).build();
        } catch (Exception e) {
            log.error("Subscription failed: {}", e.getMessage());
            throw new RpcWebSocketException("Subscription timeout", e);
        }
    }

    ///  공통 Unsubscribe
    private RpcResponse<Boolean> unsubscribe(String method, SubscriptionId subscriptionId) {
        try {
            Type type = Types.newParameterizedType(RpcResponse.class, Boolean.class);
            return this.call(method, List.of(subscriptionId), type, null);
        } catch (RpcException e) {
            throw new RpcWebSocketException(e.getMessage(), e);
        } finally {
            this.removeListener(subscriptionId);
        }
    }

    /*
       API Methods
     */

    @Override
    public RpcResponse<SubscriptionId> accountSubscribe(
            PublicKey accountKey,
            AccountSubscriptionConfig config,
            NotificationListener<RpcNotificationV2<ResValueAccountInfo>> listener
    ) {
        Objects.requireNonNull(accountKey, "accountKey must not be null");
        Objects.requireNonNull(listener, "listener must not be null");
        Type type = Types.newParameterizedType(RpcNotificationV2.class, ResValueAccountInfo.class);
        return this.subscribe("accountSubscribe", this.getParams(accountKey, config), type, listener);
    }

    @Override
    public RpcResponse<Boolean> accountUnsubscribe(SubscriptionId subscriptionId) {
        return this.unsubscribe("accountUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<SubscriptionId> blockSubscribe(Commitment commitment, String encoding, NotificationListener<RpcNotificationV2<ResValueBlock>> listener) {
        List<Object> params = List.of(
                Map.of("commitment", commitment, "encoding", encoding)
        );
        Type type = Types.newParameterizedType(
                RpcNotificationV2.class,
                ResValueBlock.class
        );
        return this.subscribe("blockSubscribe", params, type, listener);
    }

    @Override
    public RpcResponse<Boolean> blockUnsubscribe(SubscriptionId subscriptionId) {
        return this.unsubscribe("blockUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<SubscriptionId> logsSubscribe(List<String> mention, Commitment commitment, NotificationListener<RpcNotificationV2<ResValueLog>> listener) {
        List<Object> params = List.of(
                mention,
                Map.of("commitment", commitment)
        );
        Type type = Types.newParameterizedType(
                RpcNotificationV2.class,
                ResValueLog.class
        );
        return this.subscribe("logsSubscribe", params, type, listener);
    }

    @Override
    public RpcResponse<Boolean> logsUnsubscribe(SubscriptionId subscriptionId) {
        return this.unsubscribe("logsUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<SubscriptionId> programSubscribe(String programId, Commitment commitment, String encoding, NotificationListener<RpcNotificationV2<ResValueProgram>> listener) {
        List<Object> params = List.of(
                programId,
                Map.of("commitment", commitment, "encoding", encoding)
        );
        Type type = Types.newParameterizedType(
                RpcNotificationV2.class,
                ResValueProgram.class
        );
        return this.subscribe("programSubscribe", params, type, listener);
    }

    @Override
    public RpcResponse<Boolean> programUnsubscribe(SubscriptionId subscriptionId) {
        return this.unsubscribe("programUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<SubscriptionId> rootSubscribe(NotificationListener<RpcNotification<Long>> listener) {
        List<Object> params = List.of(); // 루트 구독에는 추가 파라미터가 없음
        Type type = Types.newParameterizedType(
                RpcNotification.class,
                Long.class
        );
        return this.subscribe("rootSubscribe", params, type, listener);
    }

    @Override
    public RpcResponse<Boolean> rootUnsubscribe(SubscriptionId subscriptionId) {
        return this.unsubscribe("rootUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<SubscriptionId> signatureSubscribe(String signature, NotificationListener<RpcNotificationV2<String>> listener) {
        List<Object> params = List.of(
                signature
        );
        Type type = Types.newParameterizedType(
                RpcNotificationV2.class,
                String.class
        );
        return this.subscribe("signatureSubscribe", params, type, listener);
    }

    @Override
    public RpcResponse<Boolean> signatureUnsubscribe(SubscriptionId subscriptionId) {
        return this.unsubscribe("signatureUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<SubscriptionId> slotSubscribe(NotificationListener<RpcNotification<ResValueSlot>> listener) {
        List<Object> params = List.of(); // 슬롯 구독에는 추가 파라미터가 없음
        Type type = Types.newParameterizedType(
                RpcNotification.class,
                ResValueSlot.class
        );
        return this.subscribe("slotSubscribe", params, type, listener);
    }

    @Override
    public RpcResponse<Boolean> slotUnsubscribe(SubscriptionId subscriptionId) {
        return this.unsubscribe("slotUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<SubscriptionId> slotsUpdatesSubscribe(NotificationListener<RpcNotification<ResValueSlotUpdates>> listener) {
        List<Object> params = List.of(); // 슬롯 업데이트 구독에는 추가 파라미터가 없음
        Type type = Types.newParameterizedType(
                RpcNotification.class,
                ResValueSlotUpdates.class
        );
        return this.subscribe("slotsUpdatesSubscribe", params, type, listener);
    }

    @Override
    public RpcResponse<Boolean> slotsUpdatesUnsubscribe(SubscriptionId subscriptionId) {
        return this.unsubscribe("slotsUpdatesUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<SubscriptionId> voteSubscribe(NotificationListener<RpcNotification<ResValueVote>> listener) {
        List<Object> params = List.of(); // 투표 구독에는 추가 파라미터가 없음
        Type type = Types.newParameterizedType(
                RpcNotification.class,
                ResValueVote.class
        );
        return this.subscribe("voteSubscribe", params, type, listener);
    }

    @Override
    public RpcResponse<Boolean> voteUnsubscribe(SubscriptionId subscriptionId) {
        return this.unsubscribe("voteUnsubscribe", subscriptionId);
    }
}
