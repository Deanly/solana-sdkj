package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import lombok.*;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.layout.field.UTF8StringField;
import net.deanly.solanarpcj.program.spl.token.basic.SplTokenProgram;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solanarpcj.crypto.PublicKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the UiAmountToAmount instruction (index 24) for the Solana Token Program.
 * Converts a UI-friendly formatted token amount to a raw amount (`u64`).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenInstruction24UiAmountToAmount extends SplTokenProgram.Base implements TransactionInstruction {

    /**
     * The discriminator identifies this instruction.
     */
    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 24;

    /**
     * The UI amount of tokens to convert.
     * It is encoded as a UTF-8 string.
     */
    @StructField(order = 2, type = UTF8StringField.class)
    private String uiAmount;

    /**
     * The list of accounts required for this instruction.
     * This is set via the setKeys() method.
     */
    private List<AccountMeta> keys = new ArrayList<>();

    /**
     * Configures the required accounts for the instruction.
     *
     * @param mint The PublicKey of the mint account.
     */
    public void setKeys(@NonNull PublicKey mint) {
        Objects.requireNonNull(mint, "Mint PublicKey cannot be null");

        // Configure the mint account: read-only and a non-signer.
        this.keys = new ArrayList<>();
        this.keys.add(new AccountMeta(mint, false, false)); // isWritable=false, isSigner=false
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
        TokenInstruction24UiAmountToAmount decoded = StructLayout.decode(data, TokenInstruction24UiAmountToAmount.class);
        this.uiAmount = decoded.getUiAmount();
    }

    /**
     * Static factory method to create a configured instance.
     *
     * @param mint     The PublicKey of the mint account.
     * @param uiAmount The UI amount of tokens to convert.
     * @return A fully configured TokenInstruction24UiAmountToAmount object.
     */
    public static TokenInstruction24UiAmountToAmount create(@NonNull PublicKey mint, @NonNull String uiAmount) {
        Objects.requireNonNull(mint, "Mint PublicKey cannot be null");
        Objects.requireNonNull(uiAmount, "UI Amount cannot be null or empty");
        if (uiAmount.isEmpty()) {
            throw new IllegalArgumentException("UI Amount cannot be empty");
        }

        TokenInstruction24UiAmountToAmount instruction = new TokenInstruction24UiAmountToAmount();
        instruction.setUiAmount(uiAmount);
        instruction.setKeys(mint); // Configure the mint account.
        return instruction;
    }
}