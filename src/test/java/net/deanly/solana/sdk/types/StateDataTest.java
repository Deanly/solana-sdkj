package net.deanly.solana.sdk.types;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StateDataTest {

    private final Moshi moshi = new Moshi.Builder().build();
    private final Type MAP_TYPE = Types.newParameterizedType(Map.class, String.class, Object.class);
    private final JsonAdapter<Map<String, Object>> adapter = moshi.adapter(MAP_TYPE);

    @Test
    public void test() throws IOException {
        String json = """
        {
          "jsonrpc": "2.0",
          "result": {
            "context": { "slot": 1114 },
            "value": [
              {
                "account": {
                  "data": {
                    "program": "spl-token",
                    "parsed": {
                      "info": {
                        "tokenAmount": { "amount": "1", "decimals": 1, "uiAmount": 0.1, "uiAmountString": "0.1" },
                        "delegate": "4Nd1mBQtrMJVYVfKf2PJy9NZUZdTAsp7D4xWLs4gDB4T",
                        "delegatedAmount": { "amount": "1", "decimals": 1, "uiAmount": 0.1, "uiAmountString": "0.1" },
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
        """;

        Map<String, Object> jsonMap = adapter.fromJson(json);

        StateData data = new StateData(jsonMap);

        // 예제 테스트, 별도 정의 없다면 moshi 는 숫자를 모두 double 로 만들어버림.
        assertEquals(1114.0, data.getObjectValue("result.context.slot"));
        assertEquals("3wyAj7Rt1TWVPZVteFJPLa26JmLvdb1CAKEFZm3NY75E",
                data.getObjectValue("result.value[0].account.data.parsed.info.mint"));
        assertEquals("CnPoSPKXu7wJqxe59Fs72tkBeALovhsCxYeFwPCQH9TD",
                data.getObjectValue("result.value[0].account.data.parsed.info.owner"));
        assertEquals("4Nd1mBQtrMJVYVfKf2PJy9NZUZdTAsp7D4xWLs4gDB4T",
                data.getObjectValue("result.value[0].account.data.parsed.info.delegate"));
        assertEquals(0.1, data.getObjectValue("result.value.0.account.data.parsed.info.tokenAmount.uiAmount"));
        assertEquals(1.0, data.getObjectValue("result.value[].account.data.parsed.info.tokenAmount.decimals"));
    }
}
