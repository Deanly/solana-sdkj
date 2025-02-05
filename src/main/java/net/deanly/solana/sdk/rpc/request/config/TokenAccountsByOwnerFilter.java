package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenAccountsByOwnerFilter {

    @Json(name = "mint")
    private String mint; // Optional: Pubkey of the specific token Mint to limit accounts to, as base-58 encoded string.

    @Json(name = "programId")
    private String programId; // Optional: Pubkey of the Token program that owns the accounts, as base-58 encoded string.
}