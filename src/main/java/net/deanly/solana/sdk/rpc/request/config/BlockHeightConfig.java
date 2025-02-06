package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.deanly.solana.sdk.types.Commitment;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
public class BlockHeightConfig {
    @Json(name = "commitment")
    private Commitment commitment;
    @Json(name = "minContextSlot")
    private Long minContextSlot;
}
