package net.deanly.solana.sdk.types;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InstructionError {
    @Json(name = "index")
    private int index;

    @Json(name = "error")
    private String error;
}