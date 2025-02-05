package net.deanly.solana.sdk.rpc.request.config;

import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.deanly.solana.sdk.rpc.types.Commitment;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockProductionConfig {

    @Json(name = "commitment")
    private Commitment commitment;

    @Json(name = "identity")
    private String identity; // Base-58 encoded validator identity

    @Json(name = "range")
    private Range range;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Range {
        @Json(name = "firstSlot")
        private UnsignedLong firstSlot;

        @Json(name = "lastSlot")
        private UnsignedLong lastSlot;
    }
}