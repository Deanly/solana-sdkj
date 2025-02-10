package net.deanly.solana.sdk.rpc.client.http;

import net.deanly.solana.sdk.rpc.client.exception.RpcException;
import net.deanly.solana.sdk.rpc.response.ResValueConfirmedTransaction;
import net.deanly.solana.sdk.transaction.Transaction;

public interface SyncApi {
    ResValueConfirmedTransaction sendAndConfirmTransaction(Transaction transaction) throws RpcException;
}
