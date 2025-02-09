package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.deanly.solana.sdk.types.Commitment;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@lombok.Builder
public class SignatureConfig {
    @Json(name = "commitment")
    private Commitment commitment;

    @Json(name = "enableReceivedNotification")
    private Boolean enableReceivedNotification;
}
