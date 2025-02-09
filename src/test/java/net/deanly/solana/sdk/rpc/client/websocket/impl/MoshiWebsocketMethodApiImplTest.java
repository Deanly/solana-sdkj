package net.deanly.solana.sdk.rpc.client.websocket.impl;

import com.google.common.primitives.UnsignedLong;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.rpc.client.RpcClient;
import net.deanly.solana.sdk.rpc.request.RpcRequest;
import net.deanly.solana.sdk.rpc.request.config.*;
import net.deanly.solana.sdk.rpc.request.filter.BlockFilter;
import net.deanly.solana.sdk.rpc.response.*;
import net.deanly.solana.sdk.transaction.Transaction;
import net.deanly.solana.sdk.types.*;
import net.deanly.solana.sdk.types.codec.Base58;
import net.deanly.structlayout.StructLayout;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
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
        setupResponseThread(responseJson, 200);
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
        assertEquals(5199307, notification.getParams().getResult().getContext().getSlot(), "Slot 값이 잘못되었습니다.");
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
        assertEquals(5199307, notification.getParams().getResult().getContext().getSlot(), "Slot 값이 잘못되었습니다.");

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

        // 요청 호출 전 WebSocket 응답 준비
        setupResponseThread(responseJson);

        // Spy 객체 생성 및 createRpcRequest Mocking
        MoshiWebsocketMethodApiImpl spyClientApi = spy(clientApi);
        doAnswer(invocation -> {
            String method = invocation.getArgument(0);
            List<Object> params = invocation.getArgument(1);
            return new RpcRequest(method, params, 1L); // 항상 ID 값은 1
        }).when(spyClientApi).createRpcRequest(anyString(), anyList());

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
        assertEquals(112301554, notification.getParams().getResult().getContext().getSlot(), "Slot 값이 올바르지 않습니다.");

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

}
