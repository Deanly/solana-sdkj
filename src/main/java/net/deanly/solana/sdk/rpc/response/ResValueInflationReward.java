package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.solana.sdk.types.guava.UnsignedLong;

@Getter
@ToString
public class ResValueInflationReward {

    @Json(name = "epoch")
    private UnsignedLong epoch; // Epoch for which the reward occurred.

    @Json(name = "effectiveSlot")
    private UnsignedLong effectiveSlot; // The slot in which the rewards are effective.

    @Json(name = "amount")
    private UnsignedLong amount; // Reward amount in lamports.

    @Json(name = "postBalance")
    private UnsignedLong postBalance; // Post balance of the account in lamports.

    @Json(name = "commission")
    private Integer commission; // Optional: Vote account commission when the reward was credited.
}