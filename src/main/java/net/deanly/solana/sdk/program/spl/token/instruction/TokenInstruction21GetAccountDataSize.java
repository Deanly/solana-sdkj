package net.deanly.solana.sdk.program.spl.token.instruction;

import lombok.*;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.spl.token.SplTokenProgram;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt8Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the GetAccountDataSize instruction (index 21) for the Token Program.
 * This instruction retrieves the required size for an account associated with a specific mint.
 *
 * Accounts expected:
 * 0. `[]` The mint to calculate the size for.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction21GetAccountDataSize extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 21; // Discriminator for GetAccountDataSize.

    private List<AccountMeta> keys = new ArrayList<>(); // Accounts required by this instruction.

    /**
     * Configures the required accounts (*keys*) for this instruction.
     *
     * @param mint The mint to calculate the account data size for.
     */
    public void setKeys(@NonNull PublicKey mint) {
        if (mint == null) {
            throw new IllegalArgumentException("Mint account must not be null.");
        }

        // Configure the mint account: read-only, not a signer.
        this.keys = new ArrayList<>();
        this.keys.add(new AccountMeta(mint, false, false));
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
     * @return A byte array representing serialized instruction data.
     */
    @Override
    public byte[] getData() {
        return new byte[]{(byte) discriminator}; // Only discriminator is encoded.
    }

    /**
     * Static factory method to create a fully configured instance.
     *
     * @param mint The mint account to calculate the account data size for.
     * @return A configured TokenInstruction21GetAccountDataSize object.
     */
    public static TokenInstruction21GetAccountDataSize create(@NonNull PublicKey mint) {
        Objects.requireNonNull(mint, "Mint account must not be null.");

        TokenInstruction21GetAccountDataSize instruction = new TokenInstruction21GetAccountDataSize();
        instruction.setKeys(mint); // Configure the mint as a read-only account.
        return instruction;
    }
}