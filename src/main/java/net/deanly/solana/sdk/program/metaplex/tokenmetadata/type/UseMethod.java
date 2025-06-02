package net.deanly.solana.sdk.program.metaplex.tokenmetadata.type;

public enum UseMethod {
    BURN(0),
    MULTIPLE(1),
    SINGLE(2);

    private final int value;

    UseMethod(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static UseMethod fromValue(int value) {
        for (UseMethod um : values()) {
            if (um.value == value) {
                return um;
            }
        }
        throw new IllegalArgumentException("Unknown UseMethod value: " + value);
    }
}