package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignatureStatusConfig {
    @Json(name = "searchTransactionHistory")
    private final boolean searchTransactionHistory;
}
