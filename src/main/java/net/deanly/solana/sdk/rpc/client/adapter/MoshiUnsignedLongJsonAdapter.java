package net.deanly.solana.sdk.rpc.client.adapter;

import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.*;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class MoshiUnsignedLongJsonAdapter extends JsonAdapter<UnsignedLong> {
    @Nullable
    @Override
    public UnsignedLong fromJson(JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();
        if (value == null || value.isEmpty()) {
            return null;
        }
        return UnsignedLong.valueOf(value);
    }

    @Override
    public void toJson(JsonWriter jsonWriter, @Nullable UnsignedLong unsignedLong) throws IOException {
        if (unsignedLong == null) {
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.value(unsignedLong.toString());
    }
}
