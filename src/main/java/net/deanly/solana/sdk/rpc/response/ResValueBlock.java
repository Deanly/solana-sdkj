package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ResValueBlock {

    @Json(name = "blockTime")
    private int blockTime;

    @Json(name = "blockHeight")
    private int blockHeight;

    @Json(name = "blockhash")
    private String blockhash;

    @Json(name = "parentSlot")
    private int parentSlot;

    @Json(name = "previousBlockhash")
    private String previousBlockhash;

    @Json(name = "transactions")
    private List<ResValueConfirmedTransaction> transactions;

    @Json(name = "rewards")
    private List<ResValueReward> rewards;
}
