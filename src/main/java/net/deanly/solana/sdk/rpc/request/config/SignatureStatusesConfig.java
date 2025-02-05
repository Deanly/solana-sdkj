package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignatureStatusesConfig {

    @Json(name = "searchTransactionHistory")
    private Boolean searchTransactionHistory; // Optional: If true, searches the ledger cache for any signatures not found in the recent status cache.
}