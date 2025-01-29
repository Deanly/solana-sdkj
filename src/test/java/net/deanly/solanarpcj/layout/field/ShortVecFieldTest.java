package net.deanly.solanarpcj.layout.field;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ShortVecFieldTest {

    @Test
    void testEncodeSingleByteValue() {
        ShortVecField shortVecField = new ShortVecField();
        int value = 127; // 최대 단일 바이트 값
        byte[] encoded = shortVecField.encode(value);

        assertEquals(1, encoded.length, "Encoded length should be 1 byte for values <= 127.");
        assertEquals((byte) 127, encoded[0], "Encoded value mismatch for 127.");
    }

    @Test
    void testEncodeMultiByteValue() {
        ShortVecField shortVecField = new ShortVecField();
        int value = 300; // 다중 바이트 값
        byte[] encoded = shortVecField.encode(value);

        assertEquals(2, encoded.length, "Encoded length should be 2 bytes for values >= 128.");
        assertEquals((byte) 0xAC, encoded[0], "First byte of encoded value is incorrect.");
        assertEquals((byte) 0x02, encoded[1], "Second byte of encoded value is incorrect.");
    }

    @Test
    void testEncodeMaxValue() {
        ShortVecField shortVecField = new ShortVecField();
        int value = Integer.MAX_VALUE;

        byte[] encoded = shortVecField.encode(value);

        assertTrue(encoded.length <= 5, "Encoded length for Integer.MAX_VALUE should not exceed 5 bytes.");
    }

    @Test
    void testDecodeSingleByteValue() {
        ShortVecField shortVecField = new ShortVecField();
        byte[] encoded = new byte[]{127}; // 단일 바이트 값

        int decodedValue = shortVecField.decode(encoded, 0);

        assertEquals(127, decodedValue, "Decoded value mismatch for a single-byte VLE value.");
    }

    @Test
    void testDecodeMultiByteValue() {
        ShortVecField shortVecField = new ShortVecField();
        byte[] encoded = new byte[]{(byte) 0xAC, 0x02}; // 300의 VLE 인코딩

        int decodedValue = shortVecField.decode(encoded, 0);

        assertEquals(300, decodedValue, "Decoded value mismatch for a multi-byte VLE value.");
        assertEquals(2, shortVecField.getSpan(), "Dynamic span should match the length of the encoded value.");
    }

    @Test
    void testDecodeMaxValue() {
        ShortVecField shortVecField = new ShortVecField();
        byte[] encoded = shortVecField.encode(Integer.MAX_VALUE);

        int decodedValue = shortVecField.decode(encoded, 0);

        assertEquals(Integer.MAX_VALUE, decodedValue, "Decoded value mismatch for Integer.MAX_VALUE.");
    }

    @Test
    void testCalculateEncodedSpan() {
        ShortVecField shortVecField = new ShortVecField();
        byte[] encoded = shortVecField.encode(300);

        int span = shortVecField.calculateSpan(encoded, 0);

        assertEquals(2, span, "Span calculation mismatch for encoded value 300.");
    }

    @Test
    void testEncodeAndDecodeConsistency() {
        ShortVecField shortVecField = new ShortVecField();
        int originalValue = 123456;

        byte[] encoded = shortVecField.encode(originalValue);
        int decodedValue = shortVecField.decode(encoded, 0);

        assertEquals(originalValue, decodedValue, "Decoded value should match the original value after encoding and decoding.");
    }

    @Test
    void testDecodeWithOffset() {
        ShortVecField shortVecField = new ShortVecField();
        // 데이터 앞에 2 바이트 오프셋 추가
        byte[] data = new byte[]{0x00, 0x00, (byte) 0xAC, 0x02};

        int decodedValue = shortVecField.decode(data, 2);

        assertEquals(300, decodedValue, "Decoded value mismatch when offset is applied.");
        assertEquals(2, shortVecField.getSpan(), "Dynamic span should match the length when offset is used.");
    }

    @Test
    void testDecodeThrowsOnInvalidOffset() {
        ShortVecField shortVecField = new ShortVecField();
        byte[] data = new byte[]{127};

        assertThrows(IllegalArgumentException.class, () -> shortVecField.decode(data, -1),
                "Should throw exception for negative offset.");
        assertThrows(IllegalArgumentException.class, () -> shortVecField.decode(data, 1),
                "Should throw exception when offset exceeds data length.");
    }

    @Test
    void testEncodeThrowsOnInvalidValue() {
        ShortVecField shortVecField = new ShortVecField();

        assertThrows(IllegalArgumentException.class, () -> shortVecField.encode(null), "Null value should throw exception.");
    }

    @Test
    void testCalculateSpanThrowsOnInvalidData() {
        ShortVecField shortVecField = new ShortVecField();

        assertThrows(IllegalArgumentException.class, () -> shortVecField.calculateSpan(null, 0),
                "Null data should throw exception.");
        assertThrows(IllegalArgumentException.class, () -> shortVecField.calculateSpan(new byte[]{}, 0),
                "Empty data should throw exception.");
    }

    @Test
    void testDynamicSpanAfterEncoding() {
        ShortVecField shortVecField = new ShortVecField();
        shortVecField.encode(300);

        assertEquals(2, shortVecField.getSpan(), "Dynamic span should update correctly after encoding.");
    }
}