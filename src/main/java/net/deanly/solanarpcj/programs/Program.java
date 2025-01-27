package net.deanly.solanarpcj.programs;

import net.deanly.solanarpcj.account.AccountMeta;
import net.deanly.solanarpcj.account.PublicKey;
import net.deanly.solanarpcj.transaction.TransactionInstruction;

import java.util.List;

/**
 * Abstract class for
 */
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
        return new TransactionInstruction(programId, keys, data);
    }
}
