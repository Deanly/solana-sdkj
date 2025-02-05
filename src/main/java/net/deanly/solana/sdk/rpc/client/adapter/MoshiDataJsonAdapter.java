package net.deanly.solana.sdk.rpc.client.adapter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import net.deanly.solana.sdk.rpc.types.EncodedData;
import net.deanly.solana.sdk.rpc.types.Encoding;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

// TODO: json, jsonParsed 도 처리할 수 있게 수정
public class MoshiDataJsonAdapter extends JsonAdapter<EncodedData> {
    @Nullable
    @Override
    public EncodedData fromJson(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() != JsonReader.Token.BEGIN_ARRAY) {
            throw new IOException("Expected JSON array for Data object");
        }

        jsonReader.beginArray();
        String value = jsonReader.nextString();
        String encodingStr = jsonReader.nextString();
        jsonReader.endArray();

        Encoding encoding = parseEncoding(encodingStr);
        return new EncodedData(value, encoding);
    }

    @Override
    public void toJson(JsonWriter jsonWriter, @Nullable EncodedData data) throws IOException {
        jsonWriter.beginArray();
        jsonWriter.value(data.getValue());
        jsonWriter.value(formatEncoding(data.getEncoding()));
        jsonWriter.endArray();
    }

    private Encoding parseEncoding(String encodingStr) {
        for (Encoding encoding : Encoding.values()) {
            if (encoding.name().equalsIgnoreCase(encodingStr.replace("+", "_"))) {
                return encoding;
            }
        }
        throw new IllegalArgumentException("Unknown encoding: " + encodingStr);
    }

    private String formatEncoding(Encoding encoding) {
        return encoding.name().toLowerCase().replace("_", "+");
    }
}
