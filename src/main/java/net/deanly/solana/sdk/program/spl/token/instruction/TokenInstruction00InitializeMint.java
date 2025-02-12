package net.deanly.solana.sdk.program.spl.token.instruction;

import lombok.*;
import net.deanly.solana.sdk.program.core.Sysvar;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.field.PublicKeyBorshOptionField;
import net.deanly.solana.sdk.layout.field.PublicKeyField;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.util.*;

/**
 * Represents the `InitializeMint` instruction of the Solana Token Program.
 * This instruction initializes a new mint account with the specified parameters.
 *
 * <p>Responsibilities include setting the token's decimal places,
 * specifying the authority for minting new tokens, and optionally defining a freeze authority.</p>
 *
 * <p>Once initialized, the mint account becomes the governing account for a specific token.</p>
 *
 * Structure
 * <ul>
 *   <li><b>Discriminator:</b> Identifies the instruction type.</li>
 *   <li><b>Decimals:</b> The number of decimal places for the token (base 10 representation).</li>
 *   <li><b>Mint Authority:</b> The public key authorized to mint new tokens.</li>
 *   <li><b>Freeze Authority:</b> The optional public key authorized to freeze accounts associated with this mint.</li>
 *   <li><b>Keys:</b> The list of accounts involved in the `InitializeMint` instruction.</li>
 * </ul>
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenInstruction00InitializeMint extends SplTokenInstructionBase implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 0; // Discriminator for InitializeMint

    @StructField(order = 2, type = UInt8Field.class)
    private int decimals; // Number of base 10 digits to the right of the decimal point.

    @StructField(order = 3, type = PublicKeyField.class)
    @NonNull
    private PublicKey mintAuthority; // The authority that controls minting of new tokens.

    @StructField(order = 4, type = PublicKeyBorshOptionField.class)
    private PublicKey freezeAuthority; // The authority that can freeze accounts (optional).

    private List<AccountMeta> keys = new ArrayList<>();

    /**
     * Configures the required accounts for the InitializeMint instruction.
     *
     * @param mint The mint account (writable, non-signer).
     * @param rentAccount The rent account (read-only).
     */
    public void setKeys(@NonNull PublicKey mint, @NonNull PublicKey rentAccount) {
        Objects.requireNonNull(mint, "Mint PublicKey cannot be null");

        // Configure accounts based on JavaScript logic.
        this.keys = Arrays.asList(
                new AccountMeta(mint, false, true), // Mint account: isWritable=true, isSigner=false
                new AccountMeta(rentAccount, false, false) // Rent account: isWritable=false, isSigner=false
        );
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
        TokenInstruction00InitializeMint decoded = StructLayout.decode(data, TokenInstruction00InitializeMint.class);
        this.decimals = decoded.getDecimals();
        this.mintAuthority = decoded.getMintAuthority();
        this.freezeAuthority = decoded.getFreezeAuthority();
    }

    /**
     * Static factory method to create a configured instance.
     *
     * @param mintAuthority Authority to mint tokens.
     * @param decimals Number of decimals for the token.
     * @param mint Mint account.
     * @param freezeAuthority (Optional) Authority to freeze accounts.
     * @param rentAccount Rent account or null (default rent sysvar will be used).
     * @return A fully configured TokenInstruction00InitializeMint instance.
     */
    public static TokenInstruction00InitializeMint create(
            @NonNull PublicKey mint,
            @NonNull PublicKey mintAuthority,
            int decimals,
            PublicKey freezeAuthority,
            PublicKey rentAccount
    ) {
        // Validate inputs
        Objects.requireNonNull(mintAuthority, "Mint authority must not be null");
        Objects.requireNonNull(mint, "Mint account must not be null");
        if (decimals < 0 || decimals > 255) {
            throw new IllegalArgumentException("Decimals must be between 0 and 255");
        }

        // If no rent account is provided, use the default rent sysvar.
        if (rentAccount == null) {
            rentAccount = Sysvar.SYSVAR_RENT_ADDRESS;
        }

        // Create and configure the instruction instance.
        TokenInstruction00InitializeMint instruction = new TokenInstruction00InitializeMint();
        instruction.setMintAuthority(mintAuthority);
        instruction.setDecimals(decimals);
        instruction.setFreezeAuthority(freezeAuthority);
        instruction.setKeys(mint, rentAccount);
        return instruction;
    }
}