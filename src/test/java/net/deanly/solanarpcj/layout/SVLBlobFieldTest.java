package net.deanly.solanarpcj.layout;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SVLBlobFieldTest {

    @Test
    void testEncode() {
        SVLBlobField field = new SVLBlobField();
        byte[] value = new byte[]{1, 2, 3};

        // Encode value
        byte[] encoded = field.encode(value);

        // Verify encoded result: First bytes are length, followed by actual data
        assertNotNull(encoded);
        assertTrue(encoded.length > value.length); // Must at least include encoded length prefix
        assertEquals(4, encoded.length); // VLE length (1 byte) + value length

        assertEquals(3, decodeLength(encoded, 0)); // Decode VLE prefix to get the length
        assertArrayEquals(value, extractBlob(encoded, 1, 3)); // Extract blob starting after the prefix
    }

    @Test
    void testDecode() {
        SVLBlobField field = new SVLBlobField();
        byte[] encoded = new byte[]{(byte) 0x03, 1, 2, 3}; // Length=3 (encoded in VLE) + data=[1,2,3]

        // Decode from encoded bytes
        byte[] decoded = field.decode(encoded, 0);

        // Verify decoded result
        assertNotNull(decoded);
        assertEquals(3, decoded.length);
        assertArrayEquals(new byte[]{1, 2, 3}, decoded);
    }

    @Test
    void testCalculateSpan() {
        SVLBlobField field = new SVLBlobField();
        byte[] encoded = new byte[]{(byte) 0x03, 1, 2, 3}; // Length=3 (1 byte VLE) + data=[1,2,3]

        // Calculate span
        int span = field.calculateSpan(encoded, 0);

        // Verify span calculation
        assertEquals(4, span); // 1 byte for length + 3 bytes for data
    }

    @Test
    void testEmptyValue() {
        SVLBlobField field = new SVLBlobField();
        byte[] value = new byte[0];

        // Encode empty value
        byte[] encoded = field.encode(value);

        // Verify encoding result
        assertNotNull(encoded);
        assertEquals(1, encoded.length); // Only 1 byte for length (0)
        assertEquals(0, encoded[0]); // Length prefix should be 0

        // Decode empty value
        byte[] decoded = field.decode(encoded, 0);

        // Verify decoded result
        assertNotNull(decoded);
        assertEquals(0, decoded.length); // Decoded value should be empty
    }

    @Test
    void testInvalidDecoding() {
        SVLBlobField field = new SVLBlobField();

        // Null data
        assertThrows(IllegalArgumentException.class, () -> field.decode(null, 0));

        // Offset out of bounds
        byte[] encoded = new byte[]{(byte) 0x03, 1, 2, 3};
        assertThrows(IllegalArgumentException.class, () -> field.decode(encoded, 10));

        // Insufficient data for blob
        byte[] incomplete = new byte[]{(byte) 0x03, 1};
        assertThrows(IllegalArgumentException.class, () -> field.decode(incomplete, 0));
    }

    /**
     * Helper method to decode VLE length from encoded bytes.
     */
    private int decodeLength(byte[] bytes, int offset) {
        int length = 0;
        int shift = 0;
        for (int i = offset; i < bytes.length; i++) {
            byte b = bytes[i];
            length |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                break;
            }
            shift += 7;
        }
        return length;
    }

    /**
     * Helper method to extract blob data from encoded bytes.
     */
    private byte[] extractBlob(byte[] bytes, int offset, int length) {
        byte[] blob = new byte[length];
        System.arraycopy(bytes, offset, blob, 0, length);
        return blob;
    }
}