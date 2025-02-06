package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solana.sdk.types.Commitment;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@lombok.Builder
public class BalanceConfig {
    @Json(name = "commitment")
    private Commitment commitment;

    @Json(name = "minContextSlot")
    private Long minContextSlot;
}
