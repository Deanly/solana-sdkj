package net.deanly.solana.sdk.layout.field;

import net.deanly.structlayout.type.DynamicSpanField;
import net.deanly.structlayout.type.FieldBase;

import java.nio.charset.StandardCharsets;

public class RustStringField extends FieldBase<String> implements DynamicSpanField {

    public RustStringField() {
        super(-1, String.class);
    }

    @Override
    public String decode(byte[] data, int offset) {
        // 문자열 길이 읽기 (Little-Endian u32)
        int length = ((data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8) |
                ((data[offset + 2] & 0xFF) << 16) | ((data[offset + 3] & 0xFF) << 24));
        offset += 4;

        // 문자열 데이터 읽기
        return new String(data, offset, length, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] encode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("String value cannot be null");
        }

        byte[] stringBytes = value.getBytes(StandardCharsets.UTF_8);
        int length = stringBytes.length;

        byte[] result = new byte[4 + length];

        // 문자열 길이 기록 (Little-Endian u32)
        result[0] = (byte) (length & 0xFF);
        result[1] = (byte) ((length >> 8) & 0xFF);
        result[2] = (byte) ((length >> 16) & 0xFF);
        result[3] = (byte) ((length >> 24) & 0xFF);

        // 문자열 데이터 기록
        System.arraycopy(stringBytes, 0, result, 4, length);

        return result;
    }

    @Override
    public int getSpan() {
        // Dynamic span: -1을 반환하면 안되므로 실제 길이를 계산해야 함
        throw new UnsupportedOperationException("DynamicStringField requires data to calculate span.");
    }

    @Override
    public int calculateSpan(byte[] data, int offset) {
        // 문자열 길이 읽기 (Little-Endian u32)
        int length = ((data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8) |
                ((data[offset + 2] & 0xFF) << 16) | ((data[offset + 3] & 0xFF) << 24));
        return 4 + length; // 길이 필드(4 bytes) + 문자열 데이터 길이
    }
}
