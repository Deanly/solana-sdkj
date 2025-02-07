package net.deanly.solana.sdk.rpc.client.http.impl;

import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.rpc.client.RpcClient;
import net.deanly.solana.sdk.rpc.client.adapter.MoshiNumberJsonAdapter;
import net.deanly.solana.sdk.rpc.client.exception.RpcException;
import net.deanly.solana.sdk.rpc.request.config.*;
import net.deanly.solana.sdk.rpc.response.*;
import net.deanly.solana.sdk.types.*;
import okhttp3.*;
import okio.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static net.deanly.solana.sdk.rpc.client.MoshiTestUtil.assertJsonEqualsIgnoringId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MoshiHttpMethodApiImplTest {

    private MoshiHttpMethodApiImpl clientApi; // 테스트할 대상 클래스
    private OkHttpClient mockHttpClient; // 모킹한 OkHttpClient
    private RpcClient.ClientConfig mockConfig;

    @BeforeEach
    void setup() {
        // 기본 설정 생성
        mockConfig = RpcClient.ClientConfig.builder()
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
        Moshi moshi = new Moshi.Builder().build();
        Type mapType = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapType);

        Map<String, Object> actualRequestMap = adapter.fromJson(actualRequestJson);
        Map<String, Object> expectedRequestMap = adapter.fromJson(expectedRequestJson);

        // ID는 제외하고 비교
        actualRequestMap.remove("id");
        expectedRequestMap.remove("id");
//        assertEquals(expectedRequestMap, actualRequestMap);

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
        assertEquals(9887L, result.getContext().getSlot());

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
        assertEquals(5068L, feeResult.getContext().getSlot()); // 응답 context.slot 값 검증
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
        assertEquals(54L, context.getSlot());

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
        assertEquals(2792, context.getSlot());

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
        List<ResValueProgramAccount> result = clientApi.getProgramAccounts(
                PublicKey.valueOf("4Nd1mBQtrMJVYVfKf2PJy9NZUZdTAsp7D4xWLs4gDB4T"),
                ProgramAccountsConfig.builder()
                        .filters(
                                List.of(
                                        FilterCriteria.builder()
                                                .dataSize(UnsignedLong.valueOf(17))
                                                .memcmp(
                                                        FilterCriteria.Memcmp.builder()
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
        ResValueProgramAccount programAccount = result.get(0);
        assertEquals("2R9jLfiAQ9bgdcw6h8s44439", programAccount.getAccount().getData().getValue());
        assertFalse(programAccount.getAccount().getExecutable());
        assertEquals(UnsignedLong.valueOf(15298080), programAccount.getAccount().getLamports());
        assertEquals(PublicKey.valueOf("4Nd1mBQtrMJVYVfKf2PJy9NZUZdTAsp7D4xWLs4gDB4T"), programAccount.getAccount().getOwner());
        assertEquals(UnsignedLong.valueOf(28), programAccount.getAccount().getRentEpoch());
        assertEquals(UnsignedLong.valueOf(42), programAccount.getAccount().getSpace());
        assertEquals(PublicKey.valueOf("CxELquR1gPP8wHe33gZ4QxqGB3sZ9RSwsJ2KshVewkFY"), programAccount.getPubkey());
    }
}