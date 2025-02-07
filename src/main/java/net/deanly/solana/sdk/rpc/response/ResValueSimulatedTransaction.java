package net.deanly.solana.sdk.rpc.response;

import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.types.StateData;
import net.deanly.solana.sdk.types.TransactionError;

import java.util.List;

@Getter @Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class ResValueSimulatedTransaction {

    @Json(name = "err")
    private TransactionError err;

    @Json(name = "logs")
    private List<String> logs;

    @Json(name = "accounts")
    private List<ResValueAccountInfo> accounts;

    @Json(name = "unitsConsumed")
    private UnsignedLong unitsConsumed;

    @Json(name = "returnData")
    private ReturnData returnData;

    @Json(name = "innerInstructions")
    private List<ResValueInnerInstruction> innerInstruction;

    @Getter @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReturnData {
        @Json(name = "programId")
        private PublicKey programId;

        @Json(name = "data")
        private StateData data;

    }
}
