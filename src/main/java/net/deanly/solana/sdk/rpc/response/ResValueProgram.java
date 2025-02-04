package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@lombok.Builder(builderClassName = "Builder")
public class ResValueProgram {

    @Json(name = "account")
    private ResValueAccountInfo account;

    @Json(name = "pubkey")
    private String pubkey;

}
