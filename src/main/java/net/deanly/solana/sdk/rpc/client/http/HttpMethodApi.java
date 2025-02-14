package net.deanly.solana.sdk.rpc.client.http;

import net.deanly.structlayout.type.guava.UnsignedLong;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.rpc.client.exception.RpcException;
import net.deanly.solana.sdk.rpc.request.config.*;
import net.deanly.solana.sdk.rpc.request.filter.TokenAccountsByDelegateFilter;
import net.deanly.solana.sdk.rpc.request.filter.TokenAccountsByOwnerFilter;
import net.deanly.solana.sdk.rpc.response.*;
import net.deanly.solana.sdk.transaction.Transaction;
import net.deanly.solana.sdk.types.Blockhash;
import net.deanly.solana.sdk.types.GenesisHash;
import net.deanly.solana.sdk.types.Signature;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.List;
import java.util.Map;

// https://solana.com/ko/docs/rpc/http
public interface HttpMethodApi {

    /**
     * Returns all information associated with the account of the provided Pubkey.
     *
     * @param pubkey        The public key of the account to query.
     * @param configuration Optional configuration parameters.
     * @return A ResValueAccountInfo object containing the account information.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/docs/rpc/http/getaccountinfo">getAccountInfo RPC Method</a>
     */
    RpcResultObject<ResValueAccountInfo> getAccountInfo(PublicKey pubkey, AccountInfoConfig configuration) throws RpcException;
    default RpcResultObject<ResValueAccountInfo> getAccountInfo(PublicKey pubkey) throws RpcException {
        return getAccountInfo(pubkey, null);
    }

    /**
     * Returns the balance of the account of provided Pubkey.
     *
     * @param pubkey        The public key of the account to query.
     * @param configuration Optional configuration parameters.
     * @return the account balance.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/docs/rpc/http/getbalance">getBalance RPC Method</a>
     */
    RpcResultObject<UnsignedLong> getBalance(PublicKey pubkey, BalanceConfig configuration) throws RpcException;
    default RpcResultObject<UnsignedLong> getBalance(PublicKey pubkey) throws RpcException {
        return getBalance(pubkey, null);
    }

    /**
     * Returns identity and transaction information about a confirmed block in the ledger.
     *
     * @param slot          The slot number of the block to retrieve.
     * @param configuration Optional configuration parameters.
     * @return A ResValueBlock object containing the block information.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/docs/rpc/http/getblock">getBlock RPC Method</a>
     */
    ResValueBlock getBlock(UnsignedLong slot, BlockConfig configuration) throws RpcException;
    default ResValueBlock getBlock(UnsignedLong slot) throws RpcException {
        return getBlock(slot, null);
    }

    /**
     * Returns commitment information for a particular block.
     *
     * @param slot The slot number of the block to query.
     * @return A ResValueBlockCommitment object containing the block's commitment information.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/docs/rpc/http/getblockcommitment">getBlockCommitment RPC Method</a>
     */
    ResValueBlockCommitment getBlockCommitment(UnsignedLong slot) throws RpcException;

    /**
     * Returns the current block height of the node.
     *
     * @param configuration Optional configuration parameters.
     * @return the current block height.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/docs/rpc/http/getblockheight">getBlockHeight RPC Method</a>
     */
    UnsignedLong getBlockHeight(BlockHeightConfig configuration) throws RpcException;
    default UnsignedLong getBlockHeight() throws RpcException {
        return getBlockHeight(null);
    }


    /**
     * Returns recent block production information from the current or previous epoch.
     *
     * @param configuration Optional configuration parameters.
     * @return Recent block production information.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/docs/rpc/http/getblockproduction">getBlockProduction RPC Method</a>
     */
    RpcResultObject<ResValueBlockProduction> getBlockProduction(BlockProductionConfig configuration) throws RpcException;
    default RpcResultObject<ResValueBlockProduction> getBlockProduction() throws RpcException {
        return getBlockProduction(null);
    }

    /**
     * Returns a list of confirmed blocks between two slots.
     *
     * @param startSlot    The starting slot, as an UnsignedLong.
     * @param endSlot      The optional ending slot, as an UnsignedLong. Must be no more than 500,000 slots higher than the startSlot.
     * @param configuration Optional configuration parameters.
     * @return A list of confirmed blocks between the specified slots.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getblocks">getBlocks RPC Method</a>
     */
    List<UnsignedLong> getBlocks(UnsignedLong startSlot, UnsignedLong endSlot, BlocksConfig configuration) throws RpcException;
    default List<UnsignedLong> getBlocks(UnsignedLong startSlot, UnsignedLong endSlot) throws RpcException {
        return getBlocks(startSlot, endSlot, null);
    }

