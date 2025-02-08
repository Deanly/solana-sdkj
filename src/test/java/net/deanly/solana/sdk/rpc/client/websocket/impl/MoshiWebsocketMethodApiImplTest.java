package net.deanly.solana.sdk.rpc.client.websocket.impl;

import com.google.common.primitives.UnsignedLong;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.rpc.client.RpcClient;
import net.deanly.solana.sdk.rpc.request.RpcRequest;
import net.deanly.solana.sdk.rpc.request.config.AccountSubscriptionConfig;
import net.deanly.solana.sdk.rpc.response.ResValueAccountInfo;
import net.deanly.solana.sdk.rpc.response.RpcNotificationV2;
import net.deanly.solana.sdk.rpc.response.RpcResponse;
import net.deanly.solana.sdk.types.Commitment;
import net.deanly.solana.sdk.types.Encoding;
import net.deanly.solana.sdk.types.SubscriptionId;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static net.deanly.solana.sdk.rpc.client.MoshiTestUtil.assertJsonEqualsIgnoringId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MoshiWebsocketMethodApiImplTest {
    private MoshiWebsocketMethodApiImpl clientApi; // 테스트할 대상 클래스
    private OkHttpClient mockHttpClient;
    private RpcClient.ClientConfig mockConfig;
    private WebSocket mockWebSocket;
    private WebSocketListener webSocketListener;

    @BeforeEach
    void setup() {
        // 기본 설정 생성
        this.mockConfig = RpcClient.ClientConfig.builder()
                .endpoint("https://api.devnet.solana.com")
                .readTimeoutMs(5000)
                .mediaType(MediaType.get("application/json"))
                .build();

        this.mockHttpClient = mock(OkHttpClient.class);
        this.mockWebSocket = mock(WebSocket.class);
        this.webSocketListener = mock(WebSocketListener.class);

        AtomicReference<WebSocketListener> listenerRef = new AtomicReference<>();
        AtomicReference<Request> capturedRequest = new AtomicReference<>();
        when(mockHttpClient.newWebSocket(any(Request.class), any(WebSocketListener.class)))
                .thenAnswer(invocation -> {
                    capturedRequest.set(invocation.getArgument(0));
                    WebSocketListener listener = invocation.getArgument(1);
                    listenerRef.set(listener);
                    return mockWebSocket;
                });

        // 테스트 대상 클래스 초기화 (connectWebSocket을 Mock 기반으로 처리)
        clientApi = new MoshiWebsocketMethodApiImpl(mockConfig) {
            @Override
            protected OkHttpClient createHttpClient() {
                return mockHttpClient;
            }

            @Override
            protected WebSocket connectWebSocket() {
                return super.connectWebSocket();
            }
        };

        // WebSocketListener 참조 확보
        this.webSocketListener = listenerRef.get();
        assertNotNull(this.webSocketListener, "웹소켓 리스너가 null이어서는 안됩니다.");
    }

    private void setupResponseThread(String responseJson) {
        // WebSocket 응답 처리 비동기 스레드 생성
        new Thread(() -> {
            try {
                // 구독 요청 응답 JSON을 WebSocketListener에 전달하여 비동기 처리
                Thread.sleep(200); // 약간의 지연 추가하여 테스트 흉내
                webSocketListener.onMessage(mockWebSocket, responseJson);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @Test
    void testConnectWebSocketCalled() {
        verify(mockHttpClient, times(1)).newWebSocket(any(Request.class), any(WebSocketListener.class));
    }

    @Test
    void testAccountSubscribeWithNotification() throws IOException {
        // 구독 요청 JSON (예상 전송값)
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "accountSubscribe",
      "params": [
        "CM78CPUeXjn8o3yroDHxUtKsZZgoy4GPkPPXfouKNH12",
        {
          "encoding": "base58",
          "commitment": "finalized"
        }
      ]
    }
    """;

        // 구독 응답 JSON (서버가 반환해야 하는 값)
        String responseJson = """
    {
      "jsonrpc": "2.0",
      "result": 23784,
      "id": 1
    }
    """;

        // Notification JSON (서버에서 전송하는 알림 예시)
        String notificationJson = """
    {
      "jsonrpc": "2.0",
      "method": "accountNotification",
      "params": {
        "result": {
          "context": {
            "slot": 5199307
          },
          "value": {
            "data": [
              "11116bv5nS2h3y12kD1yUKeMZvGcKLSjQgX6BeV7u1FrjeJcKfsHPXHRDEHrBesJhZyqnnq9qJeUuF7WHxiuLuL5twc38w2TXNLxnDbjmuR",
              "base58"
            ],
            "executable": false,
            "lamports": 33594,
            "owner": "11111111111111111111111111111111",
            "rentEpoch": 635,
            "space": 80
          }
        },
        "subscription": 23784
      }
    }
    """;

        AtomicReference<RpcNotificationV2<ResValueAccountInfo>> notificationReceived = new AtomicReference<>();

        // 요청 호출
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L);
        }).when(spyClientApi).createRpcRequest(anyString(), anyList());

        AccountSubscriptionConfig config = AccountSubscriptionConfig.builder()
                .commitment(Commitment.FINALIZED)
                .encoding(Encoding.BASE58)
                .build();

        // 요청 호출 전에 구독 응답 처리를 준비
        setupResponseThread(responseJson);
        RpcResponse<SubscriptionId> subResponse = spyClientApi.accountSubscribe(
                PublicKey.valueOf("CM78CPUeXjn8o3yroDHxUtKsZZgoy4GPkPPXfouKNH12"),
                config,
                notificationReceived::set
        );

        // WebSocket에서 전송된 메시지(JSON 요청) 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson); // 요청 JSON 검증

        // 구독 ID 검증
        assertNotNull(subResponse, "구독 응답은 null이 아니어야 합니다.");
        assertEquals("23784", subResponse.getResult().toString(), "구독 ID가 예상 값과 일치해야 합니다.");

        // 알림 처리
        webSocketListener.onMessage(mockWebSocket, notificationJson); // 서버 알림 전달

        // 알림 데이터 검증
        RpcNotificationV2<ResValueAccountInfo> notification = notificationReceived.get();
        assertNotNull(notification, "Notification은 null이 아니어야 합니다.");
        assertEquals(5199307, notification.getParams().getResult().getContext().getSlot(), "Slot 값이 잘못되었습니다.");
        ResValueAccountInfo value = notification.getParams().getResult().getValue();
        assertNotNull(value, "Notification Value는 null이면 안 됩니다.");
        assertEquals(UnsignedLong.valueOf(33594), value.getLamports(), "Lamports 값이 올바르지 않습니다.");
        assertEquals(PublicKey.valueOf("11111111111111111111111111111111"), value.getOwner(), "Owner 값이 일치하지 않습니다.");
        assertEquals("11116bv5nS2h3y12kD1yUKeMZvGcKLSjQgX6BeV7u1FrjeJcKfsHPXHRDEHrBesJhZyqnnq9qJeUuF7WHxiuLuL5twc38w2TXNLxnDbjmuR", value.getData().getValue());
        assertEquals(Encoding.BASE58, value.getData().getEncoding());
    }

    @Test
    void testAccountSubscribeWithParsedNotification() throws InterruptedException, IOException {
        // 구독 요청 JSON (예상 전송값)
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "accountSubscribe",
      "params": [
        "CM78CPUeXjn8o3yroDHxUtKsZZgoy4GPkPPXfouKNH12",
        {
          "encoding": "jsonParsed",
          "commitment": "finalized"
        }
      ]
    }
    """;

        // 구독 응답 JSON (서버가 반환해야 하는 구독 ID 포함 응답)
        String responseJson = """
    {
      "jsonrpc": "2.0",
      "result": 23784,
      "id": 1
    }
    """;

        // Notification JSON (서버에서 전송하는 알림 데이터)
        String notificationJson = """
    {
      "jsonrpc": "2.0",
      "method": "accountNotification",
      "params": {
        "result": {
          "context": {
            "slot": 5199307
          },
          "value": {
            "data": {
              "program": "nonce",
              "parsed": {
                "type": "initialized",
                "info": {
                  "authority": "Bbqg1M4YVVfbhEzwA9SpC9FhsaG83YMTYoR4a8oTDLX",
                  "blockhash": "LUaQTmM7WbMRiATdMMHaRGakPtCkc2GHtH57STKXs6k",
                  "feeCalculator": {
                    "lamportsPerSignature": 5000
                  }
                }
              }
            },
            "executable": false,
            "lamports": 33594,
            "owner": "11111111111111111111111111111111",
            "rentEpoch": 635,
            "space": 80
          }
        },
        "subscription": 23784
      }
    }
    """;

        // Notification 데이터를 받을 변수를 선언
        AtomicReference<RpcNotificationV2<ResValueAccountInfo>> notificationReceived = new AtomicReference<>();

        // Spy 생성 및 Mocking: createRpcRequest 목업
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L); // 항상 ID를 1로 설정
        }).when(spyClientApi).createRpcRequest(anyString(), anyList());

        // AccountSubscriptionConfig 설정
        AccountSubscriptionConfig config = AccountSubscriptionConfig.builder()
                .commitment(Commitment.FINALIZED)
                .encoding(Encoding.JSON_PARSED)
                .build();

        // 요청 호출 전에 구독 응답 처리를 준비
        setupResponseThread(responseJson);

        // 호출: accountSubscribe 실행
        RpcResponse<SubscriptionId> subResponse = spyClientApi.accountSubscribe(
                PublicKey.valueOf("CM78CPUeXjn8o3yroDHxUtKsZZgoy4GPkPPXfouKNH12"),
                config,
                notificationReceived::set
        );

        // WebSocket에서 전송된 메시지(JSON 요청 값 확인)
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson); // 요청 JSON 검증

        // 구독 응답 검증
        assertNotNull(subResponse, "구독 응답은 null이 아니어야 합니다.");
        assertEquals("23784", subResponse.getResult().toString(), "구독 ID가 예상 값(23784)과 일치해야 합니다.");

        // Notification 처리: WebSocketListener에 알림 데이터 제공
        webSocketListener.onMessage(mockWebSocket, notificationJson);

        // 알림 데이터 검증
        RpcNotificationV2<ResValueAccountInfo> notification = notificationReceived.get();
        assertNotNull(notification, "Notification은 null이 아니어야 합니다.");
        assertEquals(5199307, notification.getParams().getResult().getContext().getSlot(), "Slot 값이 잘못되었습니다.");

        ResValueAccountInfo value = notification.getParams().getResult().getValue();
        assertNotNull(value, "Notification Value는 null이면 안 됩니다.");
        assertEquals(UnsignedLong.valueOf(33594), value.getLamports(), "Lamports 값이 올바르지 않습니다.");
        assertEquals(PublicKey.valueOf("11111111111111111111111111111111"), value.getOwner(), "Owner 값이 일치하지 않습니다.");
        assertFalse(value.getExecutable(), "Executable 값은 false여야 합니다.");
        assertEquals(UnsignedLong.valueOf(635), value.getRentEpoch(), "Rent Epoch 값이 잘못되었습니다.");
        assertEquals(UnsignedLong.valueOf(80), value.getSpace(), "Space 값이 올바르지 않습니다.");

        // Parsed Data 검증
        assertNotNull(value.getData(), "Data 값이 null이면 안 됩니다.");
        assertEquals("nonce", value.getData().getObjectValue("program"), "Program 값이 잘못되었습니다.");
        assertNotNull(value.getData().getObject().get("parsed"), "Parsed 데이터는 null이면 안 됩니다.");
        assertEquals("initialized", value.getData().getObjectValue("parsed.type"), "Parsed type 값이 잘못되었습니다.");
        assertEquals("Bbqg1M4YVVfbhEzwA9SpC9FhsaG83YMTYoR4a8oTDLX", value.getData().getObjectValue("parsed.info.authority"), "Authority 값이 잘못되었습니다.");
        assertEquals("LUaQTmM7WbMRiATdMMHaRGakPtCkc2GHtH57STKXs6k", value.getData().getObjectValue("parsed.info.blockhash"), "Blockhash 값이 잘못되었습니다.");
        assertEquals(5000.0, value.getData().getObjectValue("parsed.info.feeCalculator.lamportsPerSignature"), "Lamports Per Signature 값이 잘못되었습니다.");
    }
}
