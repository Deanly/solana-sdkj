package net.deanly.solana.sdk.rpc.client;

import lombok.Getter;
import net.deanly.solana.sdk.rpc.client.http.HttpMethodApi;
import net.deanly.solana.sdk.rpc.client.http.SyncApi;
import net.deanly.solana.sdk.rpc.client.http.impl.MoshiHttpMethodApiImpl;
import net.deanly.solana.sdk.rpc.client.http.LegacyRpcApi;
import net.deanly.solana.sdk.rpc.client.http.impl.LegacyRpcApiImpl;
import net.deanly.solana.sdk.rpc.client.http.impl.MoshiSyncApiImpl;
import net.deanly.solana.sdk.rpc.client.websocket.WebsocketMethodApi;
import net.deanly.solana.sdk.rpc.client.websocket.impl.MoshiWebsocketMethodApiImpl;
import okhttp3.MediaType;

import java.util.concurrent.TimeUnit;

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

    public RpcClient(Cluster cluster) {
        this(ClientConfig.builder()
                .endpoint(cluster.getEndpoint())
                .build());
    }

    /**
     * Constructs an RpcClient with a specified endpoint.
     *
     * @param endpoint the RPC endpoint
     */
    public RpcClient(String endpoint) {
        this(ClientConfig.builder()
                .endpoint(endpoint)
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
                .endpoint(endpoint)
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
                .endpoint(endpoint)
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
                .endpoint(endpoint)
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
                .endpoint(endpoint)
                .proxyHost(proxyHost)
                .proxyPort(proxyPort)
                .build());
    }

    @Getter
    @lombok.Builder(builderClassName = "Builder")
    public static class ClientConfig {
        @lombok.Builder.Default
        private MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        @lombok.Builder.Default
        private Integer readTimeoutMs = 20_000;

        private String endpoint;

        private Integer connectTimeoutMs;

        private Integer writeTimeoutMs;

        private String proxyHost;

        private Integer proxyPort;

        private String userAgent;

        @lombok.Builder.Default
        private Integer websocketListenerCacheMaxSize = 1000;

        @lombok.Builder.Default
        private Integer websocketPendingSubscriptionCacheMaxSize = 500;

        @lombok.Builder.Default
        private Integer websocketPendingUnsubscriptionCacheMaxSize = 500;

        @lombok.Builder.Default
        private Long websocketListenerExpireTimeMs = TimeUnit.HOURS.toMillis(1);

        @lombok.Builder.Default
        private Long websocketPendingSubscriptionExpireTimeMs = TimeUnit.MINUTES.toMillis(5);

        @lombok.Builder.Default
        private Long websocketPendingUnsubscriptionExpireTimeMs = TimeUnit.MINUTES.toMillis(5);
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
        return this.config.getEndpoint();
    }

}
