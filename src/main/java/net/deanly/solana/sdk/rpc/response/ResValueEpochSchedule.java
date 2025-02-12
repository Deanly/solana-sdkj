package net.deanly.solana.sdk.rpc.response;

import net.deanly.structlayout.type.guava.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueEpochSchedule {

    @Json(name = "slotsPerEpoch")
    private UnsignedLong slotsPerEpoch; // The maximum number of slots in each epoch

    @Json(name = "leaderScheduleSlotOffset")
    private UnsignedLong leaderScheduleSlotOffset; // The number of slots before the beginning of an epoch to calculate a leader schedule for that epoch

    @Json(name = "warmup")
    private boolean warmup; // Whether epochs start short and grow

    @Json(name = "firstNormalEpoch")
    private UnsignedLong firstNormalEpoch; // First normal-length epoch

    @Json(name = "firstNormalSlot")
    private UnsignedLong firstNormalSlot; // First normal-length slot
}
