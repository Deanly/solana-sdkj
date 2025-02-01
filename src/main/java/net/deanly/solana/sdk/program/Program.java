package net.deanly.solana.sdk.program;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstructionImpl;

import java.util.List;

@Deprecated
public abstract class Program {
    /**
     * Returns a {@link TransactionInstruction} built from the specified values.
     * @param programId Solana program we are calling
     * @param keys AccountMeta keys
     * @param data byte array sent to Solana
     * @return {@link TransactionInstruction} object containing specified values
     */
    public static TransactionInstruction createTransactionInstruction(
            PublicKey programId,
            List<AccountMeta> keys,
            byte[] data
    ) {
        return new TransactionInstructionImpl(programId, keys, data);
    }
}
