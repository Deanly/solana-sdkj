package net.deanly.solana.sdk.rpc.client.websocket.impl;

import com.google.common.cache.CacheBuilder;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deanly.solana.sdk.rpc.client.RpcClient;
import net.deanly.solana.sdk.rpc.client.exception.RpcWebSocketException;
import net.deanly.solana.sdk.rpc.client.websocket.NotificationListener;
import net.deanly.solana.sdk.rpc.client.websocket.WebsocketMethodApi;
import net.deanly.solana.sdk.rpc.request.RpcRequest;
import net.deanly.solana.sdk.rpc.response.*;
import net.deanly.solana.sdk.rpc.types.Commitment;
import net.deanly.solana.sdk.rpc.response.ResValueProgram;
import okhttp3.*;
import com.google.common.cache.Cache;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class MoshiWebsocketMethodApiImpl implements WebsocketMethodApi {
    private final RpcClient.ClientConfig config;
    private final OkHttpClient client = new OkHttpClient();
    private final Moshi moshi = new Moshi.Builder().build();
    private final Cache<Long, SubscriptionContext<?>> listeners = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(); // Concurrency Safe
    private final Cache<Long, CompletableFuture<Long>> pendingSubscriptions = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(); // Concurrency Safe
    private final AtomicLong idGenerator = new AtomicLong(1);
    private WebSocket webSocket;

    @RequiredArgsConstructor
    private static class SubscriptionContext<T> {
        final Type type;
        final NotificationListener<T> listener;
        final String method;
        final List<Object> params;
    }

    public MoshiWebsocketMethodApiImpl(RpcClient.ClientConfig config) {
        this.config = config;
        connectWebSocket();
    }

    private void connectWebSocket() {
        try {
            URI endpointURI = new URI(config.getEndpoint());
            String scheme = "https".equals(endpointURI.getScheme()) ? "wss" : "ws";
            String endpointURL = (new URI(scheme + "://" + endpointURI.getHost())).toString();

            Request request = new Request.Builder().url(endpointURL).build();

            webSocket = client.newWebSocket(request, new WebSocketListener() {
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


    private void handleMessage(String message) {
        try {
            // 공통 JSON 구조 파싱 (id 또는 method 존재 여부 확인)
            JsonAdapter<Map<String, Object>> mapAdapter = moshi.adapter(
                    Types.newParameterizedType(Map.class, String.class, Object.class)
            );
            Map<String, Object> jsonMap = mapAdapter.fromJson(message);

            if (jsonMap != null) {
                if (jsonMap.containsKey("id")) {
                    handleSubscriptionResponse(message); // 구독 요청 응답 처리
                } else if (jsonMap.containsKey("method")) {
                    handleNotification(message); // Notification 처리
                } else {
                    log.warn("Unrecognized message format: {}", message);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage());
        }
    }

    private void handleSubscriptionResponse(String message) throws IOException {
        JsonAdapter<RpcResponse<Long>> responseAdapter = moshi.adapter(
                Types.newParameterizedType(RpcResponse.class, Long.class)
        );
        RpcResponse<Long> response = responseAdapter.fromJson(message);

        if (response != null) {
            Long requestId = response.getId();
            CompletableFuture<Long> future = getPendingSubscription(requestId);
            removePendingSubscription(requestId);

            if (future != null) {
                if (response.getError() != null) {
                    future.completeExceptionally(new RpcWebSocketException(response.getError().getMessage()));
                    log.error("Subscription error: {}", response.getError().getMessage());
                } else {
                    future.complete(response.getResult());
                    log.info("Subscription confirmed with ID: {}", response.getResult());
                }
            } else {
                log.warn("No pending subscription found for request ID: {}", requestId);
            }
        }
    }

    private void handleNotification(String message) throws IOException {
        JsonAdapter<RpcNotification<Object>> adapter = moshi.adapter(
                Types.newParameterizedType(RpcNotification.class, Object.class)
        );
        RpcNotification<Object> notification = adapter.fromJson(message);

        if (notification != null && notification.getParams() != null) {
            long subscriptionId = notification.getParams().getSubscription();
            SubscriptionContext<?> context = getListener(subscriptionId);

            if (context != null) {
                RpcNotification<Object> typedNotification = parseTypedNotification(message, context.type);

                @SuppressWarnings("unchecked")
                NotificationListener<Object> listener = (NotificationListener<Object>) context.listener;
                listener.onNotification(typedNotification);
            } else {
                log.warn("No listener found for subscription ID: {}", subscriptionId);
            }
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
                webSocket.send(toJson(request)); // 재구독 요청 전송
                log.info("Resubscribed to subscription ID: {}", subscriptionId);
            } catch (Exception e) {
                log.error("Failed to resubscribe for ID: {}", subscriptionId, e);
            }
        });
    }

    private RpcRequest buildResubscribeRequest(Long subscriptionId, SubscriptionContext<?> context) {
        String method = context.method;
        List<Object> params = context.params;

        return new RpcRequest(method, params);
    }

    public void addListener(Long id, SubscriptionContext<?> context) {
        listeners.put(id, context);
    }

    public void addPendingSubscription(Long id, CompletableFuture<Long> future) {
        pendingSubscriptions.put(id, future);
    }

    public SubscriptionContext<?> getListener(Long id) {
        return listeners.getIfPresent(id);  // 없으면 null 반환
    }

    public CompletableFuture<Long> getPendingSubscription(Long id) {
        return pendingSubscriptions.getIfPresent(id);
    }

    public void removeListener(Long id) {
        listeners.invalidate(id);
    }

    public void removePendingSubscription(Long id) {
        pendingSubscriptions.invalidate(id);
    }

    /// Moshi Adapter Cache
    private final Map<Class<?>, JsonAdapter<?>> adapterCache = new ConcurrentHashMap<>();

    private String toJson(Object obj) {
        try {
            @SuppressWarnings("unchecked")
            JsonAdapter<Object> adapter = (JsonAdapter<Object>) adapterCache.computeIfAbsent(
                    obj.getClass(), moshi::adapter
            );
            return adapter.toJson(obj);
        } catch (Exception e) {
            throw new RpcWebSocketException("JSON serialization failed", e);
        }
    }

    ///  공통 Subscribe
    private <T> RpcResponse<Long> subscribe(
            String method,
            List<Object> params,
            Type type,
            NotificationListener<T> listener) {
        RpcRequest request = new RpcRequest(method, params);

        long id = idGenerator.getAndIncrement();
        CompletableFuture<Long> subscriptionFuture = new CompletableFuture<>();
        pendingSubscriptions.put(id, subscriptionFuture);

        webSocket.send(toJson(request));

        try {
            long subscriptionId = subscriptionFuture.get(5, TimeUnit.SECONDS);

            listeners.put(subscriptionId, new SubscriptionContext<>(type, listener, method, params));
            return RpcResponse.<Long>builder().result(subscriptionId).build();
        } catch (Exception e) {
            log.error("Subscription failed: {}", e.getMessage());
            throw new RpcWebSocketException("Subscription timeout", e);
        }
    }

    ///  공통 Unsubscribe
    private RpcResponse<Boolean> unsubscribe(String method, Long subscriptionId) {
        RpcRequest request = new RpcRequest(method, List.of(subscriptionId));
        webSocket.send(toJson(request));

        removeListener(subscriptionId);
        return RpcResponse.<Boolean>builder().result(true).build();
    }


    @Override
    public RpcResponse<Long> accountSubscribe(
            String accountKey,
            Commitment commitment,
            String encoding,
            NotificationListener<RpcNotificationV2<ResValueAccountInfo>> listener
    ) {
        List<Object> params = List.of(
                accountKey,
                Map.of("commitment", commitment, "encoding", encoding)
        );
        Type type = Types.newParameterizedType(
                RpcNotificationV2.class,
                ResValueAccountInfo.class
        );
        return this.subscribe("accountSubscribe", params, type, listener);
    }

    @Override
    public RpcResponse<Boolean> accountUnsubscribe(Long subscriptionId) {
        return this.unsubscribe("accountUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<Long> blockSubscribe(Commitment commitment, String encoding, NotificationListener<RpcNotificationV2<ResValueBlock>> listener) {
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
    public RpcResponse<Boolean> blockUnsubscribe(Long subscriptionId) {
        return this.unsubscribe("blockUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<Long> logsSubscribe(List<String> mention, Commitment commitment, NotificationListener<RpcNotificationV2<ResValueLog>> listener) {
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
    public RpcResponse<Boolean> logsUnsubscribe(Long subscriptionId) {
        return this.unsubscribe("logsUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<Long> programSubscribe(String programId, Commitment commitment, String encoding, NotificationListener<RpcNotificationV2<ResValueProgram>> listener) {
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
    public RpcResponse<Boolean> programUnsubscribe(Long subscriptionId) {
        return this.unsubscribe("programUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<Long> rootSubscribe(NotificationListener<RpcNotification<Long>> listener) {
        List<Object> params = List.of(); // 루트 구독에는 추가 파라미터가 없음
        Type type = Types.newParameterizedType(
                RpcNotification.class,
                Long.class
        );
        return this.subscribe("rootSubscribe", params, type, listener);
    }

    @Override
    public RpcResponse<Boolean> rootUnsubscribe(Long subscriptionId) {
        return this.unsubscribe("rootUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<Long> signatureSubscribe(String signature, NotificationListener<RpcNotificationV2<String>> listener) {
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
    public RpcResponse<Boolean> signatureUnsubscribe(Long subscriptionId) {
        return this.unsubscribe("signatureUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<Long> slotSubscribe(NotificationListener<RpcNotification<ResValueSlot>> listener) {
        List<Object> params = List.of(); // 슬롯 구독에는 추가 파라미터가 없음
        Type type = Types.newParameterizedType(
                RpcNotification.class,
                ResValueSlot.class
        );
        return this.subscribe("slotSubscribe", params, type, listener);
    }

    @Override
    public RpcResponse<Boolean> slotUnsubscribe(Long subscriptionId) {
        return this.unsubscribe("slotUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<Long> slotsUpdatesSubscribe(NotificationListener<RpcNotification<ResValueSlotUpdates>> listener) {
        List<Object> params = List.of(); // 슬롯 업데이트 구독에는 추가 파라미터가 없음
        Type type = Types.newParameterizedType(
                RpcNotification.class,
                ResValueSlotUpdates.class
        );
        return this.subscribe("slotsUpdatesSubscribe", params, type, listener);
    }

    @Override
    public RpcResponse<Boolean> slotsUpdatesUnsubscribe(Long subscriptionId) {
        return this.unsubscribe("slotsUpdatesUnsubscribe", subscriptionId);
    }

    @Override
    public RpcResponse<Long> voteSubscribe(NotificationListener<RpcNotification<ResValueVote>> listener) {
        List<Object> params = List.of(); // 투표 구독에는 추가 파라미터가 없음
        Type type = Types.newParameterizedType(
                RpcNotification.class,
                ResValueVote.class
        );
        return this.subscribe("voteSubscribe", params, type, listener);
    }

    @Override
    public RpcResponse<Boolean> voteUnsubscribe(Long subscriptionId) {
        return this.unsubscribe("voteUnsubscribe", subscriptionId);
    }
}
