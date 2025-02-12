package net.deanly.solana.sdk.program.spl.token.instruction;

import lombok.*;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.field.PublicKeyBorshOptionField;
import net.deanly.solana.sdk.program.spl.token.SplTokenProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solana.sdk.layout.field.PublicKeyField;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the `InitializeMint2` instruction (type 20) for the SPL Token program.
 *
 * This instruction is used to initialize a new mint account with specific parameters
 * such as the number of decimals, mint authority, and an optional freeze authority.
 *
 * The class also provides functionality for serializing and deserializing the instruction
 * and managing the associated account metadata (`keys`).
 *
 * <h2>Instruction Fields:</h2>
 * <ul>
 *   <li><b>discriminator</b>: Fixed discriminator value (20) for the `InitializeMint2` instruction.</li>
 *   <li><b>decimals</b>: Number of base 10 decimals for the token (right of the decimal point).</li>
 *   <li><b>mintAuthority</b>: Public key of the mint authority (authorized to mint tokens).</li>
 *   <li><b>freezeAuthority</b>: Optional public key of the freeze authority (authorized to freeze accounts).</li>
 * </ul>
 *
 * The `keys` list represents the ordered set of accounts required for this instruction, as defined
 * by the SPL Token program.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction20InitializeMint2 extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 20; // Discriminator for InitializeMint2.

    @Setter
    @StructField(order = 2, type = UInt8Field.class)
    private int decimals; // Number of decimals for the mint (right of the decimal point).

    @Setter
    @StructField(order = 3, type = PublicKeyField.class)
    private PublicKey mintAuthority; // The authority to mint tokens.

    @Setter
    @StructField(order = 4, type = PublicKeyBorshOptionField.class)
    private PublicKey freezeAuthority; // The authority to freeze accounts (optional).

    /**
     * <pre>
     * Accounts expected:
     *  0. `[writable]` The mint to initialize.
     * </pre>
     */
    private List<AccountMeta> keys = new ArrayList<>(); // Keys required by this instruction.

    /**
     * Sets the account metadata (`keys`) for the instruction.
     *
     * @param mint The mint account to be initialized.
     */
    public void setKeys(@NonNull PublicKey mint) {
        if (mint == null) {
            throw new IllegalArgumentException("Mint account must not be null.");
        }

        // Initialize accounts (only mint here).
        this.keys = new ArrayList<>();
        this.keys.add(new AccountMeta(mint, false, true)); // Mint is writable, not a signer.
    }

    /**
     * Retrieves the accounts needed for this instruction.
     *
     * If `setKeys` is not called, this will return an empty list as a guide for the user.
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
     * Encodes the instruction into its byte format for serialization.
     *
     * @return Byte array of serialized instruction data.
     */
    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    /**
     * Decodes the provided byte array back into the object's annotated fields.
     *
     * **Note**: Does not restore the `keys` field.
     *
     * @param data Byte array to decode.
     */
    public void setData(byte[] data) {
        TokenInstruction20InitializeMint2 decoded =
                StructLayout.decode(data, TokenInstruction20InitializeMint2.class);

        this.decimals = decoded.getDecimals();
        this.mintAuthority = decoded.getMintAuthority();
        this.freezeAuthority = decoded.getFreezeAuthority();
    }

    /**
     * Static factory method to create a fully configured instance.
     *
     * @param mint            The mint account to be initialized (writable).
     * @param decimals        Number of base 10 decimals for the mint.
     * @param mintAuthority   Authority to mint tokens.
     * @param freezeAuthority Optional freeze authority (can be null).
     * @return A configured TokenInstruction20InitializeMint2 object.
     */
    public static TokenInstruction20InitializeMint2 create(
            @NonNull PublicKey mint,
            int decimals,
            @NonNull PublicKey mintAuthority,
            PublicKey freezeAuthority) {
        if (decimals < 0) {
            throw new IllegalArgumentException("Decimals must be a non-negative integer.");
        }
        Objects.requireNonNull(mint, "Mint account must not be null.");
        Objects.requireNonNull(mintAuthority, "Mint authority must not be null.");

        // Create and configure the instruction.
        TokenInstruction20InitializeMint2 instruction = new TokenInstruction20InitializeMint2();
        instruction.setDecimals(decimals);
        instruction.setMintAuthority(mintAuthority);
        instruction.setFreezeAuthority(freezeAuthority);
        instruction.setKeys(mint); // Configure accounts.
        return instruction;
    }
}