    /**
     * Returns a list of confirmed blocks starting at the given slot.
     *
     * @param startSlot     The starting slot, as an UnsignedLong.
     * @param limit         The number of blocks to return, as an UnsignedLong. Must be no more than 500,000 blocks higher than the startSlot.
     * @param configuration Optional configuration parameters.
     * @return A list of confirmed blocks starting at the specified slot.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getblockswithlimit">getBlocksWithLimit RPC Method</a>
     */
    List<UnsignedLong> getBlocksWithLimit(UnsignedLong startSlot, UnsignedLong limit, BlocksWithLimitConfig configuration) throws RpcException;
    default List<UnsignedLong> getBlocksWithLimit(UnsignedLong startSlot, UnsignedLong limit) throws RpcException {
        return getBlocksWithLimit(startSlot, limit, null);
    }

    /**
     * Returns the estimated production time of a block.
     *
     * @param slot The slot number of the block, as an UnsignedLong.
     * @return The estimated production time as a Unix timestamp (seconds since the Unix epoch).
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getblocktime">getBlockTime RPC Method</a>
     */
    Instant getBlockTime(UnsignedLong slot) throws RpcException;

    /**
     * Returns information about all the nodes participating in the cluster.
     *
     * @return A list of ClusterNode objects representing each node in the cluster.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getclusternodes">getClusterNodes RPC Method</a>
     */
    List<ResValueClusterNode> getClusterNodes() throws RpcException;

    /**
     * Returns information about the current epoch.
     *
     * @param configuration Optional configuration parameters.
     * @return An EpochInfo object containing details about the current epoch.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getepochinfo">getEpochInfo RPC Method</a>
     */
    ResValueEpochInfo getEpochInfo(EpochInfoConfig configuration) throws RpcException;

    /**
     * Returns the epoch schedule information from the cluster's genesis configuration.
     *
     * @return An EpochSchedule object containing details about the epoch schedule.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getepochschedule">getEpochSchedule RPC Method</a>
     */
    ResValueEpochSchedule getEpochSchedule() throws RpcException;

    /**
     * Returns the fee the network will charge for a particular Message.
     *
     * @param message        The base-64 encoded Message string.
     * @param configuration  Optional configuration parameters.
     * @return The fee corresponding to the message at the specified blockhash, or null if the blockhash has expired.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getfeeformessage">getFeeForMessage RPC Method</a>
     */
    RpcResultObject<UnsignedLong> getFeeForMessage(String message, FeeForMessageConfig configuration) throws RpcException;
    default RpcResultObject<UnsignedLong> getFeeForMessage(String message) throws RpcException {
        return getFeeForMessage(message, null);
    }

    /**
     * Returns the slot of the lowest confirmed block that has not been purged from the ledger.
     *
     * @return The slot number of the first available block as an UnsignedLong.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getfirstavailableblock">getFirstAvailableBlock RPC Method</a>
     */
    UnsignedLong getFirstAvailableBlock() throws RpcException;

    /**
     * Returns the genesis hash of the Solana network.
     *
     * @return The genesis hash as a base-58 encoded string.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getgenesishash">getGenesisHash RPC Method</a>
     */
    GenesisHash getGenesisHash() throws RpcException;

    /**
     * Retrieves the current health status of the system or application.
     *
     * This method performs an internal check to determine if the system is functioning correctly
     * and reports its health status. If the health check fails due to any internal error
     * or other unexpected conditions, an RpcException is thrown.
     *
     * @throws RpcException if there is an inability to fetch the health status or if any error occurs during the health check process.
     * @see <a href="https://solana.com/ko/docs/rpc/http/gethealth">getHealth RPC Method</a>
     */
    void getHealth() throws RpcException;

    /**
     * Performs a health check and returns the status.
     *
     * @return true if the health check passes successfully, false otherwise
     * @see <a href="https://solana.com/ko/docs/rpc/http/gethealth">getHealth RPC Method</a>
     */
    boolean getHealthCheck();

