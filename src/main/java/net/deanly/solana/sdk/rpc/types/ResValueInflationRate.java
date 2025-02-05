package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueInflationRate {

    @Json(name = "total")
    private double total; // Total inflation rate

    @Json(name = "validator")
    private double validator; // Inflation allocated to validators

    @Json(name = "foundation")
    private double foundation; // Inflation allocated to the foundation

    @Json(name = "epoch")
    private long epoch; // Epoch for which these values are valid
}