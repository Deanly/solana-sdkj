package net.deanly.solana.sdk.program.core.system;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.core.system.instruction.*;

/**
 * The SystemProgram class represents the System program in the Solana blockchain.
 * It provides static methods to create instructions for various operations supported
 * by the System program.
 *
 * The System program is an essential part of the Solana runtime and handles tasks
 * such as account creation, transfers, and assigning account properties.
 *
 * The class includes functionality for common operations like account creation,
 * transferring lamports, and managing nonce accounts.
 */
// https://github.com/solana-labs/solana-web3.js/blob/4e9988cfc561f3ed11f4c5016a29090a61d129a8/src/programs/system.ts
public class SystemProgram {

    public static final PublicKey PROGRAM_ID = new PublicKey("11111111111111111111111111111111");

    /** Create Account Instruction (0) */
    public static SystemInstruction0Create createAccount(PublicKey fundingAccount, PublicKey newAccount, long lamports, long space, PublicKey ownerProgramId) {
        return SystemInstruction0Create.create(fundingAccount, newAccount, lamports, space, ownerProgramId);
    }

    /** Assign Instruction (1) */
    public static SystemInstruction1Assign assign(PublicKey account, PublicKey ownerProgramId) {
        return SystemInstruction1Assign.create(account, ownerProgramId);
    }

    /** Transfer Instruction (2) */
    public static SystemInstruction2Transfer transfer(PublicKey fromAccount, PublicKey toAccount, long lamports) {
        return SystemInstruction2Transfer.create(fromAccount, toAccount, lamports);
    }

    /** Create With Seed Instruction (3) */
    public static SystemInstruction3CreateWithSeed createWithSeed(PublicKey fromAccount, PublicKey newAccount, PublicKey baseAccount, String seed, long lamports, long space, PublicKey ownerProgramId) {
        return SystemInstruction3CreateWithSeed.create(fromAccount, baseAccount, newAccount, seed, lamports, space, ownerProgramId);
    }

    /** Advance Nonce Account Instruction (4) */
    public static SystemInstruction4AdvanceNonceAccount advanceNonceAccount(PublicKey nonceAccount, PublicKey authorityAccount) {
        return SystemInstruction4AdvanceNonceAccount.create(nonceAccount, authorityAccount);
    }

    /** Withdraw Nonce Account Instruction (5) */
    public static SystemInstruction5WithdrawNonceAccount withdrawNonceAccount(PublicKey nonceAccount, PublicKey authorityAccount, PublicKey destinationAccount, long lamports) {
        return SystemInstruction5WithdrawNonceAccount.create(nonceAccount, authorityAccount, destinationAccount, lamports);
    }

    /** Initialize Nonce Account Instruction (6) */
    public static SystemInstruction6InitializeNonceAccount initializeNonceAccount(PublicKey nonceAccount, PublicKey authorityAccount) {
        return SystemInstruction6InitializeNonceAccount.create(nonceAccount, authorityAccount);
    }

    /** Authorize Nonce Account Instruction (7) */
    public static SystemInstruction7AuthorizeNonceAccount authorizeNonceAccount(PublicKey nonceAccount, PublicKey currentAuthority, PublicKey newAuthority) {
        return SystemInstruction7AuthorizeNonceAccount.create(nonceAccount, currentAuthority, newAuthority);
    }

    /** Allocate Instruction (8) */
    public static SystemInstruction8Allocate allocate(PublicKey account, long space) {
        return SystemInstruction8Allocate.create(account, space);
    }

    /** Allocate With Seed Instruction (9) */
    public static SystemInstruction9AllocateWithSeed allocateWithSeed(PublicKey account, PublicKey baseAccount, String seed, long space, PublicKey ownerProgramId) {
        return SystemInstruction9AllocateWithSeed.create(account, baseAccount, seed, space, ownerProgramId);
    }

    /** Assign With Seed Instruction (10) */
    public static SystemInstruction10AssignWithSeed assignWithSeed(PublicKey account, PublicKey baseAccount, String seed, PublicKey ownerProgramId) {
        return SystemInstruction10AssignWithSeed.create(account, baseAccount, seed, ownerProgramId);
    }

    /** Transfer With Seed Instruction (11) */
    public static SystemInstruction11TransferWithSeed transferWithSeed(PublicKey fromAccount, String seed, PublicKey baseAccount, PublicKey programId, long lamports, PublicKey toAccount) {
        return SystemInstruction11TransferWithSeed.create(fromAccount, baseAccount, toAccount, lamports, seed, programId);
    }

    /** Upgrade Nonce Account Instruction (12) */
    public static SystemInstruction12UpgradeNonceAccount upgradeNonceAccount(PublicKey nonceAccount) {
        return SystemInstruction12UpgradeNonceAccount.create(nonceAccount);
    }
}
