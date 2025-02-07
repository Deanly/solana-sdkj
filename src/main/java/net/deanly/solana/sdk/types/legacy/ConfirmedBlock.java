package net.deanly.solana.sdk.types.legacy;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.solana.sdk.rpc.response.ResValueConfirmedTransaction;

import java.util.List;

@Getter
@ToString
public class ConfirmedBlock {

    @Json(name = "blockTime")
    private int blockTime;

    @Json(name = "blockhash")
    private String blockhash;

    @Json(name = "parentSlot")
    private int parentSlot;

    @Json(name = "previousBlockhash")
    private String previousBlockhash;

    @Json(name = "transactions")
    private List<ResValueConfirmedTransaction> transactions;
}
