package net.deanly.solana.sdk.rpc.client.http.impl;

import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.rpc.client.RpcClient;
import net.deanly.solana.sdk.rpc.client.adapter.MoshiUnsignedLongJsonAdapter;
import net.deanly.solana.sdk.rpc.client.exception.RpcException;
import net.deanly.solana.sdk.rpc.client.http.HttpMethodApi;
import net.deanly.solana.sdk.rpc.request.RpcRequest;
import net.deanly.solana.sdk.rpc.request.config.AccountInfoConfig;
import net.deanly.solana.sdk.rpc.request.config.BaseConfig;
import net.deanly.solana.sdk.rpc.request.config.BlockConfig;
import net.deanly.solana.sdk.rpc.response.*;
import net.deanly.solana.sdk.rpc.types.Commitment;
import okhttp3.*;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MoshiHttpMethodApiImpl extends LegacyRpcApiImpl implements HttpMethodApi {
    private final RpcClient.ClientConfig config;
    private OkHttpClient httpClient;
    private final Moshi moshi = new Moshi.Builder()
            .add(UnsignedLong.class, new MoshiUnsignedLongJsonAdapter())
            .build();

    JsonAdapter<RpcRequest> rpcRequestJsonAdapter = moshi.adapter(RpcRequest.class);
    private final Map<Type, JsonAdapter<?>> adapterCache = new ConcurrentHashMap<>();

    public MoshiHttpMethodApiImpl(RpcClient.ClientConfig config) {
        super.setClient(this);
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


    /*
     * Solana RPC HTTP Methods
     */

    @Override
    public ResValueAccountInfo getAccountInfo(PublicKey account, AccountInfoConfig configuration) throws RpcException {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null");
        }
        List<Object> params = List.of(
                account.toString(),
                configuration
        );
        Type type = Types.newParameterizedType(RpcResponseV2.class, ResValueAccountInfo.class);
        return this.call("getAccountInfo", params, type, null);
    }

    @Override
    public UnsignedLong getBalance(PublicKey account, BaseConfig configuration) throws RpcException {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null");
        }
        List<Object> params = List.of(
                account.toString(),
                configuration
        );
        Type type = Types.newParameterizedType(RpcResponseV2.class, UnsignedLong.class);
        return this.call("getBalance", params, type, null);
    }

    @Override
    public ResValueBlock getBlock(int slot, BlockConfig configuration) throws RpcException {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null");
        }
        if (configuration.getCommitment().equals(Commitment.PROCESSED)) {
            throw new IllegalArgumentException("PROCESSED commitment is not supported for getBalance");
        }
        List<Object> params = List.of(
                slot,
                configuration
        );
        Type type = Types.newParameterizedType(RpcResponse.class, ResValueBlock.class);
        return this.call("getBlock", params, type, null);
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
}
