package net.deanly.solana.sdk.rpc.client.adapter;

import com.squareup.moshi.*;
import java.io.IOException;

public class MoshiNumberJsonAdapter extends JsonAdapter<Number> {
    @Override
    public Number fromJson(JsonReader reader) throws IOException {
        JsonReader.Token token = reader.peek();
        if (token == JsonReader.Token.NUMBER) {
            String value = reader.nextString();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e1) {
                try {
                    return Long.parseLong(value);
                } catch (NumberFormatException e2) {
                    return Double.parseDouble(value);
                }
            }
        }
        return null;
    }

    @Override
    public void toJson(JsonWriter writer, Number value) throws IOException {
        if (value instanceof Integer || value instanceof Long) {
            writer.value(value.longValue());
        } else {
            writer.value(value.doubleValue());
        }
    }

    public static final JsonAdapter.Factory FACTORY = (type, annotations, moshi) -> {
        if (type == Number.class || type == Integer.class || type == Long.class || type == Double.class) {
            return new MoshiNumberJsonAdapter();
        }
        return null;
    };
}
