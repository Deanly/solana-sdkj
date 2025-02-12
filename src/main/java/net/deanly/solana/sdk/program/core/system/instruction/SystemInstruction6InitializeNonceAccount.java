package net.deanly.solana.sdk.program.core.system.instruction;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.core.Sysvar;
import net.deanly.solana.sdk.layout.field.PublicKeyField;
import net.deanly.solana.sdk.program.core.system.SystemProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt32LEField;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.util.List;

/**
 * Represents a System program initialize nonce account instruction in the Solana blockchain.
 * This instruction initializes a nonce account, setting it up with an authority for future operations.
 *
 * Fields:
 * - {@code instruction}: Constant index (6) representing the initialize nonce account instruction.
 * - {@code keys}: A list of accounts involved in the transaction. This includes the nonce account and required authorities.
 * - {@code authority}: The public key of the account that will have authority over the nonce account.
 *
 * Methods:
 * - {@code getData()}: Encodes the instruction fields into a byte array.
 * - {@code setData(byte[] data)}: Decodes the given byte array to populate this instruction's attributes.
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemInstruction6InitializeNonceAccount extends SysInstructionBase implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 6; // Instruction index (6), directly defined as required

    private List<AccountMeta> keys; // Accounts used for this instruction (nonce account and authorities)

    @StructField(order = 2, type = PublicKeyField.class)
    private PublicKey authority; // Authority public key to manage the nonce account

    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        SystemInstruction6InitializeNonceAccount instruction = StructLayout.decode(data, SystemInstruction6InitializeNonceAccount.class);
        this.keys = instruction.getKeys();
        this.authority = instruction.getAuthority();
    }

    /**
     * Configures the accounts involved in this instruction.
     *
     * @param nonceAccount    The nonce account (must be writable).
     */
    public void setKeys(PublicKey nonceAccount) {
        if (nonceAccount == null) {
            throw new IllegalArgumentException("Both nonceAccount must be provided.");
        }

        this.keys = List.of(
                new AccountMeta(nonceAccount, false, true), // Nonce account: Writable, not signer
                new AccountMeta(Sysvar.SYSVAR_RECENT_BLOCKHASHES_ADDRESS, false, false), // Recent blockhashes sysvar
                new AccountMeta(Sysvar.SYSVAR_RENT_ADDRESS, false, false) // Rent sysvar
        );
    }

    /**
     * Factory method to create and initialize an instance of this instruction.
     *
     * @param nonceAccount    The nonce account to initialize.
     * @param authorityAccount The account with authority over the nonce account.
     * @return A fully-configured instruction instance.
     */
    public static SystemInstruction6InitializeNonceAccount create(
            PublicKey nonceAccount, PublicKey authorityAccount) {
        if (nonceAccount == null || authorityAccount == null) {
            throw new IllegalArgumentException("Both accounts must be provided.");
        }

        SystemInstruction6InitializeNonceAccount instruction = new SystemInstruction6InitializeNonceAccount();
        instruction.setAuthority(authorityAccount); // Set the authority
        instruction.setKeys(nonceAccount); // Configure accounts
        return instruction;
    }
}