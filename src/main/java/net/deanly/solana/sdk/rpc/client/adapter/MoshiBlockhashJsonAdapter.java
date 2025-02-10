package net.deanly.solana.sdk.rpc.client.adapter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import net.deanly.solana.sdk.types.Blockhash;

import java.io.IOException;

public class MoshiBlockhashJsonAdapter extends JsonAdapter<Blockhash> {
    @Override
    public Blockhash fromJson(JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();
        return Blockhash.of(value);
    }

    @Override
    public void toJson(JsonWriter jsonWriter, Blockhash blockhash) throws IOException {
        if (blockhash != null) {
            jsonWriter.value(blockhash.toString());
        } else {
            jsonWriter.nullValue();
        }
    }
}
