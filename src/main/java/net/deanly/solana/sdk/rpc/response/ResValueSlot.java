package net.deanly.solana.sdk.rpc.response;

import net.deanly.solana.sdk.types.guava.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueSlot {
    @Json(name = "parent")
    private UnsignedLong parent;

    @Json(name = "root")
    private UnsignedLong root;

    @Json(name = "slot")
    private UnsignedLong slot;
}
