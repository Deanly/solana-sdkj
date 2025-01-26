package net.deanly.solanarpcj.layout;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.type.FieldBase;
import net.deanly.solanarpcj.core.PublicKey;

public class PublicKeyField extends FieldBase<PublicKey> implements Field<PublicKey> {

    private static final int PUBLIC_KEY_LENGTH = 32; // 32 bytes

    public PublicKeyField() {
        super(PUBLIC_KEY_LENGTH);
    }

    @Override
    public byte[] encode(PublicKey value) {
        if (value == null) {
            throw new IllegalArgumentException("PublicKey value cannot be null.");
        }
        byte[] bytes = value.toByteArray();

        if (bytes.length != PUBLIC_KEY_LENGTH) {
            throw new IllegalArgumentException("PublicKey must be exactly " + PUBLIC_KEY_LENGTH + " bytes.");
        }

        return bytes;
    }

    @Override
    public PublicKey decode(byte[] buffer, int offset) {
        if (buffer == null || buffer.length - offset < PUBLIC_KEY_LENGTH) {
            throw new IllegalArgumentException("Buffer does not contain enough data for a PublicKey.");
        }

        byte[] publicKeyBytes = new byte[PUBLIC_KEY_LENGTH];
        System.arraycopy(buffer, offset, publicKeyBytes, 0, PUBLIC_KEY_LENGTH);

        return new PublicKey(publicKeyBytes);
    }


}
