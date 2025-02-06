package net.deanly.solana.sdk.rpc.client.adapter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import net.deanly.solana.sdk.crypto.PublicKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class MoshiPublicKeyJsonAdapter extends JsonAdapter<PublicKey> {

    @Nullable
    @Override
    public PublicKey fromJson(JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();
        return new PublicKey(value);
    }

    @Override
    public void toJson(@NotNull JsonWriter jsonWriter, @Nullable PublicKey publicKey) throws IOException {
        if (publicKey != null) {
            jsonWriter.value(publicKey.toString());
        } else {
            jsonWriter.nullValue();
        }
    }
}
