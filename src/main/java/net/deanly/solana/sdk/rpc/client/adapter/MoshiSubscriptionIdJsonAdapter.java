package net.deanly.solana.sdk.rpc.client.adapter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import net.deanly.solana.sdk.types.SubscriptionId;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class MoshiSubscriptionIdJsonAdapter extends JsonAdapter<SubscriptionId> {
    @Nullable
    @Override
    public SubscriptionId fromJson(JsonReader jsonReader) throws IOException {
        long value = jsonReader.nextLong();
        return SubscriptionId.of(value);
    }

    @Override
    public void toJson(JsonWriter jsonWriter, @Nullable SubscriptionId id) throws IOException {
        if (id != null) {
            jsonWriter.value(id.getValue());
        } else {
            jsonWriter.nullValue();
        }
    }
}
