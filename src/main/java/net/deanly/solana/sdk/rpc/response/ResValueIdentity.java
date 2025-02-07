package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.solana.sdk.crypto.PublicKey;

@Getter
@ToString
public class ResValueIdentity {
    @Json(name = "identity")
    private PublicKey identity;
}
