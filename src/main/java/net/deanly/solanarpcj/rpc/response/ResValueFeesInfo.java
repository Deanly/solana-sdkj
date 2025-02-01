package net.deanly.solanarpcj.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueFeesInfo {

    @Json(name = "blockhash")
    private String blockhash;

    @Json(name = "feeCalculator")
    private ResValueRecentBlockhash.FeeCalculator feeCalculator;

    @Json(name = "lastValidSlot")
    private long lastValidSlot;

    @Json(name = "lastValidBlockHeight")
    private long lastValidBlockHeight;

}
