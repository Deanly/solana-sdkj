package net.deanly.solanarpcj.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.solanarpcj.rpc.types.TokenResultObjects;

@Getter
@ToString
public class ResValueTokenAccountInfo {

    @Json(name = "account")
    private TokenResultObjects.Value account;

    @Json(name = "pubkey")
    private String pubkey;

}
