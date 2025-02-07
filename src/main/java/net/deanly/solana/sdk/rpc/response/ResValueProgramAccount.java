package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.solana.sdk.crypto.PublicKey;

@Getter
@ToString
public class ResValueProgramAccount {

    @Json(name = "pubkey")
    private PublicKey pubkey; // The account Pubkey as a base-58 encoded string.

    @Json(name = "account")
    private ResValueAccountInfo account; // The account details.

}