package net.deanly.solana.sdk.layout.field;

import net.deanly.structlayout.type.DynamicSpanField;
import net.deanly.structlayout.type.FieldBase;

import java.nio.charset.StandardCharsets;

/**
 * <pre>
 * ⚠ WARNING:
 * This field does **NOT** include length information. Avoid using it mixed with other fields
 * in a structure, as there is no structural offset information to reliably separate fields.
 * It is recommended to use this field in isolation or as the last field in a structure.
 * </pre>
 *
 * UTF8StringField is a dynamic field implementation that handles UTF-8 encoded strings.
 * This class extends {@link FieldBase} with the type parameter {@link String} and
 * implements the {@link DynamicSpanField} interface.
 *
 * <p>UTF8StringField is used to decode, encode, and calculate spans for data represented as UTF-8
 * strings. It is designed for scenarios where the length of the UTF-8 string is dynamic and
 * determined at runtime based on the available data.</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Decodes raw byte arrays into UTF-8 strings.</li>
 *   <li>Encodes UTF-8 strings into byte arrays.</li>
 *   <li>Calculates the span of data dynamically based on the byte array's remaining length.</li>
 * </ul>
 *
 * <p><strong>Considerations:</strong></p>
 * <ul>
 *   <li>The {@code getSpan()} method is unsupported and throws {@link UnsupportedOperationException}
 *       because a dynamic UTF-8 field requires actual data to determine the span.</li>
 *   <li>Encoding null values is not permitted and will throw an {@link IllegalArgumentException}.</li>
 * </ul>
 */
public class UTF8StringField extends FieldBase<String> implements DynamicSpanField {

    public UTF8StringField() {
        super(-1); // Dynamic field
    }

    @Override
    public String decode(byte[] data, int offset) {
        // 남은 데이터 전부를 UTF-8 문자열로 디코딩
        return new String(data, offset, data.length - offset, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] encode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("String value cannot be null");
        }

        // 문자열을 UTF-8 바이트로 인코딩
        return value.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public int getSpan() {
        // UTF-8 동적 길이: 데이터가 있어야만 계산 가능 (지원되지 않음)
        throw new UnsupportedOperationException("UTF8StringField requires data to calculate span.");
    }

    @Override
    public int calculateSpan(byte[] data, int offset) {
        // 모든 남은 데이터 길이를 반환 (offset 이후부터 끝까지)
        return data.length - offset;
    }

    @Override
    public int getNoDataSpan() {
        return 0;
    }
}