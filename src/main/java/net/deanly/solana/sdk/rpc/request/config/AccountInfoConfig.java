package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solana.sdk.rpc.types.Encoding;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@lombok.experimental.SuperBuilder
public class AccountInfoConfig extends BaseConfig {
    @Json(name = "dataSlice")
    private Encoding.DataSlice dataSlice;

    @Json(name = "minContextSlot")
    private Long minContextSlot;
}
