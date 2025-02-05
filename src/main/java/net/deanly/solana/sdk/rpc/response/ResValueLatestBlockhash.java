package net.deanly.solana.sdk.rpc.response;

import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueLatestBlockhash {

    @Json(name = "blockhash")
    private String blockhash; // A Hash as base-58 encoded string.

    @Json(name = "lastValidBlockHeight")
    private UnsignedLong lastValidBlockHeight; // Last block height at which the blockhash will be valid.
}