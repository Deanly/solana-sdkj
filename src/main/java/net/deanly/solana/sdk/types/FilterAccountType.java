package net.deanly.solana.sdk.types;

import com.squareup.moshi.Json;

public enum FilterAccountType {
    @Json(name = "circulating")
    CIRCULATING,
    @Json(name = "nonCirculating")
    NON_CIRCULATING
}