    /**
     * Returns the highest slot information that the node has snapshots for.
     *
     * @return A SnapshotSlot object containing the highest full snapshot slot and the highest incremental snapshot slot (if available).
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/gethighestsnapshotslot">getHighestSnapshotSlot RPC Method</a>
     */
    ResValueSnapshotSlot getHighestSnapshotSlot() throws RpcException;

    /**
     * Returns the identity public key for the current node.
     *
     * @return The identity public key as a base-58 encoded string.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getidentity">getIdentity RPC Method</a>
     */
    ResValueIdentity getIdentity() throws RpcException;

    /**
     * Returns the current inflation governor parameters.
     *
     * @param configuration Optional configuration parameters.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/docs/rpc/http/getinflationgovernor">getInflationGovernor RPC Method</a>
     */
    ResValueInflationGovernor getInflationGovernor(InflationGovernorConfig configuration) throws RpcException;
    default ResValueInflationGovernor getInflationGovernor() throws RpcException {
        return getInflationGovernor(null);
    }

    /**
     * Returns the specific inflation values for the current epoch.
     *
     * @return A ResValueInflationRate object containing the current epoch's inflation details.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getinflationrate">getInflationRate RPC Method</a>
     */
    ResValueInflationRate getInflationRate() throws RpcException;

    /**
     * Returns the inflation/staking reward for a list of addresses for a specific epoch.
     *
     * @param addresses     A list of account addresses as base-58 encoded strings.
     * @param configuration Optional configuration parameters.
     * @return A list of ResValueInflationReward objects containing reward information for each address.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/docs/rpc/http/getinflationreward">getInflationReward RPC Method</a>
     */
    List<ResValueInflationReward> getInflationReward(List<PublicKey> addresses, InflationRewardConfig configuration) throws RpcException;
    default List<ResValueInflationReward> getInflationReward(List<PublicKey> addresses) throws RpcException {
        return getInflationReward(addresses, null);
    }

    /**
     * Returns the 20 largest accounts by lamport balance.
     *
     * @param configuration Optional configuration parameters.
     * @return A list of ResValueLargestAccount objects containing the largest accounts' information.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getlargestaccounts">getLargestAccounts RPC Method</a>
     */
    RpcResultObject<List<ResValueLargestAccount>> getLargestAccounts(LargestAccountsConfig configuration) throws RpcException;
    default RpcResultObject<List<ResValueLargestAccount>> getLargestAccounts() throws RpcException {
        return getLargestAccounts(null);
    }

    /**
     * Returns the latest blockhash.
     *
     * @param configuration Optional configuration parameters.
     * @return A ResValueLatestBlockhash object containing the latest blockhash and its last valid block height.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getlatestblockhash">getLatestBlockhash RPC Method</a>
     */
    RpcResultObject<ResValueLatestBlockhash> getLatestBlockhash(LatestBlockhashConfig configuration) throws RpcException;
    default RpcResultObject<ResValueLatestBlockhash> getLatestBlockhash() throws RpcException {
        return getLatestBlockhash(null);
    }

    /**
     * Returns the leader schedule for an epoch.
     *
     * @param epoch         Optional; fetches the leader schedule for the epoch that corresponds to the provided slot.
     *                      If unspecified, the leader schedule for the current epoch is fetched.
     * @param configuration Optional configuration parameters.
     * @return A map where the keys are validator identities (base-58 encoded strings) and the values are lists of slot indices.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getleaderschedule">getLeaderSchedule RPC Method</a>
     */
    Map<PublicKey, List<Integer>> getLeaderSchedule(UnsignedLong epoch, LeaderScheduleConfig configuration) throws RpcException;

    /**
     * Returns the maximum slot seen from the retransmit stage.
     *
     * @return The maximum slot number as an UnsignedLong.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/docs/rpc/http/getmaxretransmitslot">getMaxRetransmitSlot RPC Method</a>
     */
    UnsignedLong getMaxRetransmitSlot() throws RpcException;

    /**
     * Returns the maximum slot seen from after shred insert.
     *
     * @return The maximum slot number as an UnsignedLong.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getmaxshredinsertslot">getMaxShredInsertSlot RPC Method</a>
     */
    UnsignedLong getMaxShredInsertSlot() throws RpcException;

