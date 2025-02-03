package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueSlot {
    @Json(name = "parent")
    private Long parent;

    @Json(name = "root")
    private Long root;

    @Json(name = "slot")
    private Long slot;
}
