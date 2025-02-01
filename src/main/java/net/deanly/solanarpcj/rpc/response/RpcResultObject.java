package net.deanly.solanarpcj.rpc.response;

import com.squareup.moshi.Json;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder(builderClassName = "Builder")
public class RpcResultObject<T> {

    @Getter
    @ToString
    public static class Context {
        @Json(name = "slot")
        private long slot;
    }

    @Json(name = "context")
    protected Context context;

    @Json(name = "value")
    T value;
}
