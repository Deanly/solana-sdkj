package net.deanly.solanarpcj.rpc.config;

import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solanarpcj.rpc.types.Encoding;

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
