package net.deanly.solana.sdk.rpc.client.websocket;

@FunctionalInterface
public interface NotificationListener<T> {
    void onNotification(T notification);
}
