package net.deanly.solana.sdk.types;

import lombok.EqualsAndHashCode;
import net.deanly.solana.sdk.types.codec.Base58;

@EqualsAndHashCode
public class Signature {
    private final String signature;

    public Signature(String signature) {
        if (!this.isValidBase58(signature)) {
            throw new IllegalArgumentException("Invalid Signature format");
        }
        this.signature = signature;
    }

    public static Signature of(String signature) {
        return new Signature(signature);
    }

    private boolean isValidBase58(String input) {
        if (input.length() < 86 || input.length() > 88) {
            return false;
        }
        return Base58.isValidBase58Char(input);
    }

    public String getValue() {
        return signature;
    }

    @Override
    public String toString() {
        return signature;
    }
}