    /**
     * Returns the minimum balance required to make an account rent-exempt.
     *
     * @param dataLength    The account's data length in bytes.
     * @param configuration Optional configuration parameters.
     * @return The minimum lamports required for the account to remain rent-free.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getminimumbalanceforrentexemption">getMinimumBalanceForRentExemption RPC Method</a>
     */
    UnsignedLong getMinimumBalanceForRentExemption(Integer dataLength, MinimumBalanceForRentExemptionConfig configuration) throws RpcException;
    default UnsignedLong getMinimumBalanceForRentExemption(Integer dataLength) throws RpcException {
        return getMinimumBalanceForRentExemption(dataLength, null);
    }

    /**
     * Returns the account information for a list of Pubkeys.
     *
     * @param accounts      A list of Pubkeys to query, as base-58 encoded strings (up to a maximum of 100).
     * @param configuration Optional configuration parameters.
     * @return A list of ResValueAccountInfo objects containing the account information.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/docs/rpc/http/getmultipleaccounts">getMultipleAccounts RPC Method</a>
     */
    RpcResultObject<List<ResValueAccountInfo>> getMultipleAccounts(List<PublicKey> accounts, MultipleAccountsConfig configuration) throws RpcException;
    default RpcResultObject<List<ResValueAccountInfo>> getMultipleAccounts(List<PublicKey> accounts) throws RpcException {
        return getMultipleAccounts(accounts, null);
    }

    /**
     * Returns all accounts owned by the provided program Pubkey.
     *
     * @param programId     The Pubkey of the program, as a base-58 encoded string.
     * @param configuration Optional configuration parameters.
     * @return A list of ResValueProgram objects containing the account information.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getprogramaccounts">getProgramAccounts RPC Method</a>
     */
    List<ResValueProgram> getProgramAccounts(PublicKey programId, ProgramAccountsConfig configuration) throws RpcException;
    default List<ResValueProgram> getProgramAccounts(PublicKey programId) throws RpcException {
        return getProgramAccounts(programId, null);
    }

    /**
     * Returns a list of recent performance samples, in reverse slot order.
     * Performance samples are taken every 60 seconds and include the number of transactions and slots
     * that occur in a given time window.
     *
     * @param limit Optional. Number of samples to return (maximum 720).
     * @return A list of ResValuePerformanceSample objects containing performance data.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getrecentperformancesamples">getRecentPerformanceSamples RPC Method</a>
     */
    List<ResValuePerformanceSample> getRecentPerformanceSamples(Integer limit) throws RpcException;
    default List<ResValuePerformanceSample> getRecentPerformanceSamples() throws RpcException {
        return getRecentPerformanceSamples(null);
    }

    /**
     * Returns a list of prioritization fees from recent blocks.
     *
     * @param accounts Optional. A list of account addresses (up to a maximum of 128 addresses) as base-58 encoded strings.
     *                 If provided, the response will reflect a fee to land a transaction locking all of the provided accounts as writable.
     * @return A list of ResValuePrioritizationFee objects containing prioritization fee data.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getrecentprioritizationfees">getRecentPrioritizationFees RPC Method</a>
     */
    List<ResValuePrioritizationFee> getRecentPrioritizationFees(List<PublicKey> accounts) throws RpcException;
    default List<ResValuePrioritizationFee> getRecentPrioritizationFees() throws RpcException {
        return getRecentPrioritizationFees(null);
    }

    /**
     * Returns signatures for confirmed transactions that include the given address in their accountKeys list.
     * Returns signatures backwards in time from the provided signature or most recent confirmed block.
     *
     * @param account       The account address as a base-58 encoded string.
     * @param configuration Optional configuration parameters.
     * @return A list of ResValueTransactionSignature objects containing transaction signature information.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getsignaturesforaddress">getSignaturesForAddress RPC Method</a>
     */
    List<ResValueTransactionSignature> getSignaturesForAddress(PublicKey account, SignaturesForAddressConfig configuration) throws RpcException;
    default List<ResValueTransactionSignature> getSignaturesForAddress(PublicKey account) throws RpcException {
        return getSignaturesForAddress(account, null);
    }

    /**
     * Returns the statuses of a list of signatures. Each signature must be a transaction ID (txid),
     * the first signature of a transaction.
     *
     * @param signatures    A list of transaction signatures to confirm, as base-58 encoded strings (up to a maximum of 256).
     * @param configuration Optional configuration parameters.
     * @return A list of ResValueSignatureStatus objects containing transaction status information.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getsignaturestatuses">getSignatureStatuses RPC Method</a>
     */
    RpcResultObject<List<ResValueSignatureStatus>> getSignatureStatuses(List<Signature> signatures, SignatureStatusesConfig configuration) throws RpcException;
    default RpcResultObject<List<ResValueSignatureStatus>> getSignatureStatuses(List<Signature> signatures) throws RpcException {
        return getSignatureStatuses(signatures, null);
    }

