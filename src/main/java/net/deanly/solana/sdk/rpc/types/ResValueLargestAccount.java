package net.deanly.solana.sdk.rpc.types;

import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueLargestAccount {

    @Json(name = "address")
    private String address; // Base-58 encoded address of the account.

    @Json(name = "lamports")
    private UnsignedLong lamports; // Number of lamports in the account.
}