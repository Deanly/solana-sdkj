package net.deanly.solanarpcj.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueFeeCalculatorInfo {

    @Json(name = "feeCalculator")
    private ResValueRecentBlockhash.FeeCalculator feeCalculator;
}

