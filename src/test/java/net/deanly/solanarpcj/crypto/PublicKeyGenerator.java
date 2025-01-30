package net.deanly.solanarpcj.crypto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class PublicKeyGenerator {

    private static final int PUBLIC_KEY_LENGTH = 32; // 32 bytes 기준

    /**
     * Generates a dummy PublicKey string based on the given input.
     *
     * @param input The input string that will be converted to Base58-like 32-byte format.
     * @return A Base58-compatible 32-byte string.
     */
    public static String createDummyPublicKey(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input string cannot be null or empty.");
        }
        // Normalize input string to replace invalid Base58 characters
        input = normalizeBase58Input(input);
        // Adjust the input string so that its length matches 32 bytes
        return adjustInputTo32Bytes(input);
    }

    /**
     * Replaces characters that are not valid in Base58 with their normalized values.
     * - Converts:
     *   - I/l -> 1
     *   - 0/O -> o
     *
     * @param input The input string to normalize.
     * @return The normalized string.
     */
    private static String normalizeBase58Input(String input) {
        // Replace invalid characters with valid Base58 equivalents
        return input.replace('I', '1')
                .replace('l', '1')
                .replace('0', 'o')
                .replace('O', 'o');
    }

    /**
     * Adjusts the input string to reliably produce a 32-byte output when Base58-decoded.
     *
     * @param input The input string.
     * @return The adjusted string that fits exactly into 32 bytes when decoded.
     */
    private static String adjustInputTo32Bytes(String input) {
        while (true) {
            // Decode the input into bytes
            byte[] decodedBytes = Base58.decode(input);
            System.out.println("Input: " + decodedBytes.length + " bytes - " + input);

            if (decodedBytes.length == PUBLIC_KEY_LENGTH) {
                // If the byte array is exactly 32 bytes, stop adjusting
                return input;
            } else if (decodedBytes.length > PUBLIC_KEY_LENGTH) {
                // If the byte array exceeds 32 bytes, truncate the input
                input = input.substring(0, input.length() - 1);
            } else {
                // If the byte array is less than 32 bytes, append '1' to the input
                input += "1";
            }
        }
    }

    /**
     * Decodes a Base58 string into a byte array.
     *
     * @param input The Base58 string to decode.
     * @return The byte array.
     */
    private static byte[] decodeBase58(String input) {
        // Base58 decoding logic (mimicking the process)
        final String BASE58_ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        byte[] result = new byte[0];
        for (char c : input.toCharArray()) {
            int index = BASE58_ALPHABET.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("Invalid Base58 character: " + c);
            }
            result = appendByte(result, (byte) index);
        }
        return result;
    }

    /**
     * Encodes a byte array into a Base58 string.
     *
     * @param input The byte array to encode.
     * @return A Base58 encoded string.
     */
    private static String encodeBase58(byte[] input) {
        final String BASE58_ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        StringBuilder encoded = new StringBuilder();

        // Initial integer representation of the byte array
        java.math.BigInteger value = new java.math.BigInteger(1, input);

        while (value.compareTo(java.math.BigInteger.ZERO) > 0) {
            java.math.BigInteger[] divMod = value.divideAndRemainder(java.math.BigInteger.valueOf(58));
            encoded.insert(0, BASE58_ALPHABET.charAt(divMod[1].intValue()));
            value = divMod[0];
        }

        // Handle leading zero bytes
        for (byte b : input) {
            if (b == 0) {
                encoded.insert(0, BASE58_ALPHABET.charAt(0));
            } else {
                break;
            }
        }

        return encoded.toString();
    }

    /**
     * Appends a single byte to a byte array.
     *
     * @param array The original byte array.
     * @param value The byte to append.
     * @return The new byte array with the appended byte.
     */
    private static byte[] appendByte(byte[] array, byte value) {
        byte[] newArray = Arrays.copyOf(array, array.length + 1);
        newArray[array.length] = value;
        return newArray;
    }

    @Test
    public void test() {
        // Example usages
        String dummyKey1 = createDummyPublicKey("HelloWor1d");
        System.out.println("Dummy PublicKey 1: " + dummyKey1 + ", bytes: " + Base58.decode(dummyKey1).length);

        String dummyKey2 = createDummyPublicKey("ThisIsAReallyLongStringThatShouldBeTruncatedOrPaddedToFit32Bytes");
        System.out.println("Dummy PublicKey 2: " + dummyKey2 + ", bytes: " + Base58.decode(dummyKey2).length);

        String dummyKey3 = createDummyPublicKey("123");
        System.out.println("Dummy PublicKey 3: " + dummyKey3 + ", bytes: " + Base58.decode(dummyKey3).length);
    }
}