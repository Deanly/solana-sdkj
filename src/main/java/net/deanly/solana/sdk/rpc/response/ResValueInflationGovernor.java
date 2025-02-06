package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueInflationGovernor {

    @Json(name = "initial")
    private double initial; // Initial inflation percentage from time 0

    @Json(name = "terminal")
    private double terminal; // Terminal inflation percentage

    @Json(name = "taper")
    private double taper; // Rate per year at which inflation is lowered

    @Json(name = "foundation")
    private double foundation; // Percentage of total inflation allocated to the foundation

    @Json(name = "foundationTerm")
    private double foundationTerm; // Duration of foundation pool inflation in years
}