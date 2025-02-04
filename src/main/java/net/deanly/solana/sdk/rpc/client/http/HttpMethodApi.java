package net.deanly.solana.sdk.rpc.client.http;

import com.google.common.primitives.UnsignedLong;
import net.deanly.solana.sdk.crypto.KeyPair;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.rpc.client.exception.RpcException;
import net.deanly.solana.sdk.rpc.request.config.*;
import net.deanly.solana.sdk.rpc.response.*;
import net.deanly.solana.sdk.rpc.types.*;
import net.deanly.solana.sdk.transaction.Transaction;

import java.util.List;
import java.util.Map;

// https://solana.com/ko/docs/rpc/http
public interface HttpMethodApi {

    // https://solana.com/ko/docs/rpc/http/getaccountinfo
    ResValueAccountInfo getAccountInfo(PublicKey account, AccountInfoConfig configuration) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getbalance
    UnsignedLong getBalance(PublicKey account, BaseConfig configuration) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getblock
    ResValueBlock getBlock(int slot, BlockConfig configuration) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getblockcommitment
    ResValueBlockCommitment getBlockCommitment(long block) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getblockheight
    long getBlockHeight(Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getblockproduction
    ResValueBlockProduction getBlockProduction(Map<String, Object> optionalParams) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getblocks
    List<Long> getBlocks(long startSlot, long endSlot, Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getblockswithlimit
    List<Long> getBlocksWithLimit(long startSlot, long limit, Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getblocktime
    long getBlockTime(long block) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getclusternodes
    List<ResResultClusterNode> getClusterNodes() throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getepochinfo
    EpochInfo getEpochInfo(Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getepochschedule
    EpochSchedule getEpochSchedule() throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getfeeformessage
    Long getFeeForMessage(String message, Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getfirstavailableblock
    long getFirstAvailableBlock() throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getgenesishash
    String getGenesisHash() throws RpcException;

    // https://solana.com/ko/docs/rpc/http/gethealth
    void getHealth() throws RpcException;
    boolean getHealthCheck();

    // https://solana.com/ko/docs/rpc/http/gethighestsnapshotslot
    SnapshotSlot getHighestSnapshotSlot() throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getidentity
    PublicKey getIdentity() throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getinflationgovernor
    InflationGovernor getInflationGovernor(Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getinflationrate
    InflationRate getInflationRate() throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getinflationreward
    List<InflationReward> getInflationReward(List<PublicKey> addresses, Long epoch, Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getlargestaccounts
    List<LargeAccount> getLargestAccounts(String filter, Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getlatestblockhash
    ResValueLatestBlockhash getLatestBlockhash(Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getleaderschedule
    List<LeaderSchedule> getLeaderSchedule(Long epoch, String identity, Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getmaxretransmitslot
    long getMaxRetransmitSlot() throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getmaxshredinsertslot
    long getMaxShredInsertSlot() throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getminimumbalanceforrentexemption
    long getMinimumBalanceForRentExemption(long dataLength, Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getmultipleaccounts
    List<ResValueAccountInfo> getMultipleAccounts(List<PublicKey> publicKeys, Map<String, Object> additionalParams) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getprogramaccounts
    List<ResValueProgram> getProgramAccounts(PublicKey account, ProgramAccountConfig programAccountConfig) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getrecentperformancesamples
    List<PerformanceSample> getRecentPerformanceSamples(int limit) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getrecentprioritizationfees
    List<RecentPrioritizationFees> getRecentPrioritizationFees(List<PublicKey> addresses) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getsignaturesforaddress
    List<SignatureInformation> getSignaturesForAddress(PublicKey account, int limit, Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getsignaturestatuses
    ResValueSignatureStatuses getSignatureStatuses(List<String> signatures, boolean searchTransactionHistory) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getslot
    long getSlot(Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getslotleader
    PublicKey getSlotLeader(Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getslotleaders
    List<PublicKey> getSlotLeaders(long startSlot, long limit) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getstakeminimumdelegation
    Long getStakeMinimumDelegation(Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getsupply
    ResValueSupply getSupply(Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/gettokenaccountbalance
    TokenResultObjects.TokenAmountInfo getTokenAccountBalance(PublicKey tokenAccount, Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/gettokenaccountsbydelegate
    List<ResValueTokenAccountInfo> getTokenAccountsByDelegate(PublicKey accountDelegate, Map<String, Object> requiredParams, Map<String, Object> optionalParams) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/gettokenaccountsbyowner
    List<ResValueTokenAccountInfo> getTokenAccountsByOwner(PublicKey accountOwner, Map<String, Object> requiredParams, Map<String, Object> optionalParams) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/gettokenlargestaccounts
    List<TokenResultObjects.TokenAccount> getTokenLargestAccounts(PublicKey tokenMint, Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/gettokensupply
    TokenResultObjects.TokenAmountInfo getTokenSupply(PublicKey tokenMint, Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/gettransaction
    ResValueConfirmedTransaction getTransaction(String signature, Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/gettransactioncount
    long getTransactionCount(Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getversion
    SolanaVersion getVersion() throws RpcException;

    // https://solana.com/ko/docs/rpc/http/getvoteaccounts
    VoteAccounts getVoteAccounts(PublicKey votePubkey, Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/isblockhashvalid
    boolean isBlockhashValid(String blockhash, Commitment commitment, Long minContextSlot) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/minimumledgerslot
    Long minimumLedgerSlot() throws RpcException;

    // https://solana.com/ko/docs/rpc/http/requestairdrop
    String requestAirdrop(PublicKey address, long lamports, Commitment commitment) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/sendtransaction
    String sendTransaction(Transaction transaction, List<KeyPair> signers, String recentBlockHash, RpcSendTransactionConfig rpcSendTransactionConfig) throws RpcException;

    // https://solana.com/ko/docs/rpc/http/simulatetransaction
    ResValueSimulatedTransaction simulateTransaction(Transaction transaction, SimulateTransactionConfig simulateTransactionConfig) throws RpcException;
}