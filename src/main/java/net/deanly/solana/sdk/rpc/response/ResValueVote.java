package net.deanly.solana.sdk.rpc.response;

import net.deanly.solana.sdk.types.guava.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import java.util.List;

@Getter
@ToString
public class ResValueVote {

    @Json(name = "hash")
    private String hash;

    @Json(name = "slots")
    private List<UnsignedLong> slots;

    @Json(name = "timestamp")
    private Long timestamp;

    @Json(name = "signature")
    private String signature;

    @Json(name = "votePubkey")
    private String votePubkey;
}