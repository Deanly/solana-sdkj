package net.deanly.solana.sdk.rpc.client.adapter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.rpc.request.filter.LogsFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MoshiLogsFilterJsonAdapter extends JsonAdapter<LogsFilter> {

    @Override
    public LogsFilter fromJson(JsonReader reader) throws IOException {
        LogsFilter filter = new LogsFilter();

        if (reader.peek() == JsonReader.Token.STRING) {
            // String type (e.g., "all", "allWithVotes")
            String typeValue = reader.nextString();
            filter.setType(LogsFilter.Type.valueOf(typeValue.toUpperCase()));
        } else if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
            // Object type (e.g., mentions)
            reader.beginObject();
            while (reader.hasNext()) {
                String key = reader.nextName();
                if ("mentions".equals(key)) {
                    List<PublicKey> mentions = new ArrayList<>();
                    reader.beginArray();
                    while (reader.hasNext()) {
                        mentions.add(new PublicKey(reader.nextString()));
                    }
                    reader.endArray();
                    filter.setMentions(mentions);
                    filter.setType(LogsFilter.Type.MENTIONS);
                } else {
                    reader.skipValue(); // Ignore unknown keys
                }
            }
            reader.endObject();
        } else {
            throw new IOException("Invalid LogsFilter format");
        }

        return filter;
    }

    @Override
    public void toJson(JsonWriter writer, LogsFilter value) throws IOException {
        if (value == null || value.getType() == null) {
            writer.nullValue();
            return;
        }

        if (value.getType() == LogsFilter.Type.ALL || value.getType() == LogsFilter.Type.ALL_WITH_VOTES) {
            // Write as a string
            writer.value(value.getType().getValue());
        } else if (value.getType() == LogsFilter.Type.MENTIONS) {
            // Write as an object with "mentions"
            writer.beginObject();
            writer.name("mentions");
            writer.beginArray();
            for (PublicKey key : Objects.requireNonNull(value.getMentions())) {
                writer.value(key.toBase58());
            }
            writer.endArray();
            writer.endObject();
        } else {
            throw new IOException("Unknown LogsFilter type: " + value.getType());
        }
    }
}
