package net.deanly.solana.sdk.rpc.client.http.impl;

import net.deanly.solana.sdk.rpc.client.ClientConfig;
import net.deanly.structlayout.type.guava.UnsignedLong;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.rpc.client.RpcClient;
import net.deanly.solana.sdk.rpc.client.adapter.MoshiNumberJsonAdapter;
import net.deanly.solana.sdk.rpc.client.exception.RpcException;
import net.deanly.solana.sdk.rpc.request.config.*;
import net.deanly.solana.sdk.rpc.request.filter.ProgramAccountFilter;
import net.deanly.solana.sdk.rpc.request.filter.TokenAccountsByDelegateFilter;
import net.deanly.solana.sdk.rpc.request.filter.TokenAccountsByOwnerFilter;
import net.deanly.solana.sdk.rpc.response.*;
import net.deanly.solana.sdk.transaction.Transaction;
import net.deanly.solana.sdk.types.*;
import net.deanly.solana.sdk.types.codec.Base58;
import okhttp3.*;
import okio.Buffer;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static net.deanly.solana.sdk.rpc.client.MoshiTestUtil.assertJsonEqualsIgnoringId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MoshiHttpMethodApiImplTest {

    private MoshiHttpMethodApiImpl clientApi; // 테스트할 대상 클래스
    private OkHttpClient mockHttpClient; // 모킹한 OkHttpClient
    private ClientConfig mockConfig;

    @BeforeEach
    void setup() {
        // 기본 설정 생성
        mockConfig = ClientConfig.builder()
                .endpoint("https://api.devnet.solana.com")
                .readTimeoutMs(5000)
                .mediaType(MediaType.get("application/json"))
                .build();

        // Mock OkHttpClient
        mockHttpClient = mock(OkHttpClient.class);

        // HttpClient를 모킹하여 실제 네트워크 대신 사용
        clientApi = new MoshiHttpMethodApiImpl(mockConfig) {
            @Override
            public OkHttpClient createHttpClient() {
                return mockHttpClient;
            }
        };
    }

    private void assertNumericEquals(Number expected, Number actual) {
        assertEquals(expected.doubleValue(), actual.doubleValue(), 0.0001);
    }

    @Test
    void testGetHealth() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("{ \"jsonrpc\": \"2.0\", \"result\": \"ok\", \"id\": 1 }", MediaType.get("application/json"))) // 빈 본문이 제공됩니다.
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 요청 실행 (이 호출은 Mocked 응답을 반환)
        clientApi.getHealth();

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // URL과 HTTP 메서드 검증
        assertEquals("https://api.devnet.solana.com/", capturedRequest.url().toString());
        assertEquals("POST", capturedRequest.method());
    }

    @Test
    void testGetHealth_NodeIsUnhealthy() throws IOException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정: Node is unhealthy
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
          {
            "jsonrpc": "2.0",
            "error": {
              "code": -32005,
              "message": "Node is unhealthy",
              "data": {}
            },
            "id": 1
          }
        """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 요청 실행 및 예외 발생 확인
        RpcException exception = assertThrows(RpcException.class, () -> {
            clientApi.getHealth();
        });

        // 4. 예외 메시지 검증
        assertNotNull(exception);
        assertEquals(-32005, exception.getErrorCode());
        assertEquals("Node is unhealthy", exception.getMessage());
    }

    @Test
    void testGetHealth_NodeIsBehind() throws IOException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정: Node is behind by 42 slots
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
          {
            "jsonrpc": "2.0",
            "error": {
              "code": -32005,
              "message": "Node is behind by 42 slots",
              "data": {
                "numSlotsBehind": 42
              }
            },
            "id": 1
          }
        """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 요청 실행 및 예외 발생 확인
        RpcException exception = assertThrows(RpcException.class, () -> {
            clientApi.getHealth();
        });

        // 4. 예외 메시지 및 데이터 검증
        assertNotNull(exception);
        assertEquals(-32005, exception.getErrorCode());
        assertEquals("Node is behind by 42 slots", exception.getMessage());
        assertNotNull(exception.getErrorData());
        Map<String, Object> errorData = (Map<String, Object>) exception.getErrorData();
        assertEquals(42, errorData.get("numSlotsBehind"));
    }

    @Test
    void testGetAccountInfo() throws RpcException, IOException {
        // Mock Call 및 응답 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
                {
                    "jsonrpc": "2.0",
                    "result": {
                        "context": { "apiVersion": "2.0.15", "slot": 341197053 },
                        "value": {
                            "data": ["", "base58"],
                            "executable": false,
                            "lamports": 88849814690250,
                            "owner": "11111111111111111111111111111111",
                            "rentEpoch": 18446744073709551615,
                            "space": 0
                        }
                    },
                    "id": 1
                }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 테스트 데이터 준비
        PublicKey account = new PublicKey("vines1vzrYbzLMRdu58ou5XTby4qAqVRLmqo36NKPTg");

        // 메서드 호출
        RpcResultObject<ResValueAccountInfo> result = clientApi.getAccountInfo(account, AccountInfoConfig.builder().encoding(Encoding.BASE58).build());

        // 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출 및 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // Moshi를 사용하여 JSON 파싱
        Moshi moshi = new Moshi.Builder().build();
        Type mapType = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapType);

        // 요청 JSON 기대 값
        String expectedRequestJson = """
        {
          "jsonrpc": "2.0",
          "id": 1,
          "method": "getAccountInfo",
          "params": [
            "vines1vzrYbzLMRdu58ou5XTby4qAqVRLmqo36NKPTg",
            { "encoding": "base58" }
          ]
        }
    """;
        Map<String, Object> actualRequestMap = adapter.fromJson(actualRequestJson);
        Map<String, Object> expectedRequestMap = adapter.fromJson(expectedRequestJson);

        // 테스트에서 `id` 값을 제외하고 검증
        actualRequestMap.remove("id");
        expectedRequestMap.remove("id");
        assertEquals(expectedRequestMap, actualRequestMap);

        // 응답 데이터 검증
        assertNotNull(result);
        assertNotNull(result.getContext());
        assertNotNull(result.getValue());
        assertEquals(UnsignedLong.valueOf(88849814690250L), result.getValue().getLamports());
        assertEquals(new PublicKey("11111111111111111111111111111111"), result.getValue().getOwner());
        assertFalse(result.getValue().getExecutable());
        assertEquals(UnsignedLong.valueOf("18446744073709551615"), result.getValue().getRentEpoch());
        assertEquals(UnsignedLong.valueOf(0L), result.getValue().getSpace());
        assertEquals("", result.getValue().getData().getValue());
        assertEquals("base58", result.getValue().getData().getEncoding().getValue());
    }

    @Test
    void testGetBalance() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("{\n" +
                        "  \"jsonrpc\": \"2.0\",\n" +
                        "  \"result\": { \"context\": { \"slot\": 1 }, \"value\": 0 },\n" +
                        "  \"id\": 1\n" +
                        "}", MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 테스트 데이터 준비
        String account = "83astBRguLMdt2h5U1Tpdq5tjFoJ6noeGwaY3mDLVcri";

        // 4. 메서드 호출
        RpcResultObject<UnsignedLong> balance = clientApi.getBalance(PublicKey.valueOf(account), null);

        // 5. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 6. 요청 본문 추출 및 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 7. 기대하는 요청 JSON
        String expectedRequestJson = """
        {
          "jsonrpc": "2.0",
          "id": 1,
          "method": "getBalance",
          "params": [
            "83astBRguLMdt2h5U1Tpdq5tjFoJ6noeGwaY3mDLVcri"
          ]
        }
    """;

        // JSON 검증
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 8. 응답 데이터 검증
        assertNotNull(balance);
        assertNotNull(balance.getContext());
        assertNotNull(balance.getValue());
        assertEquals(UnsignedLong.valueOf(0), balance.getValue()); // 기대 값: 0
    }

    @Test
    void testGetBlock() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
            {
              "jsonrpc": "2.0",
              "result": {
                "blockHeight": 428,
                "blockTime": null,
                "blockhash": "3Eq21vXNB5s86c62bVuUfTeaMif1N2kUqRPBmGRJhyTA",
                "parentSlot": 429,
                "previousBlockhash": "mfcyqEXB3DnHXki6KjjmZck6YjmZLvpAByy2fj4nh6B",
                "transactions": [
                  {
                    "meta": {
                      "err": null,
                      "fee": 5000,
                      "innerInstructions": [],
                      "logMessages": [],
                      "postBalances": [499998932500, 26858640, 1, 1, 1],
                      "postTokenBalances": [],
                      "preBalances": [499998937500, 26858640, 1, 1, 1],
                      "preTokenBalances": [],
                      "rewards": null,
                      "status": {
                        "Ok": null
                      }
                    },
                    "transaction": {
                      "message": {
                        "accountKeys": [
                          "3UVYmECPPMZSCqWKfENfuoTv51fTDTWicX9xmBD2euKe",
                          "AjozzgE83A3x1sHNUR64hfH7zaEBWeMaFuAN9kQgujrc",
                          "SysvarS1otHashes111111111111111111111111111",
                          "SysvarC1ock11111111111111111111111111111111",
                          "Vote111111111111111111111111111111111111111"
                        ],
                        "header": {
                          "numReadonlySignedAccounts": 0,
                          "numReadonlyUnsignedAccounts": 3,
                          "numRequiredSignatures": 1
                        },
                        "instructions": [
                          {
                            "accounts": [1, 2, 3, 0],
                            "data": "37u9WtQpcm6ULa3WRQHmj49EPs4if7o9f1jSRVZpm2dvihR9C8jY4NqEwXUbLwx15HBSNcP1",
                            "programIdIndex": 4
                          }
                        ],
                        "recentBlockhash": "mfcyqEXB3DnHXki6KjjmZck6YjmZLvpAByy2fj4nh6B"
                      },
                      "signatures": [
                        "2nBhEBYYvfaAe16UMNqRHre4YNSskvuYgx3M6E4JP1oDYvZEJHvoPzyUidNgNX5r9sTyN1J9UxtbCXy2rqYcuyuv"
                      ]
                    }
                  }
                ]
              },
              "id": 1
            }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 테스트 데이터 준비
        UnsignedLong slot = UnsignedLong.valueOf(430);

        // 4. 메서드 호출
        ResValueBlock result = clientApi.getBlock(
                slot,
                BlockConfig.builder()
                        .encoding(Encoding.JSON)
                        .maxSupportedTransactionVersion(0)
                        .transactionDetails(TransactionDetails.FULL)
                        .rewards(false)
                        .build()
        );

        // 5. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 6. 요청 본문 추출 및 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 7. 기대하는 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getBlock",
      "params": [
        430,
        {
          "encoding": "json",
          "maxSupportedTransactionVersion": 0,
          "transactionDetails": "full",
          "rewards": false
        }
      ]
    }
    """;

        // JSON 검증
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 8. 응답 데이터 검증
        assertNotNull(result);
        assertNumericEquals(428, result.getBlockHeight());
        assertEquals("3Eq21vXNB5s86c62bVuUfTeaMif1N2kUqRPBmGRJhyTA", result.getBlockhash());
        assertNumericEquals(429, result.getParentSlot());
        assertEquals("mfcyqEXB3DnHXki6KjjmZck6YjmZLvpAByy2fj4nh6B", result.getPreviousBlockhash());

        assertNotNull(result.getTransactions());
        assertFalse(result.getTransactions().isEmpty());

        // 첫 번째 트랜잭션 검증
        ResValueConfirmedTransaction firstTransaction = result.getTransactions().get(0);
        assertNotNull(firstTransaction.getMeta());
        assertEquals(UnsignedLong.valueOf(5000), firstTransaction.getMeta().getFee());
        assertNull(firstTransaction.getMeta().getErr());
        assertNotNull(firstTransaction.getTransaction());
        assertNotNull(firstTransaction.getTransaction().getMessage());
        assertEquals(Blockhash.of("mfcyqEXB3DnHXki6KjjmZck6YjmZLvpAByy2fj4nh6B"), firstTransaction.getTransaction().getMessage().getRecentBlockhash());
    }

    @Test
    void testGetBlockCommitment() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
            {
              "jsonrpc": "2.0",
              "result": {
                "commitment": [
                  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                  0, 0, 0, 0, 0, 10, 32
                ],
                "totalStake": 42
              },
              "id": 1
            }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 테스트 데이터 준비
        UnsignedLong block = UnsignedLong.valueOf(5);

        // 4. 메서드 호출
        ResValueBlockCommitment result = clientApi.getBlockCommitment(block);

        // 5. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 6. 요청 본문 추출 및 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 7. 기대하는 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getBlockCommitment",
      "params": [
        5
      ]
    }
    """;

        // JSON 검증
        Moshi moshi = new Moshi.Builder().add(MoshiNumberJsonAdapter.FACTORY).build();
        Type mapType = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapType);

        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 8. 응답 데이터 검증
        assertNotNull(result);
        assertEquals(UnsignedLong.valueOf(42), result.getTotalStake());

        // Commitment 배열 검증
        List<UnsignedLong> expectedCommitments = List.of(
                UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0),
                UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(0), UnsignedLong.valueOf(10), UnsignedLong.valueOf(32)
        );
        assertNotNull(result.getCommitment());
        assertEquals(expectedCommitments, result.getCommitment());
    }

    @Test
    void testGetBlockHeight() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
            {
                "jsonrpc": "2.0",
                "result": 1233,
                "id": 1
            }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        UnsignedLong blockHeight = clientApi.getBlockHeight(null);

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 5. 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc":"2.0",
      "id":1,
      "method":"getBlockHeight"
    }
    """;

        // JSON 데이터 검증
        Moshi moshi = new Moshi.Builder().build();
        Type mapType = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapType);

        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 6. 응답 데이터 검증
        assertEquals(UnsignedLong.valueOf(1233), blockHeight); // 기대하는 blockHeight 값
    }

    @Test
    void testGetBlockProduction() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
            {
              "jsonrpc": "2.0",
              "result": {
                "context": {
                  "slot": 9887
                },
                "value": {
                  "byIdentity": {
                    "85iYT5RuzRTDgjyRa3cP8SYhM2j21fj7NhfJ3peu1DPr": [9888, 9886]
                  },
                  "range": {
                    "firstSlot": 0,
                    "lastSlot": 9887
                  }
                }
              },
              "id": 1
            }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. GetBlockProduction 호출
        RpcResultObject<ResValueBlockProduction> result = clientApi.getBlockProduction(null);

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대하는 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getBlockProduction"
    }
    """;

        // JSON 검증
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(result);
        assertNotNull(result.getContext());
        assertEquals(UnsignedLong.valueOf(9887L), result.getContext().getSlot());

        // Slot Range 검증
        assertNotNull(result.getValue().getRange());
        assertEquals(UnsignedLong.valueOf(0), result.getValue().getRange().getFirstSlot());
        assertEquals(UnsignedLong.valueOf(9887), result.getValue().getRange().getLastSlot());

        // byIdentity 검증
        PublicKey expectedKey = PublicKey.valueOf("85iYT5RuzRTDgjyRa3cP8SYhM2j21fj7NhfJ3peu1DPr");
        assertNotNull(result.getValue().getByIdentity());
        assertTrue(result.getValue().getByIdentity().containsKey(expectedKey));

        ValidatorIdentityInfo identitySlots = result.getValue().getByIdentity().get(expectedKey);

        assertNotNull(identitySlots);
        assertEquals(9888, identitySlots.getNumberOfLeaderSlots());
        assertEquals(9886, identitySlots.getNumberOfBlocksProduced());
    }

    @Test
    void testGetBlocks() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
            {
              "jsonrpc": "2.0",
              "result": [5, 6, 7, 8, 9, 10],
              "id": 1
            }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        UnsignedLong startSlot = UnsignedLong.valueOf(5);
        UnsignedLong endSlot = UnsignedLong.valueOf(10);
        List<UnsignedLong> result = clientApi.getBlocks(startSlot, endSlot, null);

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대하는 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getBlocks",
      "params": [5, 10]
    }
    """;

        // JSON 검증
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(result);
        assertEquals(6, result.size());
        assertEquals(UnsignedLong.valueOf(5), result.get(0));
        assertEquals(UnsignedLong.valueOf(10), result.get(result.size() - 1));
    }

    @Test
    void testGetBlocksWithLimit() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
            {
              "jsonrpc": "2.0",
              "result": [5, 6, 7],
              "id": 1
            }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        UnsignedLong startSlot = UnsignedLong.valueOf(5);
        UnsignedLong limit = UnsignedLong.valueOf(3);
        List<UnsignedLong> result = clientApi.getBlocksWithLimit(startSlot, limit, null);

        // 4. 요청 데이터 캡처 및 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getBlocksWithLimit",
      "params": [5, 3]
    }
    """;

        // JSON 검증
        Moshi moshi = new Moshi.Builder().build();
        Type mapType = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapType);

        Map<String, Object> actualRequestMap = adapter.fromJson(actualRequestJson);
        Map<String, Object> expectedRequestMap = adapter.fromJson(expectedRequestJson);

        // ID를 제외하고 비교
        actualRequestMap.remove("id");
        expectedRequestMap.remove("id");
        assertEquals(expectedRequestMap, actualRequestMap);

        // 5. 응답 데이터 검증
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(UnsignedLong.valueOf(5), result.get(0));
        assertEquals(UnsignedLong.valueOf(7), result.get(result.size() - 1));
    }

    @Test
    void testGetBlockTime() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
            {
                "jsonrpc": "2.0",
                "result": 1574721591,
                "id": 1
            }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 호출할 슬롯 번호
        UnsignedLong slot = UnsignedLong.valueOf(5);

        // 4. 메서드 호출
        Long blockTime = clientApi.getBlockTime(slot);

        // 5. 요청 데이터 캡처 및 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
        "jsonrpc": "2.0",
        "id": 1,
        "method": "getBlockTime",
        "params": [5]
    }
    """;

        // JSON 검증
        Moshi moshi = new Moshi.Builder().build();
        Type mapType = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapType);

        Map<String, Object> actualRequestMap = adapter.fromJson(actualRequestJson);
        Map<String, Object> expectedRequestMap = adapter.fromJson(expectedRequestJson);

        // ID를 제외하고 비교
        actualRequestMap.remove("id");
        expectedRequestMap.remove("id");
        assertEquals(expectedRequestMap, actualRequestMap);

        // 6. 응답 데이터 검증
        assertNotNull(blockTime);
        assertEquals(1574721591L, blockTime); // 기대 값
    }

    @Test
    void testGetClusterNodes() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
            {
              "jsonrpc": "2.0",
              "result": [
                {
                  "gossip": "10.239.6.48:8001",
                  "pubkey": "9QzsJf7LPLj8GkXbYT3LFDKqsj2hHG7TA3xinJHu8epQ",
                  "rpc": "10.239.6.48:8899",
                  "tpu": "10.239.6.48:8856",
                  "version": "1.0.0 c375ce1f"
                }
              ],
              "id": 1
            }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        List<ResValueClusterNode> clusterNodes = clientApi.getClusterNodes();

        // 4. 요청 데이터 캡처 및 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getClusterNodes"
    }
    """;

        // JSON 검증
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(clusterNodes);
        assertEquals(1, clusterNodes.size()); // 응답에서 하나의 클러스터 노드가 반환되어야 함

        ResValueClusterNode node = clusterNodes.get(0);
        assertEquals("10.239.6.48:8001", node.getGossip());
        assertEquals(PublicKey.valueOf("9QzsJf7LPLj8GkXbYT3LFDKqsj2hHG7TA3xinJHu8epQ"), node.getPubkey());
        assertEquals("10.239.6.48:8899", node.getRpc());
        assertEquals("10.239.6.48:8856", node.getTpu());
        assertEquals("1.0.0 c375ce1f", node.getVersion());
    }

    @Test
    void testGetEpochInfo() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
            {
              "jsonrpc": "2.0",
              "result": {
                "absoluteSlot": 166598,
                "blockHeight": 166500,
                "epoch": 27,
                "slotIndex": 2790,
                "slotsInEpoch": 8192,
                "transactionCount": 22661093
              },
              "id": 1
            }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        ResValueEpochInfo epochInfo = clientApi.getEpochInfo(null);

        // 4. 요청 데이터 캡처 및 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getEpochInfo"
    }
    """;

        // JSON 검증
        Moshi moshi = new Moshi.Builder().build();
        Type mapType = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapType);

        Map<String, Object> actualRequestMap = adapter.fromJson(actualRequestJson);
        Map<String, Object> expectedRequestMap = adapter.fromJson(expectedRequestJson);

        // ID를 제외하고 비교
        actualRequestMap.remove("id");
        expectedRequestMap.remove("id");
        assertEquals(expectedRequestMap, actualRequestMap);

        // 5. 응답 데이터 검증
        assertNotNull(epochInfo);
        assertEquals(UnsignedLong.valueOf(166598), epochInfo.getAbsoluteSlot());
        assertEquals(UnsignedLong.valueOf(166500), epochInfo.getBlockHeight());
        assertEquals(UnsignedLong.valueOf(27), epochInfo.getEpoch());
        assertEquals(UnsignedLong.valueOf(2790), epochInfo.getSlotIndex());
        assertEquals(UnsignedLong.valueOf(8192), epochInfo.getSlotsInEpoch());
        assertEquals(UnsignedLong.valueOf(22661093), epochInfo.getTransactionCount());
    }

    @Test
    void testGetEpochSchedule() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
            {
              "jsonrpc": "2.0",
              "result": {
                "firstNormalEpoch": 8,
                "firstNormalSlot": 8160,
                "leaderScheduleSlotOffset": 8192,
                "slotsPerEpoch": 8192,
                "warmup": true
              },
              "id": 1
            }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        ResValueEpochSchedule epochSchedule = clientApi.getEpochSchedule();

        // 4. 요청 데이터 캡처 및 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getEpochSchedule"
    }
    """;

        // JSON 검증
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(epochSchedule);
        assertEquals(UnsignedLong.valueOf(8), epochSchedule.getFirstNormalEpoch());
        assertEquals(UnsignedLong.valueOf(8160), epochSchedule.getFirstNormalSlot());
        assertEquals(UnsignedLong.valueOf(8192), epochSchedule.getLeaderScheduleSlotOffset());
        assertEquals(UnsignedLong.valueOf(8192), epochSchedule.getSlotsPerEpoch());
        assertTrue(epochSchedule.isWarmup());
    }

    @Test
    void testGetFeeForMessage() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
            {
              "jsonrpc": "2.0",
              "result": { 
                "context": { "slot": 5068 }, 
                "value": 5000 
              },
              "id": 1
            }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. Test 데이터 준비
        String messageBase64 = "AQABAgIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEBAQAA";
        Commitment commitment = Commitment.PROCESSED;


        // 4. 메서드 호출
        RpcResultObject<UnsignedLong> feeResult = clientApi.getFeeForMessage(
                messageBase64,
                FeeForMessageConfig.builder().commitment(Commitment.PROCESSED).build());

        // 5. 요청 데이터 캡처 및 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "id": 1,
      "jsonrpc": "2.0",
      "method": "getFeeForMessage",
      "params": [
        "AQABAgIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEBAQAA",
        { "commitment": "processed" }
      ]
    }
    """;

        // JSON 검증
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 6. 응답 데이터 검증
        assertNotNull(feeResult);
        assertEquals(UnsignedLong.valueOf(5068L), feeResult.getContext().getSlot()); // 응답 context.slot 값 검증
        assertEquals(UnsignedLong.valueOf(5000), feeResult.getValue()); // 응답에서 반환된 value 값 검증
    }

    @Test
    void testGetFirstAvailableBlock() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
            {
              "jsonrpc": "2.0",
              "result": 250000,
              "id": 1
            }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        UnsignedLong firstAvailableBlock = clientApi.getFirstAvailableBlock();

        // 4. 요청 데이터 캡처 및 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getFirstAvailableBlock"
    }
    """;

        // JSON 검증
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(firstAvailableBlock);
        assertEquals(UnsignedLong.valueOf(250000L), firstAvailableBlock); // UnsignedLong 검증
    }

    @Test
    void testGetGenesisHash() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
            {
              "jsonrpc": "2.0",
              "result": "GH7ome3EiwEr7tu9JuTh2dpYWBJK3z69Xm1ZE3MEE6JC",
              "id": 1
            }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        GenesisHash genesisHash = clientApi.getGenesisHash();

        // 4. 요청 데이터 캡처 및 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc":"2.0",
      "id":1,
      "method":"getGenesisHash"
    }
    """;

        // JSON 검증
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(genesisHash);
        assertEquals(GenesisHash.of("GH7ome3EiwEr7tu9JuTh2dpYWBJK3z69Xm1ZE3MEE6JC"), genesisHash);
    }

    @Test
    void testGetHealth_Success() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (성공 케이스)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": "ok",
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        Boolean result = clientApi.getHealthCheck();

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getHealth"
    }
    """;

        // 5. 응답 데이터 검증
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    void testGetHighestSnapshotSlot_Success() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (성공 케이스)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "full": 100,
                  "incremental": 110
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        ResValueSnapshotSlot result = clientApi.getHighestSnapshotSlot();

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getHighestSnapshotSlot"
    }
    """;

        // 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(result);
        assertEquals(UnsignedLong.valueOf(100), result.getFullSnapshotSlot());
        assertEquals(UnsignedLong.valueOf(110), result.getIncrementalSnapshotSlot());
    }

    @Test
    void testGetHighestSnapshotSlot_NoSnapshotError() throws IOException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (에러 케이스: No snapshot)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "error": {
                  "code": -32008,
                  "message": "No snapshot"
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 요청 실행 및 예외 발생 확인
        RpcException exception = assertThrows(RpcException.class, () -> clientApi.getHighestSnapshotSlot());

        // 4. 예외 메시지 검증
        assertNotNull(exception);
        assertEquals(-32008, exception.getErrorCode());
        assertEquals("No snapshot", exception.getMessage());
    }

    @Test
    void testGetIdentity_Success() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (성공 케이스)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "identity": "2r1F4iWqVcb8M1DbAjQuFpebkQHY9hcVU4WuW2DJBppN"
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        ResValueIdentity result = clientApi.getIdentity();

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getIdentity"
    }
    """;

        // 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(result);
        assertEquals(PublicKey.valueOf("2r1F4iWqVcb8M1DbAjQuFpebkQHY9hcVU4WuW2DJBppN"), result.getIdentity());
    }

    @Test
    void testGetInflationGovernor_Success() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (성공 케이스)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "foundation": 0.05,
                  "foundationTerm": 7,
                  "initial": 0.15,
                  "taper": 0.15,
                  "terminal": 0.015
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        ResValueInflationGovernor result = clientApi.getInflationGovernor(null);

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getInflationGovernor"
    }
    """;

        // 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(result);
        assertEquals(0.05, result.getFoundation());
        assertEquals(7, result.getFoundationTerm());
        assertEquals(0.15, result.getInitial());
        assertEquals(0.15, result.getTaper());
        assertEquals(0.015, result.getTerminal());
    }

    @Test
    void testGetInflationRate_Success() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (성공 케이스)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "epoch": 100,
                  "foundation": 0.001,
                  "total": 0.149,
                  "validator": 0.148
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        ResValueInflationRate result = clientApi.getInflationRate();

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getInflationRate"
    }
    """;

        // 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(result);
        assertEquals(UnsignedLong.valueOf(100), result.getEpoch());
        assertEquals(0.001, (double) result.getFoundation(), 0.00001);
        assertEquals(0.149, (double) result.getTotal(), 0.00001);
        assertEquals(0.148, (double) result.getValidator(), 0.00001);
    }

    @Test
    void testGetInflationReward_Success() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (성공 케이스)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": [
                  {
                    "amount": 2500,
                    "effectiveSlot": 224,
                    "epoch": 2,
                    "postBalance": 499999442500
                  },
                  null
                ],
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        List<ResValueInflationReward> result = clientApi.getInflationReward(
                List.of(
                        PublicKey.valueOf("6dmNQ5jwLeLk5REvio1JcMshcbvkYMwy26sJ8pbkvStu"),
                        PublicKey.valueOf("BGsqMegLpV6n6Ve146sSX2dTjUMj3M92HnU8BbNRMhF2")
                ),
                InflationRewardConfig.builder().epoch(UnsignedLong.valueOf(2)).build()
        );

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getInflationReward",
      "params": [
        [
          "6dmNQ5jwLeLk5REvio1JcMshcbvkYMwy26sJ8pbkvStu",
          "BGsqMegLpV6n6Ve146sSX2dTjUMj3M92HnU8BbNRMhF2"
        ],
        {"epoch": 2}
      ]
    }
    """;

        // 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(result);
        assertEquals(2, result.size());

        // 첫 번째 결과 검증
        ResValueInflationReward firstReward = result.get(0);
        assertNotNull(firstReward);
        assertEquals(UnsignedLong.valueOf(2500), firstReward.getAmount());
        assertEquals(UnsignedLong.valueOf(224), firstReward.getEffectiveSlot());
        assertEquals(UnsignedLong.valueOf(2), firstReward.getEpoch());
        assertEquals(UnsignedLong.valueOf(499999442500L), firstReward.getPostBalance());

        // 두 번째 결과 검증 (null 확인)
        ResValueInflationReward secondReward = result.get(1);
        assertNull(secondReward);
    }

    @Test
    void testGetLargestAccounts_Success() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (성공 케이스)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "context": {
                    "slot": 54
                  },
                  "value": [
                    {
                      "lamports": 999974,
                      "address": "99P8ZgtJYe1buSK8JXkvpLh8xPsCFuLYhz9hQFNw93WJ"
                    },
                    {
                      "lamports": 42,
                      "address": "uPwWLo16MVehpyWqsLkK3Ka8nLowWvAHbBChqv2FZeL"
                    }
                  ]
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        RpcResultObject<List<ResValueLargestAccount>> result = clientApi.getLargestAccounts(null);

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getLargestAccounts"
    }
    """;

        // 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(result);
        assertNotNull(result.getContext());
        assertNotNull(result.getValue());

        RpcResultObject.Context context =  result.getContext();
        List<ResValueLargestAccount> valueList =  result.getValue();

        // 컨텍스트 검증
        assertEquals(UnsignedLong.valueOf(54L), context.getSlot());

        // 첫 번째 항목 검증
        assertEquals(2, valueList.size());
        ResValueLargestAccount firstAccount = valueList.get(0);
        assertEquals(UnsignedLong.valueOf(999974), firstAccount.getLamports());
        assertEquals(PublicKey.valueOf("99P8ZgtJYe1buSK8JXkvpLh8xPsCFuLYhz9hQFNw93WJ"), firstAccount.getAddress());

        // 두 번째 항목 검증
        ResValueLargestAccount secondAccount = valueList.get(1);
        assertEquals(UnsignedLong.valueOf(42), secondAccount.getLamports());
        assertEquals(PublicKey.valueOf("uPwWLo16MVehpyWqsLkK3Ka8nLowWvAHbBChqv2FZeL"), secondAccount.getAddress());
    }

    @Test
    void testGetLatestBlockhash_Success() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (성공 케이스)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "context": {
                    "slot": 2792
                  },
                  "value": {
                    "blockhash": "EkSnNWid2cvwEVnVx9aBqawnmiCNiDgp3gUdkDPTKN1N",
                    "lastValidBlockHeight": 3090
                  }
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        RpcResultObject<ResValueLatestBlockhash> result = clientApi.getLatestBlockhash(
                LatestBlockhashConfig.builder().commitment(Commitment.PROCESSED).build()
        );

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "id": 1,
      "jsonrpc": "2.0",
      "method": "getLatestBlockhash",
      "params": [
        {
          "commitment": "processed"
        }
      ]
    }
    """;

        // 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(result);
        assertNotNull(result.getContext());
        assertNotNull(result.getValue());

        // Context 데이터 검증
        RpcResultObject.Context context = result.getContext();
        assertEquals(UnsignedLong.valueOf(2792), context.getSlot());

        // Value 데이터 검증
        ResValueLatestBlockhash value = result.getValue();
        assertEquals(Blockhash.of("EkSnNWid2cvwEVnVx9aBqawnmiCNiDgp3gUdkDPTKN1N"), value.getBlockhash());
        assertEquals(UnsignedLong.valueOf(3090), value.getLastValidBlockHeight());
    }

    @Test
    void testGetLeaderSchedule_Success() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (성공 케이스)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "4Qkev8aNZcqFNSRhQzwyLMFSsi94jHqE8WNVTJzTP99F": [
                    0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                    21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38,
                    39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56,
                    57, 58, 59, 60, 61, 62, 63
                  ]
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        Map<PublicKey, List<Integer>> result = clientApi.getLeaderSchedule(
                null,
                LeaderScheduleConfig.builder()
                        .identity(PublicKey.valueOf("4Qkev8aNZcqFNSRhQzwyLMFSsi94jHqE8WNVTJzTP99F"))
                        .build()
        );

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getLeaderSchedule",
      "params": [
        null,
        {
          "identity": "4Qkev8aNZcqFNSRhQzwyLMFSsi94jHqE8WNVTJzTP99F"
        }
      ]
    }
    """;

        // 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(result);
        assertTrue(result.containsKey(PublicKey.valueOf("4Qkev8aNZcqFNSRhQzwyLMFSsi94jHqE8WNVTJzTP99F")));

        List<Integer> slots = result.get(PublicKey.valueOf("4Qkev8aNZcqFNSRhQzwyLMFSsi94jHqE8WNVTJzTP99F"));
        assertEquals(64, slots.size());
        for (int i = 0; i < 64; i++) {
            assertEquals(i, slots.get(i));
        }
    }

    @Test
    void testGetMaxRetransmitSlot_Success() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (성공 케이스)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": 1234,
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        UnsignedLong maxRetransmitSlot = clientApi.getMaxRetransmitSlot();

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getMaxRetransmitSlot"
    }
    """;

        // 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertEquals(UnsignedLong.valueOf(1234), maxRetransmitSlot);
    }

    @Test
    void testGetMaxShredInsertSlot_Success() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (성공 케이스)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": 1234,
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        UnsignedLong maxShredInsertSlot = clientApi.getMaxShredInsertSlot();

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getMaxShredInsertSlot"
    }
    """;

        // 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertEquals(UnsignedLong.valueOf(1234), maxShredInsertSlot);
    }

    @Test
    void testGetMinimumBalanceForRentExemption_Success() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (성공 케이스)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": 500,
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        UnsignedLong minimumBalance = clientApi.getMinimumBalanceForRentExemption(50, null);

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getMinimumBalanceForRentExemption",
      "params": [50]
    }
    """;

        // 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertEquals(UnsignedLong.valueOf(500), minimumBalance);
    }

    @Test
    void testGetMultipleAccounts_Success() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (성공 케이스)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "context": { "apiVersion": "2.0.15", "slot": 341197247 },
                  "value": [
                    {
                      "data": ["", "base58"],
                      "executable": false,
                      "lamports": 88849814690250,
                      "owner": "11111111111111111111111111111111",
                      "rentEpoch": 18446744073709551615,
                      "space": 0
                    },
                    {
                      "data": ["", "base58"],
                      "executable": false,
                      "lamports": 998763433,
                      "owner": "2WRuhE4GJFoE23DYzp2ij6ZnuQ8p9mJeU6gDgfsjR4or",
                      "rentEpoch": 18446744073709551615,
                      "space": 0
                    }
                  ]
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        RpcResultObject<List<ResValueAccountInfo>> result = clientApi.getMultipleAccounts(
                List.of(
                        PublicKey.valueOf("vines1vzrYbzLMRdu58ou5XTby4qAqVRLmqo36NKPTg"),
                        PublicKey.valueOf("4fYNw3dojWmQ4dXtSGE9epjRGy9pFSx62YypT7avPYvA")
                ),
                MultipleAccountsConfig.builder()
                        .encoding(Encoding.BASE58)
                        .build()
        );

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getMultipleAccounts",
      "params": [
        [
          "vines1vzrYbzLMRdu58ou5XTby4qAqVRLmqo36NKPTg",
          "4fYNw3dojWmQ4dXtSGE9epjRGy9pFSx62YypT7avPYvA"
        ],
        {
          "encoding": "base58"
        }
      ]
    }
    """;

        // 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(result);
        assertNotNull(result.getContext());
        assertNotNull(result.getValue());
        assertEquals(2, result.getValue().size());

        // 첫 번째 계정 검증
        ResValueAccountInfo firstAccount = result.getValue().get(0);
        assertEquals("", firstAccount.getData().getValue());
        assertEquals(Encoding.BASE58, firstAccount.getData().getEncoding());
        assertFalse(firstAccount.getExecutable());
        assertEquals(UnsignedLong.valueOf(88849814690250L), firstAccount.getLamports());
        assertEquals(PublicKey.valueOf("11111111111111111111111111111111"), firstAccount.getOwner());
        assertEquals(UnsignedLong.valueOf("18446744073709551615"), firstAccount.getRentEpoch());
        assertEquals(UnsignedLong.valueOf(0), firstAccount.getSpace());

        // 두 번째 계정 검증
        ResValueAccountInfo secondAccount = result.getValue().get(1);
        assertEquals("", secondAccount.getData().getValue());
        assertEquals(Encoding.BASE58, secondAccount.getData().getEncoding());
        assertFalse(secondAccount.getExecutable());
        assertEquals(UnsignedLong.valueOf(998763433), secondAccount.getLamports());
        assertEquals(PublicKey.valueOf("2WRuhE4GJFoE23DYzp2ij6ZnuQ8p9mJeU6gDgfsjR4or"), secondAccount.getOwner());
        assertEquals(UnsignedLong.valueOf("18446744073709551615"), secondAccount.getRentEpoch());
        assertEquals(UnsignedLong.valueOf(0), secondAccount.getSpace());
    }

    @Test
    void testGetProgramAccounts_Success() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (성공 케이스)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": [
                  {
                    "account": {
                      "data": "2R9jLfiAQ9bgdcw6h8s44439",
                      "executable": false,
                      "lamports": 15298080,
                      "owner": "4Nd1mBQtrMJVYVfKf2PJy9NZUZdTAsp7D4xWLs4gDB4T",
                      "rentEpoch": 28,
                      "space": 42
                    },
                    "pubkey": "CxELquR1gPP8wHe33gZ4QxqGB3sZ9RSwsJ2KshVewkFY"
                  }
                ],
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        List<ResValueProgram> result = clientApi.getProgramAccounts(
                PublicKey.valueOf("4Nd1mBQtrMJVYVfKf2PJy9NZUZdTAsp7D4xWLs4gDB4T"),
                ProgramAccountsConfig.builder()
                        .filters(
                                List.of(
                                        ProgramAccountFilter.builder()
                                                .dataSize(UnsignedLong.valueOf(17))
                                                .memcmp(
                                                        ProgramAccountFilter.Memcmp.builder()
                                                                .offset(4)
                                                                .bytes("3Mc6vR")
                                                                .build()
                                                )
                                                .build()
                                )
                        )
                        .build()
        );

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getProgramAccounts",
      "params": [
        "4Nd1mBQtrMJVYVfKf2PJy9NZUZdTAsp7D4xWLs4gDB4T",
        {
          "filters": [
            {
              "dataSize": 17
            },
            {
              "memcmp": {
                "offset": 4,
                "bytes": "3Mc6vR"
              }
            }
          ]
        }
      ]
    }
    """;

        // 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(result);
        assertEquals(1, result.size());

        // 첫 번째 응답 데이터 검증
        ResValueProgram programAccount = result.get(0);
        assertEquals("2R9jLfiAQ9bgdcw6h8s44439", programAccount.getAccount().getData().getValue());
        assertFalse(programAccount.getAccount().getExecutable());
        assertEquals(UnsignedLong.valueOf(15298080), programAccount.getAccount().getLamports());
        assertEquals(PublicKey.valueOf("4Nd1mBQtrMJVYVfKf2PJy9NZUZdTAsp7D4xWLs4gDB4T"), programAccount.getAccount().getOwner());
        assertEquals(UnsignedLong.valueOf(28), programAccount.getAccount().getRentEpoch());
        assertEquals(UnsignedLong.valueOf(42), programAccount.getAccount().getSpace());
        assertEquals(PublicKey.valueOf("CxELquR1gPP8wHe33gZ4QxqGB3sZ9RSwsJ2KshVewkFY"), programAccount.getPubkey());
    }

    @Test
    void testGetRecentPerformanceSamples_Success() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (성공 케이스)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": [
                  {
                    "numSlots": 126,
                    "numTransactions": 126,
                    "numNonVoteTransactions": 1,
                    "samplePeriodSecs": 60,
                    "slot": 348125
                  },
                  {
                    "numSlots": 126,
                    "numTransactions": 126,
                    "numNonVoteTransactions": 1,
                    "samplePeriodSecs": 60,
                    "slot": 347999
                  },
                  {
                    "numSlots": 125,
                    "numTransactions": 125,
                    "numNonVoteTransactions": 0,
                    "samplePeriodSecs": 60,
                    "slot": 347873
                  },
                  {
                    "numSlots": 125,
                    "numTransactions": 125,
                    "numNonVoteTransactions": 0,
                    "samplePeriodSecs": 60,
                    "slot": 347748
                  }
                ],
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        List<ResValuePerformanceSample> result = clientApi.getRecentPerformanceSamples(4);

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getRecentPerformanceSamples",
      "params": [4]
    }
    """;

        // 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(result);
        assertEquals(4, result.size());

        // 첫 번째 성능 샘플 검증
        ResValuePerformanceSample sample1 = result.get(0);
        assertEquals(UnsignedLong.valueOf(126), sample1.getNumSlots());
        assertEquals(UnsignedLong.valueOf(126), sample1.getNumTransactions());
        assertEquals(UnsignedLong.valueOf(1), sample1.getNumNonVoteTransactions());
        assertEquals(60, sample1.getSamplePeriodSecs());
        assertEquals(UnsignedLong.valueOf(348125), sample1.getSlot());

        // 두 번째 성능 샘플 검증
        ResValuePerformanceSample sample2 = result.get(1);
        assertEquals(UnsignedLong.valueOf(126), sample2.getNumSlots());
        assertEquals(UnsignedLong.valueOf(126), sample2.getNumTransactions());
        assertEquals(UnsignedLong.valueOf(1), sample2.getNumNonVoteTransactions());
        assertEquals(60, sample2.getSamplePeriodSecs());
        assertEquals(UnsignedLong.valueOf(347999), sample2.getSlot());
    }

    @Test
    void testGetRecentPrioritizationFees_Success() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (성공 케이스)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": [
                  {
                    "slot": 348125,
                    "prioritizationFee": 0
                  },
                  {
                    "slot": 348126,
                    "prioritizationFee": 1000
                  },
                  {
                    "slot": 348127,
                    "prioritizationFee": 500
                  },
                  {
                    "slot": 348128,
                    "prioritizationFee": 0
                  },
                  {
                    "slot": 348129,
                    "prioritizationFee": 1234
                  }
                ],
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        List<ResValuePrioritizationFee> result = clientApi.getRecentPrioritizationFees(
                List.of(PublicKey.valueOf("CxELquR1gPP8wHe33gZ4QxqGB3sZ9RSwsJ2KshVewkFY"))
        );

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getRecentPrioritizationFees",
      "params": [
        ["CxELquR1gPP8wHe33gZ4QxqGB3sZ9RSwsJ2KshVewkFY"]
      ]
    }
    """;

        // 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(result);
        assertEquals(5, result.size());

        // 첫 번째 항목 검증
        ResValuePrioritizationFee entry1 = result.get(0);
        assertEquals(UnsignedLong.valueOf(348125), entry1.getSlot());
        assertEquals(UnsignedLong.valueOf(0), entry1.getPrioritizationFee());

        // 두 번째 항목 검증
        ResValuePrioritizationFee entry2 = result.get(1);
        assertEquals(UnsignedLong.valueOf(348126), entry2.getSlot());
        assertEquals(UnsignedLong.valueOf(1000), entry2.getPrioritizationFee());

        // 세 번째 항목 검증
        ResValuePrioritizationFee entry3 = result.get(2);
        assertEquals(UnsignedLong.valueOf(348127), entry3.getSlot());
        assertEquals(UnsignedLong.valueOf(500), entry3.getPrioritizationFee());

        // 네 번째 항목 검증
        ResValuePrioritizationFee entry4 = result.get(3);
        assertEquals(UnsignedLong.valueOf(348128), entry4.getSlot());
        assertEquals(UnsignedLong.valueOf(0), entry4.getPrioritizationFee());

        // 다섯 번째 항목 검증
        ResValuePrioritizationFee entry5 = result.get(4);
        assertEquals(UnsignedLong.valueOf(348129), entry5.getSlot());
        assertEquals(UnsignedLong.valueOf(1234), entry5.getPrioritizationFee());
    }

    @Test
    void testGetSignaturesForAddress_Success() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (성공 케이스)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": [
                  {
                    "err": null,
                    "memo": null,
                    "signature": "5h6xBEauJ3PK6SWCZ1PGjBvj8vDdWG3KpwATGy1ARAXFSDwt8GFXM7W5Ncn16wmqokgpiKRLuS83KUxyZyv2sUYv",
                    "slot": 114,
                    "blockTime": null
                  }
                ],
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        List<ResValueTransactionSignature> result = clientApi.getSignaturesForAddress(
                PublicKey.valueOf("Vote111111111111111111111111111111111111111"),
                SignaturesForAddressConfig.builder()
                        .limit(1)
                        .build()
        );

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // JSON 요청 데이터 검증
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getSignaturesForAddress",
      "params": [
        "Vote111111111111111111111111111111111111111",
        {
          "limit": 1
        }
      ]
    }
    """;

        // 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(result);
        assertEquals(1, result.size());

        // 첫 번째 응답 항목 검증
        ResValueTransactionSignature signature = result.get(0);
        assertNull(signature.getErr());
        assertNull(signature.getMemo());
        assertEquals(Signature.of("5h6xBEauJ3PK6SWCZ1PGjBvj8vDdWG3KpwATGy1ARAXFSDwt8GFXM7W5Ncn16wmqokgpiKRLuS83KUxyZyv2sUYv"), signature.getSignature());
        assertEquals(UnsignedLong.valueOf(114), signature.getSlot());
        assertNull(signature.getBlockTime());
    }

    @Test
    void testGetSignatureStatuses_Success() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정 (성공 케이스)
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(mockConfig.getEndpoint())
                        .build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "context": {
                    "slot": 82
                  },
                  "value": [
                    {
                      "slot": 48,
                      "confirmations": null,
                      "err": null,
                      "status": {
                        "Ok": null
                      },
                      "confirmationStatus": "finalized"
                    },
                    null
                  ]
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        RpcResultObject<List<ResValueSignatureStatus>> result = clientApi.getSignatureStatuses(
                List.of(Signature.of("5VERv8NMvzbJMEkV8xnrLkEaWRtSz9CosKDYjCJjBRnbJLgp8uirBgmQpjKhoR4tjF3ZpRzrFmBV6UjKdiSZkQUW")),
                SignatureStatusesConfig.builder()
                        .searchTransactionHistory(true)
                        .build()
        );

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // JSON 요청 데이터 검증
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getSignatureStatuses",
      "params": [
        [
          "5VERv8NMvzbJMEkV8xnrLkEaWRtSz9CosKDYjCJjBRnbJLgp8uirBgmQpjKhoR4tjF3ZpRzrFmBV6UjKdiSZkQUW"
        ],
        {
          "searchTransactionHistory": true
        }
      ]
    }
    """;

        // 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(result);
        assertNotNull(result.getContext());
        assertEquals(UnsignedLong.valueOf(82), result.getContext().getSlot());

        // 응답 값 검증
        List<ResValueSignatureStatus> statuses = result.getValue();
        assertNotNull(statuses);
        assertEquals(2, statuses.size());

        // 첫 번째 상태 확인
        ResValueSignatureStatus status1 = statuses.get(0);
        assertNotNull(status1);
        assertEquals(UnsignedLong.valueOf(48), status1.getSlot());
        assertNull(status1.getConfirmations());
        assertNull(status1.getErr());
        assertEquals(Commitment.FINALIZED, status1.getConfirmationStatus());

        // 두 번째 상태 확인 (null)
        assertNull(statuses.get(1));
    }

    @Test
    void testGetSlot() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": 1234,
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        UnsignedLong slot = clientApi.getSlot(null);

        // 4. 요청 데이터 캡처
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 검증
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getSlot"
    }
    """;

        // JSON 검증
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertEquals(UnsignedLong.valueOf(1234), slot);
    }

    @Test
    void testGetSlotLeader() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": "ENvAW7JScgYq6o4zKZwewtkzzJgDzuJAFxYasvmEQdpS",
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        PublicKey slotLeader = clientApi.getSlotLeader(null);

        // 4. 요청 데이터 캡처 및 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getSlotLeader"
    }
    """;

        // JSON 비교
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertEquals(PublicKey.valueOf("ENvAW7JScgYq6o4zKZwewtkzzJgDzuJAFxYasvmEQdpS"), slotLeader);
    }

    @Test
    void testGetSlotLeaders() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": [
                  "ChorusmmK7i1AxXeiTtQgQZhQNiXYU84ULeaYF1EH15n",
                  "ChorusmmK7i1AxXeiTtQgQZhQNiXYU84ULeaYF1EH15n",
                  "ChorusmmK7i1AxXeiTtQgQZhQNiXYU84ULeaYF1EH15n",
                  "ChorusmmK7i1AxXeiTtQgQZhQNiXYU84ULeaYF1EH15n",
                  "Awes4Tr6TX8JDzEhCZY2QVNimT6iD1zWHzf1vNyGvpLM",
                  "Awes4Tr6TX8JDzEhCZY2QVNimT6iD1zWHzf1vNyGvpLM",
                  "Awes4Tr6TX8JDzEhCZY2QVNimT6iD1zWHzf1vNyGvpLM",
                  "Awes4Tr6TX8JDzEhCZY2QVNimT6iD1zWHzf1vNyGvpLM",
                  "DWvDTSh3qfn88UoQTEKRV2JnLt5jtJAVoiCo3ivtMwXP",
                  "DWvDTSh3qfn88UoQTEKRV2JnLt5jtJAVoiCo3ivtMwXP"
                ],
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 파라미터 설정 및 메서드 호출
        UnsignedLong startSlot = UnsignedLong.valueOf(100);
        UnsignedLong limit = UnsignedLong.valueOf(10);
        List<PublicKey> slotLeaders = clientApi.getSlotLeaders(startSlot, limit);

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getSlotLeaders",
      "params": [100, 10]
    }
    """;

        // JSON 검증
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(slotLeaders);
        assertEquals(10, slotLeaders.size());
        assertEquals(PublicKey.valueOf("ChorusmmK7i1AxXeiTtQgQZhQNiXYU84ULeaYF1EH15n"), slotLeaders.get(0));
        assertEquals(PublicKey.valueOf("DWvDTSh3qfn88UoQTEKRV2JnLt5jtJAVoiCo3ivtMwXP"), slotLeaders.get(slotLeaders.size() - 1));
    }

    @Test
    void testGetStakeMinimumDelegation() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "context": {
                    "slot": 501
                  },
                  "value": 1000000000
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        RpcResultObject<UnsignedLong> minimumDelegation = clientApi.getStakeMinimumDelegation(null);

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getStakeMinimumDelegation"
    }
    """;

        // JSON 비교
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertEquals(UnsignedLong.valueOf(1000000000L), minimumDelegation.getValue()); // 최소 위임 값 검증
    }

    @Test
    void testGetSupply() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "context": {
                    "slot": 1114
                  },
                  "value": {
                    "circulating": 16000,
                    "nonCirculating": 1000000,
                    "nonCirculatingAccounts": [
                      "FEy8pTbP5fEoqMV1GdTz83byuA8EKByqYat1PKDgVAq5",
                      "9huDUZfxoJ7wGMTffUE7vh1xePqef7gyrLJu9NApncqA",
                      "3mi1GmwEE3zo2jmfDuzvjSX9ovRXsDUKHvsntpkhuLJ9",
                      "BYxEJTDerkaRWBem3XgnVcdhppktBXa2HbkHPKj2Ui4Z"
                    ],
                    "total": 1016000
                  }
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        RpcResultObject<ResValueSupply> supply = clientApi.getSupply(null);

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getSupply"
    }
    """;

        // JSON 비교
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(supply);
        assertEquals(UnsignedLong.valueOf(1114L), supply.getContext().getSlot());
        assertEquals(UnsignedLong.valueOf(16000L), supply.getValue().getCirculating());
        assertEquals(UnsignedLong.valueOf(1000000L), supply.getValue().getNonCirculating());
        assertEquals(UnsignedLong.valueOf(1016000L), supply.getValue().getTotal());
        assertEquals(4, supply.getValue().getNonCirculatingAccounts().size());
        assertEquals(PublicKey.valueOf("FEy8pTbP5fEoqMV1GdTz83byuA8EKByqYat1PKDgVAq5"), supply.getValue().getNonCirculatingAccounts().get(0));
        assertEquals(PublicKey.valueOf("BYxEJTDerkaRWBem3XgnVcdhppktBXa2HbkHPKj2Ui4Z"), supply.getValue().getNonCirculatingAccounts().get(3));
    }

    @Test
    void testGetTokenAccountBalance() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "context": {
                    "slot": 1114
                  },
                  "value": {
                    "amount": "9864",
                    "decimals": 2,
                    "uiAmount": 98.64,
                    "uiAmountString": "98.64"
                  }
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        PublicKey tokenAccount = PublicKey.valueOf("7fUAJdStEuGbc3sM84cKRL6yYaaSstyLSU4ve5oovLS7");
        RpcResultObject<ResValueTokenAccountBalance> balance = clientApi.getTokenAccountBalance(tokenAccount, null);

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getTokenAccountBalance",
      "params": [
        "7fUAJdStEuGbc3sM84cKRL6yYaaSstyLSU4ve5oovLS7"
      ]
    }
    """;

        // JSON 비교
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(balance);
        assertEquals(UnsignedLong.valueOf(1114), balance.getContext().getSlot());
        assertEquals(UnsignedLong.valueOf("9864"), balance.getValue().getAmount());
        assertEquals(2, balance.getValue().getDecimals());
        assertEquals(98.64, balance.getValue().getUiAmount(), 0.01);
        assertEquals("98.64", balance.getValue().getUiAmountString());
    }

    @Test
    void testGetTokenAccountsByDelegate() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "context": {
                    "slot": 1114
                  },
                  "value": [
                    {
                      "account": {
                        "data": {
                          "program": "spl-token",
                          "parsed": {
                            "info": {
                              "tokenAmount": {
                                "amount": "1",
                                "decimals": 1,
                                "uiAmount": 0.1,
                                "uiAmountString": "0.1"
                              },
                              "delegate": "4Nd1mBQtrMJVYVfKf2PJy9NZUZdTAsp7D4xWLs4gDB4T",
                              "delegatedAmount": {
                                "amount": "1",
                                "decimals": 1,
                                "uiAmount": 0.1,
                                "uiAmountString": "0.1"
                              },
                              "state": "initialized",
                              "isNative": false,
                              "mint": "3wyAj7Rt1TWVPZVteFJPLa26JmLvdb1CAKEFZm3NY75E",
                              "owner": "CnPoSPKXu7wJqxe59Fs72tkBeALovhsCxYeFwPCQH9TD"
                            },
                            "type": "account"
                          },
                          "space": 165
                        },
                        "executable": false,
                        "lamports": 1726080,
                        "owner": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA",
                        "rentEpoch": 4,
                        "space": 165
                      },
                      "pubkey": "28YTZEwqtMHWrhWcvv34se7pjS7wctgqzCPB3gReCFKp"
                    }
                  ]
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        PublicKey delegate = PublicKey.valueOf("4Nd1mBQtrMJVYVfKf2PJy9NZUZdTAsp7D4xWLs4gDB4T");
        PublicKey programId = PublicKey.valueOf("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");
        RpcResultObject<List<ResValueTokenAccount>> response = clientApi.getTokenAccountsByDelegate(
                delegate,
                TokenAccountsByDelegateFilter.builder()
                        .programId(programId)
                        .build(),
                TokenAccountsByDelegateConfig.builder()
                        .encoding(Encoding.JSON_PARSED)
                        .build());

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getTokenAccountsByDelegate",
      "params": [
        "4Nd1mBQtrMJVYVfKf2PJy9NZUZdTAsp7D4xWLs4gDB4T",
        {
          "programId": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
        },
        {
          "encoding": "jsonParsed"
        }
      ]
    }
    """;

        // JSON 비교
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(response);
        assertEquals(UnsignedLong.valueOf(1114), response.getContext().getSlot());

        // 토큰 계정 데이터 검증
        List<ResValueTokenAccount> accounts = response.getValue();
        assertNotNull(accounts);
        assertEquals(1, accounts.size());
        ResValueTokenAccount account = accounts.get(0);

        assertEquals(PublicKey.valueOf("28YTZEwqtMHWrhWcvv34se7pjS7wctgqzCPB3gReCFKp"), account.getPubkey());
        assertEquals(UnsignedLong.valueOf("1726080"), account.getAccount().getLamports());
        assertEquals(PublicKey.valueOf("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"), account.getAccount().getOwner());
        assertFalse(account.getAccount().getExecutable());

        // Parsed 정보 검증
        StateData parsed = account.getAccount().getData();
        Map<String, Object> parsedInfo = parsed.getObject();
        assertNotNull(parsedInfo);
        assertEquals("spl-token", parsed.getObjectValue("program"));
        assertEquals(0.1, (double) parsed.getObjectValue("parsed.info.tokenAmount.uiAmount"), 0.01);
        assertEquals("0.1", parsed.getObjectValue("parsed.info.tokenAmount.uiAmountString"));
        assertEquals("4Nd1mBQtrMJVYVfKf2PJy9NZUZdTAsp7D4xWLs4gDB4T", parsed.getObjectValue("parsed.info.delegate"));
    }

    @Test
    void testGetTokenAccountsByOwner() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "context": {
                    "apiVersion": "2.0.15",
                    "slot": 341197933
                  },
                  "value": [
                    {
                      "account": {
                        "data": {
                          "parsed": {
                            "info": {
                              "isNative": false,
                              "mint": "2cHr7QS3xfuSV8wdxo3ztuF4xbiarF6Nrgx3qpx3HzXR",
                              "owner": "A1TMhSGzQxMr1TboBKtgixKz1sS6REASMxPo1qsyTSJd",
                              "state": "initialized",
                              "tokenAmount": {
                                "amount": "420000000000000",
                                "decimals": 6,
                                "uiAmount": 420000000.0,
                                "uiAmountString": "420000000"
                              }
                            },
                            "type": "account"
                          },
                          "program": "spl-token",
                          "space": 165
                        },
                        "executable": false,
                        "lamports": 2039280,
                        "owner": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA",
                        "rentEpoch": 18446744073709551615,
                        "space": 165
                      },
                      "pubkey": "BGocb4GEpbTFm8UFV2VsDSaBXHELPfAXrvd4vtt8QWrA"
                    },
                    {
                      "account": {
                        "data": {
                          "parsed": {
                            "info": {
                              "isNative": false,
                              "mint": "4KVSsAtsG8JByKfB2jYWgGwvVR9WcBSUfsqpTSL9c3Jr",
                              "owner": "A1TMhSGzQxMr1TboBKtgixKz1sS6REASMxPo1qsyTSJd",
                              "state": "initialized",
                              "tokenAmount": {
                                "amount": "10000000000000",
                                "decimals": 9,
                                "uiAmount": 10000.0,
                                "uiAmountString": "10000"
                              }
                            },
                            "type": "account"
                          },
                          "program": "spl-token",
                          "space": 165
                        },
                        "executable": false,
                        "lamports": 2039280,
                        "owner": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA",
                        "rentEpoch": 18446744073709551615,
                        "space": 165
                      },
                      "pubkey": "9PwCPoWJ75LSgZeGMubXBdufYMVd66HrcF78QzW6ZHkV"
                    }
                  ]
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        PublicKey owner = PublicKey.valueOf("A1TMhSGzQxMr1TboBKtgixKz1sS6REASMxPo1qsyTSJd");
        PublicKey programId = PublicKey.valueOf("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");
        RpcResultObject<List<ResValueTokenAccount>> response = clientApi.getTokenAccountsByOwner(
                owner,
                TokenAccountsByOwnerFilter.builder()
                        .programId(programId)
                        .build(),
                TokenAccountsByOwnerConfig.builder()
                        .encoding(Encoding.JSON_PARSED)
                        .build());

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getTokenAccountsByOwner",
      "params": [
        "A1TMhSGzQxMr1TboBKtgixKz1sS6REASMxPo1qsyTSJd",
        {
          "programId": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
        },
        {
          "encoding": "jsonParsed"
        }
      ]
    }
    """;

        // JSON 비교
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(response);
        assertEquals(UnsignedLong.valueOf(341197933), response.getContext().getSlot());
        assertEquals("2.0.15", response.getContext().getApiVersion());

        // 계정 목록 검증
        List<ResValueTokenAccount> accounts = response.getValue();
        assertEquals(2, accounts.size());

        // 첫 번째 계정 검증
        ResValueTokenAccount account1 = accounts.get(0);
        assertEquals(PublicKey.valueOf("BGocb4GEpbTFm8UFV2VsDSaBXHELPfAXrvd4vtt8QWrA"), account1.getPubkey());
        assertFalse(account1.getAccount().getExecutable());
        assertEquals(UnsignedLong.valueOf(2039280), account1.getAccount().getLamports());

        StateData parsedInfo1 = account1.getAccount().getData();
        assertEquals("2cHr7QS3xfuSV8wdxo3ztuF4xbiarF6Nrgx3qpx3HzXR", parsedInfo1.getObjectValue("parsed.info.mint"));
        assertEquals(4.2E8, parsedInfo1.getObjectValue("parsed.info.tokenAmount.uiAmount"));
        assertEquals("420000000", parsedInfo1.getObjectValue("parsed.info.tokenAmount.uiAmountString"));

        // 두 번째 계정 검증
        ResValueTokenAccount account2 = accounts.get(1);
        assertEquals(PublicKey.valueOf("9PwCPoWJ75LSgZeGMubXBdufYMVd66HrcF78QzW6ZHkV"), account2.getPubkey());
        assertFalse(account2.getAccount().getExecutable());
        assertEquals(UnsignedLong.valueOf(2039280), account2.getAccount().getLamports());

        StateData parsedInfo2 = account2.getAccount().getData();
        assertEquals("4KVSsAtsG8JByKfB2jYWgGwvVR9WcBSUfsqpTSL9c3Jr", parsedInfo2.getObjectValue("parsed.info.mint"));
        assertEquals(10000.0, parsedInfo2.getObjectValue("parsed.info.tokenAmount.uiAmount"));
        assertEquals("10000", parsedInfo2.getObjectValue("parsed.info.tokenAmount.uiAmountString"));
    }

    @Test
    void testGetTokenLargestAccounts() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "context": {
                    "slot": 1114
                  },
                  "value": [
                    {
                      "address": "FYjHNoFtSQ5uijKrZFyYAxvEr87hsKXkXcxkcmkBAf4r",
                      "amount": "771",
                      "decimals": 2,
                      "uiAmount": 7.71,
                      "uiAmountString": "7.71"
                    },
                    {
                      "address": "BnsywxTcaYeNUtzrPxQUvzAWxfzZe3ZLUJ4wMMuLESnu",
                      "amount": "229",
                      "decimals": 2,
                      "uiAmount": 2.29,
                      "uiAmountString": "2.29"
                    }
                  ]
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        PublicKey mintAddress = PublicKey.valueOf("3wyAj7Rt1TWVPZVteFJPLa26JmLvdb1CAKEFZm3NY75E");
        RpcResultObject<List<ResValueTokenLargestAccounts>> response = clientApi.getTokenLargestAccounts(
                mintAddress,
                null
        );

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getTokenLargestAccounts",
      "params": [
        "3wyAj7Rt1TWVPZVteFJPLa26JmLvdb1CAKEFZm3NY75E"
      ]
    }
    """;

        // JSON 비교
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(response);
        assertEquals(UnsignedLong.valueOf(1114), response.getContext().getSlot());
        assertNotNull(response.getValue());
        assertEquals(2, response.getValue().size());

        // 첫 번째 계정 검증
        ResValueTokenLargestAccounts account1 = response.getValue().get(0);
        assertEquals(PublicKey.valueOf("FYjHNoFtSQ5uijKrZFyYAxvEr87hsKXkXcxkcmkBAf4r"), account1.getAddress());
        assertEquals(UnsignedLong.valueOf(771), account1.getAmount());
        assertEquals(2, account1.getDecimals());
        assertEquals(7.71, account1.getUiAmount(), 0.01);
        assertEquals("7.71", account1.getUiAmountString());

        // 두 번째 계정 검증
        ResValueTokenLargestAccounts account2 = response.getValue().get(1);
        assertEquals(PublicKey.valueOf("BnsywxTcaYeNUtzrPxQUvzAWxfzZe3ZLUJ4wMMuLESnu"), account2.getAddress());
        assertEquals(UnsignedLong.valueOf(229), account2.getAmount());
        assertEquals(2, account2.getDecimals());
        assertEquals(2.29, account2.getUiAmount(), 0.01);
        assertEquals("2.29", account2.getUiAmountString());
    }

    @Test
    void testGetTokenSupply() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "context": {
                    "slot": 1114
                  },
                  "value": {
                    "amount": "100000",
                    "decimals": 2,
                    "uiAmount": 1000,
                    "uiAmountString": "1000"
                  }
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        PublicKey mintAddress = PublicKey.valueOf("3wyAj7Rt1TWVPZVteFJPLa26JmLvdb1CAKEFZm3NY75E");
        RpcResultObject<ResValueTokenSupply> response = clientApi.getTokenSupply(mintAddress, null);

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getTokenSupply",
      "params": [
        "3wyAj7Rt1TWVPZVteFJPLa26JmLvdb1CAKEFZm3NY75E"
      ]
    }
    """;

        // JSON 비교
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(response);
        assertNotNull(response.getContext());
        assertEquals(UnsignedLong.valueOf(1114), response.getContext().getSlot());

        ResValueTokenSupply value = response.getValue();
        assertNotNull(value);
        assertEquals(UnsignedLong.valueOf(100000), value.getAmount());
        assertEquals(2, value.getDecimals());
        assertEquals(1000.0, value.getUiAmount(), 0.01);
        assertEquals("1000", value.getUiAmountString());
    }

    @Test
    void testGetTransaction() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "meta": {
                    "err": null,
                    "fee": 5000,
                    "innerInstructions": [],
                    "postBalances": [499998932500, 26858640, 1, 1, 1],
                    "postTokenBalances": [],
                    "preBalances": [499998937500, 26858640, 1, 1, 1],
                    "preTokenBalances": [],
                    "rewards": [],
                    "status": {
                      "Ok": null
                    }
                  },
                  "slot": 430,
                  "transaction": {
                    "message": {
                      "accountKeys": [
                        "3UVYmECPPMZSCqWKfENfuoTv51fTDTWicX9xmBD2euKe",
                        "AjozzgE83A3x1sHNUR64hfH7zaEBWeMaFuAN9kQgujrc",
                        "SysvarS1otHashes111111111111111111111111111",
                        "SysvarC1ock11111111111111111111111111111111",
                        "Vote111111111111111111111111111111111111111"
                      ],
                      "header": {
                        "numReadonlySignedAccounts": 0,
                        "numReadonlyUnsignedAccounts": 3,
                        "numRequiredSignatures": 1
                      },
                      "instructions": [
                        {
                          "accounts": [1, 2, 3, 0],
                          "data": "37u9WtQpcm6ULa3WRQHmj49EPs4if7o9f1jSRVZpm2dvihR9C8jY4NqEwXUbLwx15HBSNcP1",
                          "programIdIndex": 4
                        }
                      ],
                      "recentBlockhash": "mfcyqEXB3DnHXki6KjjmZck6YjmZLvpAByy2fj4nh6B"
                    },
                    "signatures": [
                      "2nBhEBYYvfaAe16UMNqRHre4YNSskvuYgx3M6E4JP1oDYvZEJHvoPzyUidNgNX5r9sTyN1J9UxtbCXy2rqYcuyuv"
                    ]
                  }
                },
                "blockTime": null,
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        Signature transactionSignature = Signature.of("2nBhEBYYvfaAe16UMNqRHre4YNSskvuYgx3M6E4JP1oDYvZEJHvoPzyUidNgNX5r9sTyN1J9UxtbCXy2rqYcuyuv");
        String encoding = "json";
        ResValueConfirmedTransaction response = clientApi.getTransaction(transactionSignature, TransactionConfig.builder().encoding(Encoding.JSON).build());

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getTransaction",
      "params": [
        "2nBhEBYYvfaAe16UMNqRHre4YNSskvuYgx3M6E4JP1oDYvZEJHvoPzyUidNgNX5r9sTyN1J9UxtbCXy2rqYcuyuv",
        { "encoding": "json" }
      ]
    }
    """;

        // JSON 비교
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(response);

        // Meta 검증
        ResValueConfirmedTransaction.Meta meta = response.getMeta();
        assertNotNull(meta);
        assertNull(meta.getErr());
        assertEquals(UnsignedLong.valueOf(5000), meta.getFee());
        assertEquals(List.of(UnsignedLong.valueOf(499998932500L), UnsignedLong.valueOf(26858640L), UnsignedLong.valueOf(1L), UnsignedLong.valueOf(1L), UnsignedLong.valueOf(1L)), meta.getPostBalances());
        assertEquals(List.of(UnsignedLong.valueOf(499998937500L), UnsignedLong.valueOf(26858640), UnsignedLong.valueOf(1L), UnsignedLong.valueOf(1L), UnsignedLong.valueOf(1L)), meta.getPreBalances());

        // Transaction 검증
        ResValueTransaction transaction = response.getTransaction();
        assertNotNull(transaction);

        ResValueTransaction.Message message = transaction.getMessage();
        assertEquals(Blockhash.of("mfcyqEXB3DnHXki6KjjmZck6YjmZLvpAByy2fj4nh6B"), message.getRecentBlockhash());
        assertEquals(5, message.getAccountKeys().size());
        assertEquals(PublicKey.valueOf("Vote111111111111111111111111111111111111111"), message.getAccountKeys().get(4));

        // Instructions 검증
        ResValueInstruction instruction = message.getInstructions().get(0);
        assertNotNull(instruction);
        assertEquals(List.of(1, 2, 3, 0), instruction.getAccounts());
        assertEquals("37u9WtQpcm6ULa3WRQHmj49EPs4if7o9f1jSRVZpm2dvihR9C8jY4NqEwXUbLwx15HBSNcP1", instruction.getData().getValue());
        assertEquals(Encoding.BASE58, instruction.getData().getEncoding());
        assertEquals(4, instruction.getProgramIdIndex());
    }

    @Test
    void testGetTransactionCount() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": 268,
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        UnsignedLong transactionCount = clientApi.getTransactionCount(null);

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getTransactionCount"
    }
    """;

        // JSON 비교
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertEquals(UnsignedLong.valueOf(268), transactionCount);
    }

    @Test
    void testGetVersion() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "feature-set": 2891131721,
                  "solana-core": "1.16.7"
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        ResValueVersion response = clientApi.getVersion();

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getVersion"
    }
    """;

        // JSON 비교
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(response);
        assertEquals("1.16.7", response.getSolanaCore());
        assertEquals(2891131721L, response.getFeatureSet());
    }

    @Test
    void testGetVoteAccounts() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "current": [
                    {
                      "commission": 0,
                      "epochVoteAccount": true,
                      "epochCredits": [
                        [1, 64, 0],
                        [2, 192, 64]
                      ],
                      "nodePubkey": "B97CCUW3AEZFGy6uUg6zUdnNYvnVq5VG8PUtb2HayTDD",
                      "lastVote": 147,
                      "activatedStake": 42,
                      "votePubkey": "3ZT31jkAGhUaw8jsy4bTknwBMP8i4Eueh52By4zXcsVw"
                    }
                  ],
                  "delinquent": []
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        PublicKey votePubkey = PublicKey.valueOf("3ZT31jkAGhUaw8jsy4bTknwBMP8i4Eueh52By4zXcsVw");
        ResValueVoteAccounts response = clientApi.getVoteAccounts(VoteAccountsConfig.builder().votePubkey(votePubkey).build());

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "getVoteAccounts",
      "params": [
        {
          "votePubkey": "3ZT31jkAGhUaw8jsy4bTknwBMP8i4Eueh52By4zXcsVw"
        }
      ]
    }
    """;

        // JSON 비교
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(response);

        // Current vote accounts validation
        List<ResValueVoteAccounts.VoteAccount> current = response.getCurrent();
        assertNotNull(current);
        assertEquals(1, current.size());
        ResValueVoteAccounts.VoteAccount account = current.get(0);

        assertEquals(0, account.getCommission());
        assertTrue(account.isEpochVoteAccount());
        assertEquals(PublicKey.valueOf("B97CCUW3AEZFGy6uUg6zUdnNYvnVq5VG8PUtb2HayTDD"), account.getNodePubkey());
        assertEquals(UnsignedLong.valueOf(147), account.getLastVote());
        assertEquals(UnsignedLong.valueOf(42), account.getActivatedStake());
        assertEquals(PublicKey.valueOf("3ZT31jkAGhUaw8jsy4bTknwBMP8i4Eueh52By4zXcsVw"), account.getVotePubkey());
        assertEquals(List.of(EpochCredits.of(1, 64, 0), EpochCredits.of(2, 192, 64)), account.getEpochCredits());

        // Delinquent vote accounts validation
        List<ResValueVoteAccounts.VoteAccount> delinquent = response.getDelinquent();
        assertNotNull(delinquent);
        assertTrue(delinquent.isEmpty());
    }

    @Test
    void testIsBlockhashValid() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "context": {
                    "slot": 2483
                  },
                  "value": false
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        Blockhash blockhash = Blockhash.of("J7rBdM6AecPDEZp8aPq5iPSNKVkU5Q76F3oAV4eW5wsW");
        Commitment commitment = Commitment.PROCESSED;
        RpcResultObject<Boolean> response = clientApi.isBlockhashValid(blockhash, BlockhashValidConfig.builder().commitment(commitment).build());

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "id": 45,
      "jsonrpc": "2.0",
      "method": "isBlockhashValid",
      "params": [
        "J7rBdM6AecPDEZp8aPq5iPSNKVkU5Q76F3oAV4eW5wsW",
        {"commitment": "processed"}
      ]
    }
    """;

        // JSON 비교
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(response);

        // Context 검증
        RpcResultObject.Context context = response.getContext();
        assertNotNull(context);
        assertEquals(UnsignedLong.valueOf(2483), context.getSlot());

        // Value 검증
        assertFalse(response.getValue());
    }

    @Test
    void testMinimumLedgerSlot() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": 1234,
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        UnsignedLong minimumSlot = clientApi.minimumLedgerSlot();

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "minimumLedgerSlot"
    }
    """;

        // JSON 비교
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertEquals(UnsignedLong.valueOf(1234), minimumSlot);
    }

    @Test
    void testRequestAirdrop() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": "5VERv8NMvzbJMEkV8xnrLkEaWRtSz9CosKDYjCJjBRnbJLgp8uirBgmQpjKhoR4tjF3ZpRzrFmBV6UjKdiSZkQUW",
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        PublicKey pubKey = PublicKey.valueOf("83astBRguLMdt2h5U1Tpdq5tjFoJ6noeGwaY3mDLVcri");
        UnsignedLong lamports = UnsignedLong.valueOf(1000000000L);
        Signature transactionId = clientApi.requestAirdrop(pubKey, lamports, null);

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "requestAirdrop",
      "params": [
        "83astBRguLMdt2h5U1Tpdq5tjFoJ6noeGwaY3mDLVcri",
        1000000000
      ]
    }
    """;

        // JSON 비교
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(transactionId);
        assertEquals(Signature.of("5VERv8NMvzbJMEkV8xnrLkEaWRtSz9CosKDYjCJjBRnbJLgp8uirBgmQpjKhoR4tjF3ZpRzrFmBV6UjKdiSZkQUW"), transactionId);
    }

    @Test
    void testSendTransaction() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": "2id3YC2jK9G5Wo2phDx4gJVAew8DcY5NAojnVuao8rkxwPYPe8cSwE5GzhEgJA2y8fVjDEo6iR6ykBvDxrTQrtpb",
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        String encodedTransaction = "4hXTCkRzt9WyecNzV1XPgCDfGAZzQKNxLXgynz5QDuWWPSAZBZSHptvWRL3BjCvzUXRdKvHL2b7yGrRQcWyaqsaBCncVG7BFggS8w9snUts67BSh3EqKpXLUm5UMHfD7ZBe9GhARjbNQMLJ1QD3Spr6oMTBU6EhdB4RD8CP2xUxr2u3d6fos36PD98XS6oX8TQjLpsMwncs5DAMiD4nNnR8NBfyghGCWvCVifVwvA8B8TJxE1aiyiv2L429BCWfyzAme5sZW8rDb14NeCQHhZbtNqfXhcp2tAnaAT";
        Transaction transaction = Transaction.deserialize(Base58.decode(encodedTransaction));
        Signature transactionId = clientApi.sendTransaction(transaction, null);

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "sendTransaction",
      "params": [
        "4hXTCkRzt9WyecNzV1XPgCDfGAZzQKNxLXgynz5QDuWWPSAZBZSHptvWRL3BjCvzUXRdKvHL2b7yGrRQcWyaqsaBCncVG7BFggS8w9snUts67BSh3EqKpXLUm5UMHfD7ZBe9GhARjbNQMLJ1QD3Spr6oMTBU6EhdB4RD8CP2xUxr2u3d6fos36PD98XS6oX8TQjLpsMwncs5DAMiD4nNnR8NBfyghGCWvCVifVwvA8B8TJxE1aiyiv2L429BCWfyzAme5sZW8rDb14NeCQHhZbtNqfXhcp2tAnaAT"
      ]
    }
    """;

        // JSON 비교
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(transactionId);
        assertEquals(Signature.of("2id3YC2jK9G5Wo2phDx4gJVAew8DcY5NAojnVuao8rkxwPYPe8cSwE5GzhEgJA2y8fVjDEo6iR6ykBvDxrTQrtpb"), transactionId);
    }

    @Test
    void testSimulateTransaction() throws IOException, RpcException {
        // 1. Mock Call 설정
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        // 2. Mock 응답 설정
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockConfig.getEndpoint()).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("""
              {
                "jsonrpc": "2.0",
                "result": {
                  "context": {
                    "slot": 218
                  },
                  "value": {
                    "err": null,
                    "accounts": null,
                    "logs": [
                      "Program 83astBRguLMdt2h5U1Tpdq5tjFoJ6noeGwaY3mDLVcri invoke [1]",
                      "Program 83astBRguLMdt2h5U1Tpdq5tjFoJ6noeGwaY3mDLVcri consumed 2366 of 1400000 compute units",
                      "Program return: 83astBRguLMdt2h5U1Tpdq5tjFoJ6noeGwaY3mDLVcri KgAAAAAAAAA=",
                      "Program 83astBRguLMdt2h5U1Tpdq5tjFoJ6noeGwaY3mDLVcri success"
                    ],
                    "returnData": {
                      "data": ["Kg==", "base64"],
                      "programId": "83astBRguLMdt2h5U1Tpdq5tjFoJ6noeGwaY3mDLVcri"
                    },
                    "unitsConsumed": 2366
                  }
                },
                "id": 1
              }
            """, MediaType.get("application/json")))
                .build();
        when(mockCall.execute()).thenReturn(mockResponse);

        // 3. 메서드 호출
        String encodedTransaction = "AQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAEDArczbMia1tLmq7zz4DinMNN0pJ1JtLdqIJPUw3YrGCzYAMHBsgN27lcgB6H2WQvFgyZuJYHa46puOQo9yQ8CVQbd9uHXZaGT2cvhRs7reawctIXtX1s3kTqM9YV+/wCp20C7Wj2aiuk5TReAXo+VTVg8QTHjs0UjNMMKCvpzZ+ABAgEBARU=";
        Transaction transaction = Transaction.deserialize(Base64.decode(encodedTransaction));
        RpcResultObject<ResValueSimulatedTransaction> response = clientApi.simulateTransaction(transaction, SimulateTransactionConfig.builder().encoding(Encoding.BASE64).build());

        // 4. 요청 데이터 검증
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient).newCall(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        // 요청 본문 추출
        Buffer requestBodyBuffer = new Buffer();
        capturedRequest.body().writeTo(requestBodyBuffer);
        String actualRequestJson = requestBodyBuffer.readUtf8();

        // 기대 요청 JSON
        String expectedRequestJson = """
    {
      "jsonrpc": "2.0",
      "id": 1,
      "method": "simulateTransaction",
      "params": [
        "AQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAEDArczbMia1tLmq7zz4DinMNN0pJ1JtLdqIJPUw3YrGCzYAMHBsgN27lcgB6H2WQvFgyZuJYHa46puOQo9yQ8CVQbd9uHXZaGT2cvhRs7reawctIXtX1s3kTqM9YV+/wCp20C7Wj2aiuk5TReAXo+VTVg8QTHjs0UjNMMKCvpzZ+ABAgEBARU=",
        {
          "encoding": "base64"
        }
      ]
    }
    """;

        // JSON 비교
        assertJsonEqualsIgnoringId(expectedRequestJson, actualRequestJson);

        // 5. 응답 데이터 검증
        assertNotNull(response);

        // Context 검증
        RpcResultObject.Context context = response.getContext();
        assertNotNull(context);
        assertEquals(UnsignedLong.valueOf(218), context.getSlot());

        // Value 검증
        ResValueSimulatedTransaction value = response.getValue();
        assertNull(value.getErr());
        assertNull(value.getAccounts());
        assertEquals(UnsignedLong.valueOf(2366), value.getUnitsConsumed());

        // Logs 검증
        List<String> logs = value.getLogs();
        assertNotNull(logs);
        assertEquals(4, logs.size());
        assertEquals("Program 83astBRguLMdt2h5U1Tpdq5tjFoJ6noeGwaY3mDLVcri invoke [1]", logs.get(0));
        assertEquals("Program 83astBRguLMdt2h5U1Tpdq5tjFoJ6noeGwaY3mDLVcri success", logs.get(3));

        // Return Data 검증
        ResValueSimulatedTransaction.ReturnData returnData = value.getReturnData();
        assertNotNull(returnData);
        assertEquals(PublicKey.valueOf("83astBRguLMdt2h5U1Tpdq5tjFoJ6noeGwaY3mDLVcri"), returnData.getProgramId());
        assertEquals("Kg==", returnData.getData().getValue());
        assertEquals(Encoding.BASE64, returnData.getData().getEncoding());
    }
}