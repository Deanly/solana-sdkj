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
 * TokenInstruction10FreezeAccount represents the FreezeAccount instruction for index 10
 * in the Token Program. It freezes a token account using the mint's freeze authority.
 *
 * Accounts expected:
 *   Single owner:
 *     0. `[writable]` The account to freeze.
 *     1. `[]` The token mint.
 *     2. `[signer]` The mint's freeze authority.
 *
 *   Multisignature owner:
 *     0. `[writable]` The account to freeze.
 *     1. `[]` The token mint.
 *     2. `[]` The mint's multisignature freeze authority.
 *     3+ `[signer]` M signer accounts.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction10FreezeAccount extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 10; // Discriminator for FreezeAccount (index 10).

    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Sets the account metadata (keys) for the FreezeAccount instruction.
     * This combines both single owner and multisignature authority handling.
     *
     * @param accountToFreeze The token account to freeze (writable).
     * @param mint            The token mint account (read-only).
     * @param freezeAuthority The mint's freeze authority or multisignature authority (read-only).
     * @param multiSigners    Optional: A list of additional signer accounts for multisignature handling.
     */
    public void setKeys(PublicKey accountToFreeze, PublicKey mint, PublicKey freezeAuthority, List<PublicKey> multiSigners) {
        // Validate inputs
        if (accountToFreeze == null || mint == null || freezeAuthority == null) {
            throw new IllegalArgumentException("Account to freeze, mint, and freeze authority must not be null.");
        }

        this.keys = new ArrayList<>();

        // Add required accounts
        keys.add(new AccountMeta(accountToFreeze, false, true));   // Account to freeze: writable, not signer
        keys.add(new AccountMeta(mint, false, false));            // Mint: read-only, not signer
        keys.add(new AccountMeta(freezeAuthority, multiSigners == null || multiSigners.isEmpty(), false)); // Freeze authority: read-only, signer if no multisigners

        // Add multisigners (if any exist)
        if (multiSigners != null && !multiSigners.isEmpty()) {
            for (PublicKey signer : multiSigners) {
                keys.add(new AccountMeta(signer, true, false));   // Multisignature signers: read-only, signer
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
     * Static factory method to create and configure a FreezeAccount instruction.
     *
     * @param accountToFreeze The token account to freeze (writable).
     * @param mint            The token mint account (read-only).
     * @param freezeAuthority The mint's freeze authority or multisignature authority (read-only).
     * @param multiSigners    Optional: A list of additional signer accounts for multisignature handling.
     * @return A properly configured TokenInstruction10FreezeAccount instance.
     */
    public static TokenInstruction10FreezeAccount create(
            PublicKey accountToFreeze,
            PublicKey mint,
            PublicKey freezeAuthority,
            List<PublicKey> multiSigners
    ) {
        // Validate inputs
        if (accountToFreeze == null || mint == null || freezeAuthority == null) {
            throw new IllegalArgumentException("Account to freeze, mint, and freeze authority must not be null.");
        }

        // Create and configure the instruction
        TokenInstruction10FreezeAccount instruction = new TokenInstruction10FreezeAccount();
        instruction.setKeys(accountToFreeze, mint, freezeAuthority, multiSigners);
        return instruction;
    }
}