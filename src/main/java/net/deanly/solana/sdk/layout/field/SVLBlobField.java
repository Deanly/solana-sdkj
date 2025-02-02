package net.deanly.solana.sdk.layout.field;

import net.deanly.structlayout.type.DynamicSpanField;
import net.deanly.structlayout.type.FieldBase;

/**
 * SVLBlobField is a field implementation designed to handle binary data with a length
 * prefix encoded using a Short Vector Length (SVL) scheme. The class supports
 * encoding and decoding of binary (blob) data where the length of the data is prepended
 * as a VLE-encoded integer.
 *
 * The field provides methods to determine the span of the encoded blob, encode a blob,
 * and decode a blob from an encoded byte array. It also implements mechanisms to
 * dynamically calculate the span of the field, as required by the DynamicSpanField
 * interface.
 */
public class SVLBlobField extends FieldBase<byte[]> implements DynamicSpanField {

    public SVLBlobField() {
        super(0, byte[].class);
    }

    @Override
    public int calculateSpan(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null during span calculation.");
        }
        if (offset < 0 || offset >= data.length) {
            throw new IllegalArgumentException("Offset is out of bounds for span calculation.");
        }

        // Calculate the length prefix span using VLEField logic
        int lengthPrefixSpan = calculateEncodedLength(data, offset);

        // Decode the actual blob length using the VLE logic
        int blobLength = decodeLength(data, offset);

        return lengthPrefixSpan + blobLength;
    }

    @Override
    public int getNoDataSpan() {
        return encodeLength(0).length;
    }

    public void setSpan(int span) {
        // Since span is dynamic and calculated, there's no distinct field to set.
        // This method can be left empty unless specific behavior needs to be applied.
    }

    @Override
    public byte[] encode(byte[] value) {
        if (value == null) {
            value = new byte[0];
        }

        // Encode the length of the value array using VLEField logic
        int length = value.length;
        byte[] lengthEncoded = encodeLength(length);

        // Combine length encoding and value into one array
        byte[] result = new byte[lengthEncoded.length + value.length];
        System.arraycopy(lengthEncoded, 0, result, 0, lengthEncoded.length); // Write length
        System.arraycopy(value, 0, result, lengthEncoded.length, value.length); // Write value

        return result;
    }

    @Override
    public byte[] decode(byte[] bytes, int offset) {
        if (bytes == null) {
            throw new IllegalArgumentException("Bytes cannot be null during decoding.");
        }
        if (offset < 0 || offset >= bytes.length) {
            throw new IllegalArgumentException("Offset is out of bounds for decoding.");
        }

        // Decode the length prefix using VLEField logic
        int length = decodeLength(bytes, offset);

        // Calculate the VLE length span
        int lengthEncodedSpan = calculateEncodedLength(bytes, offset);

        // Validate whether enough data exists for the blob
        if (bytes.length < offset + lengthEncodedSpan + length) {
            throw new IllegalArgumentException("Blob data size does not match the length prefix.");
        }

        // Extract the blob data
        byte[] blob = new byte[length];
        System.arraycopy(bytes, offset + lengthEncodedSpan, blob, 0, length);

        return blob;
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

    private int calculateEncodedLength(byte[] bytes, int offset) {
        int span = 0;
        while (offset + span < bytes.length) {
            if ((bytes[offset + span] & 0x80) == 0) {
                span++;
                break;
            }
            span++;
        }
        return span;
    }
}