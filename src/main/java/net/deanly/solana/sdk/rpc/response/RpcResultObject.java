package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.*;

@Getter
@ToString
public class RpcResultObject<T> {
    @Getter
    @ToString
    public static class Context {
        @Json(name = "slot")
        private long slot;

        @Json(name = "apiVersion")
        private String apiVersion;
    }

    @Json(name = "context")
    private Context context;

    @Json(name = "value")
    private T value;
}
