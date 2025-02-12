package net.deanly.solana.sdk.program.core.stake;

import net.deanly.solana.sdk.crypto.PublicKey;

/**
 * The StakeProgram class manages staking operations in the Solana blockchain ecosystem,
 * enabling SOL holders to delegate their stakes to validators. By interacting with
 * the Stake Program, users can increase network security and decentralization
 * while earning rewards for their delegated stakes.
 *
 * The {@code PROGRAM_ID} constant defines the public key for the Stake Program,
 * which serves as the unique identifier for program-related operations.
 */
public class StakeProgram {
    public static final PublicKey PROGRAM_ID = new PublicKey("Stake11111111111111111111111111111111111111");
}
