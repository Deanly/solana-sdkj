package net.deanly.solana.sdk.rpc.response;


import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.deanly.solana.sdk.types.EncodedData;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResValueInstruction {
    /// Index into the message.accountKeys array indicating the program account that executes this instruction.
    @Json(name = "programIdIndex")
    private Integer programIdIndex;

    /// List of ordered indices into the message.accountKeys array indicating which accounts to pass to the program.
    @Json(name = "accounts")
    private List<Integer> accounts;

    /// The program input data encoded in a base-58 string.
    @Json(name = "data")
    private EncodedData data;
}
