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
 * TokenInstruction08Burn represents the Burn instruction for index 8
 * in the Token Program. It burns tokens from an account.
 *
 * Accounts expected:
 *   Single owner/delegate:
 *     0. `[writable]` The account to burn from.
 *     1. `[writable]` The token mint.
 *     2. `[signer]` The account's owner/delegate.
 *
 *   Multisignature owner/delegate:
 *     0. `[writable]` The account to burn from.
 *     1. `[writable]` The token mint.
 *     2. `[]` The account's multisignature owner/delegate.
 *     3+ `[signer]` M signer accounts.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction08Burn extends SplTokenInstructionBase implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 8; // Discriminator for Burn instruction (index 8).

    @Setter
    @StructField(order = 2, type = UInt64LEField.class)
    private long amount; // The amount of tokens to burn.

    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.


    /**
     * Configures account metadata (keys) for the Burn instruction.
     * This method dynamically supports both single owner/delegate and multisignature owners.
     *
     * @param account       The account to burn from (writable).
     * @param mint          The token mint (writable).
     * @param authority     The account's owner/delegate or multisignature delegate (read-only).
     * @param multiSigners  Optional: List of additional signers for multisignature authority.
     */
    public void setKeys(PublicKey account, PublicKey mint, PublicKey authority, List<PublicKey> multiSigners) {
        // Validate inputs
        if (account == null || mint == null || authority == null) {
            throw new IllegalArgumentException("Account, mint, and authority cannot be null.");
        }

        // Initialize keys
        keys = new ArrayList<>();
        keys.add(new AccountMeta(account, false, true));            // Account: writable, not signer
        keys.add(new AccountMeta(mint, false, true));              // Mint: writable, not signer
        keys.add(new AccountMeta(authority, true, false));         // Authority: signer, read-only

        // Add multiSigners if any
        if (multiSigners != null && !multiSigners.isEmpty()) {
            for (PublicKey signer : multiSigners) {
                keys.add(new AccountMeta(signer, true, false));    // Multi-signers: signer, read-only
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
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        // Decode the structure fields (discriminator and amount only, as keys are not decoded here).
        TokenInstruction08Burn decoded = StructLayout.decode(data, TokenInstruction08Burn.class);
        this.amount = decoded.getAmount();
    }

    /**
     * Static factory method to create a fully configured TokenInstruction08Burn instance.
     *
     * @param account       The account to burn from (writable).
     * @param mint          The token mint (writable).
     * @param authority     The account's owner/delegate or multisignature delegate (read-only).
     * @param amount        The amount of tokens to burn.
     * @param multiSigners  Optional: List of multi-signature signers.
     * @return Configured TokenInstruction08Burn instance.
     */
    public static TokenInstruction08Burn create(
            PublicKey account,
            PublicKey mint,
            PublicKey authority,
            long amount,
            List<PublicKey> multiSigners
    ) {
        // Validate inputs
        if (account == null || mint == null || authority == null) {
            throw new IllegalArgumentException("Account, mint, and authority cannot be null.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }

        // Create and configure instruction
        TokenInstruction08Burn instruction = new TokenInstruction08Burn();
        instruction.setAmount(amount);
        instruction.setKeys(account, mint, authority, multiSigners);

        return instruction;
    }

}