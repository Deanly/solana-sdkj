package net.deanly.solana.sdk.rpc.response;

import lombok.*;

@Getter
@ToString
public class RpcResponseV2<T> extends RpcResponse<RpcResultObject<T>> {
}
