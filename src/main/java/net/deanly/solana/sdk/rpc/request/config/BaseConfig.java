package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solana.sdk.rpc.types.Commitment;
import net.deanly.solana.sdk.rpc.types.Encoding;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@lombok.experimental.SuperBuilder
public class BaseConfig {
    @lombok.Builder.Default
    @Json(name = "commitment")
    private Commitment commitment = Commitment.FINALIZED;

    @lombok.Builder.Default
    @Json(name = "encoding")
    private Encoding encoding = Encoding.BASE64;
}
