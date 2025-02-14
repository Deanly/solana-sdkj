package net.deanly.solana.sdk.rpc.client;

import lombok.Getter;
import net.deanly.solana.sdk.rpc.client.config.ClientConfig;
import net.deanly.solana.sdk.rpc.client.config.Network;
import net.deanly.solana.sdk.rpc.client.http.HttpMethodApi;
import net.deanly.solana.sdk.rpc.client.http.SyncApi;
import net.deanly.solana.sdk.rpc.client.http.impl.MoshiHttpMethodApiImpl;
import net.deanly.solana.sdk.rpc.client.http.LegacyRpcApi;
import net.deanly.solana.sdk.rpc.client.http.impl.LegacyRpcApiImpl;
import net.deanly.solana.sdk.rpc.client.http.impl.MoshiSyncApiImpl;
import net.deanly.solana.sdk.rpc.client.websocket.WebsocketMethodApi;
import net.deanly.solana.sdk.rpc.client.websocket.impl.MoshiWebsocketMethodApiImpl;

/**
 * RpcClient is responsible for making RPC calls to a Solana cluster.
 */
public class RpcClient {
    @Getter
    private final ClientConfig config;
    private final HttpMethodApi httpMethodApi;
    private final WebsocketMethodApi websocketMethodApi;
    private final SyncApi syncApi;

    @Deprecated
    private final LegacyRpcApiImpl legacyRpcApi;


    public RpcClient(ClientConfig config) {
        this.config = config;
        this.httpMethodApi = new MoshiHttpMethodApiImpl(config);
        this.websocketMethodApi = new MoshiWebsocketMethodApiImpl(config);
        this.syncApi = new MoshiSyncApiImpl(this.httpMethodApi, this.websocketMethodApi);

        this.legacyRpcApi = new LegacyRpcApiImpl();
        this.legacyRpcApi.setClient((MoshiHttpMethodApiImpl) this.httpMethodApi);
    }

    public RpcClient(Network network) {
        this(ClientConfig.builder()
                .network(network)
                .build());
    }

    /**
     * Constructs an RpcClient with a specified endpoint.
     *
     * @param endpoint the RPC endpoint
     */
    public RpcClient(String endpoint) {
        this(ClientConfig.builder()
                .endpointHttp(endpoint)
                .build());
    }

    /**
     * Constructs an RpcClient with a specified endpoint and user agent.
     *
     * @param endpoint  the RPC endpoint
     * @param userAgent the user agent to set in the request header
     */
    public RpcClient(String endpoint, String userAgent) {
        this(ClientConfig.builder()
                .endpointHttp(endpoint)
                .userAgent(userAgent)
                .build());
    }

    /**
     * Constructs an RpcClient with a specified endpoint and timeout.
     *
     * @param endpoint the RPC endpoint
     * @param timeout  the read timeout in seconds
     */
    public RpcClient(String endpoint, int timeout) {
        this(ClientConfig.builder()
                .endpointHttp(endpoint)
                .readTimeoutMs(timeout * 1000)
                .build());
    }

    /**
     * Constructs an RpcClient with specified timeouts for read, connect, and write.
     *
     * @param endpoint        the RPC endpoint
     * @param readTimeoutMs   the read timeout in milliseconds
     * @param connectTimeoutMs the connect timeout in milliseconds
     * @param writeTimeoutMs  the write timeout in milliseconds
     */
    public RpcClient(String endpoint, int readTimeoutMs, int connectTimeoutMs, int writeTimeoutMs) {
        this(ClientConfig.builder()
                .endpointHttp(endpoint)
                .readTimeoutMs(readTimeoutMs)
                .connectTimeoutMs(connectTimeoutMs)
                .writeTimeoutMs(writeTimeoutMs)
                .build());
    }

    /**
     * Constructs an RpcClient with a specified endpoint and SOCKS proxy.
     *
     * @param endpoint the RPC endpoint
     * @param proxyHost the SOCKS proxy host
     * @param proxyPort the SOCKS proxy port
     */
    public RpcClient(String endpoint, String proxyHost, int proxyPort) {
        this(ClientConfig.builder()
                .endpointHttp(endpoint)
                .proxyHost(proxyHost)
                .proxyPort(proxyPort)
                .build());
    }

    /**
     * Returns the LegacyRpcApi instance associated with this client.
     *
     * @return the LegacyRpcApi instance
     */
    @Deprecated
    public LegacyRpcApi getLegacyApi() {
        return this.legacyRpcApi;
    }

    /**
     * Returns the HttpMethodApi instance associated with this client.
     *
     * @return the HttpMethodApi instance
     */
    public HttpMethodApi getRpcHttpApi() {
        return this.httpMethodApi;
    }

    /**
     * Returns the WebsocketMethodApi instance associated with this client.
     *
     * @return the WebsocketMethodApi instance
     */
    public WebsocketMethodApi getRpcWebSocketApi() {
        return this.websocketMethodApi;
    }

    /**
     * Returns the SyncApi instance associated with this client.
     *
     * @return the SyncApi instance
     */
    public SyncApi getSyncApi() {
        return this.syncApi;
    }

    /**
     * Returns the current RPC endpoint.
     *
     * @return the RPC endpoint
     */
    public String getEndpoint() {
        return this.config.getEndpointHttp();
    }

}
