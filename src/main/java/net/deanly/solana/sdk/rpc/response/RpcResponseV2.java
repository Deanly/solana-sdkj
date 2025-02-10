package net.deanly.solana.sdk.rpc.response;

import lombok.*;

@Getter
@ToString
@lombok.experimental.SuperBuilder(builderMethodName = "builder2")
public class RpcResponseV2<T> extends RpcResponse<RpcResultObject<T>> {
}
