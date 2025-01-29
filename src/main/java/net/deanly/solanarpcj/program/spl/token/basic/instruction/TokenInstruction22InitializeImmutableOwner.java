package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import lombok.*;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.spl.token.basic.SplTokenProgram;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt8Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the InitializeImmutableOwner instruction (index 22) for the Token Program.
 * This instruction initializes the `Immutable Owner` extension for a specific token account.
 *
 * Accounts expected:
 * 0. `[writable]` The token account to initialize.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction22InitializeImmutableOwner extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 22; // Discriminator for InitializeImmutableOwner.

    private List<AccountMeta> keys = new ArrayList<>(); // Accounts required by this instruction.

    /**
     * Configures the required accounts (*keys*) for this instruction.
     *
     * @param account The token account to initialize as immutable.
     */
    public void setKeys(@NonNull PublicKey account) {
        if (account == null) {
            throw new IllegalArgumentException("Account must not be null.");
        }

        // Configure the account as writable and not a signer.
        this.keys = new ArrayList<>();
        this.keys.add(new AccountMeta(account, true, false));
    }

    /**
     * Retrieves the accounts (keys) for this instruction.
     *
     * If `setKeys` is not called, this will return an empty list for user guidance.
     *
     * @return Ordered list of AccountMeta objects, or an empty list.
     */
    @Override
    public List<AccountMeta> getKeys() {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        return this.keys;
    }

    /**
     * Encodes the instruction into a byte array for serialization.
     *
     * @return A byte array representing the serialized instruction data.
     */
    @Override
    public byte[] getData() {
        return new byte[]{(byte) discriminator}; // Only discriminator is encoded.
    }

    /**
     * Static factory method to create a fully configured instance.
     *
     * @param account The token account to initialize as immutable.
     * @return A configured TokenInstruction22InitializeImmutableOwner object.
     */
    public static TokenInstruction22InitializeImmutableOwner create(@NonNull PublicKey account) {
        Objects.requireNonNull(account, "Account must not be null.");

        TokenInstruction22InitializeImmutableOwner instruction = new TokenInstruction22InitializeImmutableOwner();
        instruction.setKeys(account); // Configure the account.
        return instruction;
    }
}