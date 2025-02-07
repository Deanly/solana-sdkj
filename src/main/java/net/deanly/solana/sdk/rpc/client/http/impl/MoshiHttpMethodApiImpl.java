package net.deanly.solana.sdk.rpc.client.http.impl;

import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.rpc.client.RpcClient;
import net.deanly.solana.sdk.rpc.client.adapter.*;
import net.deanly.solana.sdk.rpc.client.exception.RpcException;
import net.deanly.solana.sdk.rpc.client.http.HttpMethodApi;
import net.deanly.solana.sdk.rpc.request.RpcRequest;
import net.deanly.solana.sdk.rpc.request.config.*;
import net.deanly.solana.sdk.rpc.response.*;
import net.deanly.solana.sdk.transaction.Transaction;
import net.deanly.solana.sdk.types.*;
import net.deanly.solana.sdk.types.codec.Base64Checker;
import okhttp3.*;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MoshiHttpMethodApiImpl implements HttpMethodApi {
    private final RpcClient.ClientConfig config;
    private OkHttpClient httpClient;
    private final Moshi moshi = new Moshi.Builder()
            .add(MoshiFilterCriteriaJsonAdapter.FACTORY)
            .add(UnsignedLong.class, new MoshiUnsignedLongJsonAdapter())
            .add(EncodedData.class, new MoshiEncodedDataJsonAdapter())
            .add(PublicKey.class, new MoshiPublicKeyJsonAdapter())
            .add(Blockhash.class, new MoshiBlockhashJsonAdapter())
            .add(GenesisHash.class, new MoshiGenesisHashJsonAdapter())
            .add(Signature.class, new MoshiSignatureJsonAdapter())
            .add(EpochCredits.class, new MoshiEpochCreditsJsonAdapter())
            .add(ValidatorIdentityInfo.class, new MoshiValidatorIdentityInfoJsonAdapter())
            .build();

    JsonAdapter<RpcRequest> rpcRequestJsonAdapter = moshi.adapter(RpcRequest.class);
    private final Map<Type, JsonAdapter<?>> adapterCache = new ConcurrentHashMap<>();

    public MoshiHttpMethodApiImpl(RpcClient.ClientConfig config) {
        this.config = config;
        this.httpClient = this.createHttpClient();
    }

    public OkHttpClient createHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(this.config.getReadTimeoutMs(), TimeUnit.MICROSECONDS);

        if (this.config.getWriteTimeoutMs() != null) {
            builder.readTimeout(this.config.getWriteTimeoutMs(), TimeUnit.MICROSECONDS);
        }
        if (this.config.getConnectTimeoutMs() != null) {
            builder.connectTimeout(this.config.getConnectTimeoutMs(), TimeUnit.MICROSECONDS);
        }
        if (this.config.getProxyHost() != null && this.config.getProxyPort() != null) {
            builder.proxy(new Proxy(
                    Proxy.Type.SOCKS,
                    new InetSocketAddress(
                            this.config.getProxyHost(),
                            this.config.getProxyPort()
                    )
            ));
        }
        if (this.config.getUserAgent() != null) {
            builder.addNetworkInterceptor(chain -> chain.proceed(
                    chain.request().newBuilder().header("User-Agent", this.config.getUserAgent()).build()));
        }

        return builder.build();
    }

    /**
     * Makes a JSON-RPC call using the specified method, parameters, and response types.
     *
     * @param method          The name of the JSON-RPC method to execute.
     * @param params          A list of objects representing the parameters for the method call.
     * @param responseType    The expected type of the response result.
     * @param errorDataType   The type of the error data to parse if an error occurs.
     * @return The result of the JSON-RPC call, parsed into the specified response type.
     * @throws RpcException   If an error occurs during the RPC call or when parsing the response.
     */
    public <T> T call(String method, List<Object> params, Type responseType, Type errorDataType) throws RpcException {
        RpcRequest rpcRequest = new RpcRequest(method, params);

        JsonAdapter<RpcResponse<T>> resultAdapter = getCachedAdapter(responseType);

        Request request = new Request.Builder().url(this.config.getEndpoint())
                .post(RequestBody.create(rpcRequestJsonAdapter.toJson(rpcRequest), this.config.getMediaType()))
                .build();

        try {
            Response response = httpClient.newCall(request).execute();
            final String result = Objects.requireNonNull(response.body()).string();
            RpcResponse<T> rpcResponse = resultAdapter.fromJson(result);

            if (rpcResponse == null) {
                throw new RpcException("Failed to parse RpcResponse: Response is null");
            }

            if (rpcResponse.getError() != null) {
                RpcResponse.Error error = rpcResponse.getError();

                Map<String, Object> errorData = null;
                if (error.getData() != null) {
                    try {
                        if (errorDataType != null) {
                            JsonAdapter<Map<String, Object>> mapAdapter = getCachedAdapter(errorDataType);
                            errorData = mapAdapter.fromJsonValue(error.getData());
                        } else {
                            Type mapType = Types.newParameterizedType(Map.class, String.class, Object.class);
                            JsonAdapter<Map<String, Object>> mapAdapter = getCachedAdapter(mapType);
                            errorData = mapAdapter.fromJsonValue(error.getData());
                        }
                    } catch (Exception e) {
                        throw new RpcException(
                                "Failed to parse error data: " + e.getMessage(),
                                (int) error.getCode(),
                                null
                        );
                    }
                }

                throw new RpcException(error.getMessage(), (int) error.getCode(), errorData);
            }

            return rpcResponse.getResult();

        } catch (SSLHandshakeException e) {
            this.httpClient = this.createHttpClient();
            throw new RpcException("SSL Handshake failed: " + e.getMessage());
        } catch (IOException e) {
            throw new RpcException("IO error during RPC call: " + e.getMessage());
        }
    }

    @Deprecated
    public <T> T call(String method, List<Object> params, Type responseType) throws RpcException {
        return this.call(method, params, responseType, null);
    }

    @SuppressWarnings("unchecked")
    private <T> JsonAdapter<T> getCachedAdapter(Type responseType) {
        return (JsonAdapter<T>) adapterCache.computeIfAbsent(responseType, moshi::adapter);
    }

    private List<Object> getParams(Object... params) {
        if (params == null || params.length == 0) {
            return null;
        }

        List<Object> result = new ArrayList<>(Arrays.asList(params));

        if (result.stream().allMatch(Objects::isNull)) {
            return null;
        }

        for (int i = result.size() - 1; i >= 0; i--) {
            if (result.get(i) != null) {
                break;
            }
            result.remove(i);
        }

        return result;
    }

    /*
     * Solana RPC HTTP Methods
     */

    @Override
    public RpcResultObject<ResValueAccountInfo> getAccountInfo(PublicKey account, AccountInfoConfig configuration) throws RpcException {
        Objects.requireNonNull(account, "account must not be null");
        Type type = Types.newParameterizedType(RpcResponseV2.class, ResValueAccountInfo.class);
        return this.call("getAccountInfo", this.getParams(account, configuration), type, null);
    }

    @Override
    public RpcResultObject<UnsignedLong> getBalance(PublicKey account, BalanceConfig configuration) throws RpcException {
        Objects.requireNonNull(account, "account must not be null");
        Type type = Types.newParameterizedType(RpcResponseV2.class, UnsignedLong.class);
        return this.call("getBalance", this.getParams(account, configuration), type, null);
    }

    private static final EnumSet<Encoding> SUPPORTED_ENCODINGS_BLOCK = EnumSet.of(
            Encoding.BASE64,
            Encoding.JSON,
            Encoding.BASE58,
            Encoding.JSON_PARSED
    );
    @Override
    public ResValueBlock getBlock(UnsignedLong slot, BlockConfig configuration) throws RpcException {
        Objects.requireNonNull(slot, "slot must not be null");
        if (configuration.getEncoding() != null && !SUPPORTED_ENCODINGS_BLOCK.contains(configuration.getEncoding())) {
            throw new IllegalArgumentException("Unsupported encoding: " + configuration.getEncoding());
        }
        if (Commitment.PROCESSED.equals(configuration.getCommitment())) {
            throw new IllegalArgumentException("PROCESSED commitment is not supported for getBalance");
        }
        Type type = Types.newParameterizedType(RpcResponse.class, ResValueBlock.class);
        return this.call("getBlock", this.getParams(slot, configuration), type, null);
    }

    @Override
    public ResValueBlockCommitment getBlockCommitment(UnsignedLong block) throws RpcException {
        Objects.requireNonNull(block, "block must not be null");
        Type type = Types.newParameterizedType(RpcResponse.class, ResValueBlockCommitment.class);
        return this.call("getBlockCommitment", this.getParams(block), type, null);
    }

    @Override
    public UnsignedLong getBlockHeight(BlockHeightConfig configuration) throws RpcException {
        Type type = Types.newParameterizedType(RpcResponse.class, UnsignedLong.class);
        return this.call("getBlockHeight", this.getParams(configuration), type, null);
    }

    @Override
    public RpcResultObject<ResValueBlockProduction> getBlockProduction(BlockProductionConfig configuration) throws RpcException {
        Type type = Types.newParameterizedType(RpcResponseV2.class, ResValueBlockProduction.class);
        return this.call("getBlockProduction", this.getParams(configuration), type, null);
    }

    @Override
    public List<UnsignedLong> getBlocks(UnsignedLong startSlot, UnsignedLong endSlot, BlocksConfig configuration) throws RpcException {
        Objects.requireNonNull(startSlot, "startSlot must not be null");
        Type type = Types.newParameterizedType(RpcResponse.class, Types.newParameterizedType(List.class, UnsignedLong.class));
        return this.call("getBlocks", this.getParams(startSlot, endSlot, configuration), type, null);
    }

    @Override
    public List<UnsignedLong> getBlocksWithLimit(UnsignedLong startSlot, UnsignedLong limit, BlocksWithLimitConfig configuration) throws RpcException {
        Objects.requireNonNull(startSlot, "startSlot must not be null");
        Objects.requireNonNull(limit, "limit must not be null");
        Type type = Types.newParameterizedType(RpcResponse.class, Types.newParameterizedType(List.class, UnsignedLong.class));
        return this.call("getBlocksWithLimit", this.getParams(startSlot, limit, configuration), type, null);
    }

    @Override
    public Long getBlockTime(UnsignedLong slot) throws RpcException {
        Objects.requireNonNull(slot, "slot must not be null");
        Type type = Types.newParameterizedType(RpcResponse.class, Long.class);
        return this.call("getBlockTime", this.getParams(slot), type, null);
    }

    @Override
    public List<ResValueClusterNode> getClusterNodes() throws RpcException {
        Type type = Types.newParameterizedType(RpcResponse.class, Types.newParameterizedType(List.class, ResValueClusterNode.class));
        return this.call("getClusterNodes", this.getParams(), type, null);
    }

    @Override
    public ResValueEpochInfo getEpochInfo(EpochInfoConfig configuration) throws RpcException {
        Type type = Types.newParameterizedType(RpcResponse.class, ResValueEpochInfo.class);
        return this.call("getEpochInfo", this.getParams(configuration), type, null);
    }

    @Override
    public ResValueEpochSchedule getEpochSchedule() throws RpcException {
        Type type = Types.newParameterizedType(RpcResponse.class, ResValueEpochSchedule.class);
        return this.call("getEpochSchedule", this.getParams(), type, null);
    }

    @Override
    public RpcResultObject<UnsignedLong> getFeeForMessage(String message, FeeForMessageConfig configuration) throws RpcException {
        Objects.requireNonNull(message, "message must not be null");
        if (!Base64Checker.isValidBase64(message)) {
            throw new IllegalArgumentException("message must not be base64 encoded");
        }

        Type type = Types.newParameterizedType(RpcResponseV2.class, UnsignedLong.class);
        return this.call("getFeeForMessage", this.getParams(message, configuration), type, null);
    }

    @Override
    public UnsignedLong getFirstAvailableBlock() throws RpcException {
        Type type = Types.newParameterizedType(RpcResponse.class, UnsignedLong.class);
        return this.call("getFirstAvailableBlock", this.getParams(), type, null);
    }

    @Override
    public GenesisHash getGenesisHash() throws RpcException {
        Type type = Types.newParameterizedType(RpcResponse.class, GenesisHash.class);
        return this.call("getGenesisHash", this.getParams(), type, null);
    }

    @Override
    public void getHealth() throws RpcException {
        Type resType = Types.newParameterizedType(RpcResponse.class, String.class);
        Type errType = Types.newParameterizedType(Map.class, String.class, Integer.class);
        this.call("getHealth", List.of(), resType, errType).equals("ok");
    }

    @Override
    public boolean getHealthCheck() {
        Type resType = Types.newParameterizedType(RpcResponse.class, String.class);
        Type errType = Types.newParameterizedType(Map.class, String.class, Integer.class);
        try {
            return this.call("getHealth", List.of(), resType, errType).equals("ok");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ResValueSnapshotSlot getHighestSnapshotSlot() throws RpcException {
        Type type = Types.newParameterizedType(RpcResponse.class, ResValueSnapshotSlot.class);
        Type errType = Types.newParameterizedType(Map.class, String.class, Integer.class);
        return this.call("getHighestSnapshotSlot", this.getParams(), type, errType);
    }

    @Override
    public ResValueIdentity getIdentity() throws RpcException {
        Type type = Types.newParameterizedType(RpcResponse.class, ResValueIdentity.class);
        return this.call("getIdentity", this.getParams(), type, null);
    }

    @Override
    public ResValueInflationGovernor getInflationGovernor(InflationGovernorConfig configuration) throws RpcException {
        Type type = Types.newParameterizedType(RpcResponse.class, ResValueInflationGovernor.class);
        return this.call("getInflationGovernor", this.getParams(configuration), type, null);
    }

    @Override
    public ResValueInflationRate getInflationRate() throws RpcException {
        Type type = Types.newParameterizedType(RpcResponse.class, ResValueInflationRate.class);
        return this.call("getInflationRate", this.getParams(), type, null);
    }

    @Override
    public List<ResValueInflationReward> getInflationReward(List<PublicKey> addresses, InflationRewardConfig configuration) throws RpcException {
        Type type = Types.newParameterizedType(RpcResponse.class, Types.newParameterizedType(List.class, ResValueInflationReward.class));
        return this.call("getInflationReward", this.getParams(addresses, configuration), type, null);
    }

    @Override
    public RpcResultObject<List<ResValueLargestAccount>> getLargestAccounts(LargestAccountsConfig configuration) throws RpcException {
        Type type = Types.newParameterizedType(RpcResponseV2.class, Types.newParameterizedType(List.class, ResValueLargestAccount.class));
        return this.call("getLargestAccounts", this.getParams(configuration), type, null);
    }

    @Override
    public RpcResultObject<ResValueLatestBlockhash> getLatestBlockhash(LatestBlockhashConfig configuration) throws RpcException {
        Type type = Types.newParameterizedType(RpcResponseV2.class, ResValueLatestBlockhash.class);
        return this.call("getLatestBlockhash", this.getParams(configuration), type, null);
    }

    @Override
    public Map<PublicKey, List<Integer>> getLeaderSchedule(UnsignedLong epoch, LeaderScheduleConfig configuration) throws RpcException {
        Type type = Types.newParameterizedType(RpcResponse.class, Types.newParameterizedType(Map.class, PublicKey.class, Types.newParameterizedType(List.class, Integer.class)));
        return this.call("getLeaderSchedule", this.getParams(epoch, configuration), type, null);
    }

    @Override
    public UnsignedLong getMaxRetransmitSlot() throws RpcException {
        Type type = Types.newParameterizedType(RpcResponse.class, UnsignedLong.class);
        return this.call("getMaxRetransmitSlot", this.getParams(), type, null);
    }

    @Override
    public UnsignedLong getMaxShredInsertSlot() throws RpcException {
        Type type = Types.newParameterizedType(RpcResponse.class, UnsignedLong.class);
        return this.call("getMaxShredInsertSlot", this.getParams(), type, null);
    }

    @Override
    public UnsignedLong getMinimumBalanceForRentExemption(Integer dataLength, MinimumBalanceForRentExemptionConfig configuration) throws RpcException {
        Type type = Types.newParameterizedType(RpcResponse.class, UnsignedLong.class);
        return this.call("getMinimumBalanceForRentExemption", this.getParams(dataLength, configuration), type, null);
    }

    @Override
    public RpcResultObject<List<ResValueAccountInfo>> getMultipleAccounts(List<PublicKey> accounts, MultipleAccountsConfig configuration) throws RpcException {
        Objects.requireNonNull(accounts, "accounts must not be null");
        Type type = Types.newParameterizedType(RpcResponseV2.class, Types.newParameterizedType(List.class, ResValueAccountInfo.class));
        return this.call("getMultipleAccounts", this.getParams(accounts, configuration), type, null);
    }

    @Override
    public List<ResValueProgramAccount> getProgramAccounts(PublicKey programId, ProgramAccountsConfig configuration) throws RpcException {
        Objects.requireNonNull(programId, "programId must not be null");
        Type type = Types.newParameterizedType(RpcResponse.class, Types.newParameterizedType(List.class, ResValueProgramAccount.class));
        return this.call("getProgramAccounts", this.getParams(programId, configuration), type, null);
    }

    @Override
    public List<ResValuePerformanceSample> getRecentPerformanceSamples(Integer limit) throws RpcException {
        return List.of();
    }

    @Override
    public List<ResValuePrioritizationFee> getRecentPrioritizationFees(List<PublicKey> accounts) throws RpcException {
        return List.of();
    }

    @Override
    public List<ResValueTransactionSignature> getSignaturesForAddress(PublicKey account, SignaturesForAddressConfig configuration) throws RpcException {
        return List.of();
    }

    @Override
    public RpcResultObject<List<ResValueSignatureStatus>> getSignatureStatuses(List<Signature> signatures, SignatureStatusesConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public UnsignedLong getSlot(SlotConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public PublicKey getSlotLeader(SlotLeaderConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public List<PublicKey> getSlotLeaders(UnsignedLong startSlot, UnsignedLong limit) throws RpcException {
        return List.of();
    }

    @Override
    public RpcResultObject<UnsignedLong> getStakeMinimumDelegation(StakeMinimumDelegationConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public RpcResultObject<ResValueSupply> getSupply(SupplyConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public RpcResultObject<ResValueTokenAccountBalance> getTokenAccountBalance(PublicKey account, TokenAccountBalanceConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public RpcResultObject<List<ResValueTokenAccount>> getTokenAccountsByDelegate(PublicKey delegate, TokenAccountsByDelegateFilter filter, TokenAccountsByDelegateConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public RpcResultObject<List<ResValueTokenAccount>> getTokenAccountsByOwner(PublicKey owner, TokenAccountsByOwnerFilter filter, TokenAccountsByOwnerConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public RpcResultObject<List<ResValueTokenLargestAccounts>> getTokenLargestAccounts(PublicKey mint, TokenLargestAccountsConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public RpcResultObject<ResValueTokenSupply> getTokenSupply(PublicKey mint, TokenSupplyConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public ResValueConfirmedTransaction getTransaction(String signature, TransactionConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public UnsignedLong getTransactionCount(TransactionCountConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public ResValueVersion getVersion() throws RpcException {
        return null;
    }

    @Override
    public ResValueVoteAccounts getVoteAccounts(VoteAccountsConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public RpcResultObject<Boolean> isBlockhashValid(Blockhash blockhash, BlockhashValidConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public UnsignedLong minimumLedgerSlot() throws RpcException {
        return null;
    }

    @Override
    public Signature requestAirdrop(PublicKey pubkey, UnsignedLong lamports, RequestAirdropConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public Signature sendTransaction(Transaction transaction, SendTransactionConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public RpcResultObject<ResValueSimulatedTransaction> simulateTransaction(Transaction transaction, SimulateTransactionConfig configuration) throws RpcException {
        return null;
    }

}
