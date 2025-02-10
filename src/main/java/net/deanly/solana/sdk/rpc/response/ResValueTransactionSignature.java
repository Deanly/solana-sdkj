package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.deanly.solana.sdk.types.guava.UnsignedLong;
import net.deanly.solana.sdk.types.Commitment;
import net.deanly.solana.sdk.types.Signature;
import net.deanly.solana.sdk.types.TransactionError;

@Getter
@ToString
@Builder
public class ResValueTransactionSignature {

    @Json(name = "signature")
    private Signature signature; // Transaction signature as a base-58 encoded string.

    @Json(name = "slot")
    private UnsignedLong slot; // The slot that contains the block with the transaction.

    @Json(name = "err")
    private TransactionError err; // Error if transaction failed, null if transaction succeeded.

    @Json(name = "memo")
    private String memo; // Memo associated with the transaction, null if no memo is present.

    @Json(name = "blockTime")
    private Long blockTime; // Estimated production time, as Unix timestamp (seconds since the Unix epoch) of when the transaction was processed. Null if not available.

    @Json(name = "confirmationStatus")
    private Commitment confirmationStatus; // The transaction's cluster confirmation status; either 'processed', 'confirmed', or 'finalized'.
}