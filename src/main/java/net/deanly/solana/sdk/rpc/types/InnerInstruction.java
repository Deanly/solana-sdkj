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
    @Json(name = "index")
    private Integer index;

    @Json(name = "instructions")
    private List<Instruction> instructions;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Instruction {
        @Json(name = "programIdIndex")
        private Integer programIdIndex;

        @Json(name = "accounts")
        private List<Integer> accounts;

        @Json(name = "data")
        private String data;
    }
}
