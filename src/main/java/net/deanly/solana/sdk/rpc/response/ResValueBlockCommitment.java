package net.deanly.solana.sdk.rpc.response;

import net.deanly.structlayout.type.guava.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import java.util.List;

@Getter
@ToString
public class ResValueBlockCommitment {

    @Json(name = "commitment")
    private List<UnsignedLong> commitment; // Commitment array or null if the block is unknown.

    @Json(name = "totalStake")
    private UnsignedLong totalStake; // Total active stake in lamports for the current epoch.
}