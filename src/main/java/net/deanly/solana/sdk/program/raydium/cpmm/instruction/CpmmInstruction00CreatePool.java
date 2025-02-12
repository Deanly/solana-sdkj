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
 * Initializes a CPMM Pool (Create Pool).
 *
 * <pre>
 * Accounts expected:
 * 0. `[signer]` The creator of the pool.
 * 1. `[]` The pool configuration account.
 * 2. `[]` The pool authority account.
 * 3. `[writable]` The pool account.
 * 4. `[]` Token A mint.
 * 5. `[]` Token B mint.
 * 6. `[writable]` LP token mint account.
 * 7. `[writable]` User's token A vault account.
 * 8. `[writable]` User's token B vault account.
 * 9. `[writable]` User's LP token account.
 * 10. `[writable]` Pool token A vault account.
 * 11. `[writable]` Pool token B vault account.
 * 12. `[writable]` Pool creation fee account.
 * 13. `[writable]` Observation account.
 * 14. `[]` Token program.
 * 15. `[]` Associated Token program.
 * 16. `[]` System program.
 * 17. `[]` Rent program.
 * </pre>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CpmmInstruction00CreatePool extends CpmmInstructionBase implements TransactionInstruction {

    @StructField(order = 1, type = Bytes8Field.class)
    private final byte[] discriminator = new byte[] {
            (byte) 175, (byte) 175, (byte) 109, (byte) 31, (byte) 13, (byte) 152, (byte) 155, (byte) 237
    }; // Discriminator for CreatePool (index 0)

    @Setter
    @StructField(order = 2, type = UInt64LEField.class)
    private long amountMaxA; // Maximum amount of Token A allowed.

    @Setter
    @StructField(order = 3, type = UInt64LEField.class)
    private long amountMaxB; // Maximum amount of Token B allowed.

    @Setter
    @StructField(order = 4, type = UInt64LEField.class)
    private long openTime; // Time when the pool will open.

    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Sets the account metadata (keys) for the CreatePool instruction.
     *
     * @param creator           The creator of the pool.
     * @param configId          The pool configuration account.
     * @param authority         The pool authority account.
     * @param poolId            The pool account.
     * @param mintA             Token A mint.
     * @param mintB             Token B mint.
     * @param lpMint            LP token mint account (writable).
     * @param userVaultA        User's token A vault account (writable).
     * @param userVaultB        User's token B vault account (writable).
     * @param userLpAccount     User's LP token account (writable).
     * @param vaultA            Pool token A vault account (writable).
     * @param vaultB            Pool token B vault account (writable).
     * @param createPoolFeeAccount Pool creation fee account (writable).
     * @param observationId     Observation account (writable).
     * @param tokenProgram      Token program.
     * @param associatedTokenProgram Associated Token program.
     * @param systemProgram     System program.
     * @param rentProgram       Rent program.
     */
    public void setKeys(
            PublicKey creator,
            PublicKey configId,
            PublicKey authority,
            PublicKey poolId,
            PublicKey mintA,
            PublicKey mintB,
            PublicKey lpMint,
            PublicKey userVaultA,
            PublicKey userVaultB,
            PublicKey userLpAccount,
            PublicKey vaultA,
            PublicKey vaultB,
            PublicKey createPoolFeeAccount,
            PublicKey observationId,
            PublicKey tokenProgram,
            PublicKey associatedTokenProgram,
            PublicKey systemProgram,
            PublicKey rentProgram
    ) {
        if (creator == null || configId == null || authority == null || poolId == null ||
                mintA == null || mintB == null || lpMint == null || userVaultA == null ||
                userVaultB == null || userLpAccount == null || vaultA == null || vaultB == null ||
                createPoolFeeAccount == null || observationId == null || tokenProgram == null ||
                associatedTokenProgram == null || systemProgram == null || rentProgram == null) {
            throw new IllegalArgumentException("All accounts must be provided and non-null.");
        }

        this.keys = new ArrayList<>();
        this.keys.add(new AccountMeta(creator, true, false)); // Creator: signer, not writable
        this.keys.add(new AccountMeta(configId, false, false)); // Config: read-only, not writable
        this.keys.add(new AccountMeta(authority, false, false)); // Authority: read-only, not writable
        this.keys.add(new AccountMeta(poolId, false, true)); // Pool: writable, not signer
        this.keys.add(new AccountMeta(mintA, false, false)); // Mint A: read-only, not writable
        this.keys.add(new AccountMeta(mintB, false, false)); // Mint B: read-only, not writable
        this.keys.add(new AccountMeta(lpMint, false, true)); // LP Mint: writable, not signer
        this.keys.add(new AccountMeta(userVaultA, false, true)); // User Vault A: writable, not signer
        this.keys.add(new AccountMeta(userVaultB, false, true)); // User Vault B: writable, not signer
        this.keys.add(new AccountMeta(userLpAccount, false, true)); // User LP Account: writable, not signer
        this.keys.add(new AccountMeta(vaultA, false, true)); // Vault A: writable, not signer
        this.keys.add(new AccountMeta(vaultB, false, true)); // Vault B: writable, not signer
        this.keys.add(new AccountMeta(createPoolFeeAccount, false, true)); // Fee Account: writable, not signer
        this.keys.add(new AccountMeta(observationId, false, true)); // Observation: writable, not signer
        this.keys.add(new AccountMeta(tokenProgram, false, false)); // Token Program: read-only, not writable
        this.keys.add(new AccountMeta(associatedTokenProgram, false, false)); // Associated Token Program: read-only, not writable
        this.keys.add(new AccountMeta(systemProgram, false, false)); // System Program: read-only, not writable
        this.keys.add(new AccountMeta(rentProgram, false, false)); // Rent Program: read-only, not writable
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
        // Encodes the discriminator, amountMaxA, amountMaxB, and openTime.
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        // Decode fields (discriminator, amountMaxA, amountMaxB, openTime). Keys must be set explicitly.
        CpmmInstruction00CreatePool decoded = StructLayout.decode(data, CpmmInstruction00CreatePool.class);
        this.amountMaxA = decoded.amountMaxA;
        this.amountMaxB = decoded.amountMaxB;
        this.openTime = decoded.openTime;
    }

    /**
     * Static factory method to create and configure a CreatePool instruction.
     *
     * @param creator           The creator of the pool.
     * @param configId          The pool configuration account.
     * @param authority         The pool authority account.
     * @param poolId            The pool account.
     * @param mintA             Token A mint.
     * @param mintB             Token B mint.
     * @param lpMint            LP token mint account (writable).
     * @param userVaultA        User's token A vault account (writable).
     * @param userVaultB        User's token B vault account (writable).
     * @param userLpAccount     User's LP token account (writable).
     * @param vaultA            Pool token A vault account (writable).
     * @param vaultB            Pool token B vault account (writable).
     * @param createPoolFeeAccount Pool creation fee account (writable).
     * @param observationId     Observation account (writable).
     * @param tokenProgram      Token program.
     * @param associatedTokenProgram Associated Token program.
     * @param systemProgram     System program.
     * @param rentProgram       Rent program.
     * @param amountMaxA        Maximum amount of Token A allowed.
     * @param amountMaxB        Maximum amount of Token B allowed.
     * @param openTime          Time when the pool will open.
     * @return A fully configured CpmmInstruction00CreatePool instance.
     */
    public static CpmmInstruction00CreatePool create(
            PublicKey creator,
            PublicKey configId,
            PublicKey authority,
            PublicKey poolId,
            PublicKey mintA,
            PublicKey mintB,
            PublicKey lpMint,
            PublicKey userVaultA,
            PublicKey userVaultB,
            PublicKey userLpAccount,
            PublicKey vaultA,
            PublicKey vaultB,
            PublicKey createPoolFeeAccount,
            PublicKey observationId,
            PublicKey tokenProgram,
            PublicKey associatedTokenProgram,
            PublicKey systemProgram,
            PublicKey rentProgram,
            long amountMaxA,
            long amountMaxB,
            long openTime
    ) {
        CpmmInstruction00CreatePool instruction = new CpmmInstruction00CreatePool();
        instruction.setAmountMaxA(amountMaxA);
        instruction.setAmountMaxB(amountMaxB);
        instruction.setOpenTime(openTime);
        instruction.setKeys(creator, configId, authority, poolId, mintA, mintB, lpMint, userVaultA, userVaultB, userLpAccount, vaultA, vaultB, createPoolFeeAccount, observationId, tokenProgram, associatedTokenProgram, systemProgram, rentProgram);
        return instruction;
    }
}