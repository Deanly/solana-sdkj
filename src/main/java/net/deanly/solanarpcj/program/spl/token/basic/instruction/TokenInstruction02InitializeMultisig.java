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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the InitializeMultisig instruction of the Token Program.
 * This instruction is used to create and configure a multisig account.
 * The multisig account requires multiple signers as specified by the `m` parameter
 * (the number of required signatures).
 *
 * This class provides functionality to set the required accounts and parameters needed
 * for constructing the InitializeMultisig instruction, such as the multisig account,
 * signer keys, optional rent sysvar, and additional accounts.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction02InitializeMultisig extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 2; // Discriminator for InitializeMultisig

    @Setter
    @StructField(order = 2, type = UInt8Field.class)
    private int m; // Number of required signers.

    /**
     * <pre>
     * Accounts expected:
     *   0. `[writable]` Multisig account to initialize.
     *   1. `[]` Rent sysvar.
     *   2+ `[signer]` The signer accounts.
     * </pre>
     */
    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Sets the account metadata (keys) for InitializeMultisig instruction.
     *
     * @param multisigAccount The multisig account to initialize. Writable, non-signer.
     * @param signerKeys List of public keys for the signers. Writable, signer.
     * @param rent Optional rent sysvar. Defaults to SysvarRent if null.
     */
    public void setKeys(
            @NonNull PublicKey multisigAccount,
            @NonNull List<PublicKey> signerKeys,
            PublicKey rent
    ) {
        if (signerKeys.isEmpty() || signerKeys.size() > 11) {
            throw new IllegalArgumentException("Invalid number of signer keys. Must be between 1 and 11.");
        }
        if (rent == null) {
            rent = Sysvar.SYSVAR_RENT_ADDRESS; // Default Rent account
        }

        // Keys 구성
        this.keys = new ArrayList<>();
        this.keys.add(new AccountMeta(multisigAccount, false, true)); // Multisig: Writable, Non-Signer
        this.keys.add(new AccountMeta(rent, false, false));          // Rent: Read-Only, Non-Signer

        // Signers
        signerKeys.forEach(signer -> this.keys.add(AccountMeta.roleReadOnlySigner(signer))); // Signers: Writable, Signer

        // Automatically set `m` if not explicitly defined
        this.m = signerKeys.size();
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
     * Creates a new TokenInstruction02InitializeMultisig.
     *
     * @param multisigAccount The multisig account to initialize. Writable, non-signer.
     * @param signerKeys List of public keys for the signers.
     * @param rent Optional rent sysvar. Defaults to the SysvarRent.
     * @return A fully configured TokenInstruction02InitializeMultisig instance.
     */
    public static TokenInstruction02InitializeMultisig create(
            @NonNull PublicKey multisigAccount,
            @NonNull List<PublicKey> signerKeys,
            PublicKey rent
    ) {
        // Validate inputs
        validateInputs(multisigAccount, signerKeys);

        // Initialize the instruction
        TokenInstruction02InitializeMultisig instruction = new TokenInstruction02InitializeMultisig();

        // Set keys and additional remaining accounts
        instruction.setKeys(multisigAccount, signerKeys, rent);

        return instruction;
    }

    /**
     * Validates the required inputs for the InitializeMultisig instruction.
     *
     * @param multisigAccount The multisig account to initialize.
     * @param signerKeys The list of public keys for the signers.
     */
    private static void validateInputs(@NonNull PublicKey multisigAccount, @NonNull List<PublicKey> signerKeys) {
        Objects.requireNonNull(multisigAccount, "Multisig account must not be null.");
        if (signerKeys == null || signerKeys.isEmpty()) {
            throw new IllegalArgumentException("At least one signer key must be provided.");
        }
    }

}