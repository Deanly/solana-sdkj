package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import lombok.*;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.system.Sysvar;
import net.deanly.solanarpcj.program.spl.token.basic.SplTokenProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;

import java.util.*;

/**
 * Represents the `InitializeAccount` instruction for the token program.
 * This instruction is used to initialize a new token account with the
 * specified mint, owner, and optionally a rent sysvar account.
 *
 * The instruction will create and set up the necessary account metadata
 * and validate the inputs provided to ensure proper configuration.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction01InitializeAccount extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 1; // Discriminator for InitializeAccount (index = 1)

    private List<AccountMeta> keys = new ArrayList<>();

    /**
     * Sets the account metadata for the InitializeAccount instruction.
     *
     * @param account The account to initialize.
     * @param mint The mint associated with the account.
     * @param owner The owner of the account.
     * @param rent (Optional) Rent sysvar account. Defaults to SysvarRent if null.
     * @param additionalAccounts (Optional) Remaining accounts to include.
     */
    public void setKeys(
            @NonNull PublicKey account,
            @NonNull PublicKey mint,
            @NonNull PublicKey owner,
            PublicKey rent,
            List<PublicKey> additionalAccounts
    ) {
        // Defaults for rent (if null)
        rent = (rent != null) ? rent : Sysvar.SYSVAR_RENT_ADDRESS;

        this.keys = new ArrayList<>(Arrays.asList(
                new AccountMeta(account, true, true),   // Account: Writable, Signer
                new AccountMeta(mint, false, false),   // Mint: Read-Only, Non-Signer
                new AccountMeta(owner, false, false),  // Owner: Read-Only, Non-Signer
                new AccountMeta(rent, false, false)    // Rent: Read-Only, Non-Signer
        ));

        if (additionalAccounts != null) {
            this.keys.addAll(additionalAccounts.stream().map(AccountMeta::roleReadOnlySigner).toList());
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

    /**
     * Static factory method for creating a fully configured InitializeAccount instruction.
     *
     * @param account The account to initialize.
     * @param mint The token mint associated with the account.
     * @param owner The owner of the account.
     * @param rent (Optional) Rent sysvar account. Defaults to SysvarRent if null.
     * @param additionalAccounts (Optional) Remaining accounts to include.
     * @return Fully configured TokenInstruction01InitializeAccount instance.
     */
    public static TokenInstruction01InitializeAccount create(
            @NonNull PublicKey account,
            @NonNull PublicKey mint,
            @NonNull PublicKey owner,
            PublicKey rent,
            List<PublicKey> additionalAccounts
    ) {
        // Validate inputs
        validateInputs(account, mint, owner);

        // Use default rent if not provided
        if (rent == null) {
            rent = Sysvar.SYSVAR_RENT_ADDRESS;
        }

        TokenInstruction01InitializeAccount instruction = new TokenInstruction01InitializeAccount();
        instruction.setKeys(account, mint, owner, rent, additionalAccounts);

        return instruction;
    }

    /**
     * Validates the input for InitializeAccount instruction.
     *
     * @param account The account to initialize.
     * @param mint The token mint associated with the account.
     * @param owner The owner of the account.
     */
    private static void validateInputs(@NonNull PublicKey account, @NonNull PublicKey mint, @NonNull PublicKey owner) {
        Objects.requireNonNull(account, "Account must not be null.");
        Objects.requireNonNull(mint, "Mint must not be null.");
        Objects.requireNonNull(owner, "Owner must not be null.");
    }
}