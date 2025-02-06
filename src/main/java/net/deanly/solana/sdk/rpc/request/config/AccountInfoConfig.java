package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solana.sdk.types.Commitment;
import net.deanly.solana.sdk.types.Encoding;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@lombok.Builder
public class AccountInfoConfig {
    @Json(name = "commitment")
    private Commitment commitment;

    @Json(name = "encoding")
    private Encoding encoding;

    @Json(name = "dataSlice")
    private Encoding.DataSlice dataSlice;

    @Json(name = "minContextSlot")
    private Long minContextSlot;
}
