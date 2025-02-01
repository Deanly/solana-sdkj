package net.deanly.solana.sdk.rpc.response;

import java.util.List;

import com.squareup.moshi.Json;
import lombok.*;

@Getter
@ToString
public class ResValueAccountInfo {

    @Json(name = "data")
    private List<String> data;

    @Json(name = "executable")
    private Boolean executable;

    @Json(name = "lamports")
    private Long lamports;

    @Json(name = "owner")
    private String owner;

    @Json(name = "rentEpoch")
    private Long rentEpoch;

}
