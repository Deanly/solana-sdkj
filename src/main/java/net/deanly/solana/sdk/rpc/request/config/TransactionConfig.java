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
public class TransactionConfig {

    @Json(name = "encoding")
    private Encoding encoding; // Optional: Encoding for the returned transaction data.

    @Json(name = "commitment")
    private Commitment commitment; // Optional: Specifies the commitment level.

    @Json(name = "maxSupportedTransactionVersion")
    private Integer maxSupportedTransactionVersion; // Optional: Set the max transaction version to return in responses.
}