package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import lombok.*;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.spl.token.basic.SplTokenProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt64LEField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.List;

/// Burns tokens by removing them from an account.  `BurnChecked` does not
/// support accounts associated with the native mint, use `CloseAccount`
/// instead.
///
/// This instruction differs from Burn in that the decimals value is checked
/// by the caller. This may be useful when creating transactions offline or
/// within a hardware wallet.
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction15BurnChecked extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 15; // Discriminator for BurnChecked (index 15).

    @StructField(order = 2, type = UInt64LEField.class)
    private long amount; // Amount of tokens to burn.

    @StructField(order = 3, type = UInt8Field.class)
    private int decimals; // Number of decimals for token validation.

     /// Accounts expected by this instruction:
     ///
     ///   * Single owner/delegate
     ///   0. `[writable]` The account to burn from.
     ///   1. `[writable]` The token mint.
     ///   2. `[signer]` The account's owner/delegate.
     ///
     ///   * Multisignature owner/delegate
     ///   0. `[writable]` The account to burn from.
     ///   1. `[writable]` The token mint.
     ///   2. `[]` The account's multisignature owner/delegate.
     ///   3. ..`3+M` `[signer]` M signer accounts.
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Sets the accounts (`keys`) for the BurnChecked instruction.
     *
     * @param account   The token account to burn tokens from (writable).
     * @param mint      The token mint account (read-only).
     * @param authority The account authority (single or multisig).
     * @param multiSigners Optional: List of multisignature signer accounts.
     */
    public void setKeys(PublicKey account, PublicKey mint, PublicKey authority, List<PublicKey> multiSigners) {
        // Validate inputs
        if (account == null || mint == null || authority == null) {
            throw new IllegalArgumentException("Account, mint, and authority are mandatory and cannot be null.");
        }

        this.keys = new ArrayList<>();

        // Add the required accounts
        this.keys.add(new AccountMeta(account, false, true)); // Writable token account (no signer)
        this.keys.add(new AccountMeta(mint, false, true));   // Read-only mint account (no signer)
        this.keys.add(new AccountMeta(authority, multiSigners == null || multiSigners.isEmpty(), false)); // Authority

        // Add multiSigners (if provided)
        if (multiSigners != null && !multiSigners.isEmpty()) {
            for (PublicKey signer : multiSigners) {
                this.keys.add(new AccountMeta(signer, true, false)); // MultiSigner: Read-only and signer
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
        TokenInstruction15BurnChecked decoded = StructLayout.decode(data, TokenInstruction15BurnChecked.class);
        this.amount = decoded.amount;
        this.decimals = decoded.decimals;
    }


    /**
     * Static factory method to create and configure a BurnChecked instruction.
     *
     * @param account      The token account to burn tokens from (writable).
     * @param mint         The token mint account (read-only).
     * @param authority    The owner/delegate of the token account (or multisignature).
     * @param amount       The amount of tokens to burn.
     * @param decimals     The number of decimals for the token.
     * @param multiSigners Optional: List of multisignature signers.
     * @return A fully configured instance of `TokenInstruction15BurnChecked`.
     */
    public static TokenInstruction15BurnChecked create(
            PublicKey account,
            PublicKey mint,
            PublicKey authority,
            long amount,
            int decimals,
            List<PublicKey> multiSigners
    ) {
        // Validate inputs
        if (account == null || mint == null || authority == null) {
            throw new IllegalArgumentException("Account, mint, and authority are required fields.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to burn must be greater than zero.");
        }
        if (decimals < 0) {
            throw new IllegalArgumentException("Decimals must be a non-negative value.");
        }

        // Create and configure the instruction
        TokenInstruction15BurnChecked instruction = new TokenInstruction15BurnChecked();
        instruction.setAmount(amount);
        instruction.setDecimals(decimals);
        instruction.setKeys(account, mint, authority, multiSigners);
        return instruction;
    }
}