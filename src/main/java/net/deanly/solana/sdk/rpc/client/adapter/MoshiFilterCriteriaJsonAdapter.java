package net.deanly.solana.sdk.rpc.client.adapter;

import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.*;
import net.deanly.solana.sdk.rpc.request.filter.ProgramAccountFilter;
import net.deanly.solana.sdk.rpc.request.filter.ProgramAccountFilter.Memcmp;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MoshiFilterCriteriaJsonAdapter extends JsonAdapter<List<ProgramAccountFilter>> {

    private final JsonAdapter<Memcmp> memcmpAdapter;

    public MoshiFilterCriteriaJsonAdapter(Moshi moshi) {
        this.memcmpAdapter = moshi.adapter(Memcmp.class);
    }

    @Nullable
    @Override
    public List<ProgramAccountFilter> fromJson(JsonReader reader) throws IOException {
        List<ProgramAccountFilter> filters = new ArrayList<>();
        reader.beginArray();

        ProgramAccountFilter dataSizeCriteria = null;
        ProgramAccountFilter memcmpCriteria = null;

        while (reader.hasNext()) {
            reader.beginObject();
            ProgramAccountFilter criteria = new ProgramAccountFilter();

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
            filters.add(dataSizeCriteria);
        }
        if (memcmpCriteria != null) {
            filters.add(memcmpCriteria);
        }

        return filters;
    }

    @Override
    public void toJson(JsonWriter writer, @Nullable List<ProgramAccountFilter> filters) throws IOException {
        if (filters == null) {
            writer.nullValue();
            return;
        }

        writer.beginArray();

        for (ProgramAccountFilter filter : filters) {
            if (filter.getDataSize() != null) {
                writer.beginObject();
                writer.name("dataSize").value(filter.getDataSize().longValue());
                writer.endObject();
            }
        }

        for (ProgramAccountFilter filter : filters) {
            if (filter.getMemcmp() != null) {
                writer.beginObject();
                writer.name("memcmp");
                memcmpAdapter.toJson(writer, filter.getMemcmp());
                writer.endObject();
            }
        }

        writer.endArray();
    }

    public static final JsonAdapter.Factory FACTORY = (type, annotations, moshi) -> {
        if (Types.equals(type, Types.newParameterizedType(List.class, ProgramAccountFilter.class))) {
            return new MoshiFilterCriteriaJsonAdapter(moshi);
        }
        return null;
    };
}