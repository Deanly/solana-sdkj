package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueVersion {

    @Json(name = "solana-core")
    private String solanaCore; // Software version of solana-core as a string.

    @Json(name = "feature-set")
    private Long featureSet; // Unique identifier of the current software's feature set as a u32.
}