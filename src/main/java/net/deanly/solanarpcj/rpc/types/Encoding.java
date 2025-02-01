package net.deanly.solanarpcj.rpc.types;

import lombok.Getter;

@Getter
public enum Encoding {
    base64("base64"),
    base58("base58");

    private final String value;

    Encoding(String enc) {
        this.value = enc;
    }

    public String getEncoding() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
