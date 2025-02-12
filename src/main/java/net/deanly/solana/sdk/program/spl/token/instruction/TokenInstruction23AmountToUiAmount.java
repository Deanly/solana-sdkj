package net.deanly.solana.sdk.program.spl.token.instruction;

import lombok.*;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.program.spl.token.SplTokenProgram;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt64LEField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solana.sdk.crypto.PublicKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the AmountToUiAmount instruction (index 23) for the Solana Token Program.
 * This instruction converts a token `amount` into its UI-friendly string representation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenInstruction23AmountToUiAmount extends SplTokenInstructionBase implements TransactionInstruction {

    /**
     * The discriminator identifies this instruction.
     */
    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 23;

    /**
     * The amount of tokens to convert into UI format.
     */
    @StructField(order = 2, type = UInt64LEField.class)
    private long amount;

    /**
     * The list of accounts required for this instruction.
     * This is set via the setKeys() method.
     */
    private List<AccountMeta> keys = new ArrayList<>();

    /**
     * Configures the required accounts (*keys*) for this instruction.
     *
     * @param mint The mint to calculate the amount for.
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
     * Encodes the instruction into a byte array for serialization.
     *
     * @return The serialized instruction data.
     */
    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    /**
     * Decodes the provided instruction data into the relevant fields.
     *
     * @param data The byte array containing the encoded instruction data.
     */
    public void setData(byte[] data) {
        TokenInstruction23AmountToUiAmount decoded = StructLayout.decode(data, TokenInstruction23AmountToUiAmount.class);
        this.amount = decoded.getAmount();
    }

    /**
     * Static factory method to create a fully configured instance.
     *
     * @param mint   The mint associated with the token amount.
     * @param amount The amount of tokens to reformat.
     * @return A configured TokenInstruction23AmountToUiAmount object.
     */
    public static TokenInstruction23AmountToUiAmount create(@NonNull PublicKey mint, long amount) {
        Objects.requireNonNull(mint, "Mint account must not be null.");
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must not be negative.");
        }

        TokenInstruction23AmountToUiAmount instruction = new TokenInstruction23AmountToUiAmount();
        instruction.setKeys(mint); // Configure the mint account.
        instruction.setAmount(amount); // Set the token amount.
        return instruction;
    }
}