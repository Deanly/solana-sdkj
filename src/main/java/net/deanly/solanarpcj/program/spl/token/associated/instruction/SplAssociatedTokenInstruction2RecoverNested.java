package net.deanly.solanarpcj.program.spl.token.associated.instruction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import net.deanly.solanarpcj.program.spl.token.basic.SplTokenProgram;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.spl.token.associated.SplAssociatedTokenProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Represents a Solana program instruction for recovering a nested associated token account.
 *
 * <p>
 * This class is part of the {@link SplAssociatedTokenProgram} and implements the {@link TransactionInstruction}
 * interface. The instruction allows recovering a nested associated token account (ATA) and transferring
 * its contents to a target associated account, adhering to Solana program conventions.
 * </p>
 *
 * <p>
 * The instruction utilizes metadata for accounts involved in the transaction, such as writable,
 * read-only, and signer properties, as well as the program key, which serves to execute the logic
 * on the Solana blockchain.
 * </p>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SplAssociatedTokenInstruction2RecoverNested extends SplAssociatedTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 2; // Instruction discriminator index (fixed).

    /**
     * <pre>
     * Expected Accounts:
     *   0. `[writable]` NestedAssociatedAccountAddress
     *   1. `[]` NestedTokenMintAddress
     *   2. `[writable]` DestinationAssociatedAccountAddress
     *   3. `[]` OwnerAssociatedAccountAddress
     *   4. `[]` OwnerTokenMintAddress
     *   5. `[signer, writable]` WalletAddress
     *   6. `[]` TokenProgram
     * </pre>
     */
    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // Associated account meta information.

    /**
     * Sets the account metadata (`keys`) for this instruction based on Solana conventions.
     *
     * @param nestedAssociatedAccountAddress Writable address for nested associated token account.
     * @param nestedTokenMintAddress Read-only token mint for the nested account.
     * @param destinationAssociatedAccountAddress Writable associated token account (destination).
     * @param ownerAssociatedAccountAddress Read-only wallet owner address of the nested ATA.
     * @param ownerTokenMintAddress Read-only token mint for the owner's ATA.
     * @param walletAddress Signer, writable wallet address of the owner owning the nested ATA.
     */
    public void setKeys(
            @NonNull PublicKey nestedAssociatedAccountAddress,
            @NonNull PublicKey nestedTokenMintAddress,
            @NonNull PublicKey destinationAssociatedAccountAddress,
            @NonNull PublicKey ownerAssociatedAccountAddress,
            @NonNull PublicKey ownerTokenMintAddress,
            @NonNull PublicKey walletAddress
    ) {
        this.keys.clear();

        // Populate account meta information in correct Solana order
        this.keys.add(new AccountMeta(nestedAssociatedAccountAddress, false, true)); // Writable
        this.keys.add(new AccountMeta(nestedTokenMintAddress, false, false)); // ReadOnly
        this.keys.add(new AccountMeta(destinationAssociatedAccountAddress, false, true)); // Writable
        this.keys.add(new AccountMeta(ownerAssociatedAccountAddress, false, false)); // ReadOnly
        this.keys.add(new AccountMeta(ownerTokenMintAddress, false, false)); // ReadOnly
        this.keys.add(new AccountMeta(walletAddress, true, true)); // Signer & Writable
        this.keys.add(new AccountMeta(SplTokenProgram.PROGRAM_ID, false, false)); // ReadOnly
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
     * Encodes the instruction data for `RecoverNestedAssociatedToken`.
     *
     * @return Encoded byte array consisting of the discriminator field.
     */
    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    /**
     * Static factory method to create and configure a RecoverNestedAssociatedToken instruction.
     *
     * @param nestedAssociatedAccountAddress Writable address for nested associated token account.
     * @param nestedTokenMintAddress Read-only token mint for the nested account.
     * @param destinationAssociatedAccountAddress Writable associated token account (destination).
     * @param ownerAssociatedAccountAddress Read-only wallet owner address of the nested ATA.
     * @param ownerTokenMintAddress Read-only token mint for the owner's ATA.
     * @param walletAddress Signer, writable wallet address of the owner owning the nested ATA.
     * @return Configured RecoverNestedAssociatedToken instruction.
     */
    public static SplAssociatedTokenInstruction2RecoverNested create(
            @NonNull PublicKey nestedAssociatedAccountAddress,
            @NonNull PublicKey nestedTokenMintAddress,
            @NonNull PublicKey destinationAssociatedAccountAddress,
            @NonNull PublicKey ownerAssociatedAccountAddress,
            @NonNull PublicKey ownerTokenMintAddress,
            @NonNull PublicKey walletAddress
    ) {
        // Instantiate and set metadata
        SplAssociatedTokenInstruction2RecoverNested instruction =
                new SplAssociatedTokenInstruction2RecoverNested();
        instruction.setKeys(
                nestedAssociatedAccountAddress,
                nestedTokenMintAddress,
                destinationAssociatedAccountAddress,
                ownerAssociatedAccountAddress,
                ownerTokenMintAddress,
                walletAddress
        );
        return instruction;
    }
}