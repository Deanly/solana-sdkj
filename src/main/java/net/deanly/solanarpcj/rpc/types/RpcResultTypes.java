package net.deanly.solanarpcj.rpc.types;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

public class RpcResultTypes {

    @Getter
    @ToString
    public static class ValueLong extends RpcResultObject {
        @Json(name = "value")
        private Long value;
    }

}
