package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.deanly.solana.sdk.types.Commitment;
import net.deanly.solana.sdk.types.Encoding;
import net.deanly.solana.sdk.types.TransactionDetails;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@lombok.experimental.SuperBuilder
public class BlockConfig2 {

    /// `processed` is not supported.
    @Json(name = "commitment")
    private Commitment commitment;

    /// encoding format for each returned Transaction
    /// - Values: `json` `jsonParsed` `base58` `base64`
    /// - jsonParsed attempts to use program-specific instruction parsers to return more human-readable and explicit data in the transaction.message.instructions list.
    /// - If jsonParsed is requested but a parser cannot be found, the instruction falls back to regular JSON encoding (accounts, data, and programIdIndex fields).
    @Json(name = "encoding")
    private Encoding encoding;

    /// level of transaction detail to return.
    /// - If accounts are requested, transaction details only include signatures and an annotated list of accounts in each transaction.
    /// - Transaction metadata is limited to only: fee, err, pre_balances, post_balances, pre_token_balances, and post_token_balances.
    @Json(name = "transactionDetails")
    private TransactionDetails transactionDetails;

    /// the max transaction version to return in responses.
    /// - If the requested block contains a transaction with a higher version, an error will be returned.
    /// - If this parameter is omitted, only legacy transactions will be returned, and a block containing any versioned transaction will prompt the error.
    @Json(name = "maxSupportedTransactionVersion")
    private Integer maxSupportedTransactionVersion;

    /// whether to populate the rewards array. If parameter not provided, the default includes rewards.
    @Json(name = "showRewards")
    private Boolean showRewards;
}