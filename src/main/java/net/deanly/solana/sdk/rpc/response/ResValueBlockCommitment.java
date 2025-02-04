package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueBlockCommitment {

    @Json(name = "commitment")
    private long[] commitment;

    @Json(name = "totalStake")
    private long totalStake;
}
