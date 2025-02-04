package net.deanly.solana.sdk.rpc.types;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionErrorType {
    @Json(name = "AccountInUse")
    ACCOUNT_IN_USE,

    @Json(name = "AccountLoadedTwice")
    ACCOUNT_LOADED_TWICE,

    @Json(name = "AccountNotFound")
    ACCOUNT_NOT_FOUND,

    @Json(name = "ProgramAccountNotFound")
    PROGRAM_ACCOUNT_NOT_FOUND,

    @Json(name = "InsufficientFundsForFee")
    INSUFFICIENT_FUNDS_FOR_FEE,

    @Json(name = "InstructionError")
    INSTRUCTION_ERROR,  // 복합 에러 처리 대상

    @Json(name = "AlreadyProcessed")
    ALREADY_PROCESSED,

    @Json(name = "BlockhashNotFound")
    BLOCKHASH_NOT_FOUND,

    @Json(name = "SignatureFailure")
    SIGNATURE_FAILURE,

    @Json(name = "SanitizeFailure")
    SANITIZE_FAILURE,

    @Json(name = "ClusterMaintenance")
    CLUSTER_MAINTENANCE
}