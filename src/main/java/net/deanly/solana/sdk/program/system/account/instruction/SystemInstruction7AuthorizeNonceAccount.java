package net.deanly.solana.sdk.program.system.account.instruction;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.deanly.solana.sdk.layout.field.PublicKeyNullableField;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.system.account.SystemProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt32LEField;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.util.List;

/**
 * Represents a System program authorize nonce account instruction in the Solana blockchain.
 * This instruction reassigns the authority of a nonce account to another public key.
 *
 * <pre>
 * Fields:
 * - {@code instruction}: Constant index (7) representing the authorize nonce account instruction.
 * - {@code keys}: A list of accounts involved in the transaction. This includes the nonce account, the old authority, and the new authority.
 * - {@code newAuthority}: The public key of the new account that will have authority over the nonce account.
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
public class SystemInstruction7AuthorizeNonceAccount extends SystemProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 7; // Instruction index (7), directly defined as required

    private List<AccountMeta> keys; // Accounts used for this instruction (nonce account and authorities)

    @StructField(order = 2, type = PublicKeyNullableField.class)
    private PublicKey newAuthority; // The new authority public key for the nonce account

    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        SystemInstruction7AuthorizeNonceAccount instruction = StructLayout.decode(data, SystemInstruction7AuthorizeNonceAccount.class);
        this.keys = instruction.getKeys();
        this.newAuthority = instruction.getNewAuthority();
    }

    /**
     * Configures the accounts involved in this instruction based on the TypeScript logic.
     *
     * Accounts setup:
     * - {@code nonceAccount} - The nonce account to modify (Writable).
     * - {@code currentAuthority} - The current authority of the nonce account (Signer).
     *
     * @param nonceAccount The PublicKey of the nonce account.
     * @param currentAuthority The PublicKey of the current authority for the nonce account.
     */
    public void setKeys(PublicKey nonceAccount, PublicKey currentAuthority) {
        if (nonceAccount == null || currentAuthority == null) {
            throw new IllegalArgumentException("Both nonceAccount and currentAuthority must be provided.");
        }

        // Set accounts as defined in the TypeScript counterpart
        this.keys = List.of(
                new AccountMeta(nonceAccount, false, true), // Writable nonce account
                new AccountMeta(currentAuthority, true, false) // Signer authority account
        );
    }

    /**
     * Factory method to create and initialize an instance of this instruction.
     * Encodes all necessary fields and keys.
     *
     * @param nonceAccount The PublicKey of the nonce account to authorize.
     * @param currentAuthority The PublicKey of the current nonce authority.
     * @param newAuthority Optiona, The PublicKey to set as the new authority. If null to remove authority.
     * @return Configured instance of {@code SystemInstruction7AuthorizeNonceAccount}.
     */
    public static SystemInstruction7AuthorizeNonceAccount create(PublicKey nonceAccount, PublicKey currentAuthority, PublicKey newAuthority) {
        if (nonceAccount == null || currentAuthority == null) {
            throw new IllegalArgumentException("All PublicKey parameters must be provided.");
        }

        SystemInstruction7AuthorizeNonceAccount instruction = new SystemInstruction7AuthorizeNonceAccount();
        instruction.setNewAuthority(newAuthority); // Set new authority
        instruction.setKeys(nonceAccount, currentAuthority); // Set necessary accounts
        return instruction;
    }
}