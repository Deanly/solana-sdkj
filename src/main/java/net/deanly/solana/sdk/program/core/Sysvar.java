package net.deanly.solana.sdk.program.core;

import net.deanly.solana.sdk.crypto.PublicKey;

/**
 * Represents all Sysvar accounts in the Solana blockchain.
 * Sysvars (System Variables) are read-only accounts that store important network data.
 */
public class Sysvar {

    /**
     * The public key for the rent sysvar account.
     * This Sysvar contains the rent policy data used by accounts in the Solana network.
     */
    public static final PublicKey SYSVAR_RENT_ADDRESS =
            new PublicKey("SysvarRent111111111111111111111111111111111");

    /**
     * The public key for the clock sysvar account.
     * Provides time-related information such as slot, epoch, and timestamp.
     */
    public static final PublicKey SYSVAR_CLOCK_ADDRESS =
            new PublicKey("SysvarC1ock11111111111111111111111111111111");

    /**
     * The public key for the epoch schedule sysvar account.
     * Contains information about the duration and schedule of epochs.
     */
    public static final PublicKey SYSVAR_EPOCH_SCHEDULE_ADDRESS =
            new PublicKey("SysvarEpochSchedu1e111111111111111111111111111");

    /**
     * The public key for the recent blockhashes sysvar account.
     * Stores a list of recent blockhashes for transaction validation.
     */
    public static final PublicKey SYSVAR_RECENT_BLOCKHASHES_ADDRESS =
            new PublicKey("SysvarRecentB1ockHashes11111111111111111111");

    /**
     * The public key for the stake history sysvar account.
     * Maintains the history of stake changes and rewards for the network.
     */
    public static final PublicKey SYSVAR_STAKE_HISTORY_ADDRESS =
            new PublicKey("SysvarStakeHistory111111111111111111111111111");

    /**
     * The public key for the slot hashes sysvar account.
     * Stores a mapping of slot numbers to recent blockhashes for consistency checks.
     */
    public static final PublicKey SYSVAR_SLOT_HASHES_ADDRESS =
            new PublicKey("SysvarS1otHashes111111111111111111111111111");

    /**
     * The public key for the slot history sysvar account.
     * Contains a record of recent slot numbers and their statuses.
     */
    public static final PublicKey SYSVAR_SLOT_HISTORY_ADDRESS =
            new PublicKey("SysvarS1otHistory111111111111111111111111111");

    /**
     * The public key for the rewards sysvar account.
     * Provides information about recent staking rewards for validators.
     */
    public static final PublicKey SYSVAR_REWARDS_ADDRESS =
            new PublicKey("SysvarRewards111111111111111111111111111111");

    /**
     * The public key for the instructions sysvar account.
     * Stores the current instructions being executed by the transaction.
     */
    public static final PublicKey SYSVAR_INSTRUCTIONS_ADDRESS =
            new PublicKey("Sysvar1nstructions1111111111111111111111111");

    /**
     * The public key for the config sysvar account.
     * Holds network-wide configuration parameters.
     */
    public static final PublicKey SYSVAR_CONFIG_ADDRESS =
            new PublicKey("Config1111111111111111111111111111111111111");
}