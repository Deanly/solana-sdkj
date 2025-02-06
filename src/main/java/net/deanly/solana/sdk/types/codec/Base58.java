package net.deanly.solana.sdk.types.codec;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Base58 인코딩/디코딩 유틸리티
 */
public class Base58 {

    private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final int BASE = ALPHABET.length;
    private static final BigInteger BASE_BIG = BigInteger.valueOf(BASE);
    private static final boolean[] BASE58_CHARSET = new boolean[128];

    static {
        String alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        for (char c : alphabet.toCharArray()) {
            BASE58_CHARSET[c] = true;
        }
    }

    /**
     * Base58 인코딩
     * @param input 바이트 배열 (예: 공개키, 해시값 등)
     * @return Base58 문자열
     */
    public static String encode(byte[] input) {
        BigInteger num = new BigInteger(1, input);
        StringBuilder sb = new StringBuilder();

        while (num.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divMod = num.divideAndRemainder(BASE_BIG);
            num = divMod[0];
            sb.insert(0, ALPHABET[divMod[1].intValue()]);
        }

        // 0바이트는 Base58에서 '1'로 변환됨
        for (byte b : input) {
            if (b == 0) {
                sb.insert(0, ALPHABET[0]);
            } else {
                break;
            }
        }
        return sb.toString();
    }

    /**
     * Base58 디코딩
     * @param input Base58 문자열
     * @return 디코딩된 바이트 배열
     */
    public static byte[] decode(String input) {
        BigInteger num = BigInteger.ZERO;

        for (char c : input.toCharArray()) {
            int index = new String(ALPHABET).indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("Invalid Base58 character: " + c);
            }
            num = num.multiply(BASE_BIG).add(BigInteger.valueOf(index));
        }

        byte[] bytes = num.toByteArray();
        if (bytes[0] == 0) {
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        }

        // Base58의 '1'은 0바이트를 의미하므로, 앞의 0 처리
        int zeroCount = 0;
        for (char c : input.toCharArray()) {
            if (c == ALPHABET[0]) {
                zeroCount++;
            } else {
                break;
            }
        }

        byte[] decoded = new byte[zeroCount + bytes.length];
        System.arraycopy(bytes, 0, decoded, zeroCount, bytes.length);
        return decoded;
    }

    /**
     * Base58 문자 유효성 검사
     * @param input 검사 대상 문자열
     * @return 검사 결과
     */
    public static boolean isValidBase58Char(String input) {
        int len = input.length();

        for (int i = 0; i < len; i++) {
            char c = input.charAt(i);
            if (c >= 128 || !BASE58_CHARSET[c]) return false; // O(1) Base58 문자 검증
        }
        return true;
    }
}