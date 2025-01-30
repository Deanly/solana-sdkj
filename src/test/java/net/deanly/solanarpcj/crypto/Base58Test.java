package net.deanly.solanarpcj.crypto;

import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

public class Base58Test {

    @Test
    public void testEncodeDecodeBasic() {
        // 간단한 바이트 배열 테스트 (30% 범위 내)
        byte[] input = {1, 2, 3};
        String encoded = Base58.encode(input);
        byte[] decoded = Base58.decode(encoded);

        assertArrayEquals(input, decoded, "Decoded bytes must match the original input");
    }

    @Test
    public void testEncodeDecodeLong() {
        // 긴 데이터 테스트
        byte[] input = new byte[128];
        Arrays.fill(input, (byte) 255); // 모든 값을 최대치로 채움
        String encoded = Base58.encode(input);
        byte[] decoded = Base58.decode(encoded);

        assertArrayEquals(input, decoded, "Decoded bytes must match the original input");
    }

    @Test
    public void testLeadingZeroBytes() {
        // 0x00 (Base58의 "1")로 시작하는 데이터
        byte[] input = {0, 0, 0, 1, 2, 3};
        String encoded = Base58.encode(input);
        byte[] decoded = Base58.decode(encoded);

        assertArrayEquals(input, decoded, "Decoded bytes must include original leading zeros");
    }

    @Test
    public void testEmptyInput() {
        // 빈 입력값 처리 테스트
        byte[] input = {};
        String encoded = Base58.encode(input);
        byte[] decoded = Base58.decode(encoded);

        assertEquals("", encoded, "Encoded string for empty input should be empty");
        assertArrayEquals(input, decoded, "Decoded bytes for empty input should be empty");
    }

    @Test
    public void testInvalidBase58Input() {
        // 잘못된 문자가 포함된 Base58 문자열 디코딩 테스트
        String invalidBase58 = "1234OIl0"; // 허용되지 않는 'O', 'I', 'l', '0' 포함

        assertThrows(IllegalArgumentException.class, () -> {
            Base58.decode(invalidBase58);
        }, "Invalid Base58 input should throw an exception");
    }

    @Test
    public void testRandomDataIntegrity() {
        // 랜덤 데이터로 정합성 확인
        byte[] input = new byte[64];
        for (int i = 0; i < 64; i++) {
            input[i] = (byte) (Math.random() * 256);
        }
        String encoded = Base58.encode(input);
        byte[] decoded = Base58.decode(encoded);

        assertArrayEquals(input, decoded, "Random data encode → decode → encode integrity failed");
    }

    @Test
    public void testEdgeCaseSingleByte() {
        // 단일 바이트 값 테스트
        for (int i = 0; i < 256; i++) {
            byte[] input = {(byte) i};
            String encoded = Base58.encode(input);
            byte[] decoded = Base58.decode(encoded);

            assertArrayEquals(input, decoded, "Edge case failed for single-byte value: " + i);
        }
    }

    @Test
    public void testEdgeCaseAllZeros() {
        // 전체가 0인 경우 테스트 (모든 byte가 0)
        byte[] input = new byte[32];
        Arrays.fill(input, (byte) 0);
        String encoded = Base58.encode(input);
        byte[] decoded = Base58.decode(encoded);

        assertArrayEquals(input, decoded, "Decoded bytes must match all-zero input");
    }

    @Test
    public void testEncodeDecodeConsistency() {
        // 다양한 고정된 샘플 데이터 테스트
        byte[][] samples = {
                {0x00}, // Base58은 "1"로 변환
                {0x61}, // 단일 문자 테스트
                {0x62, 0x62}, // 짧은 바이트 배열
                {0x63, 0x63, 0x63},
                {(byte) 0xff, (byte) 0xff, (byte) 0xff}, // 최대값 테스트
                {0x00, 0x01, 0x02, 0x03, 0x04} // Leading zero 포함
        };

        for (byte[] input : samples) {
            String encoded = Base58.encode(input);
            byte[] decoded = Base58.decode(encoded);

            assertArrayEquals(input, decoded, "Consistency failed for sample: " + Arrays.toString(input));
        }
    }

    @Test
    public void testTextEncodeDecode() {
        String text = "Hello World!";
        String encoded = Base58.encode(text.getBytes());
        String decoded = new String(Base58.decode(encoded));

        assertEquals(text, decoded);
    }

    @Test
    public void testDecodedTextMatch01() {
        String encodedText = "JxF12TrwUP45BMd";
        String decoded = new String(Base58.decode(encodedText));

        assertEquals("Hello World", decoded);
    }

