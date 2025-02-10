package net.deanly.solana.sdk.rpc.client.http.impl;

import net.deanly.solana.sdk.rpc.client.http.HttpMethodApi;
import net.deanly.solana.sdk.rpc.client.websocket.NotificationListener;
import net.deanly.solana.sdk.rpc.client.websocket.WebsocketMethodApi;
import net.deanly.solana.sdk.rpc.request.config.SendTransactionConfig;
import net.deanly.solana.sdk.rpc.request.config.SignatureConfig;
import net.deanly.solana.sdk.rpc.request.config.TransactionConfig;
import net.deanly.solana.sdk.rpc.response.*;
import net.deanly.solana.sdk.transaction.Transaction;
import net.deanly.solana.sdk.types.Signature;
import net.deanly.solana.sdk.types.SignatureStatus;
import net.deanly.solana.sdk.types.SubscriptionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MoshiSyncApiImplTest {

    private MoshiSyncApiImpl clientApi;
    private HttpMethodApi mockHttpApi;
    private WebsocketMethodApi mockWebsocketApi;

    @BeforeEach
    void setup() {
        mockHttpApi = mock(HttpMethodApi.class);
        mockWebsocketApi = mock(WebsocketMethodApi.class);
        clientApi = new MoshiSyncApiImpl(mockHttpApi, mockWebsocketApi);
    }

    private RpcNotificationV2<NotiValueSignature> createMockNotification(Signature signature, boolean isSuccess) {
        RpcNotificationV2.Params<RpcResultObject<NotiValueSignature>> params = RpcNotificationV2.Params.<RpcResultObject<NotiValueSignature>>builder()
                .result(RpcResultObject.<NotiValueSignature>builder()
                        .value(NotiValueSignature.builder()
                                .status(isSuccess ? SignatureStatus.RECEIVED_SIGNATURE : SignatureStatus.ERROR)
                                .build())
                        .build())
                .build();

        return RpcNotificationV2.<NotiValueSignature>builder2()
                .jsonrpc("2.0")
                .params(params)
                .method("signatureNotification")
                .build();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSendAndConfirmTransaction() throws Exception {
        // Mock - sendTransaction 설정
        Signature mockSignature = Signature.of("2nBhEBYYvfaAe16UMNqRHre4YNSskvuYgx3M6E4JP1oDYvZEJHvoPzyUidNgNX5r9sTyN1J9UxtbCXy2rqYcuyuv");
        when(mockHttpApi.sendTransaction(any(Transaction.class), any(SendTransactionConfig.class)))
                .thenReturn(mockSignature);

        // Mock - signatureSubscribe 설정
        CountDownLatch latch = new CountDownLatch(1);
        SubscriptionId mockSubscriptionId = SubscriptionId.of(1001);
        doAnswer(invocation -> {
            Signature signature = invocation.getArgument(0);
            SignatureConfig config = invocation.getArgument(1);
            NotificationListener<RpcNotificationV2<NotiValueSignature>> notificationListener = invocation.getArgument(2);

            // WebSocket 알림을 테스트 스레드에서 실행
            new Thread(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(100); // 약간의 지연
                    notificationListener.onNotification(
                            createMockNotification(signature, true) // 성공 알림
                    );
                    latch.countDown(); // Latch 해제
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

            return RpcResponse.<SubscriptionId>builder().result(mockSubscriptionId).build();
        }).when(mockWebsocketApi).signatureSubscribe(
                any(Signature.class), any(SignatureConfig.class), any(NotificationListener.class)
        );

        // Mock - getTransaction 설정
        ResValueConfirmedTransaction mockTransactionResponse = new ResValueConfirmedTransaction();
        when(mockHttpApi.getTransaction(any(Signature.class), any(TransactionConfig.class)))
                .thenReturn(mockTransactionResponse);

        // 테스팅 대상 메서드 호출
        Transaction mockTransaction = mock(Transaction.class);
        ResValueConfirmedTransaction result = clientApi.sendAndConfirmTransaction(mockTransaction);

        // Assertion - sendTransaction 호출 확인
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        ArgumentCaptor<SendTransactionConfig> configCaptor = ArgumentCaptor.forClass(SendTransactionConfig.class);
        verify(mockHttpApi).sendTransaction(transactionCaptor.capture(), configCaptor.capture());

        assertNotNull(transactionCaptor.getValue());
        assertNotNull(configCaptor.getValue());

        // Assertion - signatureSubscribe 호출 확인
        ArgumentCaptor<Signature> signatureCaptor = ArgumentCaptor.forClass(Signature.class);
        ArgumentCaptor<SignatureConfig> signatureConfigCaptor = ArgumentCaptor.forClass(SignatureConfig.class);
        verify(mockWebsocketApi).signatureSubscribe(signatureCaptor.capture(), signatureConfigCaptor.capture(), any());

        assertEquals(mockSignature, signatureCaptor.getValue());
        assertNotNull(signatureConfigCaptor.getValue());

        // Assertion - WebSocket 알림 제대로 처리 확인
        assertTrue(latch.await(1, TimeUnit.SECONDS), "WebSocket 알림 처리 timeout");

        // Assertion - getTransaction 호출 확인
        verify(mockHttpApi).getTransaction(eq(mockSignature), any(TransactionConfig.class));

        // 최종 결과 확인
        assertNotNull(result, "최종 트랜잭션 응답은 null이 아니어야 합니다.");
        assertEquals(mockTransactionResponse, result, "트랜잭션 응답 값이 일치하지 않습니다.");
    }

    @Test
    void testSubscriptionUnsubscribeAfterTransactionConfirmation() throws Exception {
        // Mock - sendTransaction 설정
        Signature mockSignature = Signature.of("2nBhEBYYvfaAe16UMNqRHre4YNSskvuYgx3M6E4JP1oDYvZEJHvoPzyUidNgNX5r9sTyN1J9UxtbCXy2rqYcuyuv");
        when(mockHttpApi.sendTransaction(any(Transaction.class), any(SendTransactionConfig.class)))
                .thenReturn(mockSignature);

        // Mock - signatureSubscribe 설정
        CountDownLatch latch = new CountDownLatch(1);
        SubscriptionId mockSubscriptionId = SubscriptionId.of(2002); // mock된 SubscriptionId
        doAnswer(invocation -> {
            NotificationListener<RpcNotificationV2<NotiValueSignature>> notificationListener = invocation.getArgument(2);

            // WebSocket 알림 트리거
            new Thread(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(100); // 지연
                    notificationListener.onNotification(
                            createMockNotification(mockSignature, true) // 성공 알림
                    );
                    latch.countDown(); // Latch 해제
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

            return RpcResponse.<SubscriptionId>builder().result(mockSubscriptionId).build();
        }).when(mockWebsocketApi).signatureSubscribe(any(Signature.class), any(SignatureConfig.class), any(NotificationListener.class));

        // Mock - getTransaction 설정
        ResValueConfirmedTransaction mockTransactionResponse = new ResValueConfirmedTransaction();
        when(mockHttpApi.getTransaction(any(Signature.class), any(TransactionConfig.class)))
                .thenReturn(mockTransactionResponse);

        // Mock - unsubscribe 호출 설정
        RpcResponse<Boolean> unsubscribeResponse = RpcResponse.<Boolean>builder().result(true).build();
        when(mockWebsocketApi.signatureUnsubscribe(any(SubscriptionId.class)))
                .thenReturn(unsubscribeResponse);

        // 테스팅 대상 메서드 호출
        Transaction mockTransaction = mock(Transaction.class);
        clientApi.sendAndConfirmTransaction(mockTransaction);

        // Assertion - unsubscribe 호출 확인
        ArgumentCaptor<SubscriptionId> subscriptionIdCaptor = ArgumentCaptor.forClass(SubscriptionId.class);
        verify(mockWebsocketApi).signatureUnsubscribe(subscriptionIdCaptor.capture());

        // 예상된 SubscriptionId가 unsubscribe 호출에 사용되었는지 확인
        assertEquals(mockSubscriptionId, subscriptionIdCaptor.getValue(), "Unsubscribe 호출 시 잘못된 SubscriptionId 사용");
    }
}