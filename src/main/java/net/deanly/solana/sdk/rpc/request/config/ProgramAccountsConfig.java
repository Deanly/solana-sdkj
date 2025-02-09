package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solana.sdk.types.Commitment;
import net.deanly.solana.sdk.types.Encoding;
import net.deanly.solana.sdk.rpc.request.filter.ProgramAccountFilter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramAccountsConfig {

    @Json(name = "commitment")
    private Commitment commitment; // Optional: Specifies the commitment level.

    @Json(name = "minContextSlot")
    private Long minContextSlot; // Optional: The minimum slot that the request can be evaluated at.

    @Json(name = "withContext")
    private Boolean withContext; // Optional: Wrap the result in an RpcResponse JSON object.

    @Json(name = "encoding")
    private Encoding encoding; // Optional: Encoding format for the returned Account data.

    @Json(name = "dataSlice")
    private Encoding.DataSlice dataSlice; // Optional: Request a slice of the account's data.

    @Json(name = "filters")
    private List<ProgramAccountFilter> filters; // Optional: Filter results using up to 4 filter objects.

}