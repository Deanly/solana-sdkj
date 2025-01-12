package org.p2p.solanaj.core.message;

import lombok.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Represents the message header, identifying signed and read-only account keys.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageHeader {
    static final int HEADER_LENGTH = 3;
    private static final int MAX_VALUE = 255;

    /**
     * The number of signatures required for this message to be considered valid. The
     * signatures must match the first `numRequiredSignatures` of `accountKeys`.*
     */
    private int numRequiredSignatures;
    /**
     * The last `numReadonlySignedAccounts` of the signed keys are read-only accounts
     */
    private int numReadonlySignedAccounts;
    /**
     * The last `numReadonlySignedAccounts` of the unsigned keys are read-only accounts
     */
    private int numReadonlyUnsignedAccounts;


    /**
     * Serializes the MessageHeader into a byte array.
     *
     * @return Serialized byte array representing the MessageHeader.
     */
    public byte[] serialize() {
        return new byte[]{
                toByte(numRequiredSignatures),
                toByte(numReadonlySignedAccounts),
                toByte(numReadonlyUnsignedAccounts)
        };
    }

    /**
     * Converts an integer to a byte, ensuring it fits within the valid range.
     *
     * @param value The integer to convert.
     * @return The byte value.
     * @throws IllegalArgumentException If the value exceeds the valid byte range (0-255).
     */
    private byte toByte(int value) {
        if (value < 0 || value > MAX_VALUE) {
            throw new IllegalArgumentException("Value out of range for byte: " + value);
        }
        return (byte) value;
    }

    /**
     * Deserializes a MessageHeader from a ByteBuffer.
     *
     * @param buffer ByteBuffer containing the serialized header.
     * @return Deserialized MessageHeader.
     */
    public static MessageHeader deserialize(ByteBuffer buffer) {
        return new MessageHeader(
                Byte.toUnsignedInt(buffer.get()),
                Byte.toUnsignedInt(buffer.get()),
                Byte.toUnsignedInt(buffer.get())
        );
    }

    /**
     * Deserializes a MessageHeader from a byte array.
     *
     * @param data The byte array containing the serialized header.
     * @return A deserialized MessageHeader object.
     * @throws IllegalArgumentException If the data length is invalid.
     */
    public static MessageHeader deserialize(byte[] data) {
        if (data == null || data.length != HEADER_LENGTH) {
            throw new IllegalArgumentException("Invalid data length for MessageHeader. Expected 3 bytes.");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        return deserialize(buffer);
    }

    /**
     * Gets the size of the serialized MessageHeader.
     *
     * @return The size of the serialized header in bytes.
     */
    public static int getSerializedSize() {
        return HEADER_LENGTH;
    }
}