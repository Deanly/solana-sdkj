package net.deanly.solanarpcj.layout.field;

import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.structlayout.Field;
import net.deanly.structlayout.type.FieldBase;

import java.util.Arrays;

public class PublicKeyPadding2Field extends FieldBase<PublicKey> implements Field<PublicKey> {

    private static final int TOTAL_SIZE = 34; // Always 34 bytes
    private static final int PUBLIC_KEY_EFFECTIVE_SIZE = 32; // PublicKey uses only 32 bytes

    public PublicKeyPadding2Field() {
        super(TOTAL_SIZE); // Always span is 34 bytes
    }

    @Override
    public PublicKey decode(byte[] buffer, int offset) {
        // Validate that the buffer has at least 34 bytes starting at the given offset
        if (buffer == null || buffer.length - offset < TOTAL_SIZE) {
            throw new IllegalArgumentException(
                    String.format("Buffer underflow: Expected %d bytes, but only %d bytes remain.",
                            TOTAL_SIZE, buffer.length - offset)
            );
        }

        // Step 1: Read the first byte
        byte option = buffer[offset];
        if (option == 0) {
            // If the first byte is 0, no authority exists
            return null;
        }

        // Step 2: Read the full 34 bytes but only use the first 32 bytes after the first byte
        byte[] publicKeyBytes = Arrays.copyOfRange(buffer, offset, offset + PUBLIC_KEY_EFFECTIVE_SIZE);

        return new PublicKey(publicKeyBytes);
    }

    @Override
    public byte[] encode(PublicKey value) {
        // Allocate 34 bytes for total size
        byte[] result = new byte[TOTAL_SIZE];

        if (value == null) {
            // If authority is null, the first byte is 0, and the rest remains 0
            result[0] = 0;
        } else {
            // Encode the PublicKey with option byte set to 1
            byte[] publicKeyBytes = value.toByteArray();
            if (publicKeyBytes.length != PUBLIC_KEY_EFFECTIVE_SIZE) {
                throw new IllegalArgumentException(
                        String.format("PublicKey encoding error: Expected %d bytes but got %d bytes.",
                                PUBLIC_KEY_EFFECTIVE_SIZE, publicKeyBytes.length)
                );
            }

            // Write publicKeyBytes into result starting from the first byte
            System.arraycopy(publicKeyBytes, 0, result, 0, PUBLIC_KEY_EFFECTIVE_SIZE);
        }

        return result;
    }
}