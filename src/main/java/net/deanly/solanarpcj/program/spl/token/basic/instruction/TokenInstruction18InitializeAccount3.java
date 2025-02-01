package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import lombok.*;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.spl.token.basic.SplTokenProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solanarpcj.layout.field.PublicKeyField;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TokenInstruction18InitializeAccount3 represents the InitializeAccount3 instruction for index 18
 * in the Token Program. It initializes a token account with the owner provided in instruction data.
 *
 * Accounts expected:
 *   0. `[writable]` The account to initialize.
 *   1. `[]` The mint this account will be associated with.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction18InitializeAccount3 extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 18; // Discriminator for InitializeAccount3 instruction (index 18).

    @Setter
    @StructField(order = 2, type = PublicKeyField.class)
    private PublicKey owner; // The new account's owner or multisignature authority.

    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Sets the account metadata (`keys`) for the instruction based on JavaScript-defined rules.
     *
     * @param account The writable account to initialize.
     * @param mint    The mint this account will be associated with.
     */
    public void setKeys(PublicKey account, PublicKey mint) {
        // Validate inputs
        if (account == null || mint == null) {
            throw new IllegalArgumentException("Account and mint must not be null.");
        }

        // Define `keys` ensuring the rules for `WritableAccount` and `ReadonlyAccount`
        this.keys = Arrays.asList(
                new AccountMeta(account, false, true), // Writable account; not a signer
                new AccountMeta(mint, false, false)   // Read-only mint; not a signer
        );
    }

    @Override
    public List<AccountMeta> getKeys() {
        if (this.keys == null || this.keys.isEmpty()) {
            throw new IllegalStateException(
                    "Keys are not set! Use `setKeys(PublicKey account, PublicKey mint)` to initialize the necessary accounts."
            );
        }
        return this.keys;
    }

    @Override
    public byte[] getData() {
        // Encode the discriminator and owner fields into the instruction data.
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        // Decode only the StructLayout-mapped fields (discriminator and owner), as keys must be set explicitly.
        TokenInstruction18InitializeAccount3 decoded =
                StructLayout.decode(data, TokenInstruction18InitializeAccount3.class);
        this.owner = decoded.getOwner();
    }

    /**
     * Static factory method to create an InitializeAccount3 instruction.
     *
     * @param account The account to initialize (writable).
     * @param mint    The mint this account will be associated with (read-only).
     * @param owner   The owner of the new token account.
     * @return A fully configured `TokenInstruction18InitializeAccount3` object.
     */
    public static TokenInstruction18InitializeAccount3 create(PublicKey account, PublicKey mint, PublicKey owner) {
        // Validate inputs
        if (account == null || mint == null || owner == null) {
            throw new IllegalArgumentException("Account, mint, and owner must not be null.");
        }

        // Initialize and configure the instruction
        TokenInstruction18InitializeAccount3 instruction = new TokenInstruction18InitializeAccount3();
        instruction.setOwner(owner);            // Set the owner field
        instruction.setKeys(account, mint);     // Set accounts metadata
        return instruction;                     // Return the configured object
    }
}