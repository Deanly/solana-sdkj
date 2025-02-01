package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@ToString
public class ResValueBlockProduction {

    @Getter
    @ToString
    public static class BlockProductionRange {
        @Json(name = "firstSlot")
        private double firstSlot;

        @Json(name = "lastSlot")
        private double lastSlot;

    }

    @Json(name = "byIdentity")
    private Map<String, List<Double>> byIdentity;

    public Map<String, List<Double>> getByIdentity() {
        return byIdentity;
    }

    @Json(name = "range")
    private BlockProductionRange blockProductionRange;

}
