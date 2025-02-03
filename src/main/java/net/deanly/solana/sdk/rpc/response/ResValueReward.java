package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.solana.sdk.rpc.types.RewardType;

@Getter
@ToString
public class ResValueReward {

    @Json(name = "pubkey")
    private String pubkey;

    @Json(name = "lamports")
    private double lamports;

    @Json(name = "postBalance")
    private String postBalance;

    @Json(name = "rewardType")
    private RewardType rewardType;
}
