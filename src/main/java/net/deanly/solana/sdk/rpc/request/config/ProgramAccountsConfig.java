package net.deanly.solana.sdk.rpc.request.config;

import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solana.sdk.types.Commitment;
import net.deanly.solana.sdk.types.Encoding;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramAccountsConfig {

    @Json(name = "commitment")
    private Commitment commitment; // Optional: Specifies the commitment level.

    @Json(name = "minContextSlot")
    private Long minContextSlot; // Optional: The minimum slot that the request can be evaluated at.

    @Json(name = "withContext")
    private Boolean withContext; // Optional: Wrap the result in an RpcResponse JSON object.

    @Json(name = "encoding")
    private Encoding encoding; // Optional: Encoding format for the returned Account data.

    @Json(name = "dataSlice")
    private Encoding.DataSlice dataSlice; // Optional: Request a slice of the account's data.

    @Json(name = "filters")
    private List<Filter> filters; // Optional: Filter results using up to 4 filter objects.

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Filter {
        @Json(name = "memcmp")
        private Memcmp memcmp; // Optional: Match a series of bytes at a particular offset.

        @Json(name = "dataSize")
        private UnsignedLong dataSize; // Optional: Compare the account data length with the provided data size.
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Memcmp {
        @Json(name = "offset")
        private Integer offset; // Number of bytes into account data to begin comparing.

        @Json(name = "bytes")
        private String bytes; // Data to match, as base-58 encoded string.

        @Json(name = "encoding")
        private Encoding encoding;
    }
}