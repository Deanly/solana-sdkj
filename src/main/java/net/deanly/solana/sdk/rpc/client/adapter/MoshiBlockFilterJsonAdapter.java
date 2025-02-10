package net.deanly.solana.sdk.rpc.client.adapter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.rpc.request.filter.BlockFilter;

import java.io.IOException;

public class MoshiBlockFilterJsonAdapter extends JsonAdapter<BlockFilter> {

    @Override
    public BlockFilter fromJson(JsonReader reader) throws IOException {
        BlockFilter criteria = new BlockFilter();

        // String ("all") 또는 JSON Object 파싱
        if (reader.peek() == JsonReader.Token.STRING) {
            String value = reader.nextString();
            if ("all".equals(value)) {
                criteria.setMentionsAccountOrProgram(null);
            } else {
                throw new IOException("Invalid filter value: " + value);
            }
        } else if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if ("mentionsAccountOrProgram".equals(name)) {
                    criteria.setMentionsAccountOrProgram(new PublicKey(reader.nextString()));
                } else {
                    reader.skipValue(); // 알 수 없는 필드는 무시
                }
            }
            reader.endObject();
        } else {
            throw new IOException("Invalid filter type: " + reader.peek());
        }

        return criteria;
    }

    @Override
    public void toJson(JsonWriter writer, BlockFilter criteria) throws IOException {
        if (criteria == null || criteria.getMentionsAccountOrProgram() == null) {
            // mentionsAccountOrProgram이 null인 경우 "all"로 직렬화
            writer.value("all");
        } else {
            // mentionsAccountOrProgram이 있는 경우 JSON Object로 직렬화
            writer.beginObject();
            writer.name("mentionsAccountOrProgram").value(criteria.getMentionsAccountOrProgram().toString());
            writer.endObject();
        }
    }

    public static final JsonAdapter.Factory FACTORY = (type, annotations, moshi) -> {
        if (type == BlockFilter.class) {
            return new MoshiBlockFilterJsonAdapter();
        }
        return null;
    };
}