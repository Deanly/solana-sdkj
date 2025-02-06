package net.deanly.solana.sdk.rpc.client.adapter;

import com.squareup.moshi.*;
import net.deanly.solana.sdk.types.EpochCredits;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MoshiEpochCreditsJsonAdapter extends JsonAdapter<List<EpochCredits>> {

    @Nullable
    @Override
    public List<EpochCredits> fromJson(JsonReader jsonReader) throws IOException {
        List<EpochCredits> epochCreditsList = new ArrayList<>();

        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            jsonReader.beginArray();
            long epoch = jsonReader.nextLong();
            long credits = jsonReader.nextLong();
            long previousCredits = jsonReader.nextLong();
            jsonReader.endArray();
            epochCreditsList.add(new EpochCredits(epoch, credits, previousCredits));
        }
        jsonReader.endArray();

        return epochCreditsList;
    }

    @Override
    public void toJson(@NotNull JsonWriter jsonWriter, @Nullable List<EpochCredits> epochCreditsList) throws IOException {
        if (epochCreditsList == null) {
            jsonWriter.nullValue();
            return;
        }

        jsonWriter.beginArray();
        for (EpochCredits ec : epochCreditsList) {
            jsonWriter.beginArray();
            jsonWriter.value(ec.getEpoch());
            jsonWriter.value(ec.getCredits());
            jsonWriter.value(ec.getPreviousCredits());
            jsonWriter.endArray();
        }
        jsonWriter.endArray();
    }
}