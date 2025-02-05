package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.deanly.solana.sdk.rpc.types.Commitment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EpochInfoConfig {

    @Json(name = "commitment")
    private Commitment commitment; // Optional: Default is 'finalized'
}