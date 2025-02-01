package net.deanly.solanarpcj.rpc.response;

import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solanarpcj.rpc.types.InnerInstruction;

import java.util.List;

@Getter @Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class ResValueSimulatedTransaction {

    @Json(name = "err")
    private Object err;

    @Json(name = "logs")
    private List<String> logs;

    @Json(name = "accounts")
    private List<ResValueAccountInfo> accounts;

    @Json(name = "logs")
    private List<String> logMessages;

    @Json(name = "unitsConsumed")
    private Long unitsConsumed;

    @Json(name = "returnData")
    private ReturnData returnData;

    @Json(name = "innerInstructions")
    private List<InnerInstruction> innerInstruction;

    @Getter @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReturnData {
        @Json(name = "programId")
        private String programId;

        @Json(name = "data")
        private List<String> data;

    }
}
