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
import java.util.List;

/**
 * TokenInstruction12TransferChecked represents the TransferChecked instruction for index 12
 * in the Token Program. It transfers a specified amount of tokens while validating the number
 * of decimals for the amount matches the token mint's decimals.
 *
 * Accounts expected:
 *   0. `[writable]` The source account (must be pre-filled with sufficient balance).
 *   1. `[writable]` The destination account.
 *   2. `[]` The token mint.
 *   3. `[signer]` The owner of the source account (single owner).
 *   Multisignature owner:
 *     3. `[]` The multisignature owner account.
 *     4+ `[signer]` M signer accounts.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction12TransferChecked extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 12; // Discriminator for TransferChecked (index 12)

    @Setter
    @StructField(order = 2, type = UInt64LEField.class)
    private long amount; // Amount of tokens to transfer.

    @Setter
    @StructField(order = 3, type = UInt8Field.class)
    private int decimals; // Number of decimals the transfer is validated against.

    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Sets the account metadata (keys) for the TransferChecked instruction.
     * Handles both single authority and multisignature authority configuration.
     *
     * @param source       The source account (writable).
     * @param mint         The token mint (read-only).
     * @param destination  The destination account (writable).
     * @param authority    The owner/delegate of the source account (read-only).
     * @param multiSigners Optional: A list of multisignature signer accounts.
     */
    public void setKeys(PublicKey source, PublicKey mint, PublicKey destination, PublicKey authority, List<PublicKey> multiSigners) {
        // Validate inputs
        if (source == null || mint == null || destination == null || authority == null) {
            throw new IllegalArgumentException("Source, mint, destination, and authority must not be null.");
        }

        this.keys = new ArrayList<>();

        // Add required accounts
        this.keys.add(new AccountMeta(source, true, false));    // Source: writable, not signer
        this.keys.add(new AccountMeta(mint, false, false));     // Mint: read-only, not signer
        this.keys.add(new AccountMeta(destination, true, false)); // Destination: writable, not signer
        this.keys.add(new AccountMeta(authority, false, multiSigners == null || multiSigners.isEmpty())); // Authority: read-only, signer if no multisigners

        // Add multisigners (if any exist)
        if (multiSigners != null && !multiSigners.isEmpty()) {
            for (PublicKey signer : multiSigners) {
                this.keys.add(new AccountMeta(signer, false, true)); // Multisigners: read-only, signer
            }
        }
    }

    @Override
    public List<AccountMeta> getKeys() {
        if (this.keys == null || this.keys.isEmpty()) {
            throw new IllegalStateException("Account metadata (keys) must be set before building the transaction.");
        }
        return this.keys;
    }

    @Override
    public byte[] getData() {
        // Encodes the discriminator, amount, and decimals.
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        // Decode fields (discriminator, amount, decimals). Keys must be set explicitly.
        TokenInstruction12TransferChecked decoded = StructLayout.decode(data, TokenInstruction12TransferChecked.class);
        this.amount = decoded.amount;
        this.decimals = decoded.decimals;
    }

    /**
     * Static factory method to create and configure a TransferChecked instruction.
     *
     * @param source       The source account (writable).
     * @param mint         The token mint (read-only).
     * @param destination  The destination account (writable).
     * @param authority    The owner/delegate of the source account (read-only).
     * @param amount       The amount to transfer.
     * @param decimals     The expected number of decimals.
     * @param multiSigners Optional: A list of multisignature signer accounts.
     * @return A fully configured TokenInstruction12TransferChecked instance.
     */
    public static TokenInstruction12TransferChecked create(
            PublicKey source,
            PublicKey mint,
            PublicKey destination,
            PublicKey authority,
            long amount,
            int decimals,
            List<PublicKey> multiSigners
    ) {
        // Validate inputs
        if (source == null || mint == null || destination == null || authority == null) {
            throw new IllegalArgumentException("Source, mint, destination, and authority must not be null.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (decimals < 0) {
            throw new IllegalArgumentException("Decimals cannot be negative.");
        }

        // Create and configure the instruction instance
        TokenInstruction12TransferChecked instruction = new TokenInstruction12TransferChecked();
        instruction.setAmount(amount);
        instruction.setDecimals(decimals);
        instruction.setKeys(source, mint, destination, authority, multiSigners);
        return instruction;
    }
}