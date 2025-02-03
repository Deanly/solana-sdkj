package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ResValueLog {
    @Json(name = "signature")
    private String signature;

    @Json(name = "err")
    private String err;

    @Json(name = "logs")
    private List<String> logs;
}
