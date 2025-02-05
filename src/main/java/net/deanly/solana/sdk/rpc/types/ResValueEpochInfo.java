package net.deanly.solana.sdk.rpc.types;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueEpochInfo {
    @Json(name = "absoluteSlot")
    private long absoluteSlot;

    @Json(name = "blockHeight")
    private long blockHeight;

    @Json(name = "epoch")
    private long epoch;

    @Json(name = "slotIndex")
    private long slotIndex;

    @Json(name = "slotsInEpoch")
    private long slotsInEpoch;
}
    