package net.deanly.solanarpcj.layout.field;

import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.DynamicSpanField;
import net.deanly.structlayout.type.CountableField;

public class ShortVecField extends FieldBase<Integer> implements DynamicSpanField, CountableField<Integer> {

    private int dynamicSpan;

    public ShortVecField() {
        // 기본 생성자: 초기 span은 0으로 설정
        super(0, Integer.class);
    }

    @Override
    public byte[] encode(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null for VLE encoding.");
        }
        byte[] encoded = this.encodeLength(value);
        this.dynamicSpan = encoded.length;
        return encoded;
    }

    @Override
    public Integer decode(byte[] bytes, int offset) {
        if (bytes == null) {
            throw new IllegalArgumentException("Data cannot be null for VLE decoding.");
        }
        if (offset < 0 || offset >= bytes.length) {
            throw new IllegalArgumentException("Invalid offset for VLE decoding.");
        }

        int length = this.decodeLength(bytes, offset);

        int byteCount = calculateEncodedLength(bytes, offset);
        this.dynamicSpan = byteCount;

        return length;
    }

    /**
     * Byte 배열 내에서 short vector의 인코딩된 길이를 계산합니다.
     *
     * @param bytes  데이터 배열
     * @param offset 시작 오프셋
     * @return short vector 길이를 계산한 결과 바이트 길이
     */
    private int calculateEncodedLength(byte[] bytes, int offset) {
        int span = 0;
        while (offset + span < bytes.length) {
            if ((bytes[offset + span] & 0x80) == 0) {
                // 마지막 바이트를 만났다면 종료
                span++;
                break;
            }
            span++;
        }
        return span;
    }

    @Override
    public int calculateSpan(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null for span calculation.");
        }
        if (offset < 0 || offset >= data.length) {
            throw new IllegalArgumentException("Invalid offset for span calculation.");
        }

        // 데이터를 기반으로 동적으로 span 계산
        return calculateEncodedLength(data, offset);
    }

    @Override
    public int getSpan() {
        return this.dynamicSpan;
    }

    public void setSpan(int span) {
        this.dynamicSpan = span; // 값을 외부에서 설정할 수도 있음
    }

    private byte[] encodeLength(int length) {
        byte[] buffer = new byte[5];
        int i = 0;
        while (length > 127) {
            buffer[i++] = (byte) ((length & 0x7F) | 0x80);
            length >>>= 7;
        }
        buffer[i++] = (byte) (length & 0x7F);
        byte[] result = new byte[i];
        System.arraycopy(buffer, 0, result, 0, i);
        return result;
    }

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

}
