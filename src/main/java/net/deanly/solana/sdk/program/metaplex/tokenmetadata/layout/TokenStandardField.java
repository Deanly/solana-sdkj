package net.deanly.solana.sdk.program.metaplex.tokenmetadata.layout;


import net.deanly.solana.sdk.program.metaplex.tokenmetadata.type.TokenStandard;
import net.deanly.structlayout.Field;
import net.deanly.structlayout.type.FieldBase;

public class TokenStandardField extends FieldBase<TokenStandard> implements Field<TokenStandard> {

    public TokenStandardField() {
        super(1, TokenStandard.class); // 1 byte for enum value
    }

    @Override
    public TokenStandard decode(byte[] buffer, int offset) {
        if (buffer == null || buffer.length - offset < 1) {
            throw new IllegalArgumentException(
                    String.format("Buffer underflow: expected at least 1 byte at offset %d, but only %d bytes remain.",
                            offset, buffer == null ? 0 : buffer.length - offset)
            );
        }

        int enumValue = Byte.toUnsignedInt(buffer[offset]);
        return TokenStandard.fromValue(enumValue);
    }

    @Override
    public byte[] encode(TokenStandard value) {
        if (value == null) {
            throw new IllegalArgumentException("TokenStandard value cannot be null.");
        }

        return new byte[] { (byte) value.getValue() };
    }
}