    /**
     * Returns the slot that has reached the given or default commitment level.
     *
     * @param configuration Optional configuration parameters.
     * @return The current slot as an UnsignedLong.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/docs/rpc/http/getslot">getSlot RPC Method</a>
     */
    UnsignedLong getSlot(SlotConfig configuration) throws RpcException;
    default UnsignedLong getSlot() throws RpcException {
        return getSlot(null);
    }

    /**
     * Returns the current slot leader.
     *
     * @param configuration Optional configuration parameters.
     * @return The node identity public key as a base-58 encoded string.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/docs/rpc/http/getslotleader">getSlotLeader RPC Method</a>
     */
    PublicKey getSlotLeader(SlotLeaderConfig configuration) throws RpcException;
    default PublicKey getSlotLeader() throws RpcException {
        return getSlotLeader(null);
    }

    /**
     * Returns the slot leaders for a given slot range.
     *
     * @param startSlot The starting slot, as an UnsignedLong.
     * @param limit     The limit on the number of slot leaders to return, as an UnsignedLong (between 1 and 500,000).
     * @return A list of node identity public keys as base-58 encoded strings.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/docs/rpc/http/getslotleaders">getSlotLeaders RPC Method</a>
     */
    List<PublicKey> getSlotLeaders(UnsignedLong startSlot, UnsignedLong limit) throws RpcException;
    default List<PublicKey> getSlotLeaders(UnsignedLong startSlot) throws RpcException {
        return getSlotLeaders(startSlot, null);
    }
    default List<PublicKey> getSlotLeaders() throws RpcException {
        return getSlotLeaders(null, null);
    }

    /**
     * Returns the stake minimum delegation, in lamports.
     *
     * @param configuration Optional configuration parameters.
     * @return The stake minimum delegation as an UnsignedLong.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/docs/rpc/http/getstakeminimumdelegation">getStakeMinimumDelegation RPC Method</a>
     */
    RpcResultObject<UnsignedLong> getStakeMinimumDelegation(StakeMinimumDelegationConfig configuration) throws RpcException;
    default RpcResultObject<UnsignedLong> getStakeMinimumDelegation() throws RpcException {
        return getStakeMinimumDelegation(null);
    }

    /**
     * Returns information about the current supply.
     *
     * @param configuration Optional configuration parameters.
     * @return A ResValueSupply object containing supply information.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getsupply">getSupply RPC Method</a>
     */
    RpcResultObject<ResValueSupply> getSupply(SupplyConfig configuration) throws RpcException;
    default RpcResultObject<ResValueSupply> getSupply() throws RpcException {
        return getSupply(null);
    }

    /**
     * Returns the token balance of an SPL Token account.
     *
     * @param account       The public key of the token account to query.
     * @param configuration Optional configuration parameters.
     * @return A ResValueTokenAccountBalance object containing the token balance information.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/gettokenaccountbalance">getTokenAccountBalance RPC Method</a>
     */
    RpcResultObject<ResValueTokenAccountBalance> getTokenAccountBalance(PublicKey account, TokenAccountBalanceConfig configuration) throws RpcException;
    default RpcResultObject<ResValueTokenAccountBalance> getTokenAccountBalance(PublicKey account) throws RpcException {
        return getTokenAccountBalance(account, null);
    }

    /**
     * Returns all SPL Token accounts by approved Delegate.
     *
     * @param delegate      The public key of the account delegate to query.
     * @param filter        Optional filter object to limit accounts by mint or programId.
     * @param configuration Optional configuration parameters.
     * @return A list of ResValueTokenAccount objects containing token account information.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/gettokenaccountsbydelegate">getTokenAccountsByDelegate RPC Method</a>
     */
    RpcResultObject<List<ResValueTokenAccount>> getTokenAccountsByDelegate(
            PublicKey delegate,
            TokenAccountsByDelegateFilter filter,
            TokenAccountsByDelegateConfig configuration
    ) throws RpcException;
    default RpcResultObject<List<ResValueTokenAccount>> getTokenAccountsByDelegate(PublicKey delegate, TokenAccountsByDelegateFilter filter) throws RpcException {
        return getTokenAccountsByDelegate(delegate, filter, null);
    }
    default RpcResultObject<List<ResValueTokenAccount>> getTokenAccountsByDelegate(PublicKey delegate) throws RpcException {
        return getTokenAccountsByDelegate(delegate, null, null);
    }

