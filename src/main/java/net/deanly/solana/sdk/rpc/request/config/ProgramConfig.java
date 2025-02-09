package net.deanly.solana.sdk.rpc.request.config;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.deanly.solana.sdk.rpc.request.filter.ProgramAccountFilter;
import net.deanly.solana.sdk.types.Commitment;
import net.deanly.solana.sdk.types.Encoding;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@lombok.Builder
public class ProgramConfig {
    @Json(name = "commitment")
    private Commitment commitment;

    @Json(name = "filters")
    private List<ProgramAccountFilter> filters;

    @Json(name = "encoding")
    private Encoding encoding;
}
