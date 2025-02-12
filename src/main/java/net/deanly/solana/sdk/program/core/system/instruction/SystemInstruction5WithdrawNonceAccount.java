package net.deanly.solana.sdk.program.core.system.instruction;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.core.Sysvar;
import net.deanly.solana.sdk.program.core.system.SystemProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.Int64LEField;
import net.deanly.structlayout.type.basic.UInt32LEField;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.util.List;

/**
 * Represents a System program withdraw nonce account instruction in the Solana blockchain.
 * This instruction allows withdrawing lamports from a nonce account.
 *
 * Fields:
 * - {@code instruction}: Constant index (5) representing the withdraw nonce account instruction.
 * - {@code keys}: A list of accounts involved in the transaction. This includes the nonce account, authority, and the destination account.
 * - {@code lamports}: The amount of lamports to withdraw from the nonce account.
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
public class SystemInstruction5WithdrawNonceAccount extends SysInstructionBase implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 5; // Instruction index (5), directly defined as required

    private List<AccountMeta> keys; // Accounts used for this instruction (nonce account, authority, destination)

    @StructField(order = 2, type = Int64LEField.class)
    private long lamports; // The number of lamports to withdraw from the nonce account

    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        SystemInstruction5WithdrawNonceAccount instruction = StructLayout.decode(data, SystemInstruction5WithdrawNonceAccount.class);
        this.keys = instruction.getKeys();
        this.lamports = instruction.getLamports();
    }

    /**
     * Sets the required accounts (keys) for the WithdrawNonceAccount instruction.
     * This needs:
     *  - The nonce account (writable, not signer)
     *  - The destination account to which lamports will be withdrawn (writable, not signer)
     *  - The authority account (signer, not writable)
     *  - The recent blockhashes sysvar (neither writable nor signer)
     *  - The rent sysvar (neither writable nor signer)
     *
     * @param nonceAccount      The nonce account to withdraw from.
     * @param authorityAccount  The authority account controlling the nonce account.
     * @param destinationAccount The account where the withdrawn lamports will go.
     */
    public void setKeys(PublicKey nonceAccount, PublicKey authorityAccount, PublicKey destinationAccount) {
        if (nonceAccount == null || authorityAccount == null || destinationAccount == null) {
            throw new IllegalArgumentException("All account parameters must be non-null.");
        }

        this.keys = List.of(
                new AccountMeta(nonceAccount, false, true), // Nonce account: Writable, not signer
                new AccountMeta(destinationAccount, false, true), // Destination account: Writable, not signer
                new AccountMeta(Sysvar.SYSVAR_RECENT_BLOCKHASHES_ADDRESS, false, false), // Recent blockhashes sysvar
                new AccountMeta(Sysvar.SYSVAR_RENT_ADDRESS, false, false), // Rent sysvar
                new AccountMeta(authorityAccount, true, false) // Authority account: Signer, not writable
        );
    }

    /**
     * Factory method to create a fully-setup SystemInstruction5WithdrawNonceAccount.
     *
     * @param nonceAccount      The nonce account to withdraw from.
     * @param authorityAccount  The authority controlling the nonce account.
     * @param destinationAccount The account that will receive the withdrawn lamports.
     * @param lamports          The amount of lamports to withdraw.
     * @return An instance of SystemInstruction5WithdrawNonceAccount.
     */
    public static SystemInstruction5WithdrawNonceAccount create(
            PublicKey nonceAccount,
            PublicKey authorityAccount,
            PublicKey destinationAccount,
            long lamports
    ) {
        if (nonceAccount == null || authorityAccount == null || destinationAccount == null) {
            throw new IllegalArgumentException("All accounts must be provided.");
        }
        if (lamports < 0) {
            throw new IllegalArgumentException("Lamports must be a non-negative value.");
        }

        SystemInstruction5WithdrawNonceAccount instruction = new SystemInstruction5WithdrawNonceAccount();
        instruction.setLamports(lamports);
        instruction.setKeys(nonceAccount, authorityAccount, destinationAccount);
        return instruction;
    }
}