    /**
     * Returns all SPL Token accounts by token owner.
     *
     * @param owner         The public key of the account owner to query.
     * @param filter        Filter object to limit accounts by mint or programId.
     * @param configuration Optional configuration parameters.
     * @return A list of ResValueTokenAccount objects containing token account information.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/gettokenaccountsbyowner">getTokenAccountsByOwner RPC Method</a>
     */
    RpcResultObject<List<ResValueTokenAccount>> getTokenAccountsByOwner(
            PublicKey owner,
            TokenAccountsByOwnerFilter filter,
            TokenAccountsByOwnerConfig configuration
    ) throws RpcException;
    default RpcResultObject<List<ResValueTokenAccount>> getTokenAccountsByOwner(PublicKey owner, TokenAccountsByOwnerFilter filter) throws RpcException {
        return getTokenAccountsByOwner(owner, filter, null);
    }

    /**
     * Returns the 20 largest accounts of a particular SPL Token type.
     *
     * @param mint          The public key of the token Mint to query.
     * @param configuration Optional configuration parameters.
     * @return A list of ResValueTokenLargestAccounts objects containing the largest token accounts information.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/docs/rpc/http/gettokenlargestaccounts">getTokenLargestAccounts RPC Method</a>
     */
    RpcResultObject<List<ResValueTokenLargestAccounts>> getTokenLargestAccounts(
            PublicKey mint,
            TokenLargestAccountsConfig configuration
    ) throws RpcException;
    default RpcResultObject<List<ResValueTokenLargestAccounts>> getTokenLargestAccounts(PublicKey mint) throws RpcException {
        return getTokenLargestAccounts(mint, null);
    }

    /**
     * Returns the total supply of an SPL Token type.
     *
     * @param mint          The public key of the token Mint to query.
     * @param configuration Optional configuration parameters.
     * @return A ResValueTokenSupply object containing the total token supply information.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/gettokensupply">getTokenSupply RPC Method</a>
     */
    RpcResultObject<ResValueTokenSupply> getTokenSupply(PublicKey mint, TokenSupplyConfig configuration) throws RpcException;
    default RpcResultObject<ResValueTokenSupply> getTokenSupply(PublicKey mint) throws RpcException {
        return getTokenSupply(mint, null);
    }

    /**
     * Returns transaction details for a confirmed transaction.
     *
     * @param signature     The transaction signature as a base-58 encoded string.
     * @param configuration Optional configuration parameters.
     * @return A ResGetTransaction object containing transaction details.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/gettransaction">getTransaction RPC Method</a>
     */
    ResValueConfirmedTransaction getTransaction(Signature signature, TransactionConfig configuration) throws RpcException;
    default ResValueConfirmedTransaction getTransaction(Signature signature) throws RpcException {
        return getTransaction(signature, null);
    }

    /**
     * Retrieves a parsed transaction after applying the provided signature and configuration.
     * Note that the encoding value in the configuration will be forcibly replaced with `jsonParsed`.
     *
     * @param signature the signature of the transaction to be parsed
     * @param configuration the transaction configuration, where the encoding will be replaced with `jsonParsed`
     * @return the parsed transaction as a ResValueParsedTransaction object
     * @throws RpcException if an error occurs during the parsing process
     */
    ResValueParsedTransaction getParsedTransaction(Signature signature, TransactionConfig configuration) throws RpcException;
    default ResValueParsedTransaction getParsedTransaction(Signature signature) throws RpcException {
        return getParsedTransaction(signature, null);
    }

    /**
     * Returns the current transaction count from the ledger.
     *
     * @param configuration Optional configuration parameters.
     * @return The current transaction count as an UnsignedLong.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/gettransactioncount">getTransactionCount RPC Method</a>
     */
    UnsignedLong getTransactionCount(TransactionCountConfig configuration) throws RpcException;
    default UnsignedLong getTransactionCount() throws RpcException {
        return getTransactionCount(null);
    }


