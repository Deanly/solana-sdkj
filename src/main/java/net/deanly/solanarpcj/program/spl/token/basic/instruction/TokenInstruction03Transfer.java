package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import lombok.*;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.spl.token.basic.SplTokenProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt64LEField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Represents a Transfer instruction within the Token Program,
 * primarily responsible for transferring a specific amount of tokens
 * between accounts.
 *
 * <p>This class is designed to work with the Solana blockchain's Token Program.
 * It encapsulates the data structure and behavior required to perform
 * token transfers, including specifying source, destination, and authority
 * accounts as well as managing multisignature signers if applicable.</p>
 *
 * <p>The instruction is identified by a discriminator (index 3) and
 * includes the associated amount of tokens to be transferred. The relevant
 * accounts required to execute the instruction are provided as a list of
 * {@code AccountMeta} objects, which define the permissions and roles
 * for each account in the operation.</p>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction03Transfer extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 3; // Discriminator for Transfer instruction (index 3).

    @Setter
    @StructField(order = 2, type = UInt64LEField.class)
    private long amount; // The amount of tokens to transfer.

    /**
     * <pre>
     * Accounts expected:
     *   Single owner/delegate:
     *     0. `[writable]` Source account.
     *     1. `[writable]` Destination account.
     *     2. `[signer]` The source account's owner/delegate.
     *
     *   Multisignature owner/delegate:
     *     0. `[writable]` Source account.
     *     1. `[writable]` Destination account.
     *     2. `[]` The multisignature source account's delegate.
     *     3+ `[signer]` M signer accounts.
     * </pre>
     */
    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.


    /**
     * Sets the account metadata (keys) exclusively.
     *
     * @param source The source account (Writable).
     * @param destination The destination account (Writable).
     * @param authority The authority account (Signer).
     * @param multiSigners Optional: List of additional signers for multisignature accounts.
     */
    public void setKeys(
            @NonNull PublicKey source,
            @NonNull PublicKey destination,
            @NonNull PublicKey authority,
            List<PublicKey> multiSigners
    ) {
        // Validate essential fields
        validatePrimaryAccounts(source, destination, authority);

        // Build account metadata
        List<AccountMeta> accountMetas = new ArrayList<>();
        accountMetas.add(new AccountMeta(source, false, true)); // Source: Writable, not signer.
        accountMetas.add(new AccountMeta(destination, false, true)); // Destination: Writable, not signer.

        // Add additional multisigners
        if (multiSigners != null && !multiSigners.isEmpty()) {
            accountMetas.add(AccountMeta.roleReadOnlyNoSigner(authority)); // Authority: Read-only, not signer.
            for (PublicKey signer : multiSigners) {
                accountMetas.add(new AccountMeta(signer, true, false)); // All signers: Read-only, signer.
            }
        } else {
            accountMetas.add(AccountMeta.roleReadOnlySigner(authority)); // Authority: Read-only, signer.
        }

        this.keys = accountMetas;
    }

    /**
     * Validates the required primary accounts for this instruction.
     *
     * @param source The source account.
     * @param destination The destination account.
     * @param authority The authority account.
     * @throws IllegalArgumentException if any of the accounts is null.
     */
    private static void validatePrimaryAccounts(PublicKey source, PublicKey destination, PublicKey authority) {
        if (source == null) {
            throw new IllegalArgumentException("Source account must not be null.");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Destination account must not be null.");
        }
        if (authority == null) {
            throw new IllegalArgumentException("Authority account must not be null.");
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

    public void setData(byte[] data) {
        // Decode the structure fields (discriminator and amount only, as keys are not decoded here).
        TokenInstruction03Transfer decoded = StructLayout.decode(data, TokenInstruction03Transfer.class);
        this.amount = decoded.getAmount();
    }

    /**
     * Static factory method to create a fully-configured TransferInstruction.
     *
     * @param source The source account public key.
     * @param destination The destination account public key.
     * @param authority The authority account public key.
     * @param amount The amount of tokens to transfer.
     * @param multiSigners Optional: List of additional multisignature signers.
     * @return A fully-configured TransferInstruction instance.
     */
    public static TokenInstruction03Transfer create(
            @NonNull PublicKey source,
            @NonNull PublicKey destination,
            @NonNull PublicKey authority,
            long amount,
            List<PublicKey> multiSigners
    ) {
        // Validate inputs
        validatePrimaryAccounts(source, destination, authority);
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }

        // Create instruction
        TokenInstruction03Transfer instruction = new TokenInstruction03Transfer();
        instruction.amount = amount;

        // Populate keys
        instruction.setKeys(source, destination, authority, multiSigners);

        return instruction;
    }
}