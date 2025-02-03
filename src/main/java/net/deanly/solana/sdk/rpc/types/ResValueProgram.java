package net.deanly.solana.sdk.rpc.types;

import com.squareup.moshi.Json;

import lombok.Getter;
import lombok.ToString;

import net.deanly.solana.sdk.rpc.response.ResValueAccountInfo;

@Getter
@ToString
@lombok.Builder(builderClassName = "Builder")
public class ResValueProgram {

    @Json(name = "account")
    private ResValueAccountInfo account;

    @Json(name = "pubkey")
    private String pubkey;

}
