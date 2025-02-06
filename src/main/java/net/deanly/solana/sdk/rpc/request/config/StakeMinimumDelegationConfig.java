package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solana.sdk.types.Commitment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StakeMinimumDelegationConfig {

    @Json(name = "commitment")
    private Commitment commitment; // Optional: Specifies the commitment level.
}