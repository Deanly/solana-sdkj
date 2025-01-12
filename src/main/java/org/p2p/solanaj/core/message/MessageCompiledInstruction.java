package org.p2p.solanaj.core.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.utils.ShortvecEncoding;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class MessageCompiledInstruction {
    /**
     * Index into the transaction keys array indicating the program account that executes this instruction
     */
    private final int programIdIndex;

    /**
     * Ordered indices into the transaction keys array indicating which accounts to pass to the program
     */
    private final List<Integer> accountKeyIndexes;

    /**
     * The program input data
     */
    private final byte[] data;


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