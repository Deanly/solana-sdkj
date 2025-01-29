package net.deanly.solanarpcj.program.exception;

public class ProgramException extends RuntimeException {
    public ProgramException(String message) {
        super(message);
    }
    public ProgramException(String message, Throwable cause) {
        super(message, cause);
    }
}
