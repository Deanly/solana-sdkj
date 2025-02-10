package net.deanly.solana.sdk.rpc.client.http.impl;

import net.deanly.solana.sdk.rpc.client.exception.RpcException;
import net.deanly.solana.sdk.rpc.client.http.HttpMethodApi;
import net.deanly.solana.sdk.rpc.client.http.SyncApi;
import net.deanly.solana.sdk.rpc.client.websocket.WebsocketMethodApi;
import net.deanly.solana.sdk.rpc.request.config.SendTransactionConfig;
import net.deanly.solana.sdk.rpc.request.config.SignatureConfig;
import net.deanly.solana.sdk.rpc.request.config.TransactionConfig;
import net.deanly.solana.sdk.rpc.response.ResValueConfirmedTransaction;
import net.deanly.solana.sdk.transaction.Transaction;
import net.deanly.solana.sdk.types.Signature;
import net.deanly.solana.sdk.types.SignatureStatus;
import net.deanly.solana.sdk.types.SubscriptionId;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MoshiSyncApiImpl implements SyncApi {

    private final HttpMethodApi httpApi;
    private final WebsocketMethodApi websocketApi;

    // 구독 관리 (동시성 안전한 데이터 구조)
    private final ConcurrentHashMap<String, SubscriptionId> activeSubscriptions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CountDownLatch> signalLatchMap = new ConcurrentHashMap<>(); // `Latch` 상태 추적

    public MoshiSyncApiImpl(HttpMethodApi httpApi, WebsocketMethodApi websocketApi) {
        this.httpApi = httpApi;
        this.websocketApi = websocketApi;
    }

    @Override
    public ResValueConfirmedTransaction sendAndConfirmTransaction(Transaction transaction) throws RpcException {
        // 트랜잭션 전송
        Signature signature = httpApi.sendTransaction(transaction, new SendTransactionConfig());
        String signatureKey = signature.toString();

        // 이미 구독되어 있다면 중복 방지
        CountDownLatch latch = signalLatchMap.computeIfAbsent(signatureKey, key -> new CountDownLatch(1));
        activeSubscriptions.computeIfAbsent(signatureKey, key -> {
            // 신규 구독 생성
            return subscribeToSignature(signature, latch);
        });

        try {
            // WebSocket 이벤트 대기 (타임아웃 설정)
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            if (!completed) {
                throw new RpcException("Transaction confirmation timeout");
            }

            // HTTP로 최종 트랜잭션 상태 확인
            ResValueConfirmedTransaction result = httpApi.getTransaction(signature, new TransactionConfig());
            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 현재 쓰레드 상태를 유지한 채 예외 발생
            throw new RpcException("Transaction confirmation interrupted", e);

        } finally {
            // WebSocket 구독 해제 및 리소스 정리
            cleanupSubscription(signatureKey);
        }
    }

    /**
     * 특정 Signature를 WebSocket 구독 (Subscription 등록).
     */
    private SubscriptionId subscribeToSignature(Signature signature, CountDownLatch latch) {
        return websocketApi.signatureSubscribe(signature, new SignatureConfig(),
                notification -> {
                    if (notification != null
                            && notification.getParams() != null
                            && notification.getParams().getResult() != null
                            && notification.getParams().getResult().getValue() != null) {

                        SignatureStatus status = notification.getParams().getResult().getValue().getStatus();
                        // 성공 또는 실패 상태 확인 시 Latch 해제
                        if (SignatureStatus.RECEIVED_SIGNATURE.equals(status) || SignatureStatus.ERROR.equals(status)) {
                            latch.countDown();
                        }
                    } else {
                        // Skipped
                    }
                }).getResult();
    }

    /**
     * 구독 해제를 수행하고 `activeSubscriptions` 및 `signalLatchMap`에서 제거.
     */
    private void cleanupSubscription(String signatureKey) {
        SubscriptionId subscriptionId = activeSubscriptions.remove(signatureKey);
        signalLatchMap.remove(signatureKey);

        if (subscriptionId != null) {
            // WebSocket 구독 해제
            websocketApi.signatureUnsubscribe(subscriptionId);
        }
    }
}
