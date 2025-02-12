package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.structlayout.type.guava.UnsignedLong;

@Getter
@ToString
public class ResValueTokenAccountBalance {

    @Json(name = "amount")
    private UnsignedLong amount; // The raw balance without decimals, as a string representation of u64.

    @Json(name = "decimals")
    private int decimals; // Number of base 10 digits to the right of the decimal place.

    @Json(name = "uiAmount")
    private Double uiAmount; // The balance, using mint-prescribed decimals. (Deprecated)

    @Json(name = "uiAmountString")
    private String uiAmountString; // The balance as a string, using mint-prescribed decimals.
}