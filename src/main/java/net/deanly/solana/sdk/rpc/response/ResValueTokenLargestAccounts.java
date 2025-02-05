package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import com.google.common.primitives.UnsignedLong;

@Getter
@ToString
public class ResValueTokenLargestAccounts {

    @Json(name = "address")
    private String address; // The address of the token account.

    @Json(name = "amount")
    private UnsignedLong amount; // The raw token account balance without decimals, as a string representation of u64.

    @Json(name = "decimals")
    private int decimals; // Number of base 10 digits to the right of the decimal place.

    @Json(name = "uiAmount")
    private Double uiAmount; // The token account balance, using mint-prescribed decimals. (Deprecated)

    @Json(name = "uiAmountString")
    private String uiAmountString; // The token account balance as a string, using mint-prescribed decimals.
}