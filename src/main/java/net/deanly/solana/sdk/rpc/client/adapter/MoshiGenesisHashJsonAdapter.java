package net.deanly.solana.sdk.rpc.client.adapter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import net.deanly.solana.sdk.types.GenesisHash;

import java.io.IOException;

public class MoshiGenesisHashJsonAdapter extends JsonAdapter<GenesisHash> {
    @Override
    public GenesisHash fromJson(JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();
        return GenesisHash.of(value);
    }

    @Override
    public void toJson(JsonWriter jsonWriter, GenesisHash hash) throws IOException {
        if (hash != null) {
            jsonWriter.value(hash.toString());
        } else {
            jsonWriter.nullValue();
        }
    }
}
