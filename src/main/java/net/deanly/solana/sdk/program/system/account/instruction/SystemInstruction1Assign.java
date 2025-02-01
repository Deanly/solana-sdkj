package net.deanly.solana.sdk.program.system.account.instruction;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.field.PublicKeyField;
import net.deanly.solana.sdk.program.system.account.SystemProgram;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt32LEField;

import java.util.List;

/**
 * Represents a System program assign instruction in the Solana blockchain.
 * This instruction assigns a new program ID as the owner of a specified account.
 *
 * Fields:
 * - {@code instruction}: Constant index (1) representing the assign instruction in the program.
 * - {@code keys}: A list of accounts involved in the transaction. This includes the account being assigned a new owner.
 * - {@code programId}: The public key of the new program to be assigned as the owner of the account.
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
public class SystemInstruction1Assign extends SystemProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 1; // Instruction index (1)

    private List<AccountMeta> keys; // Accounts used for this instruction

    @StructField(order = 2, type = PublicKeyField.class)
    private PublicKey ownerProgramId; // The new owner program ID for the account

    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        SystemInstruction1Assign instruction = StructLayout.decode(data, SystemInstruction1Assign.class);
        this.keys = instruction.getKeys();
        this.ownerProgramId = instruction.getProgramId();
    }

    /**
     * Sets the keys for this instruction based on Typescript standards.
     *
     * @param owner The public key of the account to be assigned a new program ID.
     */
    public void setKeys(PublicKey owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner public key cannot be null.");
        }

        // Setting account meta based on Typescript configuration
        this.keys = List.of(
                new AccountMeta(owner, true, true) // Writable=true, Signer=true
        );
    }

    /**
     * Static factory method to create a new `SystemInstruction1Assign` instance.
     *
     * @param owner     The public key of the account to assign a new owner.
     * @param ownerProgramId The public key of the program to assign as the owner.
     * @return A fully initialized `SystemInstruction1Assign` object.
     */
    public static SystemInstruction1Assign create(PublicKey owner, PublicKey ownerProgramId) {
        if (owner == null || ownerProgramId == null) {
            throw new IllegalArgumentException("Public key parameters cannot be null.");
        }

        // Creating a new instruction instance
        SystemInstruction1Assign instruction = new SystemInstruction1Assign();
        instruction.setKeys(owner);
        instruction.setOwnerProgramId(ownerProgramId);
        return instruction;
    }

}