    @Test
    public void testDecodedTextMatch02() {
        String encodedText = "11111111111";
        byte[] decoded = Base58.decode(encodedText);

        assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}, decoded);
    }

    @Test
    public void testEncodedTextMAtch01() {
        String text = "ABC";
        String encoded = Base58.encode(text.getBytes());

        assertEquals("NvLz", encoded);
    }


    @Test
    @Disabled
    public void testEncodeDecodeRandomLength() {
        for (int len = 1; len <= 1024; len++) {
            byte[] input = new byte[len];
            for (int i = 0; i < len; i++) {
                input[i] = (byte) (Math.random() * 256);
            }
            String encoded = Base58.encode(input);
            byte[] decoded = Base58.decode(encoded);

            assertArrayEquals(input, decoded, "Random length test failed at length: " + len);
        }
    }

    @Test
    public void testExactOutputLength() {
        // Base58 인코딩 결과 길이 비교
        byte[] input = new byte[32];
        Arrays.fill(input, (byte) 1);

        StructLayout.debug(input);

        String encoded = Base58.encode(input);
        assertEquals(43, encoded.length(), "Encoded Base58 result should have exact length for 32-byte input.");
    }

    @Test
    public void testNullInput() {
        // Null 입력값 테스트
        assertThrows(NullPointerException.class, () -> Base58.encode(null), "Null input for encode should throw NullPointerException");

        assertThrows(NullPointerException.class, () -> Base58.decode(null), "Null input for decode should throw NullPointerException");
    }

    @Test
    @Disabled
    public void testMaxBoundaryInput() {
        // 10만 바이트 테스트 - 메모리 경계
        byte[] maxInput = new byte[100000];
        Arrays.fill(maxInput, (byte) 255);

        String encoded = Base58.encode(maxInput);
        byte[] decoded = Base58.decode(encoded);

        assertArrayEquals(maxInput, decoded, "Max boundary test failed for very large input.");
    }

    @Test
    public void testSpecificPatternOutput() {
        // 특정 입력 데이터와 예상 출력값 일치 테스트
        byte[] input = {0x48, 0x65, 0x6C, 0x6C, 0x6F}; // "Hello"
        String expectedEncoded = "9Ajdvzr";
        String encoded = Base58.encode(input);

        assertEquals(expectedEncoded, encoded, "Base58 encoding failed for 'Hello'. Expected: 9Ajdvzr");
    }



