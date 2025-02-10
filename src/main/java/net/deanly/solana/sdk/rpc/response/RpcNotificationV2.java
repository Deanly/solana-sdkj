package net.deanly.solana.sdk.rpc.response;

@lombok.experimental.SuperBuilder(builderMethodName = "builder2")
public class RpcNotificationV2<T> extends RpcNotification<RpcResultObject<T>> {
}
