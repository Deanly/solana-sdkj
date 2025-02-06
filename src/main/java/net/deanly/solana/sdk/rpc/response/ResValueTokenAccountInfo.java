package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.solana.sdk.types.TokenResultObjects;

@Getter
@ToString
public class ResValueTokenAccountInfo {

    @Json(name = "account")
    private TokenResultObjects.Value account;

    @Json(name = "pubkey")
    private String pubkey;

}
