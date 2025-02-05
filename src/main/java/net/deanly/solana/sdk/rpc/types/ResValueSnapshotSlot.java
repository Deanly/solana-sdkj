package net.deanly.solana.sdk.rpc.types;

import com.google.common.primitives.UnsignedLong;
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
