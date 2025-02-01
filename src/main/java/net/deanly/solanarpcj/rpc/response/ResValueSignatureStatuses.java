package net.deanly.solanarpcj.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueSignatureStatuses {
    @Json(name = "slot")
    private long slot;

    @Json(name = "confirmations")
    private Long confirmations;

    @Json(name = "confirmationStatus")
    private String confirmationStatus;
}
