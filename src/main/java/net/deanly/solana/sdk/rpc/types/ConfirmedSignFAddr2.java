package net.deanly.solana.sdk.rpc.types;

import com.squareup.moshi.Json;

public class ConfirmedSignFAddr2 {

    @Json(name = "limit")
    private long limit;

    @Json(name = "before")
    private String before;

    @Json(name = "until")
    private String until;

    @Json(name = "commitment")
    private Commitment commitment;

    public ConfirmedSignFAddr2(int limit, Commitment commitment) {
        this.limit = limit;
        this.commitment = commitment;
    }
}