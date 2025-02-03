package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solana.sdk.rpc.types.Encoding;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class AccountConfig {

    @lombok.Builder.Default
    @Json(name = "addresses")
    private List<String> addresses = List.of();

    @lombok.Builder.Default
    @Json(name = "encoding")
    private Encoding encoding = Encoding.base64;
}
