package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ResValueSupply {

    @Json(name = "total")
    private long total;

    @Json(name = "circulating")
    private long circulating;

    @Json(name = "nonCirculating")
    private long nonCirculating;

    @Json(name = "nonCirculatingAccounts")
    private List<String> nonCirculatingAccounts;
}
