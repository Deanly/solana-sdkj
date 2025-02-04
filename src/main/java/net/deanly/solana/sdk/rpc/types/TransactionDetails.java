package net.deanly.solana.sdk.rpc.types;

import com.squareup.moshi.Json;

public enum TransactionDetails {
    @Json(name = "full")
    FULL,

    @Json(name = "accounts")
    ACCOUNTS,

    @Json(name = "signature")
    SIGNATURES,

    @Json(name = "none")
    NONE;
}
