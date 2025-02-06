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
            .add(UnsignedLong.class, new MoshiUnsignedLongJsonAdapter())
            .add(EncodedData.class, new MoshiEncodedDataJsonAdapter())
            .add(PublicKey.class, new MoshiPublicKeyJsonAdapter())
            .add(Blockhash.class, new MoshiBlockhashJsonAdapter())
            .add(Signature.class, new MoshiSignatureJsonAdapter())
            .add(EpochCredits.class, new MoshiEpochCreditsJsonAdapter())
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
    @SuppressWarnings("unchecked")
    public <T> T call(String method, List<Object> params, Type responseType, Type errorDataType) throws RpcException {
        RpcRequest rpcRequest = new RpcRequest(method, params);

        JsonAdapter<RpcResponse<T>> resultAdapter = getCachedAdapter(responseType);

        Request request = new Request.Builder().url(this.config.getEndpoint())
                .post(RequestBody.create(rpcRequestJsonAdapter.toJson(rpcRequest), this.config.getMediaType()))
                .build();

        try {
            Response response = httpClient.newCall(request).execute();
            final String result = Objects.requireNonNull(response.body()).string();
            RpcResponse<?> rpcResponse = resultAdapter.fromJson(result);

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

            if (rpcResponse.getResult() instanceof RpcResultObject<?>) {
                return ((RpcResultObject<T>) rpcResponse.getResult()).getValue();
            } else {
                return (T) rpcResponse.getResult();
            }
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
        List<Object> result = new ArrayList<>(params.length);
        for (Object param : params) {
            if (param != null) {
                result.add(param);
            }
        }
        return result;
    }

    /*
     * Solana RPC HTTP Methods
     */

    @Override
    public ResValueAccountInfo getAccountInfo(PublicKey account, AccountInfoConfig configuration) throws RpcException {
        if (account == null) {
            throw new IllegalArgumentException("account must not be null");
        }
        Type type = Types.newParameterizedType(RpcResponseV2.class, ResValueAccountInfo.class);
        return this.call("getAccountInfo", this.getParams(account, configuration), type, null);
    }

    @Override
    public UnsignedLong getBalance(PublicKey account, BalanceConfig configuration) throws RpcException {
        if (account == null) {
            throw new IllegalArgumentException("account must not be null");
        }
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
        if (slot == null) {
            throw new IllegalArgumentException("slot must not be null");
        }
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
        return null;
    }

    @Override
    public UnsignedLong getBlockHeight(BlockHeightConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public ResValueBlockProduction getBlockProduction(BlockProductionConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public List<UnsignedLong> getBlocks(UnsignedLong startSlot, UnsignedLong endSlot, BlocksConfig configuration) throws RpcException {
        return List.of();
    }

    @Override
    public List<UnsignedLong> getBlocksWithLimit(UnsignedLong startSlot, UnsignedLong limit, BlocksWithLimitConfig configuration) throws RpcException {
        return List.of();
    }

    @Override
    public Long getBlockTime(UnsignedLong slot) throws RpcException {
        return 0L;
    }

    @Override
    public List<ResValueClusterNode> getClusterNodes() throws RpcException {
        return List.of();
    }

    @Override
    public ResValueEpochInfo getEpochInfo(EpochInfoConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public ResValueEpochSchedule getEpochSchedule() throws RpcException {
        return null;
    }

    @Override
    public Long getFeeForMessage(String message, FeeForMessageConfig configuration) throws RpcException {
        return 0L;
    }

    @Override
    public UnsignedLong getFirstAvailableBlock() throws RpcException {
        return null;
    }

    @Override
    public String getGenesisHash() throws RpcException {
        return "";
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
        return null;
    }

    @Override
    public PublicKey getIdentity() throws RpcException {
        return null;
    }

    @Override
    public ResValueInflationGovernor getInflationGovernor(InflationGovernorConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public ResValueInflationRate getInflationRate() throws RpcException {
        return null;
    }

    @Override
    public List<ResValueInflationReward> getInflationReward(List<PublicKey> addresses, InflationRewardConfig configuration) throws RpcException {
        return List.of();
    }

    @Override
    public List<ResValueLargestAccount> getLargestAccounts(LargestAccountsConfig configuration) throws RpcException {
        return List.of();
    }

    @Override
    public ResValueLatestBlockhash getLatestBlockhash(LatestBlockhashConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public Map<String, List<Integer>> getLeaderSchedule(UnsignedLong epoch, LeaderScheduleConfig configuration) throws RpcException {
        return Map.of();
    }

    @Override
    public UnsignedLong getMaxRetransmitSlot() throws RpcException {
        return null;
    }

    @Override
    public UnsignedLong getMaxShredInsertSlot() throws RpcException {
        return null;
    }

    @Override
    public UnsignedLong getMinimumBalanceForRentExemption(Integer dataLength, MinimumBalanceForRentExemptionConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public List<ResValueAccountInfo> getMultipleAccounts(List<PublicKey> accounts, MultipleAccountsConfig configuration) throws RpcException {
        return List.of();
    }

    @Override
    public List<ResValueProgramAccount> getProgramAccounts(PublicKey programId, ProgramAccountsConfig configuration) throws RpcException {
        return List.of();
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
    public List<ResValueSignatureStatus> getSignatureStatuses(List<Signature> signatures, SignatureStatusesConfig configuration) throws RpcException {
        return List.of();
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
    public UnsignedLong getStakeMinimumDelegation(StakeMinimumDelegationConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public ResValueSupply getSupply(SupplyConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public ResValueTokenAccountBalance getTokenAccountBalance(PublicKey account, TokenAccountBalanceConfig configuration) throws RpcException {
        return null;
    }

    @Override
    public List<ResValueTokenAccount> getTokenAccountsByDelegate(PublicKey delegate, TokenAccountsByDelegateFilter filter, TokenAccountsByDelegateConfig configuration) throws RpcException {
        return List.of();
    }

    @Override
    public List<ResValueTokenAccount> getTokenAccountsByOwner(PublicKey owner, TokenAccountsByOwnerFilter filter, TokenAccountsByOwnerConfig configuration) throws RpcException {
        return List.of();
    }

    @Override
    public List<ResValueTokenLargestAccounts> getTokenLargestAccounts(PublicKey mint, TokenLargestAccountsConfig configuration) throws RpcException {
        return List.of();
    }

    @Override
    public ResValueTokenSupply getTokenSupply(PublicKey mint, TokenSupplyConfig configuration) throws RpcException {
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
    public boolean isBlockhashValid(Blockhash blockhash, BlockhashValidConfig configuration) throws RpcException {
        return false;
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
    public ResValueSimulatedTransaction simulateTransaction(Transaction transaction, SimulateTransactionConfig configuration) throws RpcException {
        return null;
    }

}
