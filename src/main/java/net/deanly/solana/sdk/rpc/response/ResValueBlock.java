package net.deanly.solana.sdk.rpc.response;

import net.deanly.solana.sdk.types.guava.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.solana.sdk.types.Signature;

import java.util.List;

@Getter
@ToString
public class ResValueBlock {
    /// this block, as base-58 encoded string
    @Json(name = "blockhash")
    private String blockhash;

    /// the blockhash of this block's parent, as base-58 encoded string; if the parent block is not available due to ledger cleanup, this field will return "11111111111111111111111111111111"
    @Json(name = "previousBlockhash")
    private String previousBlockhash;

    /// the slot index of this block's parent
    @Json(name = "parentSlot")
    private UnsignedLong parentSlot;

    /// present if "full" transaction details are requested; an array of JSON objects containing:
    @Json(name = "transactions")
    private List<ResValueConfirmedTransaction> transactions;

    /// present if "signatures" are requested for transaction details; an array of signatures strings, corresponding to the transaction order in the block
    @Json(name = "signatures")
    private List<Signature> signatures;

    /// block-level rewards, present if rewards are requested; an array of JSON objects containing:
    @Json(name = "rewards")
    private List<ResValueReward> rewards;

    /// estimated production time, as Unix timestamp (seconds since the Unix epoch). null if not available
    @Json(name = "blockTime")
    private Long blockTime;

    /// the number of blocks beneath this block
    @Json(name = "blockHeight")
    private UnsignedLong blockHeight;
}
