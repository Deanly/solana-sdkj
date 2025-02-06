package net.deanly.solana.sdk.rpc.response;

import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.types.EncodedData;

@Getter
@ToString
public class ResValueProgramAccount {

    @Json(name = "pubkey")
    private PublicKey pubkey; // The account Pubkey as a base-58 encoded string.

    @Json(name = "account")
    private ResValueAccountInfo account; // The account details.

}