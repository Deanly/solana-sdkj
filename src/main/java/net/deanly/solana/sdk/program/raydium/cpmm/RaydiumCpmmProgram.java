package net.deanly.solana.sdk.program.raydium.cpmm;

import lombok.NonNull;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.raydium.cpmm.instruction.*;

/**
 * <p>Standard AMM (CP-Swap, New)</p>
 * The RaydiumCpmmProgram class represents the CPMM (Constant Product Market Maker) program within the Raydium framework.
 * It provides functionalities to interact with a CPMM Pool, including creating pools, depositing liquidity, withdrawing liquidity,
 * executing swaps, locking liquidity, and collecting fees.
 */
public class RaydiumCpmmProgram {

    public static final PublicKey PROGRAM_ID_MAINNET = new PublicKey("CPMMoo8L3F4NbTegBCKVNunggL7H1ZpdTHKxQB5qKP1C");
    public static final PublicKey PROGRAM_ID_DEVNET = new PublicKey("CPMDWBwJDtYax9qW7AyRuVC19Cc4L4Vcy4n2BHAbHkCW");

    /**
     * Creates a new CPMM Pool.
     */
    public static CpmmInstruction00CreatePool createPool(
            @NonNull PublicKey creator,
            @NonNull PublicKey configId,
            @NonNull PublicKey authority,
            @NonNull PublicKey poolId,
            @NonNull PublicKey mintA,
            @NonNull PublicKey mintB,
            @NonNull PublicKey lpMint,
            @NonNull PublicKey userVaultA,
            @NonNull PublicKey userVaultB,
            @NonNull PublicKey userLpAccount,
            @NonNull PublicKey vaultA,
            @NonNull PublicKey vaultB,
            @NonNull PublicKey createPoolFeeAccount,
            @NonNull PublicKey mintProgramA,
            @NonNull PublicKey mintProgramB,
            @NonNull PublicKey observationId,
            long amountMaxA,
            long amountMaxB,
            long openTime
    ) {
        return null;
//        return CpmmInstruction00CreatePool.create(
//                creator, configId, authority, poolId, mintA, mintB, lpMint, userVaultA, userVaultB, userLpAccount,
//                vaultA, vaultB, createPoolFeeAccount, mintProgramA, mintProgramB, observationId,
//                amountMaxA, amountMaxB, openTime
//        );
    }

    /**
     * Deposits liquidity into a CPMM Pool.
     */
    public static CpmmInstruction01Deposit deposit(
            @NonNull PublicKey owner,
            @NonNull PublicKey authority,
            @NonNull PublicKey poolId,
            @NonNull PublicKey userLpAccount,
            @NonNull PublicKey userVaultA,
            @NonNull PublicKey userVaultB,
            @NonNull PublicKey vaultA,
            @NonNull PublicKey vaultB,
            @NonNull PublicKey mintA,
            @NonNull PublicKey mintB,
            @NonNull PublicKey lpMint,
            long lpAmount,
            long amountMaxA,
            long amountMaxB
    ) {
        return null;
//        return CpmmInstruction01Deposit.create(
//                owner, authority, poolId, userLpAccount, userVaultA, userVaultB, vaultA, vaultB, mintA, mintB, lpMint,
//                lpAmount, amountMaxA, amountMaxB
//        );
    }

    /**
     * Withdraws liquidity from a CPMM Pool.
     */
    public static CpmmInstruction02Withdraw withdraw(
            @NonNull PublicKey owner,
            @NonNull PublicKey authority,
            @NonNull PublicKey poolId,
            @NonNull PublicKey userLpAccount,
            @NonNull PublicKey userVaultA,
            @NonNull PublicKey userVaultB,
            @NonNull PublicKey vaultA,
            @NonNull PublicKey vaultB,
            @NonNull PublicKey mintA,
            @NonNull PublicKey mintB,
            @NonNull PublicKey lpMint,
            long lpAmount,
            long amountMinA,
            long amountMinB
    ) {
        return null;
//        return CpmmInstruction02Withdraw.create(
//                owner, authority, poolId, userLpAccount, userVaultA, userVaultB, vaultA, vaultB, mintA, mintB, lpMint,
//                lpAmount, amountMinA, amountMinB
//        );
    }

