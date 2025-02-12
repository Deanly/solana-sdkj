package net.deanly.solana.sdk.rpc.response;

import net.deanly.structlayout.type.guava.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class NotiValueBlock {
    @Json(name = "slot")
    private UnsignedLong slot;

    @Json(name = "err")
    private String err;

    @Json(name = "block")
    private ResValueBlock block;
}
