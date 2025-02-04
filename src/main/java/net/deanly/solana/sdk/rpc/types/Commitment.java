package net.deanly.solana.sdk.rpc.types;

import com.squareup.moshi.Json;

public enum Commitment {
    @Json(name = "finalized")
    FINALIZED,

    @Json(name = "confirmed")
    CONFIRMED,

    @Json(name = "processed")
    PROCESSED,

    @Json(name = "singleGossip")
    SINGLE_GOSSIP,

    @Json(name = "maxGossip")
    MAX_GOSSIP,

    @Json(name = "single")
    SINGLE,

    @Json(name = "root")
    ROOT,

    @Json(name = "recent")
    RECENT,

    @Json(name = "max")
    MAX;
}
