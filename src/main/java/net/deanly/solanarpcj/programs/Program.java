package net.deanly.solanarpcj.programs;

import net.deanly.solanarpcj.core.AccountMeta;
import net.deanly.solanarpcj.core.PublicKey;
import net.deanly.solanarpcj.core.TransactionInstruction;

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
