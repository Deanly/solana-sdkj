package net.deanly.solana.sdk.rpc.client.http;

import lombok.NonNull;
import net.deanly.solana.sdk.crypto.KeyPair;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.rpc.client.exception.RpcException;
import net.deanly.solana.sdk.rpc.request.config.ProgramAccountConfig;
import net.deanly.solana.sdk.rpc.request.config.RpcSendTransactionConfig;
import net.deanly.solana.sdk.rpc.request.config.SimulateTransactionConfig;
import net.deanly.solana.sdk.rpc.response.*;
import net.deanly.solana.sdk.types.*;
import net.deanly.solana.sdk.transaction.Transaction;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Deprecated
public interface LegacyRpcApi {

    ResValueLatestBlockhash getLatestBlockhash() throws RpcException;

    ResValueLatestBlockhash getLatestBlockhash(Commitment commitment) throws RpcException;

    @Deprecated
    String getRecentBlockhash() throws RpcException;

    @Deprecated
    String getRecentBlockhash(Commitment commitment) throws RpcException;

    String sendTransaction(Transaction transaction, KeyPair signer, String recentBlockHash) throws RpcException;

    String sendTransaction(Transaction transaction, KeyPair signer) throws RpcException;

    String sendTransaction(Transaction transaction) throws RpcException;

    String sendTransaction(Transaction transaction, List<KeyPair> signers, String recentBlockHash) throws RpcException;

    String sendTransaction(Transaction transaction, List<KeyPair> signers, String recentBlockHash,
                           RpcSendTransactionConfig rpcSendTransactionConfig) throws RpcException;

    String sendRawTransaction(String encodeSerializedTransaction,
                              RpcSendTransactionConfig rpcSendTransactionConfig) throws RpcException;

    long getBalance(PublicKey account) throws RpcException;

    long getBalance(PublicKey account, Commitment commitment) throws RpcException;

    ResValueConfirmedTransaction getTransaction(String signature) throws RpcException;

    ResValueConfirmedTransaction getTransaction(String signature, Commitment commitment) throws RpcException;

    List<SignatureInformation> getConfirmedSignaturesForAddress2(PublicKey account, int limit) throws RpcException;

    List<SignatureInformation> getSignaturesForAddress(PublicKey account, int limit, Commitment commitment) throws RpcException;

    List<ResValueProgram> getProgramAccounts(PublicKey account, long offset, String bytes) throws RpcException;

    List<ResValueProgram> getProgramAccountsBase64(PublicKey account, long offset, String bytes) throws RpcException;

    List<ResValueProgram> getProgramAccounts(PublicKey account) throws RpcException;

    List<ResValueProgram> getProgramAccounts(PublicKey account, ProgramAccountConfig programAccountConfig)
            throws RpcException;

    List<ResValueProgram> getProgramAccounts(PublicKey account, List<Memcmp> memcmpList, int dataSize) throws RpcException;

    List<ResValueProgram> getProgramAccounts(PublicKey account, List<Memcmp> memcmpList) throws RpcException;

    ResValueAccountInfo getAccountInfo(PublicKey account) throws RpcException;

    ResValueAccountInfo getAccountInfo(PublicKey account, Map<String, Object> additionalParams) throws RpcException;

    long getMinimumBalanceForRentExemption(long dataLength) throws RpcException;

    long getMinimumBalanceForRentExemption(long dataLength, Commitment commitment) throws RpcException;

    long getBlockTime(long block) throws RpcException;

    long getBlockHeight() throws RpcException;

    long getBlockHeight(Commitment commitment) throws RpcException;

    ResValueBlockProduction getBlockProduction() throws RpcException;

    ResValueBlockProduction getBlockProduction(Map<String, Object> optionalParams) throws RpcException;

    Long minimumLedgerSlot() throws RpcException;

    ResValueVersion getVersion() throws RpcException;

    String requestAirdrop(PublicKey address, long lamports) throws RpcException;

    String requestAirdrop(PublicKey address, long lamports, Commitment commitment) throws RpcException;

    ResValueBlockCommitment getBlockCommitment(long block) throws RpcException;

    Long getFeeForMessage(String message) throws RpcException;

    Long getFeeForMessage(String message, Commitment commitment) throws RpcException;

    List<RecentPrioritizationFees> getRecentPrioritizationFees() throws RpcException;
    List<RecentPrioritizationFees> getRecentPrioritizationFees(List<PublicKey> addresses) throws RpcException;

    Long getStakeMinimumDelegation() throws RpcException;
    Long getStakeMinimumDelegation(Commitment commitment) throws RpcException;

    long getTransactionCount() throws RpcException;
    long getTransactionCount(Commitment commitment) throws RpcException;

    long getMaxRetransmitSlot() throws RpcException;

    ResValueSimulatedTransaction simulateTransaction(@NonNull Transaction transaction,
                                                     @NonNull SimulateTransactionConfig simulateTransactionConfig)
            throws RpcException;

    List<ResValueClusterNode> getClusterNodes() throws RpcException;

    @Deprecated
    ConfirmedBlock getConfirmedBlock(int slot) throws RpcException;

    ResValueBlock getBlock(int slot) throws RpcException;
    ResValueBlock getBlock(int slot, Map<String, Object> optionalParams) throws RpcException;

