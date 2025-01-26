package net.deanly.solanarpcj.rpc.types;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RpcResultObject {

    @Getter
    @ToString
    public static class Context {
        @Json(name = "slot")
        private long slot;
    }

    @Json(name = "context")
    protected Context context;
}
