package net.deanly.solana.sdk.rpc.response;

import lombok.Getter;
import lombok.ToString;
import net.deanly.solana.sdk.types.SignatureStatus;
import net.deanly.solana.sdk.types.TransactionError;

@Getter
@ToString
@lombok.Builder
public class NotiValueSignature {
    private TransactionError error;
    private SignatureStatus status;
}
