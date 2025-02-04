package net.deanly.solana.sdk.rpc.types;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public enum Encoding {
    @Json(name = "base64")
    BASE64,

    @Json(name = "base58")
    BASE58,

    @Json(name = "base64+zstd")
    BASE64_ZSTD,

    @Json(name = "jsonParsed")
    JSON_PARSED;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    /// Data slicing is only available for base58, base64, or base64+zstd encodings.
    public static class DataSlice {
        /// number of bytes to return
        @Json(name = "length")
        private Integer length;

        /// byte offset from which to start reading
        @Json(name = "offset")
        private Integer offset;
    }
}
