package net.deanly.solana.sdk.rpc.client.adapter;

import net.deanly.solana.sdk.types.guava.UnsignedLong;
import com.squareup.moshi.*;

import java.io.IOException;

public class MoshiUnsignedLongJsonAdapter extends JsonAdapter<UnsignedLong> {
    @Override
    public UnsignedLong fromJson(JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();
        if (value == null || value.isEmpty()) {
            return null;
        }
        return UnsignedLong.valueOf(value);
    }

    @Override
    public void toJson(JsonWriter jsonWriter, UnsignedLong unsignedLong) throws IOException {
        if (unsignedLong == null) {
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.value(unsignedLong);
    }

    public static final JsonAdapter.Factory FACTORY = (type, annotations, moshi) -> {
        if (type == UnsignedLong.class) {
            return new MoshiUnsignedLongJsonAdapter();
        }
        return null;
    };
}
