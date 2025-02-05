package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyConfig {

    @Json(name = "commitment")
    private String commitment; // Optional: Specifies the commitment level.

    @Json(name = "excludeNonCirculatingAccountsList")
    private Boolean excludeNonCirculatingAccountsList; // Optional: Exclude non-circulating accounts list from response.
}