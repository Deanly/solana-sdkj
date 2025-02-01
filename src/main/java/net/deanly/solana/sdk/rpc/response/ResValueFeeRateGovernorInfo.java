package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueFeeRateGovernorInfo {
    @Getter
    @ToString
    public static class FeeRateGovernor {
        @Json(name = "burnPercent")
        private int burnPercent;

        @Json(name = "maxLamportsPerSignature")
        private double maxLamportsPerSignature;

        @Json(name = "minLamportsPerSignature")
        private double minLamportsPerSignature;

        @Json(name = "targetLamportsPerSignature")
        private double targetLamportsPerSignature;

        @Json(name = "targetSignaturesPerSlot")
        private double targetSignaturesPerSlot;
    }

    @Json(name = "feeRateGovernor")
    private FeeRateGovernor feeRateGovernor;

}
