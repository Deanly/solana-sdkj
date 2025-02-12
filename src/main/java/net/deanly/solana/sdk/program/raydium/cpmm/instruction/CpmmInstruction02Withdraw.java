package net.deanly.solana.sdk.program.raydium.cpmm.instruction;

import lombok.*;
import net.deanly.solana.sdk.program.raydium.cpmm.RaydiumCpmmProgram;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.Bytes8Field;
import net.deanly.structlayout.type.basic.UInt64LEField;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * Removes liquidity from a CPMM pool (Withdraw).
 *
 * <pre>
 * Accounts expected:
 * 0. `[signer]` The withdrawer's account.
 * 1. `[]` The pool authority account.
 * 2. `[writable]` The pool account.
 * 3. `[writable]` User's LP token account.
 * 4. `[writable]` User's token A vault account.
 * 5. `[writable]` User's token B vault account.
 * 6. `[writable]` Pool token A vault account.
 * 7. `[writable]` Pool token B vault account.
 * 8. `[]` Token program.
 * 9. `[]` Token A mint account.
 * 10. `[]` Token B mint account.
 * 11. `[writable]` LP token mint account.
 * </pre>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CpmmInstruction02Withdraw extends RaydiumCpmmProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = Bytes8Field.class)
    private final byte[] discriminator = new byte[] {
            (byte) 183, (byte) 18, (byte) 70, (byte) 156, (byte) 148, (byte) 109, (byte) 161, (byte) 34
    }; // Discriminator for Withdraw (index 2)

    @Setter
    @StructField(order = 2, type = UInt64LEField.class)
    private long lpAmount; // The amount of LP tokens to burn.

    @Setter
    @StructField(order = 3, type = UInt64LEField.class)
    private long amountMinA; // Minimum amount of Token A to receive.

    @Setter
    @StructField(order = 4, type = UInt64LEField.class)
    private long amountMinB; // Minimum amount of Token B to receive.

    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Sets the account metadata (keys) for the Withdraw instruction.
     *
     * @param withdrawer       The withdrawer's account (signer).
     * @param authority        The pool authority account.
     * @param poolId           The pool account (writable).
     * @param userLpAccount    User's LP token account (writable).
     * @param userVaultA       User's token A vault account (writable).
     * @param userVaultB       User's token B vault account (writable).
     * @param vaultA           Pool token A vault account (writable).
     * @param vaultB           Pool token B vault account (writable).
     * @param tokenProgram     Token program.
     * @param mintA            Token A mint account.
     * @param mintB            Token B mint account.
     * @param lpMint           LP token mint account (writable).
     */
    public void setKeys(
            PublicKey withdrawer,
            PublicKey authority,
            PublicKey poolId,
            PublicKey userLpAccount,
            PublicKey userVaultA,
            PublicKey userVaultB,
            PublicKey vaultA,
            PublicKey vaultB,
            PublicKey tokenProgram,
            PublicKey mintA,
            PublicKey mintB,
            PublicKey lpMint
    ) {
        if (withdrawer == null || authority == null || poolId == null || userLpAccount == null ||
                userVaultA == null || userVaultB == null || vaultA == null || vaultB == null ||
                tokenProgram == null || mintA == null || mintB == null || lpMint == null) {
            throw new IllegalArgumentException("All accounts must be provided and non-null.");
        }

        this.keys = new ArrayList<>();
        this.keys.add(new AccountMeta(withdrawer, true, false)); // Withdrawer: signer, not writable
        this.keys.add(new AccountMeta(authority, false, false)); // Authority: read-only, not writable
        this.keys.add(new AccountMeta(poolId, false, true)); // Pool: writable, not signer
        this.keys.add(new AccountMeta(userLpAccount, false, true)); // User LP Account: writable, not signer
        this.keys.add(new AccountMeta(userVaultA, false, true)); // User Vault A: writable, not signer
        this.keys.add(new AccountMeta(userVaultB, false, true)); // User Vault B: writable, not signer
        this.keys.add(new AccountMeta(vaultA, false, true)); // Vault A: writable, not signer
        this.keys.add(new AccountMeta(vaultB, false, true)); // Vault B: writable, not signer
        this.keys.add(new AccountMeta(tokenProgram, false, false)); // Token Program: read-only, not writable
        this.keys.add(new AccountMeta(mintA, false, false)); // Mint A: read-only, not writable
        this.keys.add(new AccountMeta(mintB, false, false)); // Mint B: read-only, not writable
        this.keys.add(new AccountMeta(lpMint, false, true)); // LP Mint: writable, not signer
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
        // Encodes the discriminator, lpAmount, amountMinA, and amountMinB.
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        // Decode fields (discriminator, lpAmount, amountMinA, amountMinB). Keys must be set explicitly.
        CpmmInstruction02Withdraw decoded = StructLayout.decode(data, CpmmInstruction02Withdraw.class);
        this.lpAmount = decoded.lpAmount;
        this.amountMinA = decoded.amountMinA;
        this.amountMinB = decoded.amountMinB;
    }

    /**
     * Static factory method to create and configure a Withdraw instruction.
     *
     * @param withdrawer       The withdrawer's account (signer).
     * @param authority        The pool authority account.
     * @param poolId           The pool account (writable).
     * @param userLpAccount    User's LP token account (writable).
     * @param userVaultA       User's token A vault account (writable).
     * @param userVaultB       User's token B vault account (writable).
     * @param vaultA           Pool token A vault account (writable).
     * @param vaultB           Pool token B vault account (writable).
     * @param tokenProgram     Token program.
     * @param mintA            Token A mint account.
     * @param mintB            Token B mint account.
     * @param lpMint           LP token mint account (writable).
     * @param lpAmount         The amount of LP tokens to burn.
     * @param amountMinA       Minimum amount of Token A to receive.
     * @param amountMinB       Minimum amount of Token B to receive.
     * @return A fully configured CpmmInstruction02Withdraw instance.
     */
    public static CpmmInstruction02Withdraw create(
            PublicKey withdrawer,
            PublicKey authority,
            PublicKey poolId,
            PublicKey userLpAccount,
            PublicKey userVaultA,
            PublicKey userVaultB,
            PublicKey vaultA,
            PublicKey vaultB,
            PublicKey tokenProgram,
            PublicKey mintA,
            PublicKey mintB,
            PublicKey lpMint,
            long lpAmount,
            long amountMinA,
            long amountMinB
    ) {
        CpmmInstruction02Withdraw instruction = new CpmmInstruction02Withdraw();
        instruction.setLpAmount(lpAmount);
        instruction.setAmountMinA(amountMinA);
        instruction.setAmountMinB(amountMinB);
        instruction.setKeys(withdrawer, authority, poolId, userLpAccount, userVaultA, userVaultB, vaultA, vaultB, tokenProgram, mintA, mintB, lpMint);
        return instruction;
    }
}