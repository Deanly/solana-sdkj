package net.deanly.solana.sdk.layout.field;

import net.deanly.solana.sdk.program.spl.token.type.AccountStateEnum;
import net.deanly.solana.sdk.program.spl.token.type.AuthorityType;
import net.deanly.structlayout.type.FieldBase;

public class AccountStateEnumField extends FieldBase<AccountStateEnum> {
    public AccountStateEnumField() {
        super(1, AccountStateEnum.class);
    }

    @Override
    public byte[] encode(AccountStateEnum value) {
        if (value == null) {
            throw new IllegalArgumentException("AuthorityType value cannot be null.");
        }

        byte[] result = new byte[1];
        result[0] = (byte) value.ordinal(); // Convert the enum to its ordinal value
        return result;
    }

    @Override
    public AccountStateEnum decode(byte[] bytes, int offset) {
        if (bytes == null || bytes.length - offset < 1) {
            throw new IllegalArgumentException(
                    String.format("Buffer underflow: Expected at least 1 byte, but only %d bytes remain.",
                            bytes == null ? 0 : bytes.length - offset)
            );
        }

        int stateValue = Byte.toUnsignedInt(bytes[offset]); // Read as an unsigned byte
        if (stateValue < 0 || stateValue >= AuthorityType.values().length) {
            throw new IllegalArgumentException("Invalid AccountState value: " + stateValue);
        }

        return AccountStateEnum.values()[stateValue];
    }
}
