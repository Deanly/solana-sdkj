package net.deanly.solana.sdk.program.core.system.instruction;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.field.PublicKeyField;
import net.deanly.solana.sdk.program.core.system.SystemProgram;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.Int64LEField;
import net.deanly.structlayout.type.basic.UInt32LEField;

import java.util.List;

/**
 * Represents a System program create account instruction in the Solana blockchain.
 * This instruction is used to create a new account on the Solana network with
 * allocated lamports, byte space, and a specified owner program ID.
 *
 * This class extends the {@code Base} class, providing a program ID and encoding
 * features, and implements the {@code TransactionInstruction} interface, defining
 * how the instruction data and associated account metadata are handled.
 *
 * Fields:
 * - {@code instruction}: Fixed value representing the index for the create account instruction.
 * - {@code keys}: A list of accounts involved in the transaction. This includes the funding account,
 *   the account being created, and any system program accounts.
 * - {@code lamports}: The amount of lamports (Solana's smallest unit) to transfer to the new account.
 * - {@code space}: The amount of space (bytes) to allocate for the new account.
 * - {@code newProgramId}: Specifies the owner program ID for the newly created account.
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
public class SystemInstruction0Create extends SystemProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 0; // Instruction index (0)

    private List<AccountMeta> keys; // Accounts used for this instruction

    @StructField(order = 2, type = Int64LEField.class)
    private long lamports; // Number of lamports to transfer

    @StructField(order = 3, type = Int64LEField.class)
    private long space; // Number of bytes to allocate to the new account

    @StructField(order = 4, type = PublicKeyField.class)
    private PublicKey newProgramId; // The owner program ID for the new account

    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        SystemInstruction0Create instruction = StructLayout.decode(data, SystemInstruction0Create.class);
        this.keys = instruction.getKeys();
        this.lamports = instruction.getLamports();
        this.space = instruction.getSpace();
        this.newProgramId = instruction.getNewProgramId();
    }

    /**
     * Sets the keys for this instruction. This strictly follows the Typescript configuration.
     *
     * @param fundingAccount The funding account (Writable, Signer)
     * @param newAccount     The new account being created (Writable, Signer)
     */
    public void setKeys(PublicKey fundingAccount, PublicKey newAccount) {
        // Validate inputs
        if (fundingAccount == null || newAccount == null) {
            throw new IllegalArgumentException("Public keys cannot be null.");
        }

        // Set account metadata based on Typescript specifications
        this.keys = List.of(
                new AccountMeta(fundingAccount, true, true), // Writable, Signer
                new AccountMeta(newAccount, true, true) // Writable, Signer
        );
    }

    /**
     * Static factory method for creating an instance of SystemInstruction0Create.
     *
     * @param fundingAccount The funding account (Writable, Signer)
     * @param newAccount     The new account being created (Writable, Signer)
     * @param lamports       The amount of lamports to transfer
     * @param space          The amount of space to allocate
     * @param newProgramId   The owner program ID for the new account
     * @return A fully initialized instance of SystemInstruction0Create
     */
    public static SystemInstruction0Create create(
            PublicKey fundingAccount,
            PublicKey newAccount,
            long lamports,
            long space,
            PublicKey newProgramId
    ) {
        // Validation for inputs
        if (fundingAccount == null || newAccount == null || newProgramId == null) {
            throw new IllegalArgumentException("Public keys cannot be null.");
        }
        if (lamports < 0 || space < 0) {
            throw new IllegalArgumentException("Lamports and space must be non-negative.");
        }

        // Create the instruction instance
        SystemInstruction0Create instruction = new SystemInstruction0Create();
        instruction.setKeys(fundingAccount, newAccount);
        instruction.setLamports(lamports);
        instruction.setSpace(space);
        instruction.setNewProgramId(newProgramId);
        return instruction;
    }
}