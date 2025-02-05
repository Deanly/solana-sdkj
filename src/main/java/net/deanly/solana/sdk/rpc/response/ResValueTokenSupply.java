package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import com.google.common.primitives.UnsignedLong;

@Getter
@ToString
public class ResValueTokenSupply {

    @Json(name = "amount")
    private UnsignedLong amount; // The raw total token supply without decimals, as a string representation of u64.

    @Json(name = "decimals")
    private int decimals; // Number of base 10 digits to the right of the decimal place.

    @Json(name = "uiAmount")
    private Double uiAmount; // The total token supply, using mint-prescribed decimals. (Deprecated)

    @Json(name = "uiAmountString")
    private String uiAmountString; // The total token supply as a string, using mint-prescribed decimals.
}