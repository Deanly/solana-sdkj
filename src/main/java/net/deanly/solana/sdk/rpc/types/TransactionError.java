package net.deanly.solana.sdk.rpc.types;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TransactionError {
    @Json(name = "type")
    private TransactionErrorType type;

    @Json(name = "instructionError")
    private InstructionError instructionError;  // INSTRUCTION_ERROR일 경우에만 사용
}
