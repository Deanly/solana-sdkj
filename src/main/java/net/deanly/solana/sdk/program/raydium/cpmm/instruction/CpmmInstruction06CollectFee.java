package net.deanly.solana.sdk.program.raydium.cpmm.instruction;

import lombok.*;
import net.deanly.solana.sdk.program.raydium.cpmm.RaydiumCpmmProgram;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt64LEField;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects fees from a CPMM pool.
 *
 * <pre>
 * Accounts expected:
 * 0. `[]` The pool authority account.
 * 1. `[signer]` The fee collector (owner) account.
 * 2. `[writable]` The NFT token account.
 * 3. `[writable]` The lock PDA account.
 * 4. `[writable]` The pool account.
 * 5. `[writable]` The LP token mint account.
 * 6. `[writable]` User's token A vault.
 * 7. `[writable]` User's token B vault.
 * 8. `[writable]` Pool token A vault.
 * 9. `[writable]` Pool token B vault.
 * 10. `[]` Token A mint.
 * 11. `[]` Token B mint.
 * 12. `[writable]` Lock program LP vault account.
 * 13. `[]` Token program.
 * 14. `[]` Token 2022 program.
 * 15. `[]` Memo program.
 * </pre>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CpmmInstruction06CollectFee extends RaydiumCpmmProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt64LEField.class)
    private final int discriminator = 6; // Discriminator for CollectFee (index 6)

    @Setter
    @StructField(order = 2, type = UInt64LEField.class)
    private long lpFeeAmount; // The amount of LP tokens to collect as fees.

    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Sets the account metadata (keys) for the CollectFee instruction.
     *
     * @param authority        The pool authority account.
     * @param feeCollector     The fee collector account (signer).
     * @param nftAccount       The NFT token account (writable).
     * @param lockPda          The lock PDA account (writable).
     * @param poolId           The pool account (writable).
     * @param lpMint           The LP token mint account (writable).
     * @param userVaultA       User's token A vault (writable).
     * @param userVaultB       User's token B vault (writable).
     * @param poolVaultA       Pool token A vault (writable).
     * @param poolVaultB       Pool token B vault (writable).
     * @param mintA            Token A mint.
     * @param mintB            Token B mint.
     * @param lockLpVault      Lock program LP vault account (writable).
     * @param tokenProgram     Token program.
     * @param token2022Program Token 2022 program.
     * @param memoProgram      Memo program.
     */
    public void setKeys(
            PublicKey authority,
            PublicKey feeCollector,
            PublicKey nftAccount,
            PublicKey lockPda,
            PublicKey poolId,
            PublicKey lpMint,
            PublicKey userVaultA,
            PublicKey userVaultB,
            PublicKey poolVaultA,
            PublicKey poolVaultB,
            PublicKey mintA,
            PublicKey mintB,
            PublicKey lockLpVault,
            PublicKey tokenProgram,
            PublicKey token2022Program,
            PublicKey memoProgram
    ) {
        if (authority == null || feeCollector == null || nftAccount == null || lockPda == null || poolId == null ||
                lpMint == null || userVaultA == null || userVaultB == null || poolVaultA == null ||
                poolVaultB == null || mintA == null || mintB == null || lockLpVault == null ||
                tokenProgram == null || token2022Program == null || memoProgram == null) {
            throw new IllegalArgumentException("All accounts must be provided and non-null.");
        }

        this.keys = new ArrayList<>();
        this.keys.add(new AccountMeta(authority, false, false)); // Authority: read-only, not writable
        this.keys.add(new AccountMeta(feeCollector, true, false)); // Fee Collector: signer, not writable
        this.keys.add(new AccountMeta(nftAccount, false, true)); // NFT Account: writable, not signer
        this.keys.add(new AccountMeta(lockPda, false, true)); // Lock PDA: writable, not signer
        this.keys.add(new AccountMeta(poolId, false, true)); // Pool: writable, not signer
        this.keys.add(new AccountMeta(lpMint, false, true)); // LP Mint: writable, not signer
        this.keys.add(new AccountMeta(userVaultA, false, true)); // User Vault A: writable, not signer
        this.keys.add(new AccountMeta(userVaultB, false, true)); // User Vault B: writable, not signer
        this.keys.add(new AccountMeta(poolVaultA, false, true)); // Pool Vault A: writable, not signer
        this.keys.add(new AccountMeta(poolVaultB, false, true)); // Pool Vault B: writable, not signer
        this.keys.add(new AccountMeta(mintA, false, false)); // Mint A: read-only, not writable
        this.keys.add(new AccountMeta(mintB, false, false)); // Mint B: read-only, not writable
        this.keys.add(new AccountMeta(lockLpVault, false, true)); // Lock LP Vault: writable, not signer
        this.keys.add(new AccountMeta(tokenProgram, false, false)); // Token Program: read-only, not writable
        this.keys.add(new AccountMeta(token2022Program, false, false)); // Token 2022 Program: read-only, not writable
        this.keys.add(new AccountMeta(memoProgram, false, false)); // Memo Program: read-only, not writable
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
        // Encodes the discriminator and lpFeeAmount.
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        // Decode fields (discriminator, lpFeeAmount). Keys must be set explicitly.
        CpmmInstruction06CollectFee decoded = StructLayout.decode(data, CpmmInstruction06CollectFee.class);
        this.lpFeeAmount = decoded.lpFeeAmount;
    }

    /**
     * Static factory method to create and configure a CollectFee instruction.
     *
     * @param authority        The pool authority account.
     * @param feeCollector     The fee collector account (signer).
     * @param nftAccount       The NFT token account (writable).
     * @param lockPda          The lock PDA account (writable).
     * @param poolId           The pool account (writable).
     * @param lpMint           The LP token mint account (writable).
     * @param userVaultA       User's token A vault (writable).
     * @param userVaultB       User's token B vault (writable).
     * @param poolVaultA       Pool token A vault (writable).
     * @param poolVaultB       Pool token B vault (writable).
     * @param mintA            Token A mint.
     * @param mintB            Token B mint.
     * @param lockLpVault      Lock program LP vault account (writable).
     * @param tokenProgram     Token program.
     * @param token2022Program Token 2022 program.
     * @param memoProgram      Memo program.
     * @param lpFeeAmount      The amount of LP tokens to collect as fees.
     * @return A fully configured CpmmInstruction06CollectFee instance.
     */
    public static CpmmInstruction06CollectFee create(
            PublicKey authority,
            PublicKey feeCollector,
            PublicKey nftAccount,
            PublicKey lockPda,
            PublicKey poolId,
            PublicKey lpMint,
            PublicKey userVaultA,
            PublicKey userVaultB,
            PublicKey poolVaultA,
            PublicKey poolVaultB,
            PublicKey mintA,
            PublicKey mintB,
            PublicKey lockLpVault,
            PublicKey tokenProgram,
            PublicKey token2022Program,
            PublicKey memoProgram,
            long lpFeeAmount
    ) {
        CpmmInstruction06CollectFee instruction = new CpmmInstruction06CollectFee();
        instruction.setLpFeeAmount(lpFeeAmount);
        instruction.setKeys(authority, feeCollector, nftAccount, lockPda, poolId, lpMint, userVaultA, userVaultB, poolVaultA, poolVaultB, mintA, mintB, lockLpVault, tokenProgram, token2022Program, memoProgram);
        return instruction;
    }
}