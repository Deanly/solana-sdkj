package net.deanly.solana.sdk.rpc.client.http.impl;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import net.deanly.solana.sdk.rpc.client.RpcClient;
import net.deanly.solana.sdk.rpc.client.exception.RpcException;
import net.deanly.solana.sdk.rpc.client.http.HttpMethodApi;
import net.deanly.solana.sdk.rpc.client.legacy.http.RpcApiImpl;
import net.deanly.solana.sdk.rpc.request.RpcRequest;
import net.deanly.solana.sdk.rpc.response.RpcResponse;
import net.deanly.solana.sdk.rpc.response.RpcResultObject;
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

public class MoshiHttpMethodApiImpl extends RpcApiImpl implements HttpMethodApi {
    private final RpcClient.ClientConfig config;
    private OkHttpClient httpClient;
    private final Moshi moshi = new Moshi.Builder().build();

    JsonAdapter<RpcRequest> rpcRequestJsonAdapter = moshi.adapter(RpcRequest.class);
    private final Map<Type, JsonAdapter<?>> adapterCache = new ConcurrentHashMap<>();

    public MoshiHttpMethodApiImpl(RpcClient.ClientConfig config) {
        super(config.getEndpoint());
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
     * Calls the specified RPC method with the given parameters.
     *
     * @param method the RPC method to call
     * @param params the parameters for the RPC method
     * @param responseType  the type of the expected result
     * @return the result of the RPC call
     * @throws RpcException if an error occurs during the RPC call
     */
    @SuppressWarnings("unchecked")
    public <T> T call(String method, List<Object> params, Type responseType) throws RpcException {
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
                throw new RpcException(
                        "RPC Error: " + error.getMessage(),
                        (int) error.getCode() // Convert long to Integer for RpcException
                );
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

    @SuppressWarnings("unchecked")
    private <T> JsonAdapter<RpcResponse<T>> getCachedAdapter(Type responseType) {
        return (JsonAdapter<RpcResponse<T>>) adapterCache.computeIfAbsent(
                responseType, type -> moshi.adapter(Types.newParameterizedType(RpcResponse.class, type))
        );
    }


}
