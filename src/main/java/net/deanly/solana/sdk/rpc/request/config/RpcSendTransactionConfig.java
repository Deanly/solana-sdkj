package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solana.sdk.rpc.types.Encoding;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class RpcSendTransactionConfig {

    @lombok.Builder.Default
    @Json(name = "encoding")
    private Encoding encoding = Encoding.base64;

    @lombok.Builder.Default
    @Json(name ="skipPreflight")
    private boolean skipPreFlight = false;

    @lombok.Builder.Default
    @Json(name = "maxRetries")
    private long maxRetries = 0;

    @lombok.Builder.Default
    @Json(name = "preflightCommitment")
    private String preflightCommitment = "finalized";

    @lombok.Builder.Default
    @Json(name = "minContextSlot")
    private long minContextSlot = 0;

}
