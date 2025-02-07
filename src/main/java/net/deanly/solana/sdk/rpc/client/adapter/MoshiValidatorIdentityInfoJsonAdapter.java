package net.deanly.solana.sdk.rpc.client.adapter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import net.deanly.solana.sdk.types.ValidatorIdentityInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class MoshiValidatorIdentityInfoJsonAdapter extends JsonAdapter<ValidatorIdentityInfo> {

    @Nullable
    @Override
    public ValidatorIdentityInfo fromJson(JsonReader jsonReader) throws IOException {
        ValidatorIdentityInfo info = null;

        jsonReader.beginArray();
        int numberOfLeaderSlots = jsonReader.nextInt();
        int numberOfBlocksProduced = jsonReader.nextInt();
        info = new ValidatorIdentityInfo(numberOfLeaderSlots, numberOfBlocksProduced);
        jsonReader.endArray();

        return info;
    }

    @Override
    public void toJson(@NotNull JsonWriter jsonWriter, @Nullable ValidatorIdentityInfo info) throws IOException {
        if (info == null) {
            jsonWriter.nullValue();
            return;
        }

        jsonWriter.beginArray();
        jsonWriter.value(info.getNumberOfLeaderSlots());
        jsonWriter.value(info.getNumberOfBlocksProduced());
        jsonWriter.endArray();
    }
}
