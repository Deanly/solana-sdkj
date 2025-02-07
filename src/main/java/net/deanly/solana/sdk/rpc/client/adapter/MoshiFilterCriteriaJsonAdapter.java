package net.deanly.solana.sdk.rpc.client.adapter;

import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.*;
import net.deanly.solana.sdk.types.FilterCriteria;
import net.deanly.solana.sdk.types.FilterCriteria.Memcmp;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MoshiFilterCriteriaJsonAdapter extends JsonAdapter<List<FilterCriteria>> {

    private final JsonAdapter<Memcmp> memcmpAdapter;

    public MoshiFilterCriteriaJsonAdapter(Moshi moshi) {
        this.memcmpAdapter = moshi.adapter(Memcmp.class);
    }

    @Nullable
    @Override
    public List<FilterCriteria> fromJson(JsonReader reader) throws IOException {
        List<FilterCriteria> filters = new ArrayList<>();
        reader.beginArray();

        FilterCriteria dataSizeCriteria = null;
        FilterCriteria memcmpCriteria = null;

        while (reader.hasNext()) {
            reader.beginObject();
            FilterCriteria criteria = new FilterCriteria();

            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("dataSize")) {
                    criteria.setDataSize(UnsignedLong.valueOf(reader.nextLong()));
                    dataSizeCriteria = criteria;
                } else if (name.equals("memcmp")) {
                    criteria.setMemcmp(memcmpAdapter.fromJson(reader));
                    memcmpCriteria = criteria;
                } else {
                    reader.skipValue();
                }
            }

            reader.endObject();
        }

        reader.endArray();

        if (dataSizeCriteria != null) {
            filters.add(dataSizeCriteria); // ✅ 인덱스 0에 `dataSize`
        }
        if (memcmpCriteria != null) {
            filters.add(memcmpCriteria); // ✅ 인덱스 1에 `memcmp`
        }

        return filters;
    }

    @Override
    public void toJson(JsonWriter writer, @Nullable List<FilterCriteria> filters) throws IOException {
        if (filters == null) {
            writer.nullValue();
            return;
        }

        writer.beginArray(); // ✅ `filters` 배열 시작

        // ✅ `dataSize`가 있는 경우 먼저 추가
        for (FilterCriteria filter : filters) {
            if (filter.getDataSize() != null) {
                writer.beginObject();
                writer.name("dataSize").value(filter.getDataSize().longValue());
                writer.endObject();
            }
        }

        // ✅ `memcmp`가 있는 경우 추가
        for (FilterCriteria filter : filters) {
            if (filter.getMemcmp() != null) {
                writer.beginObject();
                writer.name("memcmp");
                memcmpAdapter.toJson(writer, filter.getMemcmp());
                writer.endObject();
            }
        }

        writer.endArray(); // ✅ `filters` 배열 끝
    }

    public static final JsonAdapter.Factory FACTORY = (type, annotations, moshi) -> {
        if (Types.equals(type, Types.newParameterizedType(List.class, FilterCriteria.class))) {
            return new MoshiFilterCriteriaJsonAdapter(moshi);
        }
        return null;
    };
}