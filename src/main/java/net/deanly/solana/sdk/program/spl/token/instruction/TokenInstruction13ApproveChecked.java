package net.deanly.solana.sdk.program.spl.token.instruction;

import lombok.*;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.spl.token.SplTokenProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt64LEField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * TokenInstruction13ApproveChecked represents the ApproveChecked instruction for index 13
 * in the Token Program. This is used to approve a delegate for transferring tokens
 * with validation of decimals.
 *
 * Accounts expected:
 *   0. `[writable]` The source token account.
 *   1. `[]` The token mint.
 *   2. `[]` The delegate (account authorized to transfer tokens).
 *   3. `[signer]` The source account owner (single owner).
 *   Multisignature owner:
 *     3. `[]` The multisignature owner account.
 *     4+ `[signer]` M signer accounts.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction13ApproveChecked extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 13; // Discriminator for ApproveChecked (index 13).

    @StructField(order = 2, type = UInt64LEField.class)
    private long amount; // Amount of tokens to approve.

    @StructField(order = 3, type = UInt8Field.class)
    private int decimals; // Number of decimals for token validation.

    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Sets the account metadata (keys) for the ApproveChecked instruction,
     * handling both single and multisignature owner scenarios.
     *
     * @param source       The source account (writable).
     * @param mint         The token mint (readonly).
     * @param delegate     The delegate (readonly).
     * @param owner        The owner or multisignature authority of the source account (readonly).
     * @param multiSigners Optional list of multisignature signer accounts.
     */
    public void setKeys(PublicKey source, PublicKey mint, PublicKey delegate, PublicKey owner, List<PublicKey> multiSigners) {
        // Validate inputs
        if (source == null || mint == null || delegate == null || owner == null) {
            throw new IllegalArgumentException("Source, mint, delegate, and owner must not be null.");
        }

        this.keys = new ArrayList<>();

        // Add required accounts
        this.keys.add(new AccountMeta(source, false, true));     // Source: writable, not signer
        this.keys.add(new AccountMeta(mint, false, false));      // Mint: readonly, not signer
        this.keys.add(new AccountMeta(delegate, false, false));  // Delegate: readonly, not signer
        this.keys.add(new AccountMeta(owner, multiSigners == null || multiSigners.isEmpty(), false)); // Owner: readonly, signer if no multisigners

        // Add multisigners (if provided)
        if (multiSigners != null && !multiSigners.isEmpty()) {
            for (PublicKey signer : multiSigners) {
                this.keys.add(new AccountMeta(signer, true, false)); // Multisigners: readonly, signer
            }
        }
    }

    @Override
    public List<AccountMeta> getKeys() {
        if (this.keys == null || this.keys.isEmpty()) {
            throw new IllegalStateException("Account metadata (keys) must be set before building the transaction.");
        }
        return this.keys;
    }

    @Override
    public byte[] getData() {
        // Encodes the discriminator, amount, and decimals.
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        // Decode fields (discriminator, amount, decimals). Keys must be set explicitly.
        TokenInstruction13ApproveChecked decoded = StructLayout.decode(data, TokenInstruction13ApproveChecked.class);
        this.amount = decoded.amount;
        this.decimals = decoded.decimals;
    }

    /**
     * Static factory method to create and configure an ApproveChecked instruction.
     *
     * @param source       The source account (writable).
     * @param mint         The token mint (readonly).
     * @param delegate     The delegate (readonly).
     * @param owner        The owner or multisignature authority of the source account (readonly).
     * @param amount       The amount to approve for the delegate.
     * @param decimals     The decimals of the amount.
     * @param multiSigners Optional: List of multisignature signer accounts.
     * @return A configured TokenInstruction13ApproveChecked instance.
     */
    public static TokenInstruction13ApproveChecked create(
            PublicKey source,
            PublicKey mint,
            PublicKey delegate,
            PublicKey owner,
            long amount,
            int decimals,
            List<PublicKey> multiSigners
    ) {
        // Validate inputs
        if (source == null || mint == null || delegate == null || owner == null) {
            throw new IllegalArgumentException("Source, mint, delegate, and owner must not be null.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (decimals < 0) {
            throw new IllegalArgumentException("Decimals cannot be negative.");
        }

        // Create and configure the instance
        TokenInstruction13ApproveChecked instruction = new TokenInstruction13ApproveChecked();
        instruction.setAmount(amount);
        instruction.setDecimals(decimals);
        instruction.setKeys(source, mint, delegate, owner, multiSigners);
        return instruction;
    }
}