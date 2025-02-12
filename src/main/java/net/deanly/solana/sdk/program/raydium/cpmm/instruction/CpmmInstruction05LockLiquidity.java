package net.deanly.solana.sdk.program.raydium.cpmm.instruction;

import lombok.*;
import net.deanly.solana.sdk.program.raydium.cpmm.RaydiumCpmmProgram;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt64LEField;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.structlayout.type.borsh.BorshBooleanField;

import java.util.ArrayList;
import java.util.List;

/**
 * Locks liquidity in a CPMM pool.
 *
 * <pre>
 * Accounts expected:
 * 0. `[]` The pool authority account.
 * 1. `[signer, writable]` The payer account.
 * 2. `[writable]` The liquidity owner's account.
 * 3. `[writable]` The NFT owner account.
 * 4. `[writable]` The NFT mint account.
 * 5. `[writable]` The NFT token account.
 * 6. `[writable]` The pool account.
 * 7. `[writable]` The lock PDA account.
 * 8. `[writable]` The LP token mint account.
 * 9. `[writable]` User's LP vault account.
 * 10. `[writable]` Lock program LP vault account.
 * 11. `[writable]` Pool vault A account.
 * 12. `[writable]` Pool vault B account.
 * 13. `[writable]` Metadata account.
 * 14. `[]` Rent program.
 * 15. `[]` System program.
 * 16. `[]` Token program.
 * 17. `[]` Associated token program.
 * 18. `[]` Metadata program.
 * </pre>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CpmmInstruction05LockLiquidity extends RaydiumCpmmProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt64LEField.class)
    private final int discriminator = 5; // Discriminator for LockLiquidity (index 5)

    @Setter
    @StructField(order = 2, type = UInt64LEField.class)
    private long lpAmount; // The amount of LP tokens to lock.

    @Setter
    @StructField(order = 3, type = BorshBooleanField.class)
    private boolean withMetadata; // Whether metadata is attached to the lock.

    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Sets the account metadata (keys) for the LockLiquidity instruction.
     *
     * @param authority        The pool authority account.
     * @param payer            The payer account (signer, writable).
     * @param liquidityOwner   The liquidity owner's account (writable).
     * @param nftOwner         The NFT owner's account (writable).
     * @param nftMint          The NFT mint account (writable).
     * @param nftAccount       The NFT token account (writable).
     * @param poolId           The pool account (writable).
     * @param lockPda          The lock PDA account (writable).
     * @param lpMint           The LP token mint account (writable).
     * @param userLpVault      User's LP vault account (writable).
     * @param lockLpVault      Lock program LP vault account (writable).
     * @param poolVaultA       Pool token A vault account (writable).
     * @param poolVaultB       Pool token B vault account (writable).
     * @param metadataAccount  Metadata account (writable).
     * @param rentProgram      Rent program.
     * @param systemProgram    System program.
     * @param tokenProgram     Token program.
     * @param associatedTokenProgram Associated token program.
     * @param metadataProgram  Metadata program.
     */
    public void setKeys(
            PublicKey authority,
            PublicKey payer,
            PublicKey liquidityOwner,
            PublicKey nftOwner,
            PublicKey nftMint,
            PublicKey nftAccount,
            PublicKey poolId,
            PublicKey lockPda,
            PublicKey lpMint,
            PublicKey userLpVault,
            PublicKey lockLpVault,
            PublicKey poolVaultA,
            PublicKey poolVaultB,
            PublicKey metadataAccount,
            PublicKey rentProgram,
            PublicKey systemProgram,
            PublicKey tokenProgram,
            PublicKey associatedTokenProgram,
            PublicKey metadataProgram
    ) {
        if (authority == null || payer == null || liquidityOwner == null || nftOwner == null || nftMint == null ||
                nftAccount == null || poolId == null || lockPda == null || lpMint == null || userLpVault == null ||
                lockLpVault == null || poolVaultA == null || poolVaultB == null || metadataAccount == null ||
                rentProgram == null || systemProgram == null || tokenProgram == null ||
                associatedTokenProgram == null || metadataProgram == null) {
            throw new IllegalArgumentException("All accounts must be provided and non-null.");
        }

        this.keys = new ArrayList<>();
        this.keys.add(new AccountMeta(authority, false, false)); // Authority: read-only, not writable
        this.keys.add(new AccountMeta(payer, true, true)); // Payer: signer, writable
        this.keys.add(new AccountMeta(liquidityOwner, false, true)); // Liquidity Owner: writable, not signer
        this.keys.add(new AccountMeta(nftOwner, false, true)); // NFT Owner: writable, not signer
        this.keys.add(new AccountMeta(nftMint, false, true)); // NFT Mint: writable, not signer
        this.keys.add(new AccountMeta(nftAccount, false, true)); // NFT Account: writable, not signer
        this.keys.add(new AccountMeta(poolId, false, true)); // Pool: writable, not signer
        this.keys.add(new AccountMeta(lockPda, false, true)); // Lock PDA: writable, not signer
        this.keys.add(new AccountMeta(lpMint, false, true)); // LP Mint: writable, not signer
        this.keys.add(new AccountMeta(userLpVault, false, true)); // User LP Vault: writable, not signer
        this.keys.add(new AccountMeta(lockLpVault, false, true)); // Lock LP Vault: writable, not signer
        this.keys.add(new AccountMeta(poolVaultA, false, true)); // Pool Vault A: writable, not signer
        this.keys.add(new AccountMeta(poolVaultB, false, true)); // Pool Vault B: writable, not signer
        this.keys.add(new AccountMeta(metadataAccount, false, true)); // Metadata: writable, not signer
        this.keys.add(new AccountMeta(rentProgram, false, false)); // Rent Program: read-only, not writable
        this.keys.add(new AccountMeta(systemProgram, false, false)); // System Program: read-only, not writable
        this.keys.add(new AccountMeta(tokenProgram, false, false)); // Token Program: read-only, not writable
        this.keys.add(new AccountMeta(associatedTokenProgram, false, false)); // Associated Token Program: read-only, not writable
        this.keys.add(new AccountMeta(metadataProgram, false, false)); // Metadata Program: read-only, not writable
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
        // Encodes the discriminator, lpAmount, and withMetadata.
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        // Decode fields (discriminator, lpAmount, withMetadata). Keys must be set explicitly.
        CpmmInstruction05LockLiquidity decoded = StructLayout.decode(data, CpmmInstruction05LockLiquidity.class);
        this.lpAmount = decoded.lpAmount;
        this.withMetadata = decoded.withMetadata;
    }

    /**
     * Static factory method to create and configure a LockLiquidity instruction.
     *
     * @param authority        The pool authority account.
     * @param payer            The payer account (signer, writable).
     * @param liquidityOwner   The liquidity owner's account (writable).
     * @param nftOwner         The NFT owner's account (writable).
     * @param nftMint          The NFT mint account (writable).
     * @param nftAccount       The NFT token account (writable).
     * @param poolId           The pool account (writable).
     * @param lockPda          The lock PDA account (writable).
     * @param lpMint           The LP token mint account (writable).
     * @param userLpVault      User's LP vault account (writable).
     * @param lockLpVault      Lock program LP vault account (writable).
     * @param poolVaultA       Pool token A vault account (writable).
     * @param poolVaultB       Pool token B vault account (writable).
     * @param metadataAccount  Metadata account (writable).
     * @param rentProgram      Rent program.
     * @param systemProgram    System program.
     * @param tokenProgram     Token program.
     * @param associatedTokenProgram Associated token program.
     * @param metadataProgram  Metadata program.
     * @param lpAmount         The amount of LP tokens to lock.
     * @param withMetadata     Whether metadata is attached to the lock.
     * @return A fully configured CpmmInstruction05LockLiquidity instance.
     */
    public static CpmmInstruction05LockLiquidity create(
            PublicKey authority,
            PublicKey payer,
            PublicKey liquidityOwner,
            PublicKey nftOwner,
            PublicKey nftMint,
            PublicKey nftAccount,
            PublicKey poolId,
            PublicKey lockPda,
            PublicKey lpMint,
            PublicKey userLpVault,
            PublicKey lockLpVault,
            PublicKey poolVaultA,
            PublicKey poolVaultB,
            PublicKey metadataAccount,
            PublicKey rentProgram,
            PublicKey systemProgram,
            PublicKey tokenProgram,
            PublicKey associatedTokenProgram,
            PublicKey metadataProgram,
            long lpAmount,
            boolean withMetadata
    ) {
        CpmmInstruction05LockLiquidity instruction = new CpmmInstruction05LockLiquidity();
        instruction.setLpAmount(lpAmount);
        instruction.setWithMetadata(withMetadata);
        instruction.setKeys(authority, payer, liquidityOwner, nftOwner, nftMint, nftAccount, poolId, lockPda, lpMint, userLpVault, lockLpVault, poolVaultA, poolVaultB, metadataAccount, rentProgram, systemProgram, tokenProgram, associatedTokenProgram, metadataProgram);
        return instruction;
    }
}