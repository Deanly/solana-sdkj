package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solana.sdk.rpc.types.Commitment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestAirdropConfig {

    @Json(name = "commitment")
    private Commitment commitment; // Optional: Specifies the commitment level.
}