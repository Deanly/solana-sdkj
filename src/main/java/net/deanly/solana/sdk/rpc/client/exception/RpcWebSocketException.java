package net.deanly.solana.sdk.rpc.client.exception;

public class RpcWebSocketException extends RuntimeException {
    public RpcWebSocketException(String message) {
        super(message);
    }
    public RpcWebSocketException(String message, Throwable cause) {
        super(message, cause);
    }
}