    ResValueSnapshotSlot getHighestSnapshotSlot() throws RpcException;

    ResValueEpochInfo getEpochInfo() throws RpcException;
    ResValueEpochInfo getEpochInfo(Commitment commitment) throws RpcException;

    ResValueEpochSchedule getEpochSchedule() throws RpcException;

    PublicKey getTokenAccountsByOwner(PublicKey owner, PublicKey tokenMint) throws RpcException;

    ResValueInflationRate getInflationRate() throws RpcException;

    ResValueInflationGovernor getInflationGovernor() throws RpcException;
    ResValueInflationGovernor getInflationGovernor(Commitment commitment) throws RpcException;

    List<ResValueInflationReward> getInflationReward(List<PublicKey> addresses) throws RpcException;
    List<ResValueInflationReward> getInflationReward(List<PublicKey> addresses, Long epoch, Commitment commitment) throws RpcException;

    long getSlot() throws RpcException;
    long getSlot(Commitment commitment) throws RpcException;

    PublicKey getSlotLeader() throws RpcException;
    PublicKey getSlotLeader(Commitment commitment) throws RpcException;

    List<PublicKey> getSlotLeaders(long startSlot, long limit) throws RpcException;

    @Deprecated
    long getSnapshotSlot() throws RpcException;

    long getMaxShredInsertSlot() throws RpcException;

    PublicKey getIdentity() throws RpcException;

    ResValueSupply getSupply() throws RpcException;
    ResValueSupply getSupply(Commitment commitment) throws RpcException;

    long getFirstAvailableBlock() throws RpcException;

    String getGenesisHash() throws RpcException;

    @Deprecated
    List<Double> getConfirmedBlocks(Integer start, Integer end) throws RpcException;
    @Deprecated
    List<Double> getConfirmedBlocks(Integer start) throws RpcException;

    TokenResultObjects.TokenAmountInfo getTokenAccountBalance(PublicKey tokenAccount) throws RpcException;
    TokenResultObjects.TokenAmountInfo getTokenAccountBalance(PublicKey tokenAccount, Commitment commitment)
            throws RpcException;

    TokenResultObjects.TokenAmountInfo getTokenSupply(PublicKey tokenMint) throws RpcException;
    TokenResultObjects.TokenAmountInfo getTokenSupply(PublicKey tokenMint, Commitment commitment) throws RpcException;

    List<TokenResultObjects.TokenAccount> getTokenLargestAccounts(PublicKey tokenMint) throws RpcException;
    List<TokenResultObjects.TokenAccount> getTokenLargestAccounts(PublicKey tokenMint, Commitment commitment) throws RpcException;

    List<ResValueTokenAccountInfo> getTokenAccountsByOwner(PublicKey accountOwner, Map<String, Object> requiredParams,
                                                           Map<String, Object> optionalParams) throws RpcException;

    List<ResValueTokenAccountInfo> getTokenAccountsByDelegate(PublicKey accountDelegate, Map<String, Object> requiredParams,
                                                              Map<String, Object> optionalParams) throws RpcException;

    ResValueVoteAccounts getVoteAccounts() throws RpcException;
    ResValueVoteAccounts getVoteAccounts(PublicKey votePubkey, Commitment commitment) throws RpcException;

    @Deprecated
    StakeActivation getStakeActivation(PublicKey publicKey) throws RpcException;
    @Deprecated
    StakeActivation getStakeActivation(PublicKey publicKey, Long epoch, Commitment commitment) throws RpcException;

    ResValueSignatureStatuses getSignatureStatuses(List<String> signatures, boolean searchTransactionHistory) throws RpcException;

    List<ResValuePerformanceSample> getRecentPerformanceSamples() throws RpcException;
    List<ResValuePerformanceSample> getRecentPerformanceSamples(int limit) throws RpcException;

    boolean getHealth2() throws RpcException;

    List<ResValueLargestAccount> getLargestAccounts() throws RpcException;
    List<ResValueLargestAccount> getLargestAccounts(String filter, Commitment commitment) throws RpcException;

    List<LeaderSchedule> getLeaderSchedule() throws RpcException;
    List<LeaderSchedule> getLeaderSchedule(Long epoch, String identity, Commitment commitment) throws RpcException;

    List<ResValueAccountInfo> getMultipleAccounts(List<PublicKey> publicKeys) throws RpcException;
    List<ResValueAccountInfo> getMultipleAccounts(List<PublicKey> publicKeys, Map<String, Object> additionalParams) throws RpcException;

    Map<PublicKey, Optional<ResValueAccountInfo>> getMultipleAccountsMap(List<PublicKey> publicKeys) throws RpcException;

    boolean isBlockhashValid(String blockhash) throws RpcException;
    boolean isBlockhashValid(String blockhash, Commitment commitment, Long minContextSlot) throws RpcException;

    List<Long> getBlocks(long startSlot, long endSlot) throws RpcException;
    List<Long> getBlocks(long startSlot, long endSlot, Commitment commitment) throws RpcException;

    List<Long> getBlocksWithLimit(long startSlot, long limit) throws RpcException;
    List<Long> getBlocksWithLimit(long startSlot, long limit, Commitment commitment) throws RpcException;
}