package net.deanly.solana.sdk.rpc.client.websocket.impl;

import net.deanly.solana.sdk.rpc.client.ClientConfig;
import net.deanly.structlayout.type.guava.UnsignedLong;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.rpc.client.RpcClient;
import net.deanly.solana.sdk.rpc.client.websocket.NotificationListener;
import net.deanly.solana.sdk.rpc.request.RpcRequest;
import net.deanly.solana.sdk.rpc.request.config.*;
import net.deanly.solana.sdk.rpc.request.filter.BlockFilter;
import net.deanly.solana.sdk.rpc.request.filter.LogsFilter;
import net.deanly.solana.sdk.rpc.request.filter.ProgramAccountFilter;
import net.deanly.solana.sdk.rpc.response.*;
import net.deanly.solana.sdk.transaction.Transaction;
import net.deanly.solana.sdk.types.*;
import net.deanly.structlayout.StructLayout;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static net.deanly.solana.sdk.rpc.client.MoshiTestUtil.assertJsonEqualsIgnoringId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MoshiWebsocketMethodApiImplTest {
    private MoshiWebsocketMethodApiImpl clientApi; // 테스트할 대상 클래스
    private OkHttpClient mockHttpClient;
    private ClientConfig mockConfig;
    private WebSocket mockWebSocket;
    private WebSocketListener webSocketListener;

    @BeforeEach
    void setup() {
        // 기본 설정 생성
        this.mockConfig = ClientConfig.builder()
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
        setupResponseThread(responseJson, 100);
    }
    private void setupResponseThread(String responseJson, int delayMs) {
        // WebSocket 응답 처리 비동기 스레드 생성
        new Thread(() -> {
            try {
                // 구독 요청 응답 JSON을 WebSocketListener에 전달하여 비동기 처리
                Thread.sleep(delayMs); // 약간의 지연 추가하여 테스트 흉내
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

        AtomicReference<RpcNotificationV2<NotiValueAccountInfo>> notificationReceived = new AtomicReference<>();

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
        RpcNotificationV2<NotiValueAccountInfo> notification = notificationReceived.get();
        assertNotNull(notification, "Notification은 null이 아니어야 합니다.");
        assertEquals(UnsignedLong.valueOf(5199307), notification.getParams().getResult().getContext().getSlot(), "Slot 값이 잘못되었습니다.");
        NotiValueAccountInfo value = notification.getParams().getResult().getValue();
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
        AtomicReference<RpcNotificationV2<NotiValueAccountInfo>> notificationReceived = new AtomicReference<>();

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
        RpcNotificationV2<NotiValueAccountInfo> notification = notificationReceived.get();
        assertNotNull(notification, "Notification은 null이 아니어야 합니다.");
        assertEquals(UnsignedLong.valueOf(5199307), notification.getParams().getResult().getContext().getSlot(), "Slot 값이 잘못되었습니다.");

        NotiValueAccountInfo value = notification.getParams().getResult().getValue();
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

    @Test
    void testAccountUnsubscribe() throws IOException {
        // 구독 해제 요청 JSON (예상 전송값)
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "accountUnsubscribe",
      "params": [0]
    }
    """;

        // 구독 해제 응답 JSON (서버가 반환해야 하는 값)
        String responseJson = """
    {
      "jsonrpc": "2.0",
      "result": true,
      "id": 1
    }
    """;

        // Spy 객체 생성 및 createRpcRequest Mocking
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L); // 항상 ID 값은 1
        }).when(spyClientApi).createRpcRequest(anyString(), anyList());

        // 요청 호출 전 WebSocket 응답 준비
        setupResponseThread(responseJson);

        // 요청 호출: accountUnsubscribe 실행
        RpcResponse<Boolean> unsubResponse = spyClientApi.accountUnsubscribe(SubscriptionId.of(0));

        // WebSocket에서 전송된 메시지(JSON 요청) 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson); // 요청 JSON이 기대한 값과 일치하는지 확인

        // 구독 해제 응답 검증
        assertNotNull(unsubResponse, "구독 해제 응답은 null이 아니어야 합니다.");
        assertNotNull(unsubResponse.getResult(), "구독 해제 응답 결과는 null이 아니어야 합니다.");
        assertTrue(unsubResponse.getResult(), "구독 해제가 성공적이어야 합니다.");
    }


    @Test
    void testBlockSubscribe() throws IOException {
        // 구독 요청 JSON (예상 전송값)
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": "1",
      "method": "blockSubscribe",
      "params": [
        {
          "mentionsAccountOrProgram": "LieKvPRE8XeX3Y2xVNHjK1pAScD121YySBVQ4HqoJ5o"
        },
        {
          "commitment": "confirmed",
          "encoding": "base64",
          "showRewards": true,
          "transactionDetails": "full"
        }
      ]
    }
    """;

        // 구독 응답 JSON (서버가 반환해야 하는 값)
        String responseJson = """
    {
      "jsonrpc": "2.0",
      "result": 14,
      "id": 1
    }
    """;

        // Block Notification JSON (서버에서 전송하는 알림 데이터)
        String notificationJson = """
                {
                  "jsonrpc": "2.0",
                  "method": "blockNotification",
                  "params": {
                    "result": {
                      "context": {
                        "slot": 112301554
                      },
                      "value": {
                        "slot": 112301554,
                        "block": {
                          "previousBlockhash": "GJp125YAN4ufCSUvZJVdCyWQJ7RPWMmwxoyUQySydZA",
                          "blockhash": "6ojMHjctdqfB55JDpEpqfHnP96fiaHEcvzEQ2NNcxzHP",
                          "parentSlot": 112301553,
                          "transactions": [
                            {
                              "transaction": [
                                "ASdDdWBaKXVRA+6flVFiZokic9gK0+r1JWgwGg/GJAkLSreYrGF4rbTCXNJvyut6K6hupJtm72GztLbWNmRF1Q4BAAEDBhrZ0FOHFUhTft4+JhhJo9+3/QL6vHWyI8jkatuFPQzrerzQ2HXrwm2hsYGjM5s+8qMWlbt6vbxngnO8rc3lqgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAy+KIwZmU8DLmYglP3bPzrlpDaKkGu6VIJJwTOYQmRfUBAgIAAQwCAAAAuAsAAAAAAAA=",
                                "base64"
                              ],
                              "meta": {
                                "err": null,
                                "status": {
                                  "Ok": null
                                },
                                "fee": 5000,
                                "preBalances": [
                                  1758510880, 2067120, 1566000, 1461600, 2039280, 2039280,
                                  1900080, 1865280, 0, 3680844220, 2039280
                                ],
                                "postBalances": [
                                  1758505880, 2067120, 1566000, 1461600, 2039280, 2039280,
                                  1900080, 1865280, 0, 3680844220, 2039280
                                ],
                                "innerInstructions": [
                                  {
                                    "index": 0,
                                    "instructions": [
                                      {
                                        "programIdIndex": 13,
                                        "accounts": [1, 15, 3, 4, 2, 14],
                                        "data": "21TeLgZXNbtHXVBzCaiRmH"
                                      },
                                      {
                                        "programIdIndex": 14,
                                        "accounts": [3, 4, 1],
                                        "data": "6qfC8ic7Aq99"
                                      },
                                      {
                                        "programIdIndex": 13,
                                        "accounts": [1, 15, 3, 5, 2, 14],
                                        "data": "21TeLgZXNbsn4QEpaSEr3q"
                                      },
                                      {
                                        "programIdIndex": 14,
                                        "accounts": [3, 5, 1],
                                        "data": "6LC7BYyxhFRh"
                                      }
                                    ]
                                  },
                                  {
                                    "index": 1,
                                    "instructions": [
                                      {
                                        "programIdIndex": 14,
                                        "accounts": [4, 3, 0],
                                        "data": "7aUiLHFjSVdZ"
                                      },
                                      {
                                        "programIdIndex": 19,
                                        "accounts": [17, 18, 16, 9, 11, 12, 14],
                                        "data": "8kvZyjATKQWYxaKR1qD53V"
                                      },
                                      {
                                        "programIdIndex": 14,
                                        "accounts": [9, 11, 18],
                                        "data": "6qfC8ic7Aq99"
                                      }
                                    ]
                                  }
                                ],
                                "logMessages": [
                                  "Program QMNeHCGYnLVDn1icRAfQZpjPLBNkfGbSKRB83G5d8KB invoke [1]",
                                  "Program QMWoBmAyJLAsA1Lh9ugMTw2gciTihncciphzdNzdZYV invoke [2]"
                                ],
                                "preTokenBalances": [
                                  {
                                    "accountIndex": 4,
                                    "mint": "iouQcQBAiEXe6cKLS85zmZxUqaCqBdeHFpqKoSz615u",
                                    "uiTokenAmount": {
                                      "uiAmount": null,
                                      "decimals": 6,
                                      "amount": "0",
                                      "uiAmountString": "0"
                                    },
                                    "owner": "LieKvPRE8XeX3Y2xVNHjKlpAScD12lYySBVQ4HqoJ5op",
                                    "programId": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
                                  },
                                  {
                                    "accountIndex": 5,
                                    "mint": "iouQcQBAiEXe6cKLS85zmZxUqaCqBdeHFpqKoSz615u",
                                    "uiTokenAmount": {
                                      "uiAmount": 11513.0679,
                                      "decimals": 6,
                                      "amount": "11513067900",
                                      "uiAmountString": "11513.0679"
                                    },
                                    "owner": "rXhAofQCT7NN9TUqigyEAUzV1uLL4boeD8CRkNBSkYk",
                                    "programId": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
                                  },
                                  {
                                    "accountIndex": 10,
                                    "mint": "Saber2gLauYim4Mvftnrasomsv6NvAuncvMEZwcLpD1",
                                    "uiTokenAmount": {
                                      "uiAmount": null,
                                      "decimals": 6,
                                      "amount": "0",
                                      "uiAmountString": "0"
                                    },
                                    "owner": "CL9wkGFT3SZRRNa9dgaovuRV7jrVVigBUZ6DjcgySsCU",
                                    "programId": "TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb"
                                  },
                                  {
                                    "accountIndex": 11,
                                    "mint": "Saber2gLauYim4Mvftnrasomsv6NvAuncvMEZwcLpD1",
                                    "uiTokenAmount": {
                                      "uiAmount": 15138.514093,
                                      "decimals": 6,
                                      "amount": "15138514093",
                                      "uiAmountString": "15138.514093"
                                    },
                                    "owner": "LieKvPRE8XeX3Y2xVNHjKlpAScD12lYySBVQ4HqoJ5op",
                                    "programId": "TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb"
                                  }
                                ],
                                "postTokenBalances": [
                                  {
                                    "accountIndex": 4,
                                    "mint": "iouQcQBAiEXe6cKLS85zmZxUqaCqBdeHFpqKoSz615u",
                                    "uiTokenAmount": {
                                      "uiAmount": null,
                                      "decimals": 6,
                                      "amount": "0",
                                      "uiAmountString": "0"
                                    },
                                    "owner": "LieKvPRE8XeX3Y2xVNHjKlpAScD12lYySBVQ4HqoJ5op",
                                    "programId": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
                                  },
                                  {
                                    "accountIndex": 5,
                                    "mint": "iouQcQBAiEXe6cKLS85zmZxUqaCqBdeHFpqKoSz615u",
                                    "uiTokenAmount": {
                                      "uiAmount": 11513.103028,
                                      "decimals": 6,
                                      "amount": "11513103028",
                                      "uiAmountString": "11513.103028"
                                    },
                                    "owner": "rXhAofQCT7NN9TUqigyEAUzV1uLL4boeD8CRkNBSkYk",
                                    "programId": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
                                  },
                                  {
                                    "accountIndex": 10,
                                    "mint": "Saber2gLauYim4Mvftnrasomsv6NvAuncvMEZwcLpD1",
                                    "uiTokenAmount": {
                                      "uiAmount": null,
                                      "decimals": 6,
                                      "amount": "0",
                                      "uiAmountString": "0"
                                    },
                                    "owner": "CL9wkGFT3SZRRNa9dgaovuRV7jrVVigBUZ6DjcgySsCU",
                                    "programId": "TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb"
                                  },
                                  {
                                    "accountIndex": 11,
                                    "mint": "Saber2gLauYim4Mvftnrasomsv6NvAuncvMEZwcLpD1",
                                    "uiTokenAmount": {
                                      "uiAmount": 15489.767829,
                                      "decimals": 6,
                                      "amount": "15489767829",
                                      "uiAmountString": "15489.767829"
                                    },
                                    "owner": "BeiHVPRE8XeX3Y2xVNrSsTpAScH94nYySBVQ4HqgN9at",
                                    "programId": "TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb"
                                  }
                                ],
                                "rewards": []
                              }
                            }
                          ],
                          "blockTime": 1639926816,
                          "blockHeight": 101210751
                        },
                        "err": null
                      }
                    },
                    "subscription": 14
                  }
                }
    """;

        byte[] sampleTxBytes = Base64.getDecoder().decode("ASdDdWBaKXVRA+6flVFiZokic9gK0+r1JWgwGg/GJAkLSreYrGF4rbTCXNJvyut6K6hupJtm72GztLbWNmRF1Q4BAAEDBhrZ0FOHFUhTft4+JhhJo9+3/QL6vHWyI8jkatuFPQzrerzQ2HXrwm2hsYGjM5s+8qMWlbt6vbxngnO8rc3lqgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAy+KIwZmU8DLmYglP3bPzrlpDaKkGu6VIJJwTOYQmRfUBAgIAAQwCAAAAuAsAAAAAAAA=");
        StructLayout.debug(sampleTxBytes);
        Transaction sampleTx = StructLayout.decode(sampleTxBytes, Transaction.class);
        StructLayout.debug(sampleTx);

        AtomicReference<RpcNotificationV2<NotiValueBlock>> notificationReceived = new AtomicReference<>();

        // Spy 객체 생성 및 요청 Mocking (createRpcRequest)
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L); // 항상 ID를 1로 설정
        }).when(spyClientApi).createRpcRequest(anyString(), anyList());

        // 요청 호출 전에 구독 응답 처리를 준비
        setupResponseThread(responseJson );

        // Block Subscription 요청 생성
        RpcResponse<SubscriptionId> subResponse = spyClientApi.blockSubscribe(
                BlockFilter.builder()
                        .mentionsAccountOrProgram(PublicKey.valueOf("LieKvPRE8XeX3Y2xVNHjK1pAScD121YySBVQ4HqoJ5o"))
                        .build(),
                BlockConfig2.builder()
                        .commitment(Commitment.CONFIRMED)
                        .encoding(Encoding.BASE64)
                        .showRewards(true)
                        .transactionDetails(TransactionDetails.FULL)
                        .build(),
                notificationReceived::set // 알림 처리 콜백
        );

        // WebSocket에 전송된 메시지(JSON 요청 검증)
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson); // 요청 JSON 비교

        // 구독 응답 검증
        assertNotNull(subResponse, "구독 응답은 null이 아니어야 합니다.");
        assertEquals(SubscriptionId.of(14), subResponse.getResult(), "구독 ID가 예상 값(14)과 일치해야 합니다.");

        // 알림(Notification) 처리: WebSocketListener에 Notification 전달
        webSocketListener.onMessage(mockWebSocket, notificationJson);

        // 알림 데이터 검증
        RpcNotificationV2<NotiValueBlock> notification = notificationReceived.get();
        assertNotNull(notification, "Notification은 null이 아니어야 합니다.");
        assertEquals(UnsignedLong.valueOf(112301554), notification.getParams().getResult().getContext().getSlot(), "Slot 값이 올바르지 않습니다.");

        NotiValueBlock blockValue = notification.getParams().getResult().getValue();
        assertNotNull(blockValue, "Block value가 null이면 안 됩니다.");
        assertEquals("6ojMHjctdqfB55JDpEpqfHnP96fiaHEcvzEQ2NNcxzHP", blockValue.getBlock().getBlockhash(), "Blockhash 값이 잘못되었습니다.");
        assertEquals("GJp125YAN4ufCSUvZJVdCyWQJ7RPWMmwxoyUQySydZA", blockValue.getBlock().getPreviousBlockhash(), "PreviousBlockhash 값이 잘못되었습니다.");
        assertEquals(UnsignedLong.valueOf(112301553), blockValue.getBlock().getParentSlot(), "ParentSlot 값이 잘못되었습니다.");
        assertEquals(UnsignedLong.valueOf(101210751), blockValue.getBlock().getBlockHeight(), "BlockHeight 값이 잘못되었습니다.");
        assertEquals(Long.valueOf(1639926816), blockValue.getBlock().getBlockTime(), "BlockTime 값이 잘못되었습니다.");
        assertNotNull(blockValue.getBlock().getTransactions(), "Transactions는 null이면 안 됩니다.");

        ResValueConfirmedTransaction transaction = notification.getParams().getResult().getValue().getBlock().getTransactions().get(0);
        assertNotNull(transaction);
        assertEquals(sampleTx.getSignatures().get(0), transaction.getTransaction().getSignatures().get(0), "Transaction의 Signature 값 이 잘못되었습니다.");
        assertEquals(sampleTx.getMessage().getStaticAccountKeys().size(), transaction.getTransaction().getMessage().getAccountKeys().size(), "AccountKey 갯수가 올바르지 않습니다.");
        assertEquals(sampleTx.getMessage().getStaticAccountKeys().get(1), transaction.getTransaction().getMessage().getAccountKeys().get(1), "account 값이 잘못되었습니다.");
        assertEquals(sampleTx.getMessage().getRecentBlockhash(), transaction.getTransaction().getMessage().getRecentBlockhash(), "RecentBlockhash 값이 잘못되었습니다.");
        assertEquals(sampleTx.getMessage().getInstructions().size(), transaction.getTransaction().getMessage().getInstructions().size(), "MessageInstruction 갯수가 올바르지 않습니다.");
        assertEquals(sampleTx.getMessage().getAddressTableLookups().size(), transaction.getTransaction().getMessage().getAddressTableLookups().size(), "ATL 갯수가 올바르지 않습니다.");
        assertEquals(sampleTx.getMessage().getHeader().getNumRequiredSignatures(), transaction.getTransaction().getMessage().getHeader().getNumRequiredSignatures(), "NumRequiredSignatures 값이 올바르지 않습니다.");
        assertEquals(sampleTx.getMessage().getHeader().getNumReadonlyUnsignedAccounts(), transaction.getTransaction().getMessage().getHeader().getNumReadonlyUnsignedAccounts(), "NumReadonlyUnsignedAccounts 값이 올바르지 않습니다.");
        assertEquals(sampleTx.getMessage().getHeader().getNumReadonlySignedAccounts(), transaction.getTransaction().getMessage().getHeader().getNumReadonlySignedAccounts(), "NumReadonlySignedAccounts 값이 올바르지 않습니다.");

        ResValueConfirmedTransaction.Meta meta = notification.getParams().getResult().getValue().getBlock().getTransactions().get(0).getMeta();
        assertNotNull(meta);
        assertEquals(UnsignedLong.valueOf(5000), meta.getFee(), "Fee 값이 올바르지 않습니다.");
        assertEquals(11, meta.getPreBalances().size(), "PreBalances 의 갯수가 올바르지 않습니다.");
        assertEquals(UnsignedLong.valueOf(0), meta.getPreBalances().get(8), "PreBalances 의 값이 올바르지 않습니다.");
        assertEquals(11, meta.getPostBalances().size(), "PostBalances 의 갯수가 올바르지 않습니다.");
        assertEquals(UnsignedLong.valueOf(3680844220L), meta.getPreBalances().get(9), "PostBalances 의 값이 올바르지 않습니다.");
        assertEquals("6LC7BYyxhFRh", meta.getInnerInstructions().get(0).getInstructions().get(3).getData().getValue(), "InnerInstrcution 의 값이 올바르지 않습니다.");
        assertEquals(PublicKey.valueOf("CL9wkGFT3SZRRNa9dgaovuRV7jrVVigBUZ6DjcgySsCU"), meta.getPreTokenBalances().get(2).getOwner(), "PreTokenBalance의 Owner 값이 올바르지 않습니다.");
        assertEquals("Program QMWoBmAyJLAsA1Lh9ugMTw2gciTihncciphzdNzdZYV invoke [2]", meta.getLogMessages().get(1), "LogMessages 의 값이 올바르지 않습니다.");
    }

    @Test
    void testBlockUnsubscribe() throws IOException {
        // 예상 전송 JSON
        String expectedRequestJson = """
        {
          "jsonrpc": "2.0",
          "id": 1,
          "method": "blockUnsubscribe",
          "params": [0]
        }
        """;

        // WebSocket 응답 JSON
        String responseJson = """
        {
          "jsonrpc": "2.0",
          "result": true,
          "id": 1
        }
        """;

        // Spy 객체 구성
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L); // 항상 ID 값 1
        }).when(spyClientApi).createRpcRequest(anyString(), anyList());

        // WebSocket 응답을 설정
        setupResponseThread(responseJson);

        // blockUnsubscribe 호출 및 응답 처리
        RpcResponse<Boolean> unsubResponse = spyClientApi.blockUnsubscribe(SubscriptionId.of(0));

        // WebSocket을 통해 전송된 요청 메시지 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson); // 요청 JSON 검증

        // 응답 검증
        assertNotNull(unsubResponse, "응답 객체는 null이 아니어야 합니다.");
        assertNotNull(unsubResponse.getResult(), "응답의 결과 값(result)은 null이 아니어야 합니다.");
        assertTrue(unsubResponse.getResult(), "구독 해제가 성공적으로 이루어져야 합니다.");
    }


    @Test
    void testLogsSubscribeWithMentionsFilter() throws IOException {
        // 예상 전송 JSON
        String expectedRequestJson = """
        {
          "jsonrpc": "2.0",
          "id": 1,
          "method": "logsSubscribe",
          "params": [
            {
              "mentions": [ "11111111111111111111111111111111" ]
            },
            {
              "commitment": "finalized"
            }
          ]
        }
        """;

        // WebSocket 응답 JSON
        String responseJson = """
        {
          "jsonrpc": "2.0",
          "result": 24040,
          "id": 1
        }
        """;

        // Notification JSON
        String notificationJson = """
        {
          "jsonrpc": "2.0",
          "method": "logsNotification",
          "params": {
            "result": {
              "context": {
                "slot": 5208469
              },
              "value": {
                "signature": "5h6xBEauJ3PK6SWCZ1PGjBvj8vDdWG3KpwATGy1ARAXFSDwt8GFXM7W5Ncn16wmqokgpiKRLuS83KUxyZyv2sUYv",
                "err": null,
                "logs": [
                  "SBF program 83astBRguLMdt2h5U1Tpdq5tjFoJ6noeGwaY3mDLVcri success"
                ]
              }
            },
            "subscription": 24040
          }
        }
        """;

        // 알림 데이터를 받기 위한 AtomicReference
        AtomicReference<RpcNotificationV2<NotiValueLog>> notificationReceived = new AtomicReference<>();

        // Spy 설정
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L);
        }).when(spyClientApi).createRpcRequest(anyString(), anyList());

        // 요청 생성
        LogsFilter filter = LogsFilter.builder()
                .mentions(List.of(PublicKey.valueOf("11111111111111111111111111111111")))
                .build();
        LogsConfig config = LogsConfig.builder()
                .commitment(Commitment.FINALIZED)
                .build();

        // 응답 준비
        setupResponseThread(responseJson);

        // 요청 실행
        RpcResponse<SubscriptionId> subResponse = spyClientApi.logsSubscribe(filter, config, notificationReceived::set);

        // WebSocket으로 전송된 요청 JSON 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson);

        // 응답 수신 검증
        assertNotNull(subResponse, "구독 응답은 null이 아니어야 합니다.");
        assertEquals("24040", subResponse.getResult().toString(), "구독 ID가 예상 값과 일치해야 합니다.");

        // 알림 처리
        webSocketListener.onMessage(mockWebSocket, notificationJson);

        // 알림 데이터 검증
        RpcNotificationV2<NotiValueLog> notification = notificationReceived.get();
        assertNotNull(notification, "알림(Notification)은 null이 아니어야 합니다.");
        assertNotNull(notification.getParams(), "Notification의 Params는 null이 아니어야 합니다.");
        assertEquals(UnsignedLong.valueOf(5208469), notification.getParams().getResult().getContext().getSlot(), "Slot 값 검증 실패");

        NotiValueLog value = notification.getParams().getResult().getValue();
        assertNotNull(value, "Notification 값은 null이면 안 됩니다.");
        assertEquals("5h6xBEauJ3PK6SWCZ1PGjBvj8vDdWG3KpwATGy1ARAXFSDwt8GFXM7W5Ncn16wmqokgpiKRLuS83KUxyZyv2sUYv",
                value.getSignature(), "Signature 값 검증 실패");
        assertNull(value.getErr(), "오류 값은 null이어야 합니다.");
        assertEquals(1, value.getLogs().size(), "Logs 배열 길이가 잘못되었습니다.");
        assertEquals("SBF program 83astBRguLMdt2h5U1Tpdq5tjFoJ6noeGwaY3mDLVcri success",
                value.getLogs().get(0), "Logs 내용이 일치하지 않습니다.");
    }

    @Test
    void testLogsSubscribeWithAllFilter() throws IOException {
        // 예상 전송 JSON
        String expectedRequestJson = """
        {
          "jsonrpc": "2.0",
          "id": 1,
          "method": "logsSubscribe",
          "params": [ "all" ]
        }
        """;

        // WebSocket 응답 JSON
        String responseJson = """
        {
          "jsonrpc": "2.0",
          "result": 24040,
          "id": 1
        }
        """;

        // Notification JSON
        String notificationJson = """
        {
          "jsonrpc": "2.0",
          "method": "logsNotification",
          "params": {
            "result": {
              "context": {
                "slot": 5208469
              },
              "value": {
                "signature": "5h6xBEauJ3PK6SWCZ1PGjBvj8vDdWG3KpwATGy1ARAXFSDwt8GFXM7W5Ncn16wmqokgpiKRLuS83KUxyZyv2sUYv",
                "err": null,
                "logs": [
                  "SBF program 83astBRguLMdt2h5U1Tpdq5tjFoJ6noeGwaY3mDLVcri success"
                ]
              }
            },
            "subscription": 24040
          }
        }
        """;

        // 알림 데이터를 받기 위한 AtomicReference
        AtomicReference<RpcNotificationV2<NotiValueLog>> notificationReceived = new AtomicReference<>();

        // Spy 설정
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L);
        }).when(spyClientApi).createRpcRequest(anyString(), anyList());

        // Logs 필터: "all"
        LogsFilter filter = LogsFilter.builder().type(LogsFilter.Type.ALL).build();

        // 응답 준비
        setupResponseThread(responseJson);

        // 요청 실행
        RpcResponse<SubscriptionId> subResponse = spyClientApi.logsSubscribe(filter, null, notificationReceived::set);

        // 알림 처리
        webSocketListener.onMessage(mockWebSocket, notificationJson);

        // WebSocket으로 전송된 요청 JSON 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson);

        // 응답 수신 검증
        assertNotNull(subResponse, "구독 응답은 null이 아니어야 합니다.");
        assertEquals("24040", subResponse.getResult().toString(), "구독 ID가 예상 값과 일치해야 합니다.");
        RpcNotificationV2<NotiValueLog> notification = notificationReceived.get();
        assertNotNull(notification, "알림(Notification)은 null이 아니어야 합니다.");
    }

    @Test
    void testLogsUnsubscribe() throws IOException {
        // 예상 전송 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "logsUnsubscribe",
      "params": [0]
    }
    """;

        // WebSocket 응답 JSON
        String responseJson = """
    {
      "jsonrpc": "2.0",
      "result": true,
      "id": 1
    }
    """;

        // Spy 객체 설정
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L); // ID는 항상 1로 설정
        }).when(spyClientApi).createRpcRequest(anyString(), anyList());

        // WebSocket 응답을 설정
        setupResponseThread(responseJson);

        // logsUnsubscribe 호출
        RpcResponse<Boolean> unsubResponse = spyClientApi.logsUnsubscribe(SubscriptionId.of(0));

        // WebSocket을 통해 전송된 메시지(JSON 요청) 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson); // 요청 JSON 검증

        // 응답 검증
        assertNotNull(unsubResponse, "응답 객체는 null이 아니어야 합니다.");
        assertNotNull(unsubResponse.getResult(), "응답의 결과 값(result)은 null이 아니어야 합니다.");
        assertTrue(unsubResponse.getResult(), "구독 해제가 성공적으로 이루어져야 합니다.");
    }

    @Test
    void testProgramSubscribeWithVariousConfigurations() throws IOException {
        // 1. 테스트 요청(JSON Request)
        String expectedRequestJson1 = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "programSubscribe",
      "params": [
        "11111111111111111111111111111111",
        {
          "encoding": "base64",
          "commitment": "finalized"
        }
      ]
    }
    """;

        String expectedRequestJson2 = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "programSubscribe",
      "params": [
        "11111111111111111111111111111111",
        {
          "encoding": "json"
        }
      ]
    }
    """;

        String expectedRequestJson3 = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "programSubscribe",
      "params": [
        "11111111111111111111111111111111",
        {
          "encoding": "base64",
          "filters": [
            {
              "dataSize": 80
            }
          ]
        }
      ]
    }
    """;

        // 2. 테스트 응답(JSON Response)
        String responseJson = """
    {
      "jsonrpc": "2.0",
      "result": 24040,
      "id": 1
    }
    """;

        // 3. Notification 데이터(JSON)
        String notificationJson = """
    {
      "jsonrpc": "2.0",
      "method": "programNotification",
      "params": {
        "result": {
          "context": {
            "slot": 5208469
          },
          "value": {
            "pubkey": "H4vnBqifaSACnKa7acsxstsY1iV1bvJNxsCY7enrd1hq",
            "account": {
              "data": [
                "11116bv5nS2h3y12kD1yUKeMZvGcKLSjQgX6BeV7u1FrjeJcKfsHPXHRDEHrBesJhZyqnnq9qJeUuF7WHxiuLuL5twc38w2TXNLxnDbjmuR",
                "base58"
              ],
              "executable": false,
              "lamports": 33594,
              "owner": "11111111111111111111111111111111",
              "rentEpoch": 636,
              "space": 80
            }
          }
        },
        "subscription": 24040
      }
    }
    """;

        // 알림 데이터를 받을 변수
        AtomicReference<RpcNotificationV2<NotiValueProgram>> notificationReceived = new AtomicReference<>();

        // Spy 클래스를 생성
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L);
        }).when(spyClientApi).createRpcRequest(anyString(), anyList());

        // 4. 요청 케이스 별 테스트 실행

        // Case 1: Encoding Base64 with Finalized Commitment
        setupResponseThread(responseJson);
        RpcResponse<SubscriptionId> subResponse1 = spyClientApi.programSubscribe(
                PublicKey.valueOf("11111111111111111111111111111111"),
                ProgramConfig.builder()
                        .encoding(Encoding.BASE64)
                        .commitment(Commitment.FINALIZED)
                        .build(),
                notificationReceived::set
        );

        // WebSocket Request 검증
        ArgumentCaptor<String> messageCaptor1 = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket, times(1)).send(messageCaptor1.capture());
        String capturedRequestJson1 = messageCaptor1.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson1, capturedRequestJson1);

        // 응답 검증
        assertNotNull(subResponse1, "구독 응답은 null이 아니어야 합니다.");
        assertEquals("24040", subResponse1.getResult().toString(), "구독 ID 검증 실패");

        // 알림 처리: WebSocketListener에 JSON 전달
        webSocketListener.onMessage(mockWebSocket, notificationJson);

        // 알림 데이터 검증
        RpcNotificationV2<NotiValueProgram> notification1 = notificationReceived.get();
        assertNotNull(notification1, "알림은 null이 아니어야 합니다.");
        assertEquals(UnsignedLong.valueOf(5208469), notification1.getParams().getResult().getContext().getSlot(), "Slot 데이터 검증 실패");

        NotiValueProgram programAccount = notification1.getParams().getResult().getValue();
        assertNotNull(programAccount, "Program Account 데이터는 null이면 안 됩니다.");
        assertEquals("H4vnBqifaSACnKa7acsxstsY1iV1bvJNxsCY7enrd1hq", programAccount.getPubkey().toString(), "Pubkey 값 검증 실패");
        assertEquals(UnsignedLong.valueOf(33594), programAccount.getAccount().getLamports(), "Lamports 검증 실패");
        assertEquals("11111111111111111111111111111111", programAccount.getAccount().getOwner().toString(), "Owner 값 검증 실패");

        // Case 2: Encoding JSON
        setupResponseThread(responseJson);
        RpcResponse<SubscriptionId> subResponse2 = spyClientApi.programSubscribe(
                PublicKey.valueOf("11111111111111111111111111111111"),
                ProgramConfig.builder()
                        .encoding(Encoding.JSON)
                        .build(),
                notificationReceived::set
        );

        // WebSocket Request 검증
        ArgumentCaptor<String> messageCaptor2 = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket, times(2)).send(messageCaptor2.capture());
        String capturedRequestJson2 = messageCaptor2.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson2, capturedRequestJson2);

        // 응답 검증
        assertNotNull(subResponse2, "JSON_PARSED 응답은 null이 아니어야 합니다.");
        assertEquals("24040", subResponse2.getResult().toString(), "JSON_PARSED 구독 ID 검증 실패");

        // Case 3: Encoding Base64 with Filters (Data Size = 80)
        setupResponseThread(responseJson);
        RpcResponse<SubscriptionId> subResponse3 = spyClientApi.programSubscribe(
                PublicKey.valueOf("11111111111111111111111111111111"),
                ProgramConfig.builder()
                        .encoding(Encoding.BASE64)
                        .filters(List.of(ProgramAccountFilter.builder().dataSize(UnsignedLong.valueOf(80)).build()))
                        .build(),
                notificationReceived::set
        );

        // WebSocket Request 검증
        ArgumentCaptor<String> messageCaptor3 = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket, times(3)).send(messageCaptor3.capture());
        String capturedRequestJson3 = messageCaptor3.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson3, capturedRequestJson3);

        // 응답 검증
        assertNotNull(subResponse3, "Filters 구독 응답은 null이 아니어야 합니다.");
        assertEquals("24040", subResponse3.getResult().toString(), "Filters 구독 ID 검증 실패");
    }

    @Test
    void testProgramUnsubscribe() throws IOException {
        // 예상 전송 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "programUnsubscribe",
      "params": [0]
    }
    """;

        // WebSocket 응답 JSON
        String responseJson = """
    {
      "jsonrpc": "2.0",
      "result": true,
      "id": 1
    }
    """;

        // Spy 객체 설정
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L); // ID는 항상 1로 설정
        }).when(spyClientApi).createRpcRequest(anyString(), anyList());

        // WebSocket 응답을 설정
        setupResponseThread(responseJson);

        // programUnsubscribe 호출
        RpcResponse<Boolean> unsubResponse = spyClientApi.programUnsubscribe(SubscriptionId.of(0));

        // WebSocket을 통해 전송된 메시지(JSON 요청) 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson); // 요청 JSON 검증

        // 응답 검증
        assertNotNull(unsubResponse, "응답 객체는 null이 아니어야 합니다.");
        assertNotNull(unsubResponse.getResult(), "응답의 결과 값(result)은 null이 아니어야 합니다.");
        assertTrue(unsubResponse.getResult(), "구독 해제가 성공적으로 이루어져야 합니다.");
    }

    @Test
    void testRootSubscribe() throws IOException {
        // 예상 전송 JSON 요청
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "rootSubscribe"
    }
    """;

        // WebSocket 응답 JSON
        String responseJson = """
    {
      "jsonrpc": "2.0",
      "result": 0,
      "id": 1
    }
    """;

        // WebSocket 알림 JSON
        String notificationJson = """
    {
      "jsonrpc": "2.0",
      "method": "rootNotification",
      "params": {
        "result": 42,
        "subscription": 0
      }
    }
    """;

        // 변수: 알림 결과 저장
        AtomicReference<RpcNotification<UnsignedLong>> notificationReceived = new AtomicReference<>();

        // Spy 객체 설정
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L); // ID는 항상 1로 설정
        }).when(spyClientApi).createRpcRequest(anyString(), nullable(List.class));

        // WebSocket 응답 설정
        setupResponseThread(responseJson);

        // rootSubscribe 호출
        RpcResponse<SubscriptionId> subscriptionResponse = spyClientApi.rootSubscribe(notificationReceived::set);

        // WebSocketListener에 알림 전송 (알림 JSON을 전달)
        webSocketListener.onMessage(mockWebSocket, notificationJson);

        // 1. WebSocket 요청 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson); // 요청 JSON 검증

        // 2. 응답 검증
        assertNotNull(subscriptionResponse, "응답 객체는 null이 아니어야 합니다.");
        assertNotNull(subscriptionResponse.getResult(), "응답의 결과 값(result)은 null이 아니어야 합니다.");
        assertEquals(SubscriptionId.of(0), subscriptionResponse.getResult(), "구독 ID는 0이어야 합니다.");
        assertEquals(1, subscriptionResponse.getId(), "구독 ID는 0이어야 합니다.");

        // 3. 알림 데이터 검증
        UnsignedLong notificationResult = notificationReceived.get().getParams().getResult();
        assertNotNull(notificationResult, "알림 결과는 null이 아니어야 합니다.");
        assertEquals(UnsignedLong.valueOf(42), notificationResult, "알림에서 받은 결과 값(result)은 42여야 합니다.");
    }

    @Test
    void testRootUnsubscribe() throws IOException {
        // 예상 전송 JSON 요청
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "rootUnsubscribe",
      "params": [0]
    }
    """;

        // WebSocket 응답 JSON
        String responseJson = """
    {
      "jsonrpc": "2.0",
      "result": true,
      "id": 1
    }
    """;

        // Spy 객체 설정
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L); // 항상 ID는 1로 설정
        }).when(spyClientApi).createRpcRequest(anyString(), anyList());

        // WebSocket 응답 설정
        setupResponseThread(responseJson);

        // rootUnsubscribe 호출
        RpcResponse<Boolean> unsubResponse = spyClientApi.rootUnsubscribe(SubscriptionId.of(0));

        // 1. WebSocket 요청 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson); // 요청 JSON 검증

        // 2. 응답 검증
        assertNotNull(unsubResponse, "응답 객체는 null이 아니어야 합니다.");
        assertNotNull(unsubResponse.getResult(), "응답의 결과 값(result)은 null이 아니어야 합니다.");
        assertTrue(unsubResponse.getResult(), "rootUnsubscribe 호출이 성공적이어야 합니다.");
    }

    @Test
    void testSignatureSubscribe() throws IOException {
        // 예상 전송 JSON 요청
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "signatureSubscribe",
      "params": [
        "2EBVM6cB8vAAD93Ktr6Vd8p67XPbQzCJX47MpReuiCXJAtcjaxpvWpcg9Ege1Nr5Tk3a2GFrByT7WPBjdsTycY9b",
        {
          "commitment": "finalized",
          "enableReceivedNotification": false
        }
      ]
    }
    """;

        // WebSocket 응답 JSON
        String responseJson = """
    {
      "jsonrpc": "2.0",
      "result": 24006,
      "id": 1
    }
    """;

        // WebSocket 알림 JSON - 첫 번째 케이스
        String notificationJson1 = """
    {
      "jsonrpc": "2.0",
      "method": "signatureNotification",
      "params": {
        "result": {
          "context": {
            "slot": 5207624
          },
          "value": {
            "err": null
          }
        },
        "subscription": 24006
      }
    }
    """;

        // WebSocket 알림 JSON - 두 번째 케이스
        String notificationJson2 = """
    {
      "jsonrpc": "2.0",
      "method": "signatureNotification",
      "params": {
        "result": {
          "context": {
            "slot": 5207624
          },
          "value": "receivedSignature"
        },
        "subscription": 24006
      }
    }
    """;

        // 변수: 알림 결과 저장
        AtomicReference<RpcNotificationV2<NotiValueSignature>> notificationReceived = new AtomicReference<>();

        // Spy 객체 설정
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L); // 항상 ID는 1로 설정
        }).when(spyClientApi).createRpcRequest(anyString(), anyList());

        // WebSocket 응답 설정
        setupResponseThread(responseJson);

        // signatureSubscribe 호출
        RpcResponse<SubscriptionId> subscriptionResponse = spyClientApi.signatureSubscribe(
                Signature.of("2EBVM6cB8vAAD93Ktr6Vd8p67XPbQzCJX47MpReuiCXJAtcjaxpvWpcg9Ege1Nr5Tk3a2GFrByT7WPBjdsTycY9b"),
                SignatureConfig.builder()
                        .commitment(Commitment.FINALIZED)
                        .enableReceivedNotification(false)
                        .build(),
                notificationReceived::set
        );

        // 1. WebSocket 요청 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson); // 요청 JSON 검증

        // 2. 응답 검증
        assertNotNull(subscriptionResponse, "응답 객체는 null이 아니어야 합니다.");
        assertNotNull(subscriptionResponse.getResult(), "응답의 결과 값(result)은 null이 아니어야 합니다.");
        assertEquals(SubscriptionId.of(24006), subscriptionResponse.getResult(), "구독 ID는 24006이어야 합니다.");

        // WebSocketListener에 첫 번째 알림 전송 (알림 JSON을 전달)
        webSocketListener.onMessage(mockWebSocket, notificationJson1);

        // 3. 첫 번째 알림 데이터 검증
        RpcNotificationV2<NotiValueSignature> firstNotification = notificationReceived.get();
        assertNotNull(firstNotification, "첫 번째 알림은 null이 아니어야 합니다.");
        assertEquals(UnsignedLong.valueOf(5207624), firstNotification.getParams().getResult().getContext().getSlot(), "첫 번째 알림의 slot 값이 올바르지 않습니다.");
        NotiValueSignature firstResult = firstNotification.getParams().getResult().getValue();
        assertNotNull(firstResult, "첫 번째 알림의 결과 값은 null이 아니어야 합니다.");
        assertNull(firstResult.getError(), "첫 번째 알림의 err 값은 null이어야 합니다.");

        // WebSocketListener에 두 번째 알림 전송 (알림 JSON을 전달)
        webSocketListener.onMessage(mockWebSocket, notificationJson2);

        // 4. 두 번째 알림 데이터 검증
        RpcNotificationV2<NotiValueSignature> secondNotification = notificationReceived.get();
        assertNotNull(secondNotification, "두 번째 알림은 null이 아니어야 합니다.");
        assertEquals(UnsignedLong.valueOf(5207624), secondNotification.getParams().getResult().getContext().getSlot(), "두 번째 알림의 slot 값이 올바르지 않습니다.");
        NotiValueSignature secondResult = secondNotification.getParams().getResult().getValue();
        assertEquals(SignatureStatus.RECEIVED_SIGNATURE, secondResult.getStatus(), "두 번째 알림의 값이 'receivedSignature'여야 합니다.");
        assertNull(secondResult.getError(), "두 번째 알림의 에러값이 null여야 합니다.");
    }

    @Test
    void testSignatureUnsubscribe() throws IOException {
        // 예상 전송 JSON 요청
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "signatureUnsubscribe",
      "params": [0]
    }
    """;

        // WebSocket 응답 JSON
        String responseJson = """
    {
      "jsonrpc": "2.0",
      "result": true,
      "id": 1
    }
    """;

        // Spy 객체 생성
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L); // 항상 ID를 1로 설정
        }).when(spyClientApi).createRpcRequest(anyString(), anyList());

        // WebSocket 응답 설정
        setupResponseThread(responseJson);

        // signatureUnsubscribe 호출
        RpcResponse<Boolean> unsubResponse = spyClientApi.signatureUnsubscribe(SubscriptionId.of(0));

        // 1. WebSocket 요청 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson); // 요청 JSON 검증

        // 2. 응답 검증
        assertNotNull(unsubResponse, "응답 객체는 null이 아니어야 합니다.");
        assertNotNull(unsubResponse.getResult(), "응답의 결과 값(result)은 null이 아니어야 합니다.");
        assertTrue(unsubResponse.getResult(), "signatureUnsubscribe 요청이 성공적이어야 합니다.");
    }

    @Test
    void testSlotSubscribe() throws IOException {
        // 예상 전송 JSON 요청
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "slotSubscribe"
    }
    """;

        // WebSocket 응답 JSON
        String responseJson = """
    {
      "jsonrpc": "2.0",
      "result": 0,
      "id": 1
    }
    """;

        // WebSocket 알림 JSON
        String notificationJson = """
    {
      "jsonrpc": "2.0",
      "method": "slotNotification",
      "params": {
        "result": {
          "parent": 75,
          "root": 44,
          "slot": 76
        },
        "subscription": 0
      }
    }
    """;

        // 변수: 알림 결과 저장
        AtomicReference<RpcNotification<NotiValueSlot>> notificationReceived = new AtomicReference<>();

        // Spy 객체 생성
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L); // 항상 ID를 1로 설정
        }).when(spyClientApi).createRpcRequest(anyString(), nullable(List.class));

        // WebSocket 응답 설정
        setupResponseThread(responseJson);

        // slotSubscribe 호출
        RpcResponse<SubscriptionId> subscriptionResponse = spyClientApi.slotSubscribe(notificationReceived::set);

        // 1. WebSocket 요청 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson); // 요청 JSON 검증

        // 2. 응답 검증
        assertNotNull(subscriptionResponse, "응답 객체는 null이 아니어야 합니다.");
        assertNotNull(subscriptionResponse.getResult(), "응답의 결과 값(result)은 null이 아니어야 합니다.");
        assertEquals(SubscriptionId.of(0), subscriptionResponse.getResult(), "구독 ID는 0이어야 합니다.");

        // WebSocketListener에 알림 전송 (알림 JSON을 전달)
        webSocketListener.onMessage(mockWebSocket, notificationJson);

        // 3. 알림 데이터 검증
        RpcNotification<NotiValueSlot> notification = notificationReceived.get();
        assertNotNull(notification, "알림 객체는 null이 아니어야 합니다.");
        assertEquals(SubscriptionId.of(0), notification.getParams().getSubscription(), "알림의 subscription ID는 0이어야 합니다.");
        NotiValueSlot slotDetails = notification.getParams().getResult();
        assertNotNull(slotDetails, "알림의 result 값은 null이 아니어야 합니다.");
        assertEquals(UnsignedLong.valueOf(75), slotDetails.getParent(), "알림의 parent 값이 올바르지 않습니다.");
        assertEquals(UnsignedLong.valueOf(44), slotDetails.getRoot(), "알림의 root 값이 올바르지 않습니다.");
        assertEquals(UnsignedLong.valueOf(76), slotDetails.getSlot(), "알림의 slot 값이 올바르지 않습니다.");
    }

    @Test
    void testSlotsUpdatesSubscribe() throws IOException {
        // 예상 전송 JSON 요청
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "slotsUpdatesSubscribe"
    }
    """;

        // WebSocket 응답 JSON
        String responseJson = """
    {
      "jsonrpc": "2.0",
      "result": 0,
      "id": 1
    }
    """;

        // WebSocket 알림 JSON
        String notificationJson = """
    {
      "jsonrpc": "2.0",
      "method": "slotsUpdatesNotification",
      "params": {
        "result": {
          "parent": 75,
          "slot": 76,
          "timestamp": 1625081266243,
          "type": "optimisticConfirmation"
        },
        "subscription": 0
      }
    }
    """;

        // 알림 결과를 저장할 변수
        AtomicReference<RpcNotification<NotiValueSlotUpdates>> notificationReceived = new AtomicReference<>();

        // Spy 객체 설정
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L); // 항상 ID는 1로 설정
        }).when(spyClientApi).createRpcRequest(anyString(), nullable(List.class));

        // WebSocket 응답 설정
        setupResponseThread(responseJson);

        // slotsUpdatesSubscribe 호출
        RpcResponse<SubscriptionId> subscriptionResponse = spyClientApi.slotsUpdatesSubscribe(notificationReceived::set);

        // 1. WebSocket 요청 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson); // 요청 JSON 검증

        // 2. 응답 검증
        assertNotNull(subscriptionResponse, "응답 객체는 null이 아니어야 합니다.");
        assertNotNull(subscriptionResponse.getResult(), "응답의 결과 값(result)은 null이 아니어야 합니다.");
        assertEquals(SubscriptionId.of(0), subscriptionResponse.getResult(), "구독 ID는 0이어야 합니다.");

        // WebSocketListener에 알림 전송 (알림 JSON을 전달)
        webSocketListener.onMessage(mockWebSocket, notificationJson);

        // 3. 알림 데이터 검증
        RpcNotification<NotiValueSlotUpdates> notification = notificationReceived.get();
        assertNotNull(notification, "알림 객체는 null이 아니어야 합니다.");
        assertEquals(SubscriptionId.of(0), notification.getParams().getSubscription(), "알림의 subscription ID는 0이어야 합니다.");
        NotiValueSlotUpdates slotsUpdateDetails = notification.getParams().getResult();
        assertNotNull(slotsUpdateDetails, "알림의 result 값은 null이 아니어야 합니다.");
        assertEquals(UnsignedLong.valueOf(75), slotsUpdateDetails.getParent(), "알림의 parent 값이 올바르지 않습니다.");
        assertEquals(UnsignedLong.valueOf(76), slotsUpdateDetails.getSlot(), "알림의 slot 값이 올바르지 않습니다.");
        assertEquals(1625081266243L, slotsUpdateDetails.getTimestamp(), "알림의 timestamp 값이 올바르지 않습니다.");
        assertEquals(SlotUpdateType.OPTIMISTIC_CONFIRMATION, slotsUpdateDetails.getType(), "알림의 type 값이 올바르지 않습니다.");
    }

    @Test
    void testSlotsUpdatesUnsubscribe() throws IOException {
        // 예상 전송 JSON 요청
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "slotsUpdatesUnsubscribe",
      "params": [0]
    }
    """;

        // WebSocket 응답 JSON
        String responseJson = """
    {
      "jsonrpc": "2.0",
      "result": true,
      "id": 1
    }
    """;

        // Spy 객체 생성
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L); // 항상 ID를 1로 설정
        }).when(spyClientApi).createRpcRequest(anyString(), anyList());

        // WebSocket 응답 설정
        setupResponseThread(responseJson);

        // slotsUpdatesUnsubscribe 호출
        RpcResponse<Boolean> unsubResponse = spyClientApi.slotsUpdatesUnsubscribe(SubscriptionId.of(0));

        // 1. WebSocket 요청 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson); // 요청 JSON 검증

        // 2. 응답 검증
        assertNotNull(unsubResponse, "응답 객체는 null이 아니어야 합니다.");
        assertNotNull(unsubResponse.getResult(), "응답의 결과 값(result)은 null이 아니어야 합니다.");
        assertTrue(unsubResponse.getResult(), "slotsUpdatesUnsubscribe 요청이 성공적이어야 합니다.");
    }

    @Test
    void testSlotUnsubscribe() throws IOException {
        // 예상 전송 JSON 요청
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "slotUnsubscribe",
      "params": [0]
    }
    """;

        // WebSocket 응답 JSON
        String responseJson = """
    {
      "jsonrpc": "2.0",
      "result": true,
      "id": 1
    }
    """;

        // Spy 객체 생성
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L); // 항상 ID를 1로 설정
        }).when(spyClientApi).createRpcRequest(anyString(), anyList());

        // WebSocket 응답 설정
        setupResponseThread(responseJson);

        // slotUnsubscribe 호출
        RpcResponse<Boolean> unsubResponse = spyClientApi.slotUnsubscribe(SubscriptionId.of(0));

        // 1. WebSocket 요청 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson); // 요청 JSON 검증

        // 2. 응답 검증
        assertNotNull(unsubResponse, "응답 객체는 null이 아니어야 합니다.");
        assertNotNull(unsubResponse.getResult(), "응답의 결과 값(result)은 null이 아니어야 합니다.");
        assertTrue(unsubResponse.getResult(), "slotUnsubscribe 요청이 성공적이어야 합니다.");
    }

    @Test
    void testVoteSubscribe() throws IOException {
        // 예상 전송 JSON 요청
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "voteSubscribe"
    }
    """;

        // WebSocket 응답 JSON
        String responseJson = """
    {
      "jsonrpc": "2.0",
      "result": 0,
      "id": 1
    }
    """;

        // WebSocket 알림 JSON
        String notificationJson = """
    {
      "jsonrpc": "2.0",
      "method": "voteNotification",
      "params": {
        "result": {
          "hash": "8Rshv2oMkPu5E4opXTRyuyBeZBqQ4S477VG26wUTFxUM",
          "slots": [1, 2],
          "timestamp": null
        },
        "subscription": 0
      }
    }
    """;

        // 알림 데이터를 저장할 Atomic 변수
        AtomicReference<RpcNotification<NotiValueVote>> notificationReceived = new AtomicReference<>();

        // Spy 객체 생성
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L); // 항상 ID를 1로 설정
        }).when(spyClientApi).createRpcRequest(anyString(), nullable(List.class));

        // WebSocket 응답 설정
        setupResponseThread(responseJson);

        // voteSubscribe 호출
        RpcResponse<SubscriptionId> subscriptionResponse = spyClientApi.voteSubscribe(notificationReceived::set);

        // 1. WebSocket 요청 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson); // 요청 JSON 검증

        // 2. 응답 검증
        assertNotNull(subscriptionResponse, "응답 객체는 null이 아니어야 합니다.");
        assertNotNull(subscriptionResponse.getResult(), "응답의 결과 값(result)은 null이 아니어야 합니다.");
        assertEquals(SubscriptionId.of(0), subscriptionResponse.getResult(), "구독 ID는 0이어야 합니다.");

        // WebSocketListener에 알림 전송 (알림 JSON 전달)
        webSocketListener.onMessage(mockWebSocket, notificationJson);

        // 3. 알림 데이터 검증
        RpcNotification<NotiValueVote> notification = notificationReceived.get();
        assertNotNull(notification, "알림 객체는 null이 아니어야 합니다.");
        assertEquals(SubscriptionId.of(0), notification.getParams().getSubscription(), "알림의 subscription ID는 0이어야 합니다.");
        NotiValueVote voteDetails = notification.getParams().getResult();
        assertNotNull(voteDetails, "알림의 result 값은 null이 아니어야 합니다.");
        assertEquals("8Rshv2oMkPu5E4opXTRyuyBeZBqQ4S477VG26wUTFxUM", voteDetails.getHash(), "알림의 hash 값이 올바르지 않습니다.");
        assertEquals(List.of(UnsignedLong.valueOf(1), UnsignedLong.valueOf(2)), voteDetails.getSlots(), "알림의 slots 값이 올바르지 않습니다.");
        assertNull(voteDetails.getTimestamp(), "알림의 timestamp 값이 null이어야 합니다.");
    }

    @Test
    void testVoteUnsubscribe() throws IOException {
        // 예상 전송 JSON 요청
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "voteUnsubscribe",
      "params": [0]
    }
    """;

        // WebSocket 응답 JSON
        String responseJson = """
    {
      "jsonrpc": "2.0",
      "result": true,
      "id": 1
    }
    """;

        // Spy 객체 생성
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L); // 항상 ID는 1로 설정
        }).when(spyClientApi).createRpcRequest(anyString(), anyList());

        // WebSocket 응답 설정
        setupResponseThread(responseJson);

        // voteUnsubscribe 호출
        RpcResponse<Boolean> unsubResponse = spyClientApi.voteUnsubscribe(SubscriptionId.of(0));

        // 1. WebSocket 요청 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket).send(messageCaptor.capture());
        String sentJson = messageCaptor.getValue();
        assertJsonEqualsIgnoringId(expectedRequestJson, sentJson); // 요청 JSON 검증

        // 2. 응답 검증
        assertNotNull(unsubResponse, "응답 객체는 null이 아니어야 합니다.");
        assertNotNull(unsubResponse.getResult(), "응답의 결과 값(result)은 null이 아니어야 합니다.");
        assertTrue(unsubResponse.getResult(), "voteUnsubscribe 요청이 성공적이어야 합니다.");
    }

    @Test
    void testResubscribeAll() throws IOException {
        // Mock SubscriptionId 및 SubscriptionContext 추가
        SubscriptionId mockId1 = SubscriptionId.of(1L);
        SubscriptionId mockId2 = SubscriptionId.of(2L);

        Type type1 = String.class; // 테스트 타입
        NotificationListener<RpcRequest> listener1 = mock(NotificationListener.class);
        MoshiWebsocketMethodApiImpl.SubscriptionContext<?> context1 =
                new MoshiWebsocketMethodApiImpl.SubscriptionContext<>(RpcRequest.class, listener1, "mockMethod1", List.of("param1"));

        Type type2 = Integer.class; // 다른 테스트 타입
        NotificationListener<RpcRequest> listener2 = mock(NotificationListener.class);
        MoshiWebsocketMethodApiImpl.SubscriptionContext<?> context2 =
                new MoshiWebsocketMethodApiImpl.SubscriptionContext<>(RpcRequest.class, listener2, "mockMethod2", List.of("param2"));

        // listeners에 mock된 구독 추가
        clientApi.listeners.put(mockId1, context1);
        clientApi.listeners.put(mockId2, context2);

        // Resubscribe 요청을 전송하기 전에 WebSocket 응답을 준비
        String expectedResponseJson1 = """
    {
      "jsonrpc": "2.0",
      "result": 1,
      "id": 1
    }
    """;

        String expectedResponseJson2 = """
    {
      "jsonrpc": "2.0",
      "result": 2,
      "id": 2
    }
    """;
        setupResponseThread(expectedResponseJson1, 100); // 비동기적으로 첫 번째 응답 생성
        setupResponseThread(expectedResponseJson2, 200); // 비동기적으로 두 번째 응답 생성

        // ResubscribeAll 호출 (테스트 대상 메서드)
        clientApi.resubscribeAll();

        // WebSocket 요청이 전송되었는지 검증
        ArgumentCaptor<String> requestCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket, times(2)).send(requestCaptor.capture()); // 두 번 전송되었는지 확인
        List<String> capturedRequests = requestCaptor.getAllValues();
        capturedRequests.sort((req1, req2) -> {
            if (req1.contains("mockMethod1")) {
                return -1;
            } else if (req2.contains("mockMethod1")) {
                return 1;
            } else {
                return 0;
            }
        });

        // 요청된 메시지가 예상 값과 일치하는지 확인
        assertTrue(capturedRequests.get(0).contains("mockMethod1"), "첫 번째 재구독 요청이 올바르지 않습니다.");
        assertTrue(capturedRequests.get(0).contains("param1"), "첫 번째 재구독 요청의 매개변수가 잘못되었습니다.");
        assertTrue(capturedRequests.get(1).contains("mockMethod2"), "두 번째 재구독 요청이 올바르지 않습니다.");
        assertTrue(capturedRequests.get(1).contains("param2"), "두 번째 재구독 요청의 매개변수가 잘못되었습니다.");

        // Pending Subscription 갱신이 제대로 이루어졌는지 확인
        assertEquals(2, clientApi.countListeners(), "Listener 개수가 일치하지 않습니다.");
    }

    @Test
    void testReconnectWebSocket_FirstAttemptSuccess() throws InterruptedException {
        OkHttpClient mockHttpClient = mock(OkHttpClient.class);
        WebSocket mockWebSocket = mock(WebSocket.class);

        // Request와 Listener를 캡처하기 위한 AtomicReferences
        AtomicReference<Request> capturedRequest = new AtomicReference<>();
        AtomicReference<WebSocketListener> listenerRef = new AtomicReference<>();

        // 동기화를 위한 CountDownLatch 추가
        CountDownLatch latch = new CountDownLatch(1);

        when(mockHttpClient.newWebSocket(any(Request.class), any(WebSocketListener.class)))
                .thenAnswer(invocation -> {
                    capturedRequest.set(invocation.getArgument(0, Request.class));
                    WebSocketListener listener = invocation.getArgument(1, WebSocketListener.class);
                    listenerRef.set(listener);

                    // Listener 설정이 완료되었음을 알림
                    latch.countDown();

                    return mockWebSocket;
                });

        // 테스트 대상 클래스 스파이
        MoshiWebsocketMethodApiImpl clientApi = spy(new MoshiWebsocketMethodApiImpl(mockConfig) {
            @Override
            protected OkHttpClient createHttpClient() {
                return mockHttpClient;
            }
        });
        doReturn(mockHttpClient).when(clientApi).createHttpClient();
        doReturn(mockWebSocket).when(clientApi).connectWebSocket();

        // WebSocket 연결 트리거
        clientApi.triggerReconnect();

        // Listener 설정 확인 및 대기
        boolean listenerSet = latch.await(2, TimeUnit.SECONDS); // 최대 2초 대기
        assertTrue(listenerSet, "WebSocketListener 설정이 완료되지 않았습니다.");
        assertNotNull(listenerRef.get(), "WebSocketListener는 null이 아니어야 합니다.");

        // WebSocket 연결 실패 시뮬레이션
        RuntimeException exception = new RuntimeException("Test failure");
        WebSocketListener webSocketListener = listenerRef.get();
        webSocketListener.onFailure(mockWebSocket, exception, null);

        // 비동기 작업 대기
        Thread.sleep(1100);

        // connectWebSocket 호출 확인
        verify(clientApi, atLeastOnce()).connectWebSocket();
    }

    @Test
    @Disabled // 테스트에 3분 소요되서 Disable 처리함
    void testReconnectWebSocket_NinthAttemptSuccess() throws InterruptedException {
        // 재연결 시도 횟수 추적
        AtomicInteger reconnectAttempts = new AtomicInteger(0);
        AtomicBoolean successfullyConnected = new AtomicBoolean(false);
        AtomicInteger concurrentAttempts = new AtomicInteger(0);

        CountDownLatch successLatch = new CountDownLatch(1); // 연결 성공 대기를 위한 CountDownLatch

        // Mock 대상 생성
        OkHttpClient mockHttpClient = mock(OkHttpClient.class);
        WebSocket mockWebSocket = mock(WebSocket.class);

        AtomicReference<WebSocketListener> listenerRef = new AtomicReference<>();

        // HttpClient Mocking: Listener를 설정하고 반환
        when(mockHttpClient.newWebSocket(any(Request.class), any(WebSocketListener.class)))
                .thenAnswer(invocation -> {
                    WebSocketListener listener = invocation.getArgument(1, WebSocketListener.class);
                    listenerRef.set(listener);
                    return mockWebSocket;
                });

        // 테스트 대상 클래스
        MoshiWebsocketMethodApiImpl clientApi = spy(new MoshiWebsocketMethodApiImpl(mockConfig) {
            @Override
            protected OkHttpClient createHttpClient() {
                return mockHttpClient;
            }
        });

        // 9번째 시도에서만 연결 성공하도록 Mocking
        doAnswer(invocation -> {
            int attempt = reconnectAttempts.incrementAndGet();

            // 동시성 체크
            if (concurrentAttempts.incrementAndGet() > 1) {
                concurrentAttempts.decrementAndGet();
                fail("Concurrent reconnect attempts detected!");
            }

            concurrentAttempts.decrementAndGet();

            // 9번째 재연결에서 성공 처리
            if (attempt == 9) {
                successfullyConnected.set(true);
                successLatch.countDown(); // 연결 성공 상태를 알림
                return mockWebSocket;
            }

            // 실패 처리
            throw new RuntimeException("Reconnect failed");
        }).when(clientApi).connectWebSocket();

        // WebSocketListener에서 연결 실패를 트리거
        clientApi.triggerReconnect();
        WebSocketListener listener = listenerRef.get();
        assertNotNull(listener, "WebSocketListener는 null이 아니어야 합니다.");
        listener.onFailure(mockWebSocket, new RuntimeException("WebSocket failed"), null);

        // 연결 성공을 대기
        // - 1번째 시도: 1초 (1000 ms)
        // - 2번째 시도: 2초 (2000 ms)
        // - 3번째 시도: 4초 (4000 ms)
        // - 4번째 시도: 8초 (8000 ms)
        // - 5번째 시도: 16초 (16000 ms)
        // - 6번째 시도 이후: 최대 지연 시간인 30초로 고정
        // 총 121초 (2분 1초) 재연결 30초 대기까지 포함해서 3분 대기 설정
        boolean success = successLatch.await(3, TimeUnit.MINUTES); // 12초 내로 연결 성공 대기
        assertTrue(success, "WebSocket이 9번째 시도에서 성공적으로 연결되지 않았습니다.");

        // 검증
        assertEquals(9, reconnectAttempts.get(), "9번째 시도에서 연결 성공해야 합니다.");
        verify(clientApi, times(9)).connectWebSocket(); // 정확히 9번 호출되었는지 확인
    }
}
