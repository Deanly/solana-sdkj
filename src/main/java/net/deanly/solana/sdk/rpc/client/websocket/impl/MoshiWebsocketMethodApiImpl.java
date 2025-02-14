package net.deanly.solana.sdk.rpc.client.websocket.impl;

import net.deanly.solana.sdk.cache.RemovalAwareLRUCache;
import net.deanly.solana.sdk.rpc.client.config.ClientConfig;
import net.deanly.structlayout.type.guava.UnsignedLong;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.rpc.client.adapter.*;
import net.deanly.solana.sdk.rpc.client.exception.RpcWebSocketException;
import net.deanly.solana.sdk.rpc.client.websocket.NotificationListener;
import net.deanly.solana.sdk.rpc.client.websocket.WebsocketMethodApi;
import net.deanly.solana.sdk.rpc.request.RpcRequest;
import net.deanly.solana.sdk.rpc.request.config.*;
import net.deanly.solana.sdk.rpc.request.filter.BlockFilter;
import net.deanly.solana.sdk.rpc.request.filter.LogsFilter;
import net.deanly.solana.sdk.rpc.response.*;
import net.deanly.solana.sdk.types.*;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class MoshiWebsocketMethodApiImpl implements WebsocketMethodApi {

    private final ClientConfig config;
    private OkHttpClient httpClient;
    private WebSocket webSocket;

    protected final RemovalAwareLRUCache<SubscriptionId, SubscriptionContext<?>> listeners;
    protected final RemovalAwareLRUCache<Long, CompletableFuture<SubscriptionId>> pendingSubscriptions;
    protected final RemovalAwareLRUCache<Long, CompletableFuture<Boolean>> pendingUnsubscriptions;


    private final Moshi moshi = new Moshi.Builder()
            .add(MoshiFilterCriteriaJsonAdapter.FACTORY)
            .add(MoshiResValueTransactionJsonAdapter.FACTORY)
            .add(MoshiNotiValueSignatureJsonAdapter.FACTORY)
            .add(UnsignedLong.class, new MoshiUnsignedLongJsonAdapter())
            .add(StateData.class, new MoshiStateDataJsonAdapter())
            .add(PublicKey.class, new MoshiPublicKeyJsonAdapter())
            .add(Blockhash.class, new MoshiBlockhashJsonAdapter())
            .add(GenesisHash.class, new MoshiGenesisHashJsonAdapter())
            .add(Signature.class, new MoshiSignatureJsonAdapter())
            .add(EpochCredits.class, new MoshiEpochCreditsJsonAdapter())
            .add(ValidatorIdentityInfo.class, new MoshiValidatorIdentityInfoJsonAdapter())
            .add(SubscriptionId.class, new MoshiSubscriptionIdJsonAdapter())
            .add(BlockFilter.class, new MoshiBlockFilterJsonAdapter())
            .add(LogsFilter.class, new MoshiLogsFilterJsonAdapter())
//            .add(ResValueTransaction.class, new MoshiResValueTransactionJsonAdapter())
            .build();

    private JsonAdapter<RpcRequest> rpcRequestJsonAdapter = moshi.adapter(RpcRequest.class);

    @RequiredArgsConstructor
    protected static class SubscriptionContext<T> {
        final Type type;
        final NotificationListener<T> listener;
        final String method;
        final List<Object> params;
    }

    public MoshiWebsocketMethodApiImpl(ClientConfig config) {
        this.config = config;
        this.httpClient = createHttpClient();
        this.webSocket = connectWebSocket();

        this.listeners = new RemovalAwareLRUCache<>(
                this.config.getWebsocketListenerCacheMaxSize(),
                this.config.getWebsocketListenerExpireTimeMs(),
                (key, cause) -> log.debug("Subscribe Listener Removed: SubscriptionID={}, Cause={}", key, cause)
        );

        this.pendingSubscriptions = new RemovalAwareLRUCache<>(
                this.config.getWebsocketPendingSubscriptionCacheMaxSize(),
                this.config.getWebsocketPendingSubscriptionExpireTimeMs(),
                (key, cause) -> log.debug("Pending Listener Removed: RequestID={}, Cause={}", key, cause)
        );

        this.pendingUnsubscriptions = new RemovalAwareLRUCache<>(
                this.config.getWebsocketPendingUnsubscriptionCacheMaxSize(),
                this.config.getWebsocketPendingUnsubscriptionExpireTimeMs(),
                (key, cause) -> log.debug("Pending Listener Removed: RequestID={}, Cause={}", key, cause)
        );
    }

    protected OkHttpClient createHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(this.config.getReadTimeoutMs(), TimeUnit.MILLISECONDS);

        if (this.config.getWriteTimeoutMs() != null) {
            builder.readTimeout(this.config.getWriteTimeoutMs(), TimeUnit.MILLISECONDS);
        }
        if (this.config.getConnectTimeoutMs() != null) {
            builder.connectTimeout(this.config.getConnectTimeoutMs(), TimeUnit.MILLISECONDS);
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
            String endpointURL = this.config.getEndpointWebsocket();
            if (endpointURL == null) {
                URI endpointURI = new URI(config.getEndpointHttp());
                String scheme = "https".equals(endpointURI.getScheme()) ? "wss" : "ws";
                endpointURL = (new URI(scheme + "://" + endpointURI.getHost())).toString();
            }

            Request.Builder requestBuilder = new Request.Builder();
            requestBuilder.url(endpointURL);
            if (this.config.getHeaders() != null) {
                this.config.getHeaders().forEach(header -> {
                    requestBuilder.addHeader(header.getKey(), header.getValue());
                });
            }
            Request request = requestBuilder.build();

            log.info("Connecting to WebSocket: {}", endpointURL);
            return this.httpClient.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    log.info("WebSocket connected");
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    handleMessage(text); // 메시지 핸들링
                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    log.warn("WebSocket closed: " + reason);
                    // 재연결 처리
                    triggerReconnect();
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    log.error("WebSocket failure: " + t.getMessage());
                    // 재연결 처리
                    triggerReconnect();
                }
            });
        } catch (Exception e) {
            throw new RpcWebSocketException("Failed to connect to WebSocket", e);
        }
    }

    private final AtomicBoolean reconnecting = new AtomicBoolean(false);
    protected void triggerReconnect() {
        if (reconnecting.compareAndSet(false, true)) {
            reconnectWebSocket();
        }
    }

    private int reconnectDelay = 1000; // 초기 1초
    private int reconnectAttempts = 0;
    protected static final int MAX_RECONNECT_ATTEMPTS = 10; // 재연결 최대 시도 횟수
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    protected void reconnectWebSocket() {
        if (this.reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            log.error("Max reconnect attempts reached. Cannot reconnect.");
            return;
        }
        this.reconnectAttempts++;

        log.info("Reconnecting WebSocket... (attempt: {}, delay: {} ms)", this.reconnectAttempts, this.reconnectDelay);

        scheduler.schedule(() -> {
            try {
                this.webSocket = connectWebSocket();
                resubscribeAll();
                this.reconnectDelay = 1000; // 성공 시 초기화
                this.reconnectAttempts = 0; // 재시도 횟수 초기화
            } catch (Exception e) {
                // 지수 백오프 방식
                this.reconnectDelay = Math.min(this.reconnectDelay * 2, 30000); // 최대 30초
                log.warn("Reconnect attempt failed. Retrying... (next delay: {} ms)", this.reconnectDelay);
                reconnectWebSocket();
            }
        }, this.reconnectDelay, TimeUnit.MILLISECONDS);
    }

    protected void resubscribeAll() {
        log.info("Resubscribing to all active subscriptions...");

        listeners.asMap().forEach((subscriptionId, context) -> {
            try {
                RpcRequest request = buildResubscribeRequest(subscriptionId, context);
                webSocket.send(getCachedAdapter(context.type).toJson(request)); // 재구독 요청 전송
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
        return new RpcRequest(method, params, subscriptionId.getValue()); // 구독을 요청 ID로 재사용 🤣
    }

    @Getter
    private static class IdSubscriptionTuple {
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
            log.debug("Received Message: RequestID={}, SubscriptionID={}", tuple.getRequestId(), tuple.getSubscription() != null ? tuple.getSubscription().getValue() : null);

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
        if (this.getPendingSubscription(id) != null) {
            JsonAdapter<RpcResponse<SubscriptionId>> responseAdapter = this.getCachedAdapter(Types.newParameterizedType(RpcResponse.class, SubscriptionId.class));
            RpcResponse<SubscriptionId> response = responseAdapter.fromJson(message);

            if (response != null) {
                Long requestId = response.getId(); // same id
                CompletableFuture<SubscriptionId> future = this.getPendingSubscription(requestId);
                this.removePendingSubscription(requestId);

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
        } else if (this.getPendingUnsubscription(id) != null) {
            JsonAdapter<RpcResponse<Boolean>> responseAdapter = this.getCachedAdapter(Types.newParameterizedType(RpcResponse.class, Boolean.class));
            RpcResponse<Boolean> response = responseAdapter.fromJson(message);

            if (response != null) {
                Long requestId = response.getId(); // same id
                CompletableFuture<Boolean> future = this.getPendingUnsubscription(requestId);
                this.removePendingUnsubscription(requestId);

                if (future != null) {
                    if (response.getError() != null) {
                        future.completeExceptionally(new RpcWebSocketException(response.getError().getMessage()));
                        log.error("Unsubscription error: {}", response.getError().getMessage());
                    } else {
                        future.complete(response.getResult());
                    }
                } else {
                    log.warn("No pending unsubscription found for request ID: {}", requestId);
                }
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

    private SubscriptionContext<?> getListener(SubscriptionId id) {
        return listeners.getIfPresent(id);  // 없으면 null 반환
    }

    private CompletableFuture<SubscriptionId> getPendingSubscription(Long id) {
        return pendingSubscriptions.getIfPresent(id);
    }

    private CompletableFuture<Boolean> getPendingUnsubscription(Long id) {
        return pendingUnsubscriptions.getIfPresent(id);
    }

    private void removeListener(SubscriptionId id) {
        listeners.invalidate(id);
    }

    private void removePendingSubscription(Long id) {
        pendingSubscriptions.invalidate(id);
    }

    private void removePendingUnsubscription(Long id) {
        pendingUnsubscriptions.invalidate(id);
    }

    public int countListeners() {
        return this.listeners.size();
    }

    /// Moshi Adapter Cache
    private final Map<Type, JsonAdapter<?>> adapterCache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private <T> JsonAdapter<T> getCachedAdapter(Type responseType) {
        return (JsonAdapter<T>) adapterCache.computeIfAbsent(responseType, moshi::adapter);
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
        log.debug("Pending Listener Added: RequestID={}, Method={}", id, method);

        Type requestType = Types.newParameterizedType(RpcRequest.class, RpcRequest.class);
        JsonAdapter<RpcRequest> requestJsonAdapter = this.getCachedAdapter(requestType);
        this.webSocket.send(requestJsonAdapter.toJson(request));

        try {
            SubscriptionId subscriptionId = subscriptionFuture.get(5, TimeUnit.SECONDS);

            this.listeners.put(subscriptionId, new SubscriptionContext<>(type, listener, method, params));
            log.debug("Listener Added: SubscriptionID={}, Method={}", subscriptionId, method);
            log.info("Subscribe \"{}\" Started: SubscriptionID={}", method, subscriptionId);

            return RpcResponse.<SubscriptionId>builder()
                    .id(id)
                    .jsonrpc(request.getJsonrpc())
                    .result(subscriptionId)
                    .build();

        } catch (Exception e) {
            log.error("Subscription failed: {}", e.getMessage());
            throw new RpcWebSocketException("Subscription timeout", e);
        }
    }

    ///  공통 Unsubscribe
    private RpcResponse<Boolean> unsubscribe(String method, SubscriptionId subscriptionId) {
        try {
            RpcRequest request = createRpcRequest(method, List.of(subscriptionId));

            Long id = request.getId();
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            this.pendingUnsubscriptions.put(id, future);
            log.debug("Pending Listener Added: RequestID={}, Method={}", id, method);

            String requestJson = rpcRequestJsonAdapter.toJson(request);
            webSocket.send(requestJson);

            Boolean result = future.get(5, TimeUnit.SECONDS); // 타임아웃 설정
            return RpcResponse.<Boolean>builder().result(result).build();

        } catch (Exception e) {
            log.error("Unsubscribe failed: {}", e.getMessage());
            throw new RpcWebSocketException(e.getMessage(), e);

        } finally {
            // 요청 완료 후 Listener 제거
            this.removeListener(subscriptionId);
            log.info("Subscribe \"{}\" Ended: SubscriptionID={}", method, subscriptionId);
        }
    }

    /*
       API Methods
     */

    private static final EnumSet<Encoding> SUPPORTED_ENCODINGS = EnumSet.of(
            Encoding.BASE58,
            Encoding.BASE64,
//            Encoding.JSON_PARSED, // TODO: jsonParsed 를 위한 API 추가
//            Encoding.BASE64_ZSTD, // TODO: base64+zstd 파서 기능 추가
            Encoding.JSON
    );

    @Override
    public RpcResponse<SubscriptionId> accountSubscribe(
            PublicKey accountKey,
            AccountSubscriptionConfig config,
            NotificationListener<RpcNotificationV2<NotiValueAccountInfo>> listener
    ) {
        Objects.requireNonNull(accountKey, "accountKey must not be null");
        Objects.requireNonNull(listener, "listener must not be null");
        Type type = Types.newParameterizedType(RpcNotificationV2.class, NotiValueAccountInfo.class);
        return this.subscribe("accountSubscribe", this.getParams(accountKey, config), type, listener);
    }

    @Override
    public RpcResponse<Boolean> accountUnsubscribe(SubscriptionId subscriptionId) {
        Objects.requireNonNull(subscriptionId, "subscriptionId must not be null");
        return this.unsubscribe("accountUnsubscribe", subscriptionId);
    }


    @Override
    public RpcResponse<SubscriptionId> blockSubscribe(
            BlockFilter filter, BlockConfig2 config,
            NotificationListener<RpcNotificationV2<NotiValueBlock>> listener) {
        Objects.requireNonNull(filter, "filter must not be null");
        Objects.requireNonNull(listener, "listener must not be null");
        if (config != null && !SUPPORTED_ENCODINGS.contains(config.getEncoding())) {
            throw new IllegalArgumentException("Unsupported encoding: " + config.getEncoding());
        }
        Type type = Types.newParameterizedType(RpcNotificationV2.class, NotiValueBlock.class);
        return this.subscribe("blockSubscribe", this.getParams(filter, config), type, listener);
    }

    @Override
    public RpcResponse<Boolean> blockUnsubscribe(SubscriptionId subscriptionId) {
        Objects.requireNonNull(subscriptionId, "subscriptionId must not be null");
        return this.unsubscribe("blockUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<SubscriptionId> logsSubscribe(
            LogsFilter filter, LogsConfig config,
            NotificationListener<RpcNotificationV2<NotiValueLog>> listener) {
        Objects.requireNonNull(filter, "filter must not be null");
        Objects.requireNonNull(listener, "listener must not be null");
        Type type = Types.newParameterizedType(RpcNotificationV2.class, NotiValueLog.class);
        return this.subscribe("logsSubscribe", this.getParams(filter, config), type, listener);
    }

    @Override
    public RpcResponse<Boolean> logsUnsubscribe(SubscriptionId subscriptionId) {
        Objects.requireNonNull(subscriptionId, "subscriptionId must not be null");
        return this.unsubscribe("logsUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<SubscriptionId> programSubscribe(
            PublicKey programId, ProgramConfig config,
            NotificationListener<RpcNotificationV2<NotiValueProgram>> listener) {
        Objects.requireNonNull(programId, "programId must not be null");
        Objects.requireNonNull(listener, "listener must not be null");
        if (config != null && !SUPPORTED_ENCODINGS.contains(config.getEncoding())) {
            throw new IllegalArgumentException("Unsupported encoding: " + config.getEncoding());
        }
        Type type = Types.newParameterizedType(RpcNotificationV2.class, NotiValueProgram.class);
        return this.subscribe("programSubscribe", this.getParams(programId, config), type, listener);
    }

    @Override
    public RpcResponse<Boolean> programUnsubscribe(SubscriptionId subscriptionId) {
        Objects.requireNonNull(subscriptionId, "subscriptionId must not be null");
        return this.unsubscribe("programUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<SubscriptionId> rootSubscribe(NotificationListener<RpcNotification<UnsignedLong>> listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        Type type = Types.newParameterizedType(RpcNotification.class, UnsignedLong.class);
        return this.subscribe("rootSubscribe", null, type, listener);
    }

    @Override
    public RpcResponse<Boolean> rootUnsubscribe(SubscriptionId subscriptionId) {
        Objects.requireNonNull(subscriptionId, "subscriptionId must not be null");
        return this.unsubscribe("rootUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<SubscriptionId> signatureSubscribe(
            Signature signature, SignatureConfig config,
            NotificationListener<RpcNotificationV2<NotiValueSignature>> listener) {
        Objects.requireNonNull(signature, "signature must not be null");
        Objects.requireNonNull(listener, "listener must not be null");
        Type type = Types.newParameterizedType(RpcNotificationV2.class, NotiValueSignature.class);
        return this.subscribe("signatureSubscribe", this.getParams(signature, config), type, listener);
    }

    @Override
    public RpcResponse<Boolean> signatureUnsubscribe(SubscriptionId subscriptionId) {
        Objects.requireNonNull(subscriptionId, "subscriptionId must not be null");
        return this.unsubscribe("signatureUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<SubscriptionId> slotSubscribe(NotificationListener<RpcNotification<NotiValueSlot>> listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        Type type = Types.newParameterizedType(RpcNotification.class, NotiValueSlot.class);
        return this.subscribe("slotSubscribe", null, type, listener);
    }

    @Override
    public RpcResponse<Boolean> slotUnsubscribe(SubscriptionId subscriptionId) {
        Objects.requireNonNull(subscriptionId, "subscriptionId must not be null");
        return this.unsubscribe("slotUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<SubscriptionId> slotsUpdatesSubscribe(NotificationListener<RpcNotification<NotiValueSlotUpdates>> listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        Type type = Types.newParameterizedType(RpcNotification.class, NotiValueSlotUpdates.class);
        return this.subscribe("slotsUpdatesSubscribe", null, type, listener);
    }

    @Override
    public RpcResponse<Boolean> slotsUpdatesUnsubscribe(SubscriptionId subscriptionId) {
        Objects.requireNonNull(subscriptionId, "subscriptionId must not be null");
        return this.unsubscribe("slotsUpdatesUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<SubscriptionId> voteSubscribe(NotificationListener<RpcNotification<NotiValueVote>> listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        Type type = Types.newParameterizedType(RpcNotification.class, NotiValueVote.class);
        return this.subscribe("voteSubscribe", null, type, listener);
    }

    @Override
    public RpcResponse<Boolean> voteUnsubscribe(SubscriptionId subscriptionId) {
        Objects.requireNonNull(subscriptionId, "subscriptionId must not be null");
        return this.unsubscribe("voteUnsubscribe", subscriptionId);
    }
}
