package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString
@NoArgsConstructor
@lombok.AllArgsConstructor
@lombok.Builder(builderClassName = "Builder")
public class RpcResponse<T> {

    @Getter
    @ToString
    public static class Error {
        @Json(name = "code")
        private long code;

        @Json(name = "message")
        private String message;

        @Json(name = "data")
        private Map<String, Object> data;
    }

    @Json(name = "jsonrpc")
    private String jsonrpc;

    @Json(name = "result")
    private T result;

    @Json(name = "error")
    private Error error;

    @Json(name = "id")
    private Long id;

}
