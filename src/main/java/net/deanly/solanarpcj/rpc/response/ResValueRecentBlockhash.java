package net.deanly.solanarpcj.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueRecentBlockhash {

    @Getter
    @ToString
    public static class FeeCalculator {

        @Json(name = "lamportsPerSignature")
        private double lamportsPerSignature;
    }

    @Json(name = "blockhash")
    private String blockhash;

    @Json(name = "feeCalculator")
    private FeeCalculator feeCalculator;
}
