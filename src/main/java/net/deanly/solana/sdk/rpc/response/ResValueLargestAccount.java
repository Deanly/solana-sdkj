package net.deanly.solana.sdk.rpc.response;

import net.deanly.solana.sdk.types.guava.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.solana.sdk.crypto.PublicKey;

@Getter
@ToString
public class ResValueLargestAccount {

    @Json(name = "address")
    private PublicKey address; // Base-58 encoded address of the account.

    @Json(name = "lamports")
    private UnsignedLong lamports; // Number of lamports in the account.
}