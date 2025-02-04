package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.deanly.solana.sdk.rpc.types.TransactionDetails;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@lombok.experimental.SuperBuilder
public class BlockConfig extends BaseConfig {

    /// level of transaction detail to return.
    /// - If accounts are requested, transaction details only include signatures and an annotated list of accounts in each transaction.
    /// - Transaction metadata is limited to only: fee, err, pre_balances, post_balances, pre_token_balances, and post_token_balances.
    @lombok.Builder.Default
    @Json(name = "transactionDetails")
    private TransactionDetails transactionDetails = TransactionDetails.FULL;

    /// whether to populate the rewards array. If parameter not provided, the default includes rewards.
    @lombok.Builder.Default
    @Json(name = "rewards")
    private Boolean rewards = true;

    /// the max transaction version to return in responses.
    /// - If the requested block contains a transaction with a higher version, an error will be returned.
    /// - If this parameter is omitted, only legacy transactions will be returned, and a block containing any versioned transaction will prompt the error.
    @Json(name = "maxSupportedTransactionVersion")
    private Integer maxSupportedTransactionVersion = null;
}