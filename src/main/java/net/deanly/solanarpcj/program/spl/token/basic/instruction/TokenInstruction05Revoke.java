package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import lombok.*;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.spl.token.basic.SplTokenProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TokenInstruction05Revoke represents the Revoke instruction for index 5
 * in the Token Program. It revokes the delegate's authority.
 *
 * Accounts expected:
 *   Single owner:
 *     0. `[writable]` Source account.
 *     1. `[signer]` Source account owner.
 *
 *   Multisignature owner:
 *     0. `[writable]` Source account.
 *     1. `[]` Source account's multisignature owner.
 *     2+ `[signer]` M signer accounts.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction05Revoke extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 5; // Discriminator for Revoke instruction (index 5).

    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Sets the account metadata for this instruction.
     * This method focuses exclusively on setting `this.keys`.
     *
     * @param source       The source account (Writable).
     * @param owner        The owner account (Read-only, Signer).
     * @param multiSigners Optional: Additional signer accounts (Read-only, Signer).
     */
    public void setKeys(@NonNull PublicKey source, @NonNull PublicKey owner, List<PublicKey> multiSigners) {
        // Clear the existing keys list
        this.keys.clear();

        // Configure AccountMetas
        this.keys.add(new AccountMeta(source, false, true)); // Source: Writable, not signer.
        this.keys.add(new AccountMeta(owner, true, false)); // Owner: Read-only, signer.

        if (multiSigners != null && !multiSigners.isEmpty()) {
            for (PublicKey signer : multiSigners) {
                this.keys.add(new AccountMeta(signer, true, false)); // MultiSigners: Read-only, signer.
            }
        }
    }

    /**
     * Provides account metadata for this instruction.
     *
     * @return List of accounts used in this transaction.
     */
    @Override
    public List<AccountMeta> getKeys() {
        return keys == null ? Collections.emptyList() : keys;
    }

    /**
     * Encodes the instruction data for Revoke.
     *
     * @return Encoded byte array for this instruction.
     */
    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    /**
     * Static factory method to create and configure a Revoke instruction.
     *
     * @param source       The source account public key.
     * @param owner        The owner account public key.
     * @param multiSigners Optional: List of additional multi-signers.
     * @return A configured instance of TokenInstruction05Revoke.
     */
    public static TokenInstruction05Revoke create(
            @NonNull PublicKey source,
            @NonNull PublicKey owner,
            List<PublicKey> multiSigners
    ) {
        // Validate inputs
        validateInputs(source, owner);

        TokenInstruction05Revoke instruction = new TokenInstruction05Revoke();

        // Set accounts using setKeys
        instruction.setKeys(source, owner, multiSigners);

        return instruction; // Return newly created instruction
    }

    /**
     * Validates inputs for the Revoke instruction.
     *
     * @param source The source account public key.
     * @param owner  The owner account public key.
     */
    private static void validateInputs(PublicKey source, PublicKey owner) {
        if (source == null) {
            throw new IllegalArgumentException("Source account must not be null.");
        }
        if (owner == null) {
            throw new IllegalArgumentException("Owner account must not be null.");
        }
    }
}