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
import java.util.Collections;
import java.util.List;

/**
 * TokenInstruction04Approve represents the Approve instruction for index 4
 * in the Token Program. It approves a delegate for managing tokens.
 *
 * Accounts expected:
 *   Single owner:
 *     0. `[writable]` Source account.
 *     1. `[]` The delegate.
 *     2. `[signer]` The source account owner.
 *
 *   Multisignature owner:
 *     0. `[writable]` Source account.
 *     1. `[]` The delegate.
 *     2. `[]` The source account's multisignature owner.
 *     3+ `[signer]` M signer accounts.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction04Approve extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 4; // Discriminator for Approve instruction (index 4).

    @Setter
    @StructField(order = 2, type = UInt64LEField.class)
    private long amount; // The amount of tokens the delegate is approved for.

    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Sets the account metadata (keys) exclusively for this instruction.
     *
     * @param source   The source account (Writable).
     * @param delegate The delegate account (Read-only).
     * @param owner    The owner account (Signer).
     * @param multiSigners Optional: List of multi-signature accounts.
     */
    public void setKeys(
            @NonNull PublicKey source,
            @NonNull PublicKey delegate,
            @NonNull PublicKey owner,
            List<PublicKey> multiSigners
    ) {
        // Clear the existing keys list
        this.keys.clear();

        // Add required keys (source, delegate, owner)
        this.keys.add(new AccountMeta(source, false, true)); // Source: Writable, not signer.
        this.keys.add(new AccountMeta(delegate, false, false)); // Delegate: Read-only, not signer.
        this.keys.add(new AccountMeta(owner, true, false)); // Owner: Read-only, signer.

        // Add additional multi-signers
        if (multiSigners != null) {
            for (PublicKey signer : multiSigners) {
                this.keys.add(new AccountMeta(signer, true, false)); // Each signer: Read-only, signer.
            }
        }
    }

    @Override
    public List<AccountMeta> getKeys() {
        return keys == null ? Collections.emptyList() : keys;
    }

    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        TokenInstruction04Approve decoded = StructLayout.decode(data, TokenInstruction04Approve.class);
        this.amount = decoded.getAmount();
    }

    /**
     * Static factory method to create a fully configured ApproveInstruction.
     *
     * @param source   The source account public key.
     * @param delegate The delegate account public key.
     * @param owner    The owner account public key.
     * @param amount   The amount of tokens to approve.
     * @param multiSigners Optional: List of additional signers for multi-signature accounts.
     * @return A configured instance of TokenInstruction04Approve.
     */
    public static TokenInstruction04Approve create(
            @NonNull PublicKey source,
            @NonNull PublicKey delegate,
            @NonNull PublicKey owner,
            long amount,
            List<PublicKey> multiSigners
    ) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }

        TokenInstruction04Approve instruction = new TokenInstruction04Approve();
        instruction.amount = amount;
        instruction.setKeys(source, delegate, owner, multiSigners);
        return instruction;
    }
}