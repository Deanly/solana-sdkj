package net.deanly.solanarpcj.transaction;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode
public class TransactionSignature {
    String signature;

    public TransactionSignature(String signature) {
        if (signature == null || signature.isEmpty()) {
            throw new IllegalArgumentException("Transaction signature cannot be null or empty");
        }
        this.signature = signature;
    }

    @Override
    public String toString() {
        return signature;
    }
}
