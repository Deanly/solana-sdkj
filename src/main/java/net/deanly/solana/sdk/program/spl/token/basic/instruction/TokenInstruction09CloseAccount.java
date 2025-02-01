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
 * TokenInstruction09CloseAccount represents the CloseAccount instruction for index 9
 * in the Token Program. It closes a token account by transferring its balance to
 * a specified destination.
 *
 * Accounts expected:
 *   Single owner:
 *     0. `[writable]` The account to close.
 *     1. `[writable]` The destination account to receive the balance.
 *     2. `[signer]` The owner of the account to close.
 *
 *   Multisignature owner:
 *     0. `[writable]` The account to close.
 *     1. `[writable]` The destination account to receive the balance.
 *     2. `[]` The multisignature owner's delegate.
 *     3+ `[signer]` M signer accounts.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction09CloseAccount extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 9; // Discriminator for CloseAccount (index 9).

    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Sets the account metadata (keys) for the CloseAccount instruction.
     * This method handles both single owner and multisignature owners dynamically.
     *
     * @param accountToClose The token account to close (writable).
     * @param destination    The destination account (writable).
     * @param owner          The account's owner/delegate or multisig delegate (read-only).
     * @param multiSigners   Optional list of additional signer accounts required for multisignature.
     */
    public void setKeys(PublicKey accountToClose, PublicKey destination, PublicKey owner, List<PublicKey> multiSigners) {
        // Validate inputs
        if (accountToClose == null || destination == null || owner == null) {
            throw new IllegalArgumentException("Account to close, destination, and owner cannot be null.");
        }

        // Initialize keys
        keys = new ArrayList<>();
        keys.add(new AccountMeta(accountToClose, false, true));     // Account to close: writable, not signer
        keys.add(new AccountMeta(destination, false, true));        // Destination: writable, not signer
        keys.add(new AccountMeta(owner, true, false));             // Owner: read-only, signer

        // Add multiSigners if provided
        if (multiSigners != null && !multiSigners.isEmpty()) {
            for (PublicKey signer : multiSigners) {
                keys.add(new AccountMeta(signer, true, false));    // Multi-signers: read-only, signer
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
     * Static factory method to create a TokenInstruction09CloseAccount instance.
     *
     * @param accountToClose The token account to close (writable).
     * @param destination    The destination account (writable).
     * @param owner          The account's owner or multisignature delegate.
     * @param multiSigners   Optional: List of additional signer accounts for multisignature.
     * @return A configured TokenInstruction09CloseAccount instance.
     */
    public static TokenInstruction09CloseAccount create(
            PublicKey accountToClose,
            PublicKey destination,
            PublicKey owner,
            List<PublicKey> multiSigners
    ) {
        // Validate inputs
        if (accountToClose == null || destination == null || owner == null) {
            throw new IllegalArgumentException("Account to close, destination, and owner cannot be null.");
        }

        // Create and configure instance
        TokenInstruction09CloseAccount instruction = new TokenInstruction09CloseAccount();
        instruction.setKeys(accountToClose, destination, owner, multiSigners);
        return instruction;
    }
}