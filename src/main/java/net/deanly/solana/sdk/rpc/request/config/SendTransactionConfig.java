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
public class SendTransactionConfig {

    @Json(name = "skipPreflight")
    private Boolean skipPreflight; // Optional: If true, skip the preflight transaction checks (default: false).

    @Json(name = "preflightCommitment")
    private Commitment preflightCommitment; // Optional: Commitment level to use for preflight (default: "finalized").

    @Json(name = "encoding")
    private Encoding encoding; // Optional: Encoding used for the transaction data (default: "base58").

    @Json(name = "maxRetries")
    private Integer maxRetries; // Optional: Maximum number of times for the RPC node to retry sending the transaction to the leader.

    @Json(name = "minContextSlot")
    private Long minContextSlot; // Optional: The minimum slot that the request can be evaluated at.
}