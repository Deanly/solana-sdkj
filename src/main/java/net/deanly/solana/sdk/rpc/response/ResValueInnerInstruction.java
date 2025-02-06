package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class ResValueInnerInstruction {
    /// Index of the transaction instruction from which the inner instruction(s) originated
    @Json(name = "index")
    private Integer index;

    /// Ordered list of inner program instructions that were invoked during a single transaction instruction.
    @Json(name = "instructions")
    private List<ResValueInstruction> instructions;

}
