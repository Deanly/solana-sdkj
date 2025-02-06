package net.deanly.solana.sdk.rpc.client.adapter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import net.deanly.solana.sdk.types.Signature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class MoshiSignatureJsonAdapter extends JsonAdapter<Signature> {
    @Nullable
    @Override
    public Signature fromJson(JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();
        return Signature.of(value);
    }

    @Override
    public void toJson(@NotNull JsonWriter jsonWriter, @Nullable Signature blockhash) throws IOException {
        if (blockhash != null) {
            jsonWriter.value(blockhash.toString());
        } else {
            jsonWriter.nullValue();
        }
    }
}