//
//
//    @Test
//    public void testBasicEncodeDecode() {
//        String text = "Hello Base58";
//        String encoded1 = org.bitcoinj.core.Base58.encode(text.getBytes());
//        String encoded2 = Base58.encode(text.getBytes());
//
//        assertEquals(encoded1, encoded2, "Encoded values should match");
//
//        byte[] decoded1 = org.bitcoinj.core.Base58.decode(encoded1);
//        byte[] decoded2 = Base58.decode(encoded2);
//
//        assertArrayEquals(decoded1, decoded2, "Decoded values should match");
//        assertEquals(text, new String(decoded1), "Original text should match decoded text");
//    }
//
//    @Test
//    public void testEdgeCasesLeadingZeros() {
//        byte[] input = {0, 0, 1, 2, 3};
//        String encoded1 = org.bitcoinj.core.Base58.encode(input);
//        String encoded2 = Base58.encode(input);
//
//        assertEquals(encoded1, encoded2, "Encoded values should match");
//
//        byte[] decoded1 = org.bitcoinj.core.Base58.decode(encoded1);
//        byte[] decoded2 = Base58.decode(encoded2);
//
//        assertArrayEquals(decoded1, decoded2, "Decoded values should match");
//        assertArrayEquals(input, decoded1, "Original input should match decoded result");
//    }
//
//    @Test
//    public void testAllZeros() {
//        byte[] input = new byte[10]; // All zeros
//        String encoded1 = org.bitcoinj.core.Base58.encode(input);
//        String encoded2 = Base58.encode(input);
//
//        assertEquals(encoded1, encoded2, "Encoded values should match");
//
//        byte[] decoded1 = org.bitcoinj.core.Base58.decode(encoded1);
//        byte[] decoded2 = Base58.decode(encoded2);
//
//        assertArrayEquals(decoded1, decoded2, "Decoded values should match");
//        assertArrayEquals(input, decoded1, "Original input should match decoded result");
//    }
//
//    @Test
//    public void testEmptyInput2() {
//        byte[] input = new byte[0]; // Empty array
//        String encoded1 = org.bitcoinj.core.Base58.encode(input);
//        String encoded2 = Base58.encode(input);
//
//        assertEquals(encoded1, encoded2, "Encoded values should match");
//        assertTrue(encoded1.isEmpty(), "Encoded Base58 string should be empty for empty input");
//
//        byte[] decoded1 = org.bitcoinj.core.Base58.decode(encoded1);
//        byte[] decoded2 = Base58.decode(encoded2);
//
//        assertArrayEquals(decoded1, decoded2, "Decoded values should match");
//        assertEquals(0, decoded1.length, "Decoded array should be empty for empty input");
//    }
//
//    @Test
//    public void testSingleByteValues() {
//        for (int i = 0; i < 256; i++) {
//            byte[] input = {(byte) i};
//            String encoded1 = org.bitcoinj.core.Base58.encode(input);
//            String encoded2 = Base58.encode(input);
//
//            assertEquals(encoded1, encoded2, "Encoded values should match for byte " + i);
//
//            byte[] decoded1 = org.bitcoinj.core.Base58.decode(encoded1);
//            byte[] decoded2 = Base58.decode(encoded2);
//
//            assertArrayEquals(decoded1, decoded2, "Decoded values should match for byte " + i);
//            assertArrayEquals(input, decoded1, "Original input should match decoded result for byte " + i);
//        }
//    }
//
//    @Test
//    public void testLongInput() {
//        byte[] input = new byte[1024];
//        Arrays.fill(input, (byte) 123); // Large input filled with same value
//        String encoded1 = org.bitcoinj.core.Base58.encode(input);
//        String encoded2 = Base58.encode(input);
//
//        assertEquals(encoded1, encoded2, "Encoded values should match");
//
//        byte[] decoded1 = org.bitcoinj.core.Base58.decode(encoded1);
//        byte[] decoded2 = Base58.decode(encoded2);
//
//        assertArrayEquals(decoded1, decoded2, "Decoded values should match");
//        assertArrayEquals(input, decoded1, "Original input should match decoded result");
//    }
//
//    @Test
//    public void testInvalidInput() {
//        String invalidBase58 = "O0lI"; // Invalid characters
//        assertThrows(IllegalArgumentException.class, () -> org.bitcoinj.core.Base58.decode(invalidBase58),
//                "Decoding invalid Base58 should throw IllegalArgumentException");
//        assertThrows(IllegalArgumentException.class, () -> org.bitcoinj.core.Base58.decode(invalidBase58),
//                "Decoding invalid Base58 using bitcoinj should throw IllegalArgumentException");
//    }
//
//    @Test
//    public void testDifferentEncodings() {
//        String text = "Testing";
//        byte[] utf8Bytes = text.getBytes(); // UTF-8 encoding
//        byte[] asciiBytes = text.getBytes(java.nio.charset.StandardCharsets.US_ASCII); // ASCII encoding
//
//        String encodedUtf8 = org.bitcoinj.core.Base58.encode(utf8Bytes);
//        String encodedAscii = Base58.encode(asciiBytes);
//
//        assertEquals(encodedUtf8, encodedAscii, "UTF-8 and ASCII encodings should produce the same Base58 result");
//
//        byte[] decodedUtf8 = org.bitcoinj.core.Base58.decode(encodedUtf8);
//        byte[] decodedAscii = Base58.decode(encodedAscii);
//
//        assertArrayEquals(decodedUtf8, utf8Bytes, "Decoded UTF-8 result should match original");
//        assertArrayEquals(decodedAscii, asciiBytes, "Decoded ASCII result should match original");
//    }
//
//    @Test
//    public void testWithSpecialCharacters() {
//        String specialChars = "!@#$%^&*()_+";
//        byte[] input = specialChars.getBytes();
//
//        String encoded1 = org.bitcoinj.core.Base58.encode(input);
//        String encoded2 = Base58.encode(input);
//
//        assertEquals(encoded1, encoded2, "Encoded values should match for special characters");
//
//        byte[] decoded1 = org.bitcoinj.core.Base58.decode(encoded1);
//        byte[] decoded2 = Base58.decode(encoded2);
//
//        assertArrayEquals(decoded1, decoded2, "Decoded values should match for special characters");
//        assertEquals(specialChars, new String(decoded1), "Original input should match decoded result for special characters");
//    }
//
//    @Test
//    public void testConsistencyWithBitcoinJ() {
//        for (int i = 0; i < 1000; i++) {
//            byte[] input = new byte[i];
//            Arrays.fill(input, (byte) (i % 256)); // Fill with some predictable data
//            String encoded1 = org.bitcoinj.core.Base58.encode(input);
//            String encoded2 = Base58.encode(input);
//
//            assertEquals(encoded1, encoded2, "Encoded values should match for input of length " + i);
//
//            byte[] decoded1 = org.bitcoinj.core.Base58.decode(encoded1);
//            byte[] decoded2 = Base58.decode(encoded2);
//
//            assertArrayEquals(decoded1, decoded2, "Decoded values should match for input of length " + i);
//            assertArrayEquals(input, decoded1, "Original input should match decoded result for input of length " + i);
//        }
//    }
}