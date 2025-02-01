package net.deanly.solana.sdk.program.spl.token.basic.instruction;

import lombok.*;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.spl.token.basic.SplTokenProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * TokenInstruction11ThawAccount represents the ThawAccount instruction for index 11
 * in the Token Program. It thaws a previously frozen token account.
 *
 * Accounts expected:
 *   Single owner:
 *     0. `[writable]` The account to thaw.
 *     1. `[]` The token mint.
 *     2. `[signer]` The mint's freeze authority.
 *
 *   Multisignature owner:
 *     0. `[writable]` The account to thaw.
 *     1. `[]` The token mint.
 *     2. `[]` The mint's multisignature freeze authority.
 *     3+ `[signer]` M signer accounts.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction11ThawAccount extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 11; // Discriminator for ThawAccount (index 11).

    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Sets the account metadata (keys) for the ThawAccount instruction.
     * Handles both single owner and multisignature authority.
     *
     * @param accountToThaw   The account to thaw (writable).
     * @param mint            The token mint (read-only).
     * @param freezeAuthority The mint's freeze authority or multisignature authority (read-only).
     * @param multiSigners    Optional: A list of M signer accounts for multisignature handling.
     */
    public void setKeys(PublicKey accountToThaw, PublicKey mint, PublicKey freezeAuthority, List<PublicKey> multiSigners) {
        // Validate inputs
        if (accountToThaw == null || mint == null || freezeAuthority == null) {
            throw new IllegalArgumentException("Account to thaw, mint, and freeze authority must not be null.");
        }

        this.keys = new ArrayList<>();

        // Add required accounts
        this.keys.add(new AccountMeta(accountToThaw, false, true));   // Account to thaw: writable, not signer
        this.keys.add(new AccountMeta(mint, false, false));          // Mint: read-only, not signer
        this.keys.add(new AccountMeta(freezeAuthority, multiSigners == null || multiSigners.isEmpty(), false)); // Freeze authority: read-only, signer if no multisigners

        // Add multisigners (if any exist)
        if (multiSigners != null && !multiSigners.isEmpty()) {
            for (PublicKey signer : multiSigners) {
                this.keys.add(new AccountMeta(signer, true, false)); // Multisigners: read-only, signer
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
        // Encodes only the structure fields (discriminator in this case)
        return StructLayout.encode(this);
    }

    /**
     * Static factory method to create and configure a ThawAccount instruction.
     *
     * @param accountToThaw   The token account to thaw (writable).
     * @param mint            The token mint (read-only).
     * @param freezeAuthority The mint's freeze authority or multisignature authority (read-only).
     * @param multiSigners    Optional: A list of signer accounts for multisignature handling.
     * @return A fully configured TokenInstruction11ThawAccount instance.
     */
    public static TokenInstruction11ThawAccount create(
            PublicKey accountToThaw,
            PublicKey mint,
            PublicKey freezeAuthority,
            List<PublicKey> multiSigners
    ) {
        // Validate inputs
        if (accountToThaw == null || mint == null || freezeAuthority == null) {
            throw new IllegalArgumentException("Account to thaw, mint, and freeze authority must not be null.");
        }

        // Create and configure the instruction
        TokenInstruction11ThawAccount instruction = new TokenInstruction11ThawAccount();
        instruction.setKeys(accountToThaw, mint, freezeAuthority, multiSigners);
        return instruction;
    }
}