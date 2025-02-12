package net.deanly.solana.sdk.program.core.system.instruction;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.field.PublicKeyField;
import net.deanly.solana.sdk.layout.field.RustStringField;
import net.deanly.solana.sdk.program.core.system.SystemProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt64LEField;
import net.deanly.structlayout.type.basic.UInt32LEField;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.util.List;

/**
 * Represents a System program TransferWithSeed instruction in the Solana blockchain.
 * This instruction transfers lamports from a derived account using a base public key and a seed string.
 *
 * Fields:
 * - {@code instruction}: Constant index (11) representing the TransferWithSeed instruction.
 * - {@code keys}: A list of accounts involved in the transaction.
 * - {@code lamports}: The amount of lamports to transfer.
 * - {@code seed}: Seed string used to derive the source account.
 * - {@code base}: Base public key used for deriving the source account.
 * - {@code programId}: Program ID specifying the account owning authority.
 *
 * Methods:
 * - {@code getData()}: Encodes instruction fields to byte array.
 * - {@code setData(byte[] data)}: Decodes the byte array to populate this instruction's attributes.
 */

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemInstruction11TransferWithSeed extends SysInstructionBase implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 11; // Instruction index (11) for TransferWithSeed

    @StructField(order = 2, type = UInt64LEField.class)
    private long lamports; // Amount of lamports to transfer (u64 format)

    @StructField(order = 3, type = RustStringField.class)
    private String seed; // Seed string for deriving the source account

    @StructField(order = 4, type = PublicKeyField.class)
    private PublicKey programId; // Program ID to which the derived source account is assigned


    private List<AccountMeta> keys; // Accounts involved in the transaction (source, destination)


    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    /**
     * Configures the keys required for this instruction.
     * This method must exactly reflect Typescript's key ordering and attributes.
     */
    public void setKeys(PublicKey fromPubkey, PublicKey basePubkey, PublicKey toPubkey) {
        this.keys = List.of(
                new AccountMeta(fromPubkey, false, true),   // Sender (Writable, Non-Signer)
                new AccountMeta(basePubkey, true, false), // Base (Read-only, Signer)
                new AccountMeta(toPubkey, false, true)    // Receiver (Writable, Non-signer)
        );
    }

    public void setData(byte[] data) {
        SystemInstruction11TransferWithSeed instruction = StructLayout.decode(data, SystemInstruction11TransferWithSeed.class);
        this.keys = instruction.getKeys();
        this.lamports = instruction.getLamports();
        this.seed = instruction.getSeed();
        this.programId = instruction.getProgramId();
    }


    /**
     * Fully initializes the instruction with all fields and returns its instance.
     * @param fromPubkey Key for the funding account
     * @param basePubkey Base key for deriving the funding account
     * @param toPubkey Key for the receiving account
     * @param lamports Amount of lamports to transfer
     * @param seed Seed string for deriving the source account
     * @param programId ID of the program owning the derived account
     * @return An initialized SystemInstruction11TransferWithSeed instance
     */
    public static SystemInstruction11TransferWithSeed create(
            PublicKey fromPubkey,
            PublicKey basePubkey,
            PublicKey toPubkey,
            long lamports,
            String seed,
            PublicKey programId
    ) {
        // Validation - ensure all parameters are valid
        if (fromPubkey == null || basePubkey == null || toPubkey == null || programId == null) {
            throw new IllegalArgumentException("Public keys cannot be null");
        }
        if (lamports <= 0) {
            throw new IllegalArgumentException("Lamports must be greater than zero");
        }
        if (seed == null || seed.isEmpty()) {
            throw new IllegalArgumentException("Seed cannot be null or empty");
        }

        // Create and initialize the instruction
        SystemInstruction11TransferWithSeed instruction = new SystemInstruction11TransferWithSeed();
        instruction.setKeys(fromPubkey, basePubkey, toPubkey);
        instruction.setLamports(lamports);
        instruction.setSeed(seed);
        instruction.setProgramId(programId);
        return instruction;
    }
}