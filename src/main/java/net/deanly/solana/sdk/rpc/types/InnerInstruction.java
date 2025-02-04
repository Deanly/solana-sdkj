package net.deanly.solana.sdk.rpc.types;

import com.squareup.moshi.Json;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class InnerInstruction {
    /// Index of the transaction instruction from which the inner instruction(s) originated
    @Json(name = "index")
    private Integer index;

    /// Ordered list of inner program instructions that were invoked during a single transaction instruction.
    @Json(name = "instructions")
    private List<Instruction> instructions;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Instruction {
        /// Index into the message.accountKeys array indicating the program account that executes this instruction.
        @Json(name = "programIdIndex")
        private Integer programIdIndex;

        /// List of ordered indices into the message.accountKeys array indicating which accounts to pass to the program.
        @Json(name = "accounts")
        private List<Integer> accounts;

        /// The program input data encoded in a base-58 string.
        @Json(name = "data")
        private String data;
    }
}
