package net.deanly.solana.sdk.rpc.client.exception;

import lombok.Getter;

import java.io.Serial;
import java.util.Map;

@Getter
public class RpcException extends Exception {
    private final Integer errorCode;
    private final Map<String, Object> errorData;

    public RpcException(String message) {
        super(message);
        this.errorCode = null; // No error code provided
        this.errorData = null;
    }

    public RpcException(String message, Integer errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.errorData = null;
    }

    public RpcException(String message, Integer errorCode,  Map<String, Object> errorData) {
        super(message);
        this.errorCode = errorCode;
        this.errorData = errorData;
    }

    @Override
    public String toString() {
        return "RpcException(" + super.getMessage() + ", errorCode=" + errorCode + ")";
    }
}
