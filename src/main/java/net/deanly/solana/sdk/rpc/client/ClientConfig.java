package net.deanly.solana.sdk.rpc.client;


import lombok.Getter;
import okhttp3.MediaType;

import java.util.concurrent.TimeUnit;

@Getter
@lombok.Builder
public class ClientConfig {
    @lombok.Builder.Default
    private MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    @lombok.Builder.Default
    private Integer readTimeoutMs = 20_000;

    @lombok.Builder.Default
    private Network network = Network.MAINNET;

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

    public String getEndpoint() {
        if (endpoint == null) {
            return network.getEndpoint();
        }
        return endpoint;
    }
}
