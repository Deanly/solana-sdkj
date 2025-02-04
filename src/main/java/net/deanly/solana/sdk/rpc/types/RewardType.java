package net.deanly.solana.sdk.rpc.types;

import com.squareup.moshi.Json;

public enum RewardType {
    @Json(name = "fee")
    FEE,

    @Json(name = "rent")
    RENT,

    @Json(name = "voting")
    VOTING,

    @Json(name = "staking")
    Staking
}
