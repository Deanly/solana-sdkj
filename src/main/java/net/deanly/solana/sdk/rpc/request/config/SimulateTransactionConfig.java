package net.deanly.solana.sdk.rpc.request.config;

import lombok.*;
import com.squareup.moshi.Json;
import net.deanly.solana.sdk.rpc.types.Commitment;
import net.deanly.solana.sdk.rpc.types.Encoding;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
public class SimulateTransactionConfig {

    @Json(name = "commitment")
    private Commitment commitment;

    @Json(name = "sigVerify")
    private Boolean sigVerify;

    @Json(name = "replaceRecentBlockhash")
    private Boolean replaceRecentBlockhash;

    @Json(name = "minContextSlot")
    private Long minContextSlot;

    @Json(name = "encoding")
    private Encoding encoding;

    @Json(name = "innerInstructions")
    private Boolean innerInstructions;

    @Json(name = "accounts")
    private Accounts accounts;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.Builder
    public static class Accounts {

        @Json(name = "encoding")
        private Encoding encoding; // Optional: Encoding for returned account data.

        @Json(name = "addresses")
        private List<String> addresses; // An array of accounts to return, as base-58 encoded strings.
    }
}