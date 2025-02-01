package net.deanly.solanarpcj.program.system.account.instruction;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.system.Sysvar;
import net.deanly.solanarpcj.program.system.account.SystemProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt32LEField;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;

import java.util.List;

/**
 * Represents a System program advance nonce account instruction in the Solana blockchain.
 * This instruction advances the nonce of a nonce account to the next value.
 *
 * Fields:
 * - {@code instruction}: Constant index (4) representing the advance nonce instruction.
 * - {@code keys}: A list of accounts involved in the transaction. This includes the nonce account and required authorities.
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
public class SystemInstruction4AdvanceNonceAccount extends SystemProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 4; // Instruction index (4), directly defined as required

    private List<AccountMeta> keys; // Accounts used for this instruction (nonce account and authorities)

    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        SystemInstruction4AdvanceNonceAccount instruction = StructLayout.decode(data, SystemInstruction4AdvanceNonceAccount.class);
        this.keys = instruction.getKeys();
    }

    /**
     * Sets the required accounts (keys) for the Advance Nonce Account instruction.
     * According to the instruction structure, we need three accounts:
     *  1. The nonce account (writable, not signer)
     *  2. The authority account (signer, not writable)
     *  3. Recent blockhashes sysvar (not writable, not signer)
     *
     * @param nonceAccount PublicKey of the nonce account
     * @param authorityAccount PublicKey of the authority account
     */
    public void setKeys(PublicKey nonceAccount, PublicKey authorityAccount) {
        if (nonceAccount == null || authorityAccount == null) {
            throw new IllegalArgumentException("NonceAccount and AuthorityAccount cannot be null.");
        }

        this.keys = List.of(
                new AccountMeta(nonceAccount, false, true), // Nonce account: writable, non-signer
                new AccountMeta(Sysvar.SYSVAR_RECENT_BLOCKHASHES_ADDRESS, false, false), // Recent blockhash sysvar
                new AccountMeta(authorityAccount, true, false) // Authority account: signer, not writable
        );
    }

    /**
     * Factory method to create a new `SystemInstruction4AdvanceNonceAccount` instance.
     *
     * @param nonceAccount PublicKey of the nonce account
     * @param authorityAccount PublicKey of the authority account
     * @return A fully initialized instance of `SystemInstruction4AdvanceNonceAccount`
     */
    public static SystemInstruction4AdvanceNonceAccount create(PublicKey nonceAccount, PublicKey authorityAccount) {
        if (nonceAccount == null || authorityAccount == null) {
            throw new IllegalArgumentException("NonceAccount and AuthorityAccount cannot be null.");
        }

        // Create the instruction instance
        SystemInstruction4AdvanceNonceAccount instruction = new SystemInstruction4AdvanceNonceAccount();

        // Set the keys using the setter method
        instruction.setKeys(nonceAccount, authorityAccount);

        return instruction;
    }
}