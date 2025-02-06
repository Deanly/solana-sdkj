package net.deanly.solana.sdk.types;

import com.squareup.moshi.Json;
import lombok.*;

@Getter
@RequiredArgsConstructor
public enum Encoding {
    @Json(name = "base64")
    BASE64("base64"),

    @Json(name = "base58")
    BASE58("base58"),

    @Json(name = "base64+zstd")
    BASE64_ZSTD("base64+zstd"),

    @Json(name = "jsonParsed")
    JSON_PARSED("jsonParsed"),

    @Json(name = "json")
    JSON("json");

    private final String value;

    @Override
    public String toString() {
        return value;
    }

    public static Encoding fromString(String encoding) {
        for (Encoding e : Encoding.values()) {
            if (e.value.equals(encoding)) {
                return e;
            }
        }
        return null;
    }

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
