package net.deanly.solana.sdk.rpc.request.config;

import net.deanly.structlayout.type.guava.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.deanly.solana.sdk.types.Commitment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InflationRewardConfig {

    @Json(name = "commitment")
    private Commitment commitment; // Optional: Specifies the commitment level.

    @Json(name = "epoch")
    private UnsignedLong epoch; // Optional: Epoch for which the reward occurs. If omitted, the previous epoch will be used.

    @Json(name = "minContextSlot")
    private Long minContextSlot; // Optional: The minimum slot that the request can be evaluated at.
}