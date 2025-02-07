package net.deanly.solana.sdk.rpc.client.adapter;

import com.squareup.moshi.*;
import net.deanly.solana.sdk.types.StateData;
import net.deanly.solana.sdk.types.Encoding;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

public class MoshiEncodedDataJsonAdapter extends JsonAdapter<StateData> {

    private static final Pattern BASE58_PATTERN = Pattern.compile("^[1-9A-HJ-NP-Za-km-z]+$");
    private static final Pattern BASE64_PATTERN = Pattern.compile("^[A-Za-z0-9+/=]+$");

    private final Moshi moshi = new Moshi.Builder().build();
    private final JsonAdapter<Map<String, Object>> mapJsonAdapter = moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));

    @Nullable
    @Override
    public StateData fromJson(JsonReader jsonReader) throws IOException {
        JsonReader.Token token = jsonReader.peek();

        // JSON 객체 처리 (jsonParsed)
        if (token == JsonReader.Token.BEGIN_OBJECT) {
            Map<String, Object> jsonObject = mapJsonAdapter.fromJson(jsonReader);
            return new StateData(jsonObject);
        }

        // JSON 배열 처리 (base58, base64, json, base64+zstd)
        if (token == JsonReader.Token.BEGIN_ARRAY) {
            jsonReader.beginArray();
            String value = jsonReader.nextString();
            String encodingStr = jsonReader.nextString();
            jsonReader.endArray();

            Encoding encoding = Encoding.fromString(encodingStr);
            if (encoding == null) {
                encoding = detectEncoding(value);
            }
            return new StateData(encoding, value);
        }

        // 단일 문자열 처리 ({"data": "some-value"})
        if (token == JsonReader.Token.STRING) {
            String value = jsonReader.nextString();
            Encoding encoding = detectEncoding(value);
            return new StateData(encoding, value, true);
        }

        throw new IOException("Unexpected JSON format for EncodedData");
    }

    @Override
    public void toJson(@NotNull JsonWriter jsonWriter, @Nullable StateData data) throws IOException {
        if (data == null) {
            jsonWriter.nullValue();
            return;
        }

        Encoding encoding = data.getEncoding();

        // JSON_PARSED → JSON 객체로 직렬화
        if (encoding == Encoding.JSON_PARSED) {
            if (data.getObject() != null) {
                mapJsonAdapter.toJson(jsonWriter, data.getObject());
            } else {
                jsonWriter.nullValue();
            }
            return;
        }

        // 단일 문자열 데이터 → JSON 단일 값으로 직렬화
        if (data.isSingleType()) {
            jsonWriter.value(data.getValue());
            return;
        }

        // 배열 형태로 직렬화
        jsonWriter.beginArray();
        jsonWriter.value(data.getValue());
        jsonWriter.value(formatEncoding(encoding));
        jsonWriter.endArray();
    }

    /**
     * Base58, Base64, JSON 여부를 자동으로 감지
     */
    private Encoding detectEncoding(String value) {
        if (isBase58(value)) {
            return Encoding.BASE58;
        }
        if (isBase64(value)) {
            return Encoding.BASE64;
        }
        if (isJson(value)) {
            return Encoding.JSON;
        }
        throw new IllegalArgumentException("Unknown encoding format: " + value);
    }

    private boolean isBase58(String value) {
        return BASE58_PATTERN.matcher(value).matches();
    }

    private boolean isBase64(String value) {
        return BASE64_PATTERN.matcher(value).matches();
    }

    private boolean isJson(String value) {
        return value.startsWith("{") && value.endsWith("}");
    }

    private String formatEncoding(Encoding encoding) {
        return encoding.name().toLowerCase().replace("_", "+");
    }
}