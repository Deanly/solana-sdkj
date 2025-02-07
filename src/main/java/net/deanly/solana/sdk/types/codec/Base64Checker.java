package net.deanly.solana.sdk.types.codec;

import java.util.HashSet;
import java.util.Set;

public class Base64Checker {

    // Base64 허용 문자 목록
    private static final Set<Character> BASE64_CHARS = new HashSet<>();

    static {
        for (char c = 'A'; c <= 'Z'; c++) BASE64_CHARS.add(c);
        for (char c = 'a'; c <= 'z'; c++) BASE64_CHARS.add(c);
        for (char c = '0'; c <= '9'; c++) BASE64_CHARS.add(c);
        BASE64_CHARS.add('+');
        BASE64_CHARS.add('/');
        BASE64_CHARS.add('='); // 패딩 문자
    }

    /**
     * Base64 문자열 여부를 빠르게 확인하는 메서드
     */
    public static boolean isValidBase64(String str) {
        if (str == null || str.isEmpty() || str.length() % 4 != 0) {
            return false; // Base64는 항상 4의 배수 길이
        }

        int len = str.length();
        int paddingCount = 0;

        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);

            if (!BASE64_CHARS.contains(c)) {
                return false; // 허용되지 않은 문자 발견
            }

            // 마지막 두 글자가 '='일 경우 패딩 개수 체크
            if (c == '=') {
                paddingCount++;
                if (paddingCount > 2) {
                    return false; // 패딩 문자가 3개 이상이면 잘못된 Base64
                }
            } else if (paddingCount > 0) {
                return false; // '=' 뒤에 다른 문자가 오면 잘못된 Base64
            }
        }
        return true;
    }
}
