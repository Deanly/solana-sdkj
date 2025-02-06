package net.deanly.solana.sdk.rpc.response;

import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.solana.sdk.types.Blockhash;

@Getter
@ToString
public class ResValueLatestBlockhash {

    @Json(name = "blockhash")
    private Blockhash blockhash; // A Hash as base-58 encoded string.

    @Json(name = "lastValidBlockHeight")
    private UnsignedLong lastValidBlockHeight; // Last block height at which the blockhash will be valid.
}