package net.deanly.solana.sdk.rpc.client.adapter;

import com.squareup.moshi.*;
import net.deanly.solana.sdk.types.EpochCredits;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;

public class MoshiEpochCreditsJsonAdapter extends JsonAdapter<EpochCredits> {

    @Nullable
    @Override
    public EpochCredits fromJson(JsonReader jsonReader) throws IOException {
        jsonReader.beginArray();
        long epoch = jsonReader.nextLong();
        long credits = jsonReader.nextLong();
        long previousCredits = jsonReader.nextLong();
        jsonReader.endArray();
        return new EpochCredits(epoch, credits, previousCredits);
    }

    @Override
    public void toJson(@NotNull JsonWriter jsonWriter, @Nullable EpochCredits epochCredits) throws IOException {
        if (epochCredits == null) {
            jsonWriter.nullValue();
            return;
        }

        jsonWriter.beginArray();
        jsonWriter.value(epochCredits.getEpoch());
        jsonWriter.value(epochCredits.getCredits());
        jsonWriter.value(epochCredits.getPreviousCredits());
        jsonWriter.endArray();
    }
}