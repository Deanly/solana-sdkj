package net.deanly.solana.sdk.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@ToString
public class StateData {
    private final Encoding encoding;
    private final String value;
    private final Map<String, Object> object;
    private final boolean isSingleType;

    public StateData(Encoding encoding, String value) {
        this.encoding = encoding;
        this.value = value;
        this.object = null;
        this.isSingleType = false;
    }

    public StateData(Map<String, Object> object) {
        this.encoding = Encoding.JSON_PARSED;
        this.value = null;
        this.object = object;
        this.isSingleType = false;
    }

    public StateData(Encoding encoding, String value, boolean isSingleType) {
        this.encoding = encoding;
        this.value = value;
        this.object = null;
        this.isSingleType = isSingleType;
    }


    private static final Pattern ARRAY_INDEX_PATTERN = Pattern.compile("(\\w+)\\[(\\d+)]");
    private static final Pattern ARRAY_WILDCARD_PATTERN = Pattern.compile("(.+)\\[\\]");

    /**
     * Retrieves the value from the nested structure within the `object` field of this class,
     * based on the dot-delimited path provided.
     *
     * @param path The dot-delimited path to the target value in the nested structure.
     *             Example:
     *             - `"key1.key2[0]"` → Access a specific value within a Map or a nested List structure.
     *             - `"result.value[1].pubkey"` → Access an array element within a map.
     * @return The value corresponding to the specified path if it exists, otherwise `null`.
     */
    public Object getObjectValue(String path) {
        if (this.object == null || path == null || path.isEmpty()) {
            return null;
        }

        String[] keys = path.split("\\.");
        Object current = this.object;

        for (int keyIndex = 0; keyIndex < keys.length; keyIndex++) {
            String key = keys[keyIndex];
            if (current instanceof Map) {
                // 배열 인덱스 매칭 (예: value[0])
                Matcher indexMatcher = ARRAY_INDEX_PATTERN.matcher(key);
                if (indexMatcher.matches()) {
                    String arrayKey = indexMatcher.group(1);
                    int index = Integer.parseInt(indexMatcher.group(2));

                    Object arrayObj = ((Map<?, ?>) current).get(arrayKey);
                    if (arrayObj instanceof List) {
                        current = findElementInList((List<?>) arrayObj, index);
                    } else {
                        return null;
                    }
                }
                // 배열 전체 탐색 (`[]` 사용)
                else {
                    Matcher wildcardMatcher = ARRAY_WILDCARD_PATTERN.matcher(key);
                    if (wildcardMatcher.matches()) {
                        String arrayKey = wildcardMatcher.group(1);
                        Object arrayObj = ((Map<?, ?>) current).get(arrayKey);
                        if (arrayObj instanceof List) {
                            Tuple tuple =  findFirstElementContainTargetKey((List<?>) arrayObj, keys[keyIndex + 1]);
                            keyIndex += tuple.i;
                            current = tuple.o;
                        } else {
                            return null;
                        }
                    } else {
                        current = ((Map<?, ?>) current).get(key);
                    }
                }
            }
            // 배열 내부 요소 접근
            else if (current instanceof List) {
                if (key.matches("\\d+")) {
                    int index = Integer.parseInt(key);
                    current = findElementInList((List<?>) current, index);
                } else {
                    return null; // 잘못된 경로
                }
            } else {
                return null; // 존재하지 않는 경로
            }

            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private static Object findElementInList(List<?> list, int index) {
        return (index >= 0 && index < list.size()) ? list.get(index) : null;
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class Tuple {
        final Object o;
        final Integer i;
    }
    private Tuple findFirstElementContainTargetKey(List<?> list, String targetKey) {
        for (Object obj : list) {
            if (obj instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) obj;
                if (map.containsKey(targetKey)) {
                    return Tuple.of(map.get(targetKey), 1);
                }
            } else if (obj instanceof List) {
                return findFirstElementContainTargetKey((List<?>) obj, targetKey);
            }
        }
        return Tuple.of(null, 0);
    }
}
