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
 * Represents a System program allocate instruction in the Solana blockchain.
 * This instruction allocates space for an account on-chain without transferring lamports.
 *
 * <pre>
 * Fields:
 * - {@code instruction}: Constant index (8) representing the allocate instruction.
 * - {@code keys}: A list of accounts involved in the transaction.
 * - {@code space}: The amount of space (in bytes) to allocate for the account.
 *
 * Methods:
 * - {@code getData()}: Encodes the instruction fields into a byte array.
 * - {@code setData(byte[] data)}: Decodes the given byte array to populate this instruction's attributes.
 * </pre>
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemInstruction8Allocate extends SystemProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 8; // Instruction index (8), directly defined as required

    @StructField(order = 2, type = Int64LEField.class)
    private long space; // The amount of space to allocate in bytes for the account

    private List<AccountMeta> keys; // Accounts used for this instruction (target account)

    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        SystemInstruction8Allocate instruction = StructLayout.decode(data, SystemInstruction8Allocate.class);
        this.keys = instruction.getKeys();
        this.space = instruction.getSpace();
    }

    /**
     * Configures the accounts involved in this instruction.
     * TypeScript logic precisely translated: prepares the required accounts and ensures valid ordering of isSigner and isWritable flags.
     *
     * @param accountPubkey PublicKey of the account to allocate space for.
     */
    public void setKeys(PublicKey accountPubkey) {
        if (accountPubkey == null) {
            throw new IllegalArgumentException("accountPubkey must not be null.");
        }

        // Configure keys with appropriate signer and writable flags
        this.keys = List.of(
                new AccountMeta(accountPubkey, true, true) // Signer + Writable = Account request with Allocate
        );
    }

    /**
     * Factory method to create an instance of this instruction.
     *
     * @param accountPubkey PublicKey of the account to allocate space for.
     * @param space Amount of space (in bytes) to allocate.
     * @return Configured instance of {@code SystemInstruction8Allocate}.
     */
    public static SystemInstruction8Allocate create(PublicKey accountPubkey, long space) {
        if (accountPubkey == null || space <= 0) {
            throw new IllegalArgumentException("accountPubkey must not be null, and space must be greater than zero.");
        }

        SystemInstruction8Allocate instruction = new SystemInstruction8Allocate();
        instruction.setSpace(space); // Set the space to allocate
        instruction.setKeys(accountPubkey); // Set the required keys
        return instruction;
    }
}