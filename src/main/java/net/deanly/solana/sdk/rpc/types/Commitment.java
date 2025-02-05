package net.deanly.solana.sdk.rpc.types;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Commitment {
    @Json(name = "finalized")
    FINALIZED("finalized"),

    @Json(name = "confirmed")
    CONFIRMED("confirmed"),

    @Json(name = "processed")
    PROCESSED("processed"),

    @Json(name = "singleGossip")
    SINGLE_GOSSIP("singleGossip"),

    @Json(name = "maxGossip")
    MAX_GOSSIP("maxGossip"),

    @Json(name = "single")
    SINGLE("single"),

    @Json(name = "root")
    ROOT("root"),

    @Json(name = "recent")
    RECENT("recent"),

    @Json(name = "max")
    MAX("max");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
