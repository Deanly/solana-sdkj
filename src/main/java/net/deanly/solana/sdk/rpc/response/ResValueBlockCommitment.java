package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import java.util.List;

@Getter
@ToString
public class ResValueBlockCommitment {

    @Json(name = "commitment")
    private List<Long> commitment; // Commitment array or null if the block is unknown.

    @Json(name = "totalStake")
    private Long totalStake; // Total active stake in lamports for the current epoch.
}