package net.deanly.solana.sdk.rpc.response;

import net.deanly.structlayout.type.guava.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueEpochInfo {
    @Json(name = "absoluteSlot")
    private UnsignedLong absoluteSlot;

    @Json(name = "blockHeight")
    private UnsignedLong blockHeight;

    @Json(name = "epoch")
    private UnsignedLong epoch;

    @Json(name = "slotIndex")
    private UnsignedLong slotIndex;

    @Json(name = "slotsInEpoch")
    private UnsignedLong slotsInEpoch;

    @Json(name = "transactionCount")
    private UnsignedLong transactionCount;
}
    