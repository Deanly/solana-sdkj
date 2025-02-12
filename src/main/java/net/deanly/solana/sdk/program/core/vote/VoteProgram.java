package net.deanly.solana.sdk.program.core.vote;

import net.deanly.solana.sdk.crypto.PublicKey;

/**
 * Represents the Vote Program in a blockchain system, responsible for handling the accounts
 * associated with validators and enabling them to submit votes on the state of the blockchain.
 *
 * The program facilitates the interaction of validators with the blockchain by managing the
 * vote accounts and recording the voting behavior as part of the network's consensus process.
 * Each transaction related to the voting mechanism is guided by this program.
 *
 * The Vote Program is identified by a unique program ID, which is represented as a public key.
 * This identifier allows clients and on-chain components to interact with and access the program.
 */
public class VoteProgram {
    public static final PublicKey PROGRAM_ID = new PublicKey("Vote111111111111111111111111111111111111111");
}