    /**
     * Executes a swap in a CPMM Pool (Base Input).
     */
    public static CpmmInstruction03SwapBaseInput swapBaseInput(
            @NonNull PublicKey payer,
            @NonNull PublicKey authority,
            @NonNull PublicKey configId,
            @NonNull PublicKey poolId,
            @NonNull PublicKey userInputAccount,
            @NonNull PublicKey userOutputAccount,
            @NonNull PublicKey inputVault,
            @NonNull PublicKey outputVault,
            @NonNull PublicKey inputTokenProgram,
            @NonNull PublicKey outputTokenProgram,
            @NonNull PublicKey inputMint,
            @NonNull PublicKey outputMint,
            @NonNull PublicKey observationId,
            long amountIn,
            long amountOutMin
    ) {
        return null;
//        return CpmmInstruction03SwapBaseInput.create(
//                payer, authority, configId, poolId, userInputAccount, userOutputAccount, inputVault, outputVault,
//                inputTokenProgram, outputTokenProgram, inputMint, outputMint, observationId,
//                amountIn, amountOutMin
//        );
    }

    /**
     * Executes a swap in a CPMM Pool (Base Output).
     */
    public static CpmmInstruction04SwapBaseOutput swapBaseOutput(
            @NonNull PublicKey payer,
            @NonNull PublicKey authority,
            @NonNull PublicKey configId,
            @NonNull PublicKey poolId,
            @NonNull PublicKey userInputAccount,
            @NonNull PublicKey userOutputAccount,
            @NonNull PublicKey inputVault,
            @NonNull PublicKey outputVault,
            @NonNull PublicKey inputTokenProgram,
            @NonNull PublicKey outputTokenProgram,
            @NonNull PublicKey inputMint,
            @NonNull PublicKey outputMint,
            @NonNull PublicKey observationId,
            long amountInMax,
            long amountOut
    ) {
        return null;
//        return CpmmInstruction04SwapBaseOutput.create(
//                payer, authority, configId, poolId, userInputAccount, userOutputAccount, inputVault, outputVault,
//                inputTokenProgram, outputTokenProgram, inputMint, outputMint, observationId,
//                amountInMax, amountOut
//        );
    }

    /**
     * Locks liquidity in a CPMM Pool.
     */
    public static CpmmInstruction05LockLiquidity lockLiquidity(
            @NonNull PublicKey payer,
            @NonNull PublicKey authority,
            @NonNull PublicKey poolId,
            @NonNull PublicKey lpMint,
            @NonNull PublicKey userLpVault,
            @NonNull PublicKey lockLpVault,
            @NonNull PublicKey nftMint,
            @NonNull PublicKey nftAccount,
            long lpAmount,
            boolean withMetadata
    ) {
        return null;
//        return CpmmInstruction05LockLiquidity.create(
//                payer, authority, poolId, lpMint, userLpVault, lockLpVault, nftMint, nftAccount, lpAmount, withMetadata
//        );
    }

    /**
     * Collects fees from a CPMM Pool.
     */
    public static CpmmInstruction06CollectFee collectFee(
            @NonNull PublicKey payer,
            @NonNull PublicKey authority,
            @NonNull PublicKey poolId,
            @NonNull PublicKey lockLpVault,
            @NonNull PublicKey userVaultA,
            @NonNull PublicKey userVaultB,
            long lpFeeAmount
    ) {
        return null;
//        return CpmmInstruction06CollectFee.create(
//                payer, authority, poolId, lockLpVault, userVaultA, userVaultB, lpFeeAmount
//        );
    }
}