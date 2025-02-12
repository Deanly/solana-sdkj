package net.deanly.solana.sdk.program.core.system.instruction;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.core.system.SystemProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt32LEField;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.util.List;

/**
 * Represents a System program UpgradeNonceAccount instruction in the Solana blockchain.
 * This instruction upgrades a nonce account for specific system purposes.
 * <a href="https://github.com/solana-labs/solana/issues/25787">issue</a>
 *
 * Fields:
 * - {@code instruction}: Constant index (12) representing the UpgradeNonceAccount instruction.
 * - {@code keys}: A list of accounts involved in the transaction.
 *
 * Methods:
 * - {@code getData()}: Encodes the instruction fields to a byte array.
 * - {@code setData(byte[] data)}: Decodes the given byte array to populate this instruction's attributes.
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemInstruction12UpgradeNonceAccount extends SysInstructionBase implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 12; // Instruction index (12) for UpgradeNonceAccount

    private List<AccountMeta> keys; // Accounts involved in the transaction (Nonce Account and Authority)

    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        SystemInstruction12UpgradeNonceAccount instruction = StructLayout.decode(data, SystemInstruction12UpgradeNonceAccount.class);
        this.keys = instruction.getKeys();
    }

    /**
     * Sets the keys for this instruction. This must strictly follow the Typescript configuration.
     *
     * @param nonceAccount      The nonce account public key.
     */
    public void setKeys(PublicKey nonceAccount) {
        // Ensure all parameters are non-null
        if (nonceAccount == null) {
            throw new IllegalArgumentException("PublicKeys cannot be null.");
        }

        this.keys = List.of(
                new AccountMeta(nonceAccount, false, true) // Nonce account (Writable, Non-signer)
        );
    }

    /**
     * A static factory method to create an instance of SystemInstruction12UpgradeNonceAccount
     * with all required fields.
     *
     * @param nonceAccount      The nonce account public key.
     * @return A new and fully initialized instance.
     */
    public static SystemInstruction12UpgradeNonceAccount create(
            PublicKey nonceAccount
    ) {
        // Validation
        if (nonceAccount == null) {
            throw new IllegalArgumentException("PublicKeys cannot be null.");
        }

        // Create the instruction instance
        SystemInstruction12UpgradeNonceAccount instruction = new SystemInstruction12UpgradeNonceAccount();
        instruction.setKeys(nonceAccount);
        return instruction;
    }
}