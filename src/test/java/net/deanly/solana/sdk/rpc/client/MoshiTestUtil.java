package net.deanly.solana.sdk.rpc.client;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import net.deanly.solana.sdk.rpc.client.adapter.MoshiNumberJsonAdapter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MoshiTestUtil {

    private static final Moshi MOSHI = new Moshi.Builder()
            .add(MoshiNumberJsonAdapter.FACTORY)
            .build();
    private static final JsonAdapter<Map<String, Object>> ADAPTER =
            MOSHI.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));

    public static void assertJsonEqualsIgnoringId(String expectedJson, String actualJson) throws IOException {
        Map<String, Object> expectedMap = ADAPTER.fromJson(expectedJson);
        Map<String, Object> actualMap = ADAPTER.fromJson(actualJson);

        if (expectedMap == null || actualMap == null) {
            throw new IOException("JSON parsing failed");
        }

        // "id" 필드 제거
        expectedMap.remove("id");
        actualMap.remove("id");

        // 숫자 정규화
        normalizeNumbers(expectedMap);
        normalizeNumbers(actualMap);

        // Map 직접 비교
        if (!deepEquals(expectedMap, actualMap)) {
            throw new AssertionError("JSONs do not match!\nExpected: " + expectedMap + "\nActual  : " + actualMap);
        }
    }

    @SuppressWarnings("unchecked")
    private static void normalizeNumbers(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Integer) {
                entry.setValue(((Integer) value).longValue()); // Integer → Long 변환
            } else if (value instanceof List) {
                normalizeListNumbers((List<?>) value);
            } else if (value instanceof Map) {
                normalizeNumbers((Map<String, Object>) value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void normalizeListNumbers(List<?> list) {
        List<Object> modifiableList = (List<Object>) list;

        for (int i = 0; i < modifiableList.size(); i++) {
            Object value = modifiableList.get(i);
            if (value instanceof Integer) {
                modifiableList.set(i, ((Integer) value).longValue()); // Integer → Long 변환
            } else if (value instanceof List) {
                normalizeListNumbers((List<?>) value);
            } else if (value instanceof Map) {
                normalizeNumbers((Map<String, Object>) value);
            }
        }
    }

    private static boolean deepEquals(Object obj1, Object obj2) {
        if (obj1 instanceof Map && obj2 instanceof Map) {
            Map<?, ?> map1 = (Map<?, ?>) obj1;
            Map<?, ?> map2 = (Map<?, ?>) obj2;
            if (map1.size() != map2.size()) return false;
            for (Object key : map1.keySet()) {
                if (!deepEquals(map1.get(key), map2.get(key))) return false;
            }
            return true;
        }

        if (obj1 instanceof List && obj2 instanceof List) {
            List<?> list1 = (List<?>) obj1;
            List<?> list2 = (List<?>) obj2;
            if (list1.size() != list2.size()) return false;
            for (int i = 0; i < list1.size(); i++) {
                if (!deepEquals(list1.get(i), list2.get(i))) return false;
            }
            return true;
        }

        return Objects.equals(obj1, obj2);
    }
}