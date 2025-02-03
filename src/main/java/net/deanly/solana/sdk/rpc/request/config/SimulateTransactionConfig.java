package net.deanly.solana.sdk.rpc.request.config;

import lombok.*;
import com.squareup.moshi.Json;
import net.deanly.solana.sdk.rpc.types.Encoding;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class SimulateTransactionConfig {

    @lombok.Builder.Default
    @Json(name = "commitment")
    private String commitment = "finalized";

    @lombok.Builder.Default
    @Json(name = "sigVerify")
    private Boolean sigVerify = false;

    @lombok.Builder.Default
    @Json(name = "replaceRecentBlockhash")
    private Boolean replaceRecentBlockhash = false;

    @lombok.Builder.Default
    @Json(name = "minContextSlot")
    private Long minContextSlot = null;

    @lombok.Builder.Default
    @Json(name = "encoding")
    private Encoding encoding = Encoding.base64;

    @lombok.Builder.Default
    @Json(name = "innerInstructions")
    private Boolean innerInstructions = false;

    @lombok.Builder.Default
    @Json(name = "accounts")
    private AccountConfig accounts = null;

    public SimulateTransactionConfig(Encoding encoding) {
        this.encoding = encoding;
    }

}