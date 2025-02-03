package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RpcNotification<T> {
    @Json(name = "jsonrpc")
    private String jsonrpc;

    @Json(name = "method")
    private String method;

    @Json(name = "params")
    private Params<T> params;

    @Getter
    @ToString
    public static class Params<T> {
        @Json(name = "result")
        private T result;

        @Json(name = "subscription")
        private Long subscription;
    }

}
