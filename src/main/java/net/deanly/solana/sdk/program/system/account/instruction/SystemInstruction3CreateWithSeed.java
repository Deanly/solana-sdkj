package net.deanly.solana.sdk.program.system.account.instruction;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.field.PublicKeyField;
import net.deanly.solana.sdk.layout.field.RustStringField;
import net.deanly.solana.sdk.program.system.account.SystemProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.Int64LEField;
import net.deanly.structlayout.type.basic.UInt32LEField;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a System program create with seed instruction in the Solana blockchain.
 * This instruction creates a new account derived from a base public key and seed.
 *
 * Fields:
 * - {@code instruction}: Constant index (3) representing the create with seed instruction.
 * - {@code keys}: A list of accounts involved in the transaction. This includes the funding account,
 *   the new account, and any system program accounts.
 * - {@code base}: The base public key used to derive the new account's address.
 * - {@code seed}: A string seed used in combination with the base public key to derive the new account address.
 * - {@code lamports}: The amount of lamports to transfer to the new account.
 * - {@code space}: The amount of space (bytes) to allocate for the new account.
 * - {@code programId}: The public key of the program to assign as the owner of the new account.
 *
 * Methods:
 * - {@code getData()}: Encodes the instruction fields into a byte array that represents the serialized
 *   data for this instruction.
 * - {@code setData(byte[] data)}: Decodes the given byte array to populate this instruction's attributes.
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemInstruction3CreateWithSeed extends SystemProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 3; // Instruction index (3), directly defined as required

    @StructField(order = 2, type = PublicKeyField.class)
    private PublicKey base; // Base public key for deriving a new account

    @StructField(order = 3, type = RustStringField.class)
    private String seed; // Seed string for deriving the new account address

    @StructField(order = 4, type = Int64LEField.class)
    private long lamports; // Number of lamports to transfer

    @StructField(order = 5, type = Int64LEField.class)
    private long space; // Number of bytes to allocate to the new account

    @StructField(order = 6, type = PublicKeyField.class)
    private PublicKey programId; // Owner program ID for the new account


    private List<AccountMeta> keys; // Accounts used for this instruction


    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        SystemInstruction3CreateWithSeed instruction = StructLayout.decode(data, SystemInstruction3CreateWithSeed.class);
        this.keys = instruction.getKeys();
        this.base = instruction.getBase();
        this.seed = instruction.getSeed();
        this.lamports = instruction.getLamports();
        this.space = instruction.getSpace();
        this.programId = instruction.getProgramId();
    }

    /**
     * Sets the keys required for this instruction.
     *
     * @param fromAccount The from-account for the new account (signer, writable).
     * @param newAccount     The new account being created (writable, not signer).
     * @param baseAccount    Opitonal, The base public key for deriving the account (signer, not writable in this case).
     */
    public void setKeys(PublicKey fromAccount, PublicKey newAccount, PublicKey baseAccount) {
        if (fromAccount == null || newAccount == null) {
            throw new IllegalArgumentException("All accounts must be non-null.");
        }
        this.keys = new ArrayList<>();
        this.keys.add(new AccountMeta(fromAccount, true, true));  // from account, signer, writable
        this.keys.add(new AccountMeta(newAccount, false, false));    // New account, writable, not a signer

        if (!fromAccount.equals(baseAccount)) {
            this.keys.add(AccountMeta.roleReadOnlySigner(baseAccount));
        }
    }

    /**
     * Factory method to create a new `SystemInstruction3CreateWithSeed` instance.
     *
     * @param fromAccount   Account for the new account
     * @param newAccount    The new account being created
     * @param baseAccount    Base account for deriving the new account.
     * @param seed           Seed for deriving the new account.
     * @param lamports       Lamports to transfer to the new account.
     * @param space          Space to allocate to the new account.
     * @param programId      Program ID for the new account owner.
     * @return Initialized `SystemInstruction3CreateWithSeed` instance.
     */
    public static SystemInstruction3CreateWithSeed create(
            PublicKey fromAccount,
            PublicKey newAccount,
            PublicKey baseAccount,
            String seed,
            long lamports,
            long space,
            PublicKey programId
    ) {
        if (fromAccount == null || newAccount == null) {
            throw new IllegalArgumentException("All accounts must be non-null.");
        }
        if (seed == null || seed.isEmpty()) {
            throw new IllegalArgumentException("Seed must be provided and non-empty.");
        }
        if (lamports <= 0) {
            throw new IllegalArgumentException("Lamports must be greater than zero.");
        }
        if (space <= 0) {
            throw new IllegalArgumentException("Space must be greater than zero.");
        }

        SystemInstruction3CreateWithSeed instruction = new SystemInstruction3CreateWithSeed();
        instruction.setKeys(fromAccount, newAccount, baseAccount);
        instruction.setBase(baseAccount);
        instruction.setSeed(seed);
        instruction.setLamports(lamports);
        instruction.setSpace(space);
        instruction.setProgramId(programId);
        return instruction;
    }
}