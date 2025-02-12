package net.deanly.solana.sdk.program.spl.ata.instruction;

import lombok.*;
import net.deanly.solana.sdk.program.spl.token.SplTokenProgram;
import net.deanly.solana.sdk.program.core.system.SystemProgram;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.spl.ata.AssociatedTokenAccountProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Represents the instruction `CreateAssociatedToken` for the Solana Blockchain `AssociatedTokenProgram`.
 * <p>
 * This instruction is used to create an associated token account for a given wallet and mint.
 * It utilizes the predefined program ID for `AssociatedTokenProgram` and requires specific
 * account metadata and configuration to execute properly.
 * </p>
 *
 * <p>
 * This class extends {@link AssociatedTokenAccountProgram} and implements the {@link TransactionInstruction}
 * interface, encapsulating the instruction logic, account metadata, and related data.
 * </p>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AssociatedTokenAccountInstruction0Create extends SplAtaInstructionBase implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 0; // Discriminator for the instruction.

    /**
     * <pre>
     * Expected Accounts:
     *   0. `[signer, writable]` Payer account.
     *   1. `[writable]` Associated token account to create.
     *   2. `[]` Wallet address (owning the associated token account).
     *   3. `[]` Token mint.
     *   4. `[]` System program.
     *   5. `[]` SPL Token program.
     * </pre>
     */
    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of account metadata for the instruction.

    /**
     * Sets the account metadata (`keys`) for this instruction.
     *
     * @param payer        Payer account (signer, writable).
     * @param owner        Wallet address (readonly).
     * @param mint         Token mint (readonly).
     */
    public void setKeys(
            @NonNull PublicKey payer,
            @NonNull PublicKey owner,
            @NonNull PublicKey mint
    ) {
        // Clear any existing keys
        this.keys.clear();

        PublicKey ata = findAssociatedTokenAddress(owner, mint);

        // Add keys to the instruction in the expected order
        this.keys.add(new AccountMeta(payer, true, true)); // Payer: Signer & Writable
        this.keys.add(new AccountMeta(ata, false, true)); // Associated token account: Writable, not signer
        this.keys.add(new AccountMeta(owner, false, false)); // Wallet address: Readonly, not signer
        this.keys.add(new AccountMeta(mint, false, false)); // Token mint: Readonly, not signer
        this.keys.add(new AccountMeta(SystemProgram.PROGRAM_ID, false, false)); // System program: Readonly, not signer
        this.keys.add(new AccountMeta(SplTokenProgram.PROGRAM_ID, false, false)); // SPL Token program: Readonly, not signer
    }

    /**
     * Provides the account metadata (`keys`) for this instruction.
     *
     * @return List of account metadata.
     */
    @Override
    public List<AccountMeta> getKeys() {
        return keys == null ? Collections.emptyList() : keys;
    }

    /**
     * Encodes the instruction data for CreateAssociatedToken.
     *
     * @return Encoded byte array for this instruction.
     */
    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    /**
     * Static factory method to create and configure a CreateAssociatedToken instruction.
     *
     * @param payer        Payer account (signer, writable).
     * @param owner        Wallet address (readonly).
     * @param mint         Token mint (readonly).
     * @return Configured CreateAssociatedToken instruction.
     */
    public static AssociatedTokenAccountInstruction0Create create(
            @NonNull PublicKey payer,
            @NonNull PublicKey owner,
            @NonNull PublicKey mint
    ) {
        // Create a new instruction instance
        AssociatedTokenAccountInstruction0Create instruction = new AssociatedTokenAccountInstruction0Create();

        // Set the account keys for the instruction
        instruction.setKeys(payer, owner, mint);

        // Return the configured instruction
        return instruction;
    }
}