package net.deanly.solana.sdk.rpc.response;

import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.*;

@Getter
@ToString
public class RpcResultObject<T> {
    @Getter
    @ToString
    public static class Context {
        @Json(name = "slot")
        private UnsignedLong slot;

        @Json(name = "apiVersion")
        private String apiVersion;
    }

    @Json(name = "context")
    private Context context;

    @Json(name = "value")
    private T value;
}
