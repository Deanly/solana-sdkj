package net.deanly.solana.sdk.types;

import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString
public class EncodedData {
    private final Encoding encoding;
    private final String value;
    private final Map<String, Object> object;
    private final boolean isSingleType;

    public EncodedData(Encoding encoding, String value) {
        this.encoding = encoding;
        this.value = value;
        this.object = null;
        this.isSingleType = false;
    }

    public EncodedData(Map<String, Object> object) {
        this.encoding = Encoding.JSON_PARSED;
        this.value = null;
        this.object = object;
        this.isSingleType = false;
    }

    public EncodedData(Encoding encoding, String value, boolean isSingleType) {
        this.encoding = encoding;
        this.value = value;
        this.object = null;
        this.isSingleType = isSingleType;
    }
}
