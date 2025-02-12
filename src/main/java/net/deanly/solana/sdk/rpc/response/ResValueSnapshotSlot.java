package net.deanly.solana.sdk.rpc.response;

import net.deanly.structlayout.type.guava.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueSnapshotSlot {

    @Json(name = "full")
    private UnsignedLong fullSnapshotSlot;

    @Json(name = "incremental")
    private UnsignedLong incrementalSnapshotSlot;

}
