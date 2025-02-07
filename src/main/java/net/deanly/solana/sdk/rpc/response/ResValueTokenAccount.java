package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import com.google.common.primitives.UnsignedLong;
import net.deanly.solana.sdk.crypto.PublicKey;

@Getter
@ToString
public class ResValueTokenAccount {

    @Json(name = "pubkey")
    private PublicKey pubkey; // The account Pubkey as base-58 encoded string.

    @Json(name = "account")
    private ResValueAccountInfo account; // Account information.

}