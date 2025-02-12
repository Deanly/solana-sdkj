package net.deanly.solana.sdk.rpc.response;

import net.deanly.structlayout.type.guava.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.types.ValidatorIdentityInfo;

import java.util.Map;

@Getter
@ToString
public class ResValueBlockProduction {

    @Json(name = "byIdentity")
    private Map<PublicKey, ValidatorIdentityInfo> byIdentity;

    @Json(name = "range")
    private Range range;

    @Getter
    @ToString
    public static class Range {
        @Json(name = "firstSlot")
        private UnsignedLong firstSlot;

        @Json(name = "lastSlot")
        private UnsignedLong lastSlot;
    }
}
