package net.deanly.solana.sdk.rpc.response;

import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solana.sdk.rpc.types.RewardType;

import java.math.BigInteger;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResValueReward {
    /// The public key, as base-58 encoded string, of the account that received the reward
    @Json(name = "pubkey")
    private String pubkey;

    /// number of reward lamports credited or debited by the account, as a i64
    @Json(name = "lamports")
    private Long lamports;

    /// account balance in lamports after the reward was applied
    @Json(name = "postBalance")
    private UnsignedLong postBalance;

    /// type of reward: "fee", "rent", "voting", "staking"
    @Json(name = "rewardType")
    private RewardType rewardType;

    /// vote account commission when the reward was credited, only present for voting and staking rewards
    @Json(name = "commission")
    private Integer commission;
}
