package net.deanly.solana.sdk.rpc.client.websocket;

import net.deanly.solana.sdk.rpc.response.RpcNotification;

@FunctionalInterface
public interface NotificationListener<T> {
    void onNotification(RpcNotification<T> notification);
}
