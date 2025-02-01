package net.deanly.solana.sdk.rpc.types;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DataSize {

    @Json(name = "dataSize")
    private int dataSize;
}