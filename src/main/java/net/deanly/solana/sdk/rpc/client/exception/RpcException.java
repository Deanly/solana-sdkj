package net.deanly.solana.sdk.rpc.client.exception;

import lombok.Getter;
import lombok.ToString;

@Getter
public class RpcException extends Exception {
    private static final long serialVersionUID = 8315999767009642193L;

    private final Integer errorCode;

    public RpcException(String message) {
        super(message);
        this.errorCode = null; // No error code provided
    }

    public RpcException(String message, Integer errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "RpcException(" + super.getMessage() + ", errorCode=" + errorCode + ")";
    }
}
