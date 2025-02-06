package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.deanly.solana.sdk.types.Commitment;
import net.deanly.solana.sdk.types.FilterAccountType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LargestAccountsConfig {

    @Json(name = "commitment")
    private Commitment commitment; // Optional: Specifies the commitment level.

    @Json(name = "filter")
    private FilterAccountType filter; // Optional: Filter results by account type. Valid values are "circulating" and "nonCirculating".
}