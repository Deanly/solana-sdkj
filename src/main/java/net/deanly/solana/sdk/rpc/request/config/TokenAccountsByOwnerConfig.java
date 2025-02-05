package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solana.sdk.rpc.types.Commitment;
import net.deanly.solana.sdk.rpc.types.Encoding;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenAccountsByOwnerConfig {

    @Json(name = "commitment")
    private Commitment commitment; // Optional: Specifies the commitment level.

    @Json(name = "minContextSlot")
    private Long minContextSlot; // Optional: The minimum slot that the request can be evaluated at.

    @Json(name = "encoding")
    private Encoding encoding; // Optional: Encoding format for Account data.

    @Json(name = "dataSlice")
    private Encoding.DataSlice dataSlice; // Optional: Request a slice of the account's data.
}