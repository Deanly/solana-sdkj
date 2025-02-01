package net.deanly.solana.sdk.transaction.message.meta;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solana.sdk.crypto.Base58;
import net.deanly.solana.sdk.layout.field.SVLBlobField;
import net.deanly.solana.sdk.layout.field.ShortVecField;
import net.deanly.solana.sdk.transaction.codec.ShortvecEncoding;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MessageCompiledInstruction {
    /**
     * Index into the transaction keys array indicating the program account that executes this instruction
     */
    @StructField(order = 1, type = UInt8Field.class)
    private int programIdIndex;

    /**
     * Ordered indices into the transaction keys array indicating which accounts to pass to the program
     */
    @StructSequenceField(order = 2, elementType = UInt8Field.class, lengthType = ShortVecField.class)
    private List<Integer> accountKeyIndexes;

    /**
     * The program input data
     */
    @StructField(order = 3, type = SVLBlobField.class)
    private byte[] data;


    /**
     * Serialize this instruction into a byte array.
     *
     * @return Serialized byte array.
     */
    public byte[] serialize() {
        byte[] accountKeyIndexesLength = ShortvecEncoding.encodeLength(accountKeyIndexes.size());
        byte[] dataLength = ShortvecEncoding.encodeLength(data.length);

        ByteBuffer buffer = ByteBuffer.allocate(
                1 + // programIdIndex
                        accountKeyIndexesLength.length +
                        accountKeyIndexes.size() +
                        dataLength.length +
                        data.length
        );
        buffer.put((byte) programIdIndex);
        buffer.put(accountKeyIndexesLength);

        for (Integer index : accountKeyIndexes) {
            buffer.put(index.byteValue());
        }

        buffer.put(dataLength);
        buffer.put(data);

        return buffer.array();
    }

    /**
     * Deserialize a MessageCompiledInstruction from a ByteBuffer.
     *
     * @param buffer ByteBuffer containing serialized data.
     * @return Deserialized MessageCompiledInstruction.
     */
    public static MessageCompiledInstruction deserialize(ByteBuffer buffer) {
        // Decode the programIdIndex
        int programIdIndex = Byte.toUnsignedInt(buffer.get());

        // Decode accountKeyIndexes length using Shortvec
        int accountKeyIndexesLength = ShortvecEncoding.decodeLength(buffer);
        List<Integer> accountKeyIndexes = new ArrayList<>();
        for (int i = 0; i < accountKeyIndexesLength; i++) {
            accountKeyIndexes.add(Byte.toUnsignedInt(buffer.get()));
        }

        // Decode instruction data length using Shortvec
        int instructionDataLength = ShortvecEncoding.decodeLength(buffer);
        byte[] instructionData = new byte[instructionDataLength];
        buffer.get(instructionData);

        return new MessageCompiledInstruction(programIdIndex, accountKeyIndexes, instructionData);
    }

    /**
     * Deserialize a MessageCompiledInstruction from a byte array.
     *
     * @param data Byte array containing serialized data.
     * @return Deserialized MessageCompiledInstruction.
     */
    public static MessageCompiledInstruction deserialize(byte[] data) {
        return deserialize(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN));
    }

    /**
     * Calculates the serialized length of this instruction without allocating memory.
     *
     * @return The total length in bytes required for serialization.
     */
    public int getSerializedLength() {
        // 1 byte for programIdIndex
        int length = 1;

        // Add the length of the encoded accountKeyIndexes length (ShortvecEncoding)
        length += ShortvecEncoding.encodeLength(accountKeyIndexes.size()).length;

        // Add the size of all account key indices
        length += accountKeyIndexes.size();

        // Add the length of the encoded data length (ShortvecEncoding)
        length += ShortvecEncoding.encodeLength(data.length).length;

        // Add the size of the data itself
        length += data.length;

        return length;
    }

    @Override
    public String toString() {
        return "MessageCompiledInstruction{" +
                "programIdIndex=" + programIdIndex +
                ", accountKeyIndexes=" + accountKeyIndexes +
                ", data=" + Base58.encode(data) +
                '}';
    }
}