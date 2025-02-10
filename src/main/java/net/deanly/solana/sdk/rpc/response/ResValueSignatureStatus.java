package net.deanly.solana.sdk.rpc.response;

import net.deanly.solana.sdk.types.guava.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.solana.sdk.types.Commitment;
import net.deanly.solana.sdk.types.TransactionError;

@Getter
@ToString
public class ResValueSignatureStatus {

    @Json(name = "slot")
    private UnsignedLong slot; // The slot the transaction was processed in.

    @Json(name = "confirmations")
    private Integer confirmations; // Number of blocks since signature confirmation, null if rooted and finalized by a supermajority of the cluster.

    @Json(name = "err")
    private TransactionError err; // Error if the transaction failed, null if the transaction succeeded.

    @Json(name = "confirmationStatus")
    private Commitment confirmationStatus; // The transaction's cluster confirmation status; either 'processed', 'confirmed', or 'finalized'.
}