package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solana.sdk.rpc.types.Commitment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignaturesForAddressConfig {

    @Json(name = "commitment")
    private Commitment commitment; // Optional: Specifies the commitment level.

    @Json(name = "minContextSlot")
    private Long minContextSlot; // Optional: The minimum slot that the request can be evaluated at.

    @Json(name = "limit")
    private Integer limit; // Optional: Maximum transaction signatures to return (between 1 and 1,000). Default is 1,000.

    @Json(name = "before")
    private String before; // Optional: Start searching backwards from this transaction signature.

    @Json(name = "until")
    private String until; // Optional: Search until this transaction signature, if found before limit is reached.
}