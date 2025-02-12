package net.deanly.solana.sdk.transaction.instruction;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.rpc.client.Network;

import java.util.*;

/**
 * Represents an instruction to be executed by a Solana program.
 */
public interface TransactionInstruction {
    PublicKey getProgramId(Network network);
    PublicKey getProgramId();
    List<AccountMeta> getKeys();
    byte[] getData();
}
