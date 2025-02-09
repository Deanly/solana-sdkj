package net.deanly.solana.sdk.rpc.request.filter;

import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solana.sdk.types.Encoding;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramAccountFilter {

    // array element 1
    @Json(name = "dataSize")
    private UnsignedLong dataSize; // Optional: Compare the account data length with the provided data size.

    // array element 2
    private Memcmp memcmp; // Optional: Match a series of bytes at a particular offset.

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
