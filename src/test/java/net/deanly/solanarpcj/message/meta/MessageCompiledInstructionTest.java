package net.deanly.solanarpcj.message.meta;

import org.junit.jupiter.api.Test;
import net.deanly.solanarpcj.utils.ShortvecEncoding;

import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MessageCompiledInstructionTest {

    @Test
    public void constructor_validInputs_shouldInitializeFieldsCorrectly() {
        int programIdIndex = 1;
        List<Integer> accountKeyIndexes = List.of(2, 3);
        byte[] data = new byte[] {0x01, 0x02, 0x03};

        MessageCompiledInstruction instruction = new MessageCompiledInstruction(programIdIndex, accountKeyIndexes, data);

        assertEquals(programIdIndex, instruction.getProgramIdIndex(), "ProgramIdIndex does not match!");
        assertEquals(accountKeyIndexes, instruction.getAccountKeyIndexes(), "AccountKeyIndexes do not match!");
        assertArrayEquals(data, instruction.getData(), "Data does not match!");
    }

    @Test
    public void serializeAndDeserialize_shouldReturnEquivalentObject() {
        int programIdIndex = 2;
        List<Integer> accountKeyIndexes = List.of(3, 4, 5);
        byte[] data = new byte[] {0x0A, 0x0B};

        MessageCompiledInstruction originalInstruction = new MessageCompiledInstruction(programIdIndex, accountKeyIndexes, data);

        // Serialize the instruction
        byte[] serializedData = originalInstruction.serialize();

        // Deserialize back to an object
        MessageCompiledInstruction deserializedInstruction = MessageCompiledInstruction.deserialize(serializedData);

        // Compare original and deserialized instruction
        assertEquals(originalInstruction.getProgramIdIndex(), deserializedInstruction.getProgramIdIndex(), "ProgramIDIndex mismatch!");
        assertEquals(originalInstruction.getAccountKeyIndexes(), deserializedInstruction.getAccountKeyIndexes(), "AccountKeyIndexes mismatch!");
        assertArrayEquals(originalInstruction.getData(), deserializedInstruction.getData(), "Data mismatch!");
    }

    @Test
    public void getSerializedLength_shouldReturnCorrectLength() {
        int programIdIndex = 0;
        List<Integer> accountKeyIndexes = List.of(1, 2, 3, 4);
        byte[] data = new byte[] {0x01, 0x02, 0x03};

        MessageCompiledInstruction instruction = new MessageCompiledInstruction(programIdIndex, accountKeyIndexes, data);

        // Expected length calculation:
        int expectedLength = 1 + // programIdIndex
                ShortvecEncoding.encodeLength(accountKeyIndexes.size()).length + // accountKeyIndexes length
                accountKeyIndexes.size() + // accountKeyIndexes
                ShortvecEncoding.encodeLength(data.length).length + // data length
                data.length; // data bytes

        assertEquals(expectedLength, instruction.getSerializedLength(), "Serialized length does not match!");
    }

    @Test
    public void serializeAndDeserialize_emptyData_shouldWorkCorrectly() {
        int programIdIndex = 0;
        List<Integer> accountKeyIndexes = List.of();
        byte[] data = new byte[] {}; // Empty data

        MessageCompiledInstruction instruction = new MessageCompiledInstruction(programIdIndex, accountKeyIndexes, data);

        byte[] serialized = instruction.serialize();
        MessageCompiledInstruction deserialized = MessageCompiledInstruction.deserialize(serialized);

        assertEquals(instruction.getProgramIdIndex(), deserialized.getProgramIdIndex(), "ProgramIDIndex mismatch!");
        assertEquals(instruction.getAccountKeyIndexes(), deserialized.getAccountKeyIndexes(), "AccountKeyIndexes mismatch!");
        assertArrayEquals(instruction.getData(), deserialized.getData(), "Data mismatch!");
    }

    @Test
    public void serializeAndDeserialize_maxData_shouldWorkCorrectly() {
        int programIdIndex = 255;
        List<Integer> accountKeyIndexes = new ArrayList<>();
        for (int i = 0; i < 255; i++) {
            accountKeyIndexes.add(i);
        } // 255 writable indexes

        byte[] data = new byte[255];
        for (int i = 0; i < 255; i++) {
            data[i] = (byte) i;
        } // 255 bytes of data

        MessageCompiledInstruction instruction = new MessageCompiledInstruction(programIdIndex, accountKeyIndexes, data);

        byte[] serialized = instruction.serialize();
        MessageCompiledInstruction deserialized = MessageCompiledInstruction.deserialize(serialized);

        assertEquals(instruction.getProgramIdIndex(), deserialized.getProgramIdIndex(), "ProgramIDIndex mismatch!");
        assertEquals(instruction.getAccountKeyIndexes(), deserialized.getAccountKeyIndexes(), "AccountKeyIndexes mismatch!");
        assertArrayEquals(instruction.getData(), deserialized.getData(), "Data mismatch!");
    }

    @Test
    public void deserialize_invalidData_shouldThrowException() {
        byte[] invalidData = new byte[] {0x01, 0x02}; // Insufficient data

        Exception exception = assertThrows(
                BufferUnderflowException.class,
                () -> MessageCompiledInstruction.deserialize(invalidData)
        );

        assertNotNull(exception, "Expected an exception to be thrown!");
    }
}
