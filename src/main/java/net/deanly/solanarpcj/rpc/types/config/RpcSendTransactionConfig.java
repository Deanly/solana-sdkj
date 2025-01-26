package net.deanly.solanarpcj.rpc.types.config;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RpcSendTransactionConfig {

    public static enum Encoding {
        base64("base64"),
        base58("base58");

        private String enc;

        Encoding(String enc) {
            this.enc = enc;
        }

        public String getEncoding() {
            return enc;
        }

    }

    @Builder.Default
    @Json(name = "encoding")
    private Encoding encoding = Encoding.base64;

    @Builder.Default
    @Json(name ="skipPreflight")
    private boolean skipPreFlight = false;

    @Builder.Default
    @Json(name = "maxRetries")
    private long maxRetries = 0;

    @Builder.Default
    @Json(name = "preflightCommitment")
    private String preflightCommitment = "finalized";

    @Builder.Default
    @Json(name = "minContextSlot")
    private long minContextSlot = 0;

}
