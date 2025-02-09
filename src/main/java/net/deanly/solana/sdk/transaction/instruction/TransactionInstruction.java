package net.deanly.solana.sdk.transaction.instruction;

import net.deanly.solana.sdk.crypto.PublicKey;

import java.util.*;

/**
 * Represents an instruction to be executed by a Solana program.
 */
public interface TransactionInstruction {
    PublicKey getProgramId();
    List<AccountMeta> getKeys();
    byte[] getData();
}
