package net.deanly.solana.sdk.program.system.account.instruction;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.program.system.account.SystemProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.Int64LEField;
import net.deanly.structlayout.type.basic.UInt32LEField;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.util.List;

/**
 * Represents a System program transfer instruction in the Solana blockchain.
 * This instruction is used to transfer lamports (smallest unit of SOL) between accounts.
 *
 * Fields:
 * - {@code instruction}: Constant index (2) representing the transfer instruction in the program.
 * - {@code keys}: A list of accounts involved in the transaction. This includes the sender and receiver accounts.
 * - {@code lamports}: The amount of lamports to transfer between accounts.
 *
 * Methods:
 * - {@code getData()}: Encodes the instruction fields into a byte array (serialized structure).
 * - {@code setData(byte[] data)}: Decodes the given byte array to populate this instruction's fields.
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemInstruction2Transfer extends SystemProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 2; // Instruction index (2)

    private List<AccountMeta> keys; // Accounts used for this instruction (sender & receiver)

    @StructField(order = 2, type = Int64LEField.class)
    private long lamports; // Number of lamports to transfer

    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    /**
     * Sets the keys for this instruction.
     *
     * @param fromAccount PublicKey of the account transferring lamports (signer, writable).
     * @param toAccount   PublicKey of the account receiving lamports (writable).
     */
    public void setKeys(PublicKey fromAccount, PublicKey toAccount) {
        if (fromAccount == null || toAccount == null) {
            throw new IllegalArgumentException("fromAccount and toAccount cannot be null.");
        }

        // Configure account metadata based on Typescript
        this.keys = List.of(
                new AccountMeta(fromAccount, true, true), // sender (signer, writable)
                new AccountMeta(toAccount, false, true)  // receiver (non-signer, writable)
        );
    }

    /**
     * Static factory method to create a new `SystemInstruction2Transfer` instance.
     *
     * @param fromAccount PublicKey of the sender account.
     * @param toAccount   PublicKey of the receiver account.
     * @param lamports    Amount of lamports to transfer.
     * @return An initialized `SystemInstruction2Transfer` object.
     */
    public static SystemInstruction2Transfer create(PublicKey fromAccount, PublicKey toAccount, long lamports) {
        if (lamports <= 0) {
            throw new IllegalArgumentException("Lamports must be greater than zero.");
        }

        SystemInstruction2Transfer instruction = new SystemInstruction2Transfer();
        instruction.setKeys(fromAccount, toAccount);
        instruction.setLamports(lamports);
        return instruction;
    }

    public void setData(byte[] data) {
        SystemInstruction2Transfer instruction = StructLayout.decode(data, SystemInstruction2Transfer.class);
        this.keys = instruction.getKeys();
        this.lamports = instruction.getLamports();
    }
}