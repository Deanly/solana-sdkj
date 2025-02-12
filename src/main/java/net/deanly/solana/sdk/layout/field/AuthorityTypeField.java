package net.deanly.solana.sdk.layout.field;

import net.deanly.solana.sdk.program.spl.token.type.AuthorityType;
import net.deanly.structlayout.Field;
import net.deanly.structlayout.type.FieldBase;

public class AuthorityTypeField extends FieldBase<AuthorityType> implements Field<AuthorityType> {

    public AuthorityTypeField() {
        super(1, AuthorityType.class); // 1 byte for AuthorityType
    }

    @Override
    public AuthorityType decode(byte[] buffer, int offset) {
        // Validate that the buffer has at least 1 byte starting at the given offset
        if (buffer == null || buffer.length - offset < 1) {
            throw new IllegalArgumentException(
                    String.format("Buffer underflow: Expected at least 1 byte, but only %d bytes remain.",
                            buffer == null ? 0 : buffer.length - offset)
            );
        }

        int authorityTypeValue = Byte.toUnsignedInt(buffer[offset]); // Read as an unsigned byte
        if (authorityTypeValue < 0 || authorityTypeValue >= AuthorityType.values().length) {
            throw new IllegalArgumentException("Invalid AuthorityType value: " + authorityTypeValue);
        }

        return AuthorityType.values()[authorityTypeValue];
    }

    @Override
    public byte[] encode(AuthorityType value) {
        if (value == null) {
            throw new IllegalArgumentException("AuthorityType value cannot be null.");
        }

        byte[] result = new byte[1];
        result[0] = (byte) value.ordinal(); // Convert the enum to its ordinal value
        return result;
    }
}