    /**
     * Returns the current Solana version running on the node.
     *
     * @return A ResGetVersion object containing the version details.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/getversion">getVersion RPC Method</a>
     */
    ResValueVersion getVersion() throws RpcException;

    /**
     * Returns the account info and associated stake for all the voting accounts in the current bank.
     *
     * @param configuration Optional configuration parameters.
     * @return A ResValueVoteAccounts object containing current and delinquent vote accounts.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/docs/rpc/http/getvoteaccounts">getVoteAccounts RPC Method</a>
     */
    ResValueVoteAccounts getVoteAccounts(VoteAccountsConfig configuration) throws RpcException;
    default ResValueVoteAccounts getVoteAccounts() throws RpcException {
        return getVoteAccounts(null);
    }

    /**
     * Returns whether a blockhash is still valid or not.
     *
     * @param blockhash     The blockhash to evaluate, as a base-58 encoded string.
     * @param configuration Optional configuration parameters.
     * @return True if the blockhash is still valid; false otherwise.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/docs/rpc/http/isblockhashvalid">isBlockhashValid RPC Method</a>
     */
    RpcResultObject<Boolean> isBlockhashValid(Blockhash blockhash, BlockhashValidConfig configuration) throws RpcException;
    default RpcResultObject<Boolean> isBlockhashValid(Blockhash blockhash) throws RpcException {
        return isBlockhashValid(blockhash, null);
    }

    /**
     * Returns the lowest slot that the node has information about in its ledger.
     *
     * @return The lowest slot number as an UnsignedLong.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/docs/rpc/http/minimumledgerslot">minimumLedgerSlot RPC Method</a>
     */
    UnsignedLong minimumLedgerSlot() throws RpcException;

    /**
     * Requests an airdrop of lamports to a specified public key.
     *
     * @param pubkey        The public key of the account to receive lamports.
     * @param lamports      The amount of lamports to airdrop.
     * @param configuration Optional configuration parameters.
     * @return The transaction signature of the airdrop as a base-58 encoded string.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/requestairdrop">requestAirdrop RPC Method</a>
     */
    Signature requestAirdrop(PublicKey pubkey, UnsignedLong lamports, RequestAirdropConfig configuration) throws RpcException;
    default Signature requestAirdrop(PublicKey pubkey, UnsignedLong lamports) throws RpcException {
        return requestAirdrop(pubkey, lamports, null);
    }

    /**
     * Submits a signed transaction to the cluster for processing.
     *
     * @param transaction   The fully-signed Transaction.
     * @param configuration Optional configuration parameters.
     * @return The first transaction signature embedded in the transaction, as a base-58 encoded string (transaction id).
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/sendtransaction">sendTransaction RPC Method</a>
     */
    Signature sendTransaction(Transaction transaction, SendTransactionConfig configuration) throws RpcException;
    default Signature sendTransaction(Transaction transaction) throws RpcException {
        return sendTransaction(transaction, null);
    }

    /**
     * Simulates sending a transaction.
     *
     * @param transaction   The transaction to simulate.
     * @param configuration Optional configuration parameters.
     * @return A ResSimulateTransaction object containing the simulation result.
     * @throws RpcException If an error occurs during the RPC call.
     * @see <a href="https://solana.com/ko/docs/rpc/http/simulatetransaction">simulateTransaction RPC Method</a>
     */
    RpcResultObject<ResValueSimulatedTransaction> simulateTransaction(Transaction transaction, SimulateTransactionConfig configuration) throws RpcException;
    default RpcResultObject<ResValueSimulatedTransaction> simulateTransaction(Transaction transaction) throws RpcException {
        return simulateTransaction(transaction, null);
    }

    /**
     * Sends a request to a remote procedure call (RPC) endpoint and processes the response.
     *
     * @param <T> The type of the response object.
     * @param method The name of the RPC method being called.
     * @param params A list of parameters to be sent with the RPC request.
     * @param responseType The type of the expected response object.
     * @param errorDataType The type of the error data in case of an RPC error.
     * @return The response object of the specified type.
     * @throws RpcException If an error occurs during the RPC request or response processing.
     */
    <T> T requestV2(String method, List<Object> params, Type responseType, Type errorDataType) throws RpcException;
    default <T> T requestV2(String method, List<Object> params, Type responseType) throws RpcException {
        return requestV2(method, params, responseType, null);
    }

}