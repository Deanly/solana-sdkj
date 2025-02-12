package net.deanly.solana.sdk.program.raydium.cpmm.instruction;

import lombok.*;
import net.deanly.solana.sdk.program.raydium.cpmm.RaydiumCpmmProgram;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.Bytes8Field;
import net.deanly.structlayout.type.basic.UInt64LEField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes a swap in a CPMM Pool (Base Input).
 *
 * <pre>
 * Accounts expected:
 * 0. `[signer]` The payer of the transaction.
 * 1. `[]` The authority of the pool.
 * 2. `[]` The config account.
 * 3. `[writable]` The CPMM pool account.
 * 4. `[writable]` The user's input token account.
 * 5. `[writable]` The user's output token account.
 * 6. `[writable]` The input token vault account.
 * 7. `[writable]` The output token vault account.
 * 8. `[]` The input token program.
 * 9. `[]` The output token program.
 * 10. `[]` The input mint.
 * 11. `[]` The output mint.
 * 12. `[writable]` The observation account.
 * </pre>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CpmmInstruction03SwapBaseInput extends CpmmInstructionBase implements TransactionInstruction {

    @StructField(order = 1, type = Bytes8Field.class)
    private final byte[] discriminator = new byte[] {
            (byte) 143, (byte) 190, (byte) 90, (byte) 218, (byte) 196, (byte) 30, (byte) 51, (byte) 222
    }; // Discriminator for SwapBaseInput (index 3)

    @Setter
    @StructField(order = 2, type = UInt64LEField.class)
    private long amountIn; // Amount of input tokens to swap.

    @Setter
    @StructField(order = 3, type = UInt64LEField.class)
    private long amountOutMin; // Minimum amount of output tokens expected.

    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Sets the account metadata (keys) for the SwapBaseInput instruction.
     *
     * @param payer             The payer of the transaction.
     * @param authority         The authority of the pool.
     * @param configId          The config account of the pool.
     * @param poolId            The CPMM pool account.
     * @param userInputAccount  The user's input token account (writable).
     * @param userOutputAccount The user's output token account (writable).
     * @param inputVault        The input token vault account (writable).
     * @param outputVault       The output token vault account (writable).
     * @param inputTokenProgram The input token program (read-only).
     * @param outputTokenProgram The output token program (read-only).
     * @param inputMint         The input token mint (read-only).
     * @param outputMint        The output token mint (read-only).
     * @param observationId     The observation account (writable).
     */
    public void setKeys(
            PublicKey payer,
            PublicKey authority,
            PublicKey configId,
            PublicKey poolId,
            PublicKey userInputAccount,
            PublicKey userOutputAccount,
            PublicKey inputVault,
            PublicKey outputVault,
            PublicKey inputTokenProgram,
            PublicKey outputTokenProgram,
            PublicKey inputMint,
            PublicKey outputMint,
            PublicKey observationId
    ) {
        if (payer == null || authority == null || configId == null || poolId == null ||
                userInputAccount == null || userOutputAccount == null || inputVault == null ||
                outputVault == null || inputTokenProgram == null || outputTokenProgram == null ||
                inputMint == null || outputMint == null || observationId == null) {
            throw new IllegalArgumentException("All accounts must be provided and non-null.");
        }

        this.keys = new ArrayList<>();
        this.keys.add(new AccountMeta(payer, true, false)); // Payer: signer, not writable
        this.keys.add(new AccountMeta(authority, false, false)); // Authority: read-only, not writable
        this.keys.add(new AccountMeta(configId, false, false)); // Config: read-only, not writable
        this.keys.add(new AccountMeta(poolId, false, true)); // Pool: writable, not signer
        this.keys.add(new AccountMeta(userInputAccount, false, true)); // User input: writable, not signer
        this.keys.add(new AccountMeta(userOutputAccount, false, true)); // User output: writable, not signer
        this.keys.add(new AccountMeta(inputVault, false, true)); // Input vault: writable, not signer
        this.keys.add(new AccountMeta(outputVault, false, true)); // Output vault: writable, not signer
        this.keys.add(new AccountMeta(inputTokenProgram, false, false)); // Input program: read-only, not writable
        this.keys.add(new AccountMeta(outputTokenProgram, false, false)); // Output program: read-only, not writable
        this.keys.add(new AccountMeta(inputMint, false, false)); // Input mint: read-only, not writable
        this.keys.add(new AccountMeta(outputMint, false, false)); // Output mint: read-only, not writable
        this.keys.add(new AccountMeta(observationId, false, true)); // Observation: writable, not signer
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
        // Encodes the discriminator, amountIn, and amountOutMin.
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        // Decode fields (discriminator, amountIn, amountOutMin). Keys must be set explicitly.
        CpmmInstruction03SwapBaseInput decoded = StructLayout.decode(data, CpmmInstruction03SwapBaseInput.class);
        this.amountIn = decoded.amountIn;
        this.amountOutMin = decoded.amountOutMin;
    }

    /**
     * Static factory method to create and configure a SwapBaseInput instruction.
     *
     * @param payer             The payer of the transaction.
     * @param authority         The authority of the pool.
     * @param configId          The config account of the pool.
     * @param poolId            The CPMM pool account.
     * @param userInputAccount  The user's input token account (writable).
     * @param userOutputAccount The user's output token account (writable).
     * @param inputVault        The input token vault account (writable).
     * @param outputVault       The output token vault account (writable).
     * @param inputTokenProgram The input token program (read-only).
     * @param outputTokenProgram The output token program (read-only).
     * @param inputMint         The input token mint (read-only).
     * @param outputMint        The output token mint (read-only).
     * @param observationId     The observation account (writable).
     * @param amountIn          The amount of input tokens to swap.
     * @param amountOutMin      The minimum amount of output tokens expected.
     * @return A fully configured CpmmInstruction03SwapBaseInput instance.
     */
    public static CpmmInstruction03SwapBaseInput create(
            PublicKey payer,
            PublicKey authority,
            PublicKey configId,
            PublicKey poolId,
            PublicKey userInputAccount,
            PublicKey userOutputAccount,
            PublicKey inputVault,
            PublicKey outputVault,
            PublicKey inputTokenProgram,
            PublicKey outputTokenProgram,
            PublicKey inputMint,
            PublicKey outputMint,
            PublicKey observationId,
            long amountIn,
            long amountOutMin
    ) {
        CpmmInstruction03SwapBaseInput instruction = new CpmmInstruction03SwapBaseInput();
        instruction.setAmountIn(amountIn);
        instruction.setAmountOutMin(amountOutMin);
        instruction.setKeys(payer, authority, configId, poolId, userInputAccount, userOutputAccount, inputVault, outputVault, inputTokenProgram, outputTokenProgram, inputMint, outputMint, observationId);
        return instruction;
    }
}