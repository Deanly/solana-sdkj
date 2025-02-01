package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueLatestBlockhash {
    @Json(name = "blockhash")
    private String blockhash;

    @Json(name = "lastValidBlockHeight")
    private long lastValidBlockHeight;
}
