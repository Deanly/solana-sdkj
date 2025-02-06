package net.deanly.solana.sdk.rpc.client.http.impl;

import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.rpc.client.RpcClient;
import net.deanly.solana.sdk.rpc.client.exception.RpcException;
import net.deanly.solana.sdk.rpc.request.config.AccountInfoConfig;
import net.deanly.solana.sdk.rpc.request.config.BlockConfig;
import net.deanly.solana.sdk.rpc.response.ResValueAccountInfo;
import net.deanly.solana.sdk.rpc.response.ResValueBlock;
import net.deanly.solana.sdk.rpc.response.ResValueConfirmedTransaction;
import net.deanly.solana.sdk.types.Encoding;
import net.deanly.solana.sdk.rpc.response.ResValueBlockCommitment;
import net.deanly.solana.sdk.types.TransactionDetails;
import okhttp3.*;
import okio.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MoshiHttpMethodApiTest {

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
        ResValueAccountInfo result = clientApi.getAccountInfo(account, AccountInfoConfig.builder().encoding(Encoding.BASE58).build());

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
        assertEquals(UnsignedLong.valueOf(88849814690250L), result.getLamports());
        assertEquals(new PublicKey("11111111111111111111111111111111"), result.getOwner());
        assertFalse(result.getExecutable());
        assertEquals(UnsignedLong.valueOf("18446744073709551615"), result.getRentEpoch());
        assertEquals(UnsignedLong.valueOf(0L), result.getSpace());
        assertEquals("", result.getData().getValue());
        assertEquals("base58", result.getData().getEncoding().getValue());
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
        UnsignedLong balance = clientApi.getBalance(PublicKey.valueOf(account), null);

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
        Moshi moshi = new Moshi.Builder().build();
        Type mapType = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapType);

        Map<String, Object> actualRequestMap = adapter.fromJson(actualRequestJson);
        Map<String, Object> expectedRequestMap = adapter.fromJson(expectedRequestJson);

        // ID는 제외하고 비교
        actualRequestMap.remove("id");
        expectedRequestMap.remove("id");
        assertEquals(expectedRequestMap, actualRequestMap);

        // 8. 응답 데이터 검증
        assertNotNull(balance);
        assertEquals(UnsignedLong.valueOf(0), balance); // 기대 값: 0
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
        assertEquals("mfcyqEXB3DnHXki6KjjmZck6YjmZLvpAByy2fj4nh6B", firstTransaction.getTransaction().getMessage().getRecentBlockhash());
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
        Moshi moshi = new Moshi.Builder().build();
        Type mapType = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapType);

        Map<String, Object> actualRequestMap = adapter.fromJson(actualRequestJson);
        Map<String, Object> expectedRequestMap = adapter.fromJson(expectedRequestJson);

        // `id`는 제외하고 검증
        actualRequestMap.remove("id");
        expectedRequestMap.remove("id");
        assertEquals(expectedRequestMap, actualRequestMap);

        // 8. 응답 데이터 검증
        assertNotNull(result);
        assertEquals(42L, result.getTotalStake());

        // Commitment 배열 검증
        List<Integer> expectedCommitments = List.of(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 10, 32
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
        int blockHeight = clientApi.call(
                "getBlockHeight",
                List.of(), // 이 메서드는 파라미터가 없음
                Integer.class // 기대하는 응답 타입
        );

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

        Map<String, Object> actualRequestMap = adapter.fromJson(actualRequestJson);
        Map<String, Object> expectedRequestMap = adapter.fromJson(expectedRequestJson);

        // `id`는 제외하고 검증
        actualRequestMap.remove("id");
        expectedRequestMap.remove("id");
        assertEquals(expectedRequestMap, actualRequestMap);

        // 6. 응답 데이터 검증
        assertEquals(1233, blockHeight); // 기대하는 blockHeight 값
    }

}