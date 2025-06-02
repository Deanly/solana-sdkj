package net.deanly.solana.sdk.program.metaplex.tokenmetadata.layout;


import net.deanly.solana.sdk.program.metaplex.tokenmetadata.type.UseMethod;
import net.deanly.structlayout.Field;
import net.deanly.structlayout.type.FieldBase;

public class UseMethodField extends FieldBase<UseMethod> implements Field<UseMethod> {

    public UseMethodField() {
        super(1, UseMethod.class); // 1 byte enum
    }

    @Override
    public UseMethod decode(byte[] buffer, int offset) {
        if (buffer == null || buffer.length - offset < 1) {
            throw new IllegalArgumentException(
                    String.format("Buffer underflow: expected at least 1 byte at offset %d, but only %d bytes remain.",
                            offset, buffer == null ? 0 : buffer.length - offset)
            );
        }

        int enumValue = Byte.toUnsignedInt(buffer[offset]);
        return UseMethod.fromValue(enumValue);
    }

    @Override
    public byte[] encode(UseMethod value) {
        if (value == null) {
            throw new IllegalArgumentException("UseMethod value cannot be null.");
        }

        return new byte[] { (byte) value.getValue() };
    }
}
