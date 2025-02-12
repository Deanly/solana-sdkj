package net.deanly.solana.sdk.types.codec;

import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class Base58Test {

    @Test
    void testEncodeSmallInput() {
        byte[] input = {1, 2, 3}; // Small input (less than 8 bytes)
        String expected = "Ldp"; // Expected Base58 string

        String encoded = Base58.encode(input);
        assertEquals(expected, encoded, "Encoded Base58 string should match expected value");
    }

    @Test
    void testEncodeLargeInput() {
        byte[] input = new byte[10]; // Larger input (more than 8 bytes)
        Arrays.fill(input, (byte) 1); // Fill with non-zero values

        String encoded = Base58.encode(input);
        assertNotNull(encoded, "Encoded Base58 string should not be null");
        assertFalse(encoded.isEmpty(), "Encoded Base58 string should not be empty");
    }

    @Test
    void testDecodeSmallInput() {
        String input = "Ldp"; // Small Base58 string
        byte[] expected = {1, 2, 3};

        byte[] decoded = Base58.decode(input);
        assertArrayEquals(expected, decoded, "Decoded byte array should match expected value");
    }

    @Test
    void testDecodeLargeInput() {
        byte[] input = new byte[10]; // Larger input (more than 8 bytes)
        Arrays.fill(input, (byte) 1);
        String encoded = Base58.encode(input);

        byte[] decoded = Base58.decode(encoded);
        assertArrayEquals(input, decoded, "Decoded byte array should match the original input");
    }

    @Test
    void testEncodeDecodeWithZeroBytes() {
        byte[] input = {0, 0, 1, 2, 3}; // Input with leading zero bytes
        String encoded = Base58.encode(input);
        byte[] decoded = Base58.decode(encoded);

        assertArrayEquals(input, decoded, "Decoded byte array should match the original input with leading zeros");
    }

    @Test
    void testIsValidBase58Char() {
        String validBase58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        String invalidBase58 = "0IOl"; // Contains invalid Base58 characters

        assertTrue(Base58.isValidBase58Char(validBase58), "Valid Base58 string should return true");
        assertFalse(Base58.isValidBase58Char(invalidBase58), "Invalid Base58 string should return false");
    }

    @Test
    void testDecodeInvalidCharacter() {
        String invalidInput = "Ldp0"; // Contains invalid Base58 character '0'

        assertThrows(IllegalArgumentException.class, () -> Base58.decode(invalidInput), "Decoding invalid Base58 string should throw exception");
    }

    @Test
    void testEncodeEmptyInput() {
        byte[] input = {}; // Empty input
        String encoded = Base58.encode(input);

        assertEquals("", encoded, "Encoded Base58 string for empty input should be empty");
    }

    @Test
    void testDecodeEmptyInput() {
        String input = ""; // Empty Base58 string
        byte[] decoded = Base58.decode(input);

        assertArrayEquals(new byte[]{}, decoded, "Decoded byte array for empty Base58 string should be empty");
    }

    @Test
    void testEncodeDecodeEdgeCase() {
        byte[] input = new BigInteger("18446744073709551615").toByteArray(); // Max value for UnsignedLong
        String encoded = Base58.encode(input);
        byte[] decoded = Base58.decode(encoded);

        StructLayout.debug(input);
        StructLayout.debug(decoded);

        assertArrayEquals(input, decoded, "Decoded byte array should match the original input for edge case");
    }

    @Test
    void testEncodeDecodeLargeRandomInput() {
        byte[] input = new byte[100]; // Large random input
        Arrays.fill(input, (byte) 5); // Fill with arbitrary value
        String encoded = Base58.encode(input);
        byte[] decoded = Base58.decode(encoded);

        assertArrayEquals(input, decoded, "Decoded byte array should match the original input for large random input");
    }

    @Test
    void testEncodeDecode_EmptyInput() {
        byte[] input = new byte[0];
        String encoded = Base58.encode(input);
        byte[] decoded = Base58.decode(encoded);

        assertEquals("", encoded, "Encoding empty input should produce an empty string");
        assertArrayEquals(input, decoded, "Decoding the encoded empty string should produce an empty byte array");
    }

    @Test
    void testEncodeDecode_SingleByte() {
        byte[] input = new byte[]{(byte) 0x01};
        String encoded = Base58.encode(input);
        byte[] decoded = Base58.decode(encoded);

        assertNotNull(encoded, "Encoded result should not be null");
        assertArrayEquals(input, decoded, "Decoded result should match the original input");
    }

    @Test
    void testEncodeDecode_SmallUnsignedLongBoundary() {
        byte[] input = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}; // Max UnsignedLong
        String encoded = Base58.encode(input);
        byte[] decoded = Base58.decode(encoded);

        assertNotNull(encoded, "Encoded result should not be null");
        assertEquals(11, encoded.length(), "Encoded Base58 length should be 11 for 8-byte input");
        assertArrayEquals(input, decoded, "Decoded result should match the original input");
    }

    @Test
    void testEncodeDecode_BeyondUnsignedLongBoundary() {
        byte[] input = new byte[]{(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x01}; // 9 bytes (requires BigInteger)
        String encoded = Base58.encode(input);
        byte[] decoded = Base58.decode(encoded);
        String encoded2 = Base58.encode(decoded);

        assertEquals(encoded2, encoded, "Encoded result should match the original input after decoding");
        assertNotNull(encoded, "Encoded result should not be null");
        assertTrue(encoded.length() > 10, "Encoded Base58 length should be greater than 11 for 9-byte input: " + encoded);
        assertArrayEquals(input, decoded, "Decoded result should match the original input");
    }

    @Test
    void testEncodeDecode_WithLeadingZeros() {
        byte[] input = new byte[]{0x00, 0x00, 0x01, 0x02, 0x03};
        String encoded = Base58.encode(input);
        byte[] decoded = Base58.decode(encoded);

        assertTrue(encoded.startsWith("11"), "Encoded Base58 string should start with '11' for two leading zeros");
        assertArrayEquals(input, decoded, "Decoded result should match the original input with leading zeros preserved");
    }

    @Test
    void testEncodeDecode_LargeInput() {
        byte[] input = new byte[128];
        Arrays.fill(input, (byte) 0x01); // Fill with 0x01 for consistency
        String encoded = Base58.encode(input);
        byte[] decoded = Base58.decode(encoded);

        assertNotNull(encoded, "Encoded result should not be null for large input");
        assertTrue(encoded.length() > 0, "Encoded Base58 length should be non-zero for large input");
        assertArrayEquals(input, decoded, "Decoded result should match the original input for large input");
    }

    @Test
    void testDecode_InvalidCharacter() {
        String invalidBase58 = "Invalid@@@";
        assertThrows(IllegalArgumentException.class, () -> Base58.decode(invalidBase58),
                "Decoding an invalid Base58 string should throw IllegalArgumentException");
    }

    @Test
    void testIsValidBase58Char_ValidInput() {
        assertTrue(Base58.isValidBase58Char("123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"),
                "All valid Base58 characters should be recognized as valid");
    }

    @Test
    void testIsValidBase58Char_InvalidInput() {
        assertFalse(Base58.isValidBase58Char("Invalid@@@"),
                "Strings containing non-Base58 characters should be recognized as invalid");
    }

    @Test
    void testBoundaryConditions() {
        // Smallest non-zero input
        byte[] smallestInput = new byte[]{0x01};
        String encodedSmallest = Base58.encode(smallestInput);
        byte[] decodedSmallest = Base58.decode(encodedSmallest);

        assertArrayEquals(smallestInput, decodedSmallest, "Boundary case for smallest non-zero input should pass");

        // Largest possible UnsignedLong
        byte[] largestInput = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        String encodedLargest = Base58.encode(largestInput);
        byte[] decodedLargest = Base58.decode(encodedLargest);

        assertArrayEquals(largestInput, decodedLargest, "Boundary case for largest UnsignedLong input should pass");
    }
}
