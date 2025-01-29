package net.deanly.solanarpcj.transaction.message.meta;

import net.deanly.solanarpcj.transaction.message.meta.MessageHeader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MessageHeaderTest {
    @Test
    public void constructor_validInputs_shouldInitializeFieldsCorrectly() {
        int numRequiredSignatures = 2;
        int numReadonlySignedAccounts = 1;
        int numReadonlyUnsignedAccounts = 3;

        MessageHeader header = new MessageHeader(
                numRequiredSignatures,
                numReadonlySignedAccounts,
                numReadonlyUnsignedAccounts
        );

        assertEquals(numRequiredSignatures, header.getNumRequiredSignatures(), "numRequiredSignatures does not match!");
        assertEquals(numReadonlySignedAccounts, header.getNumReadonlySignedAccounts(), "numReadonlySignedAccounts does not match!");
        assertEquals(numReadonlyUnsignedAccounts, header.getNumReadonlyUnsignedAccounts(), "numReadonlyUnsignedAccounts does not match!");
    }

    @Test
    public void serializeAndDeserialize_shouldReturnEquivalentObject() {
        int numRequiredSignatures = 2;
        int numReadonlySignedAccounts = 1;
        int numReadonlyUnsignedAccounts = 3;

        MessageHeader originalHeader = new MessageHeader(
                numRequiredSignatures,
                numReadonlySignedAccounts,
                numReadonlyUnsignedAccounts
        );

        // Serialize the header
        byte[] serializedData = originalHeader.serialize();

        // Deserialize back to an object
        MessageHeader deserializedHeader = MessageHeader.deserialize(serializedData);

        // Compare original and deserialized headers
        assertEquals(originalHeader.getNumRequiredSignatures(), deserializedHeader.getNumRequiredSignatures(), "numRequiredSignatures mismatch!");
        assertEquals(originalHeader.getNumReadonlySignedAccounts(), deserializedHeader.getNumReadonlySignedAccounts(), "numReadonlySignedAccounts mismatch!");
        assertEquals(originalHeader.getNumReadonlyUnsignedAccounts(), deserializedHeader.getNumReadonlyUnsignedAccounts(), "numReadonlyUnsignedAccounts mismatch!");
    }

    @Test
    public void deserialize_invalidLength_shouldThrowException() {
        byte[] invalidData = new byte[] {0x01, 0x02}; // Only 2 bytes, expected 3

        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> MessageHeader.deserialize(invalidData)
        );

        assertEquals("Invalid data length for MessageHeader. Expected 3 bytes.", exception.getMessage());
    }

    @Test
    public void constructor_valueOutOfRange_shouldThrowException() {
        int invalidValue = 256; // Out of valid byte range (0-255)

        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> new MessageHeader(invalidValue, 1, 1)
        );

        assertEquals("numRequiredSignatures should be in the range 0-255. Provided: 256", exception.getMessage());
    }

    @Test
    public void serializeAndDeserialize_minValues_shouldWorkCorrectly() {
        int numRequiredSignatures = 0;
        int numReadonlySignedAccounts = 0;
        int numReadonlyUnsignedAccounts = 0;

        MessageHeader header = new MessageHeader(
                numRequiredSignatures,
                numReadonlySignedAccounts,
                numReadonlyUnsignedAccounts
        );

        byte[] serializedData = header.serialize();
        MessageHeader deserializedHeader = MessageHeader.deserialize(serializedData);

        assertEquals(numRequiredSignatures, deserializedHeader.getNumRequiredSignatures(), "numRequiredSignatures mismatch!");
        assertEquals(numReadonlySignedAccounts, deserializedHeader.getNumReadonlySignedAccounts(), "numReadonlySignedAccounts mismatch!");
        assertEquals(numReadonlyUnsignedAccounts, deserializedHeader.getNumReadonlyUnsignedAccounts(), "numReadonlyUnsignedAccounts mismatch!");
    }

    @Test
    public void serializeAndDeserialize_maxValues_shouldWorkCorrectly() {
        int numRequiredSignatures = 255;
        int numReadonlySignedAccounts = 255;
        int numReadonlyUnsignedAccounts = 255;

        MessageHeader header = new MessageHeader(
                numRequiredSignatures,
                numReadonlySignedAccounts,
                numReadonlyUnsignedAccounts
        );

        byte[] serializedData = header.serialize();
        MessageHeader deserializedHeader = MessageHeader.deserialize(serializedData);

        assertEquals(numRequiredSignatures, deserializedHeader.getNumRequiredSignatures(), "numRequiredSignatures mismatch!");
        assertEquals(numReadonlySignedAccounts, deserializedHeader.getNumReadonlySignedAccounts(), "numReadonlySignedAccounts mismatch!");
        assertEquals(numReadonlyUnsignedAccounts, deserializedHeader.getNumReadonlyUnsignedAccounts(), "numReadonlyUnsignedAccounts mismatch!");
    }

    @Test
    public void getSerializedSize_shouldReturnCorrectSize() {
        assertEquals(3, MessageHeader.getSerializedSize(), "MessageHeader serialized size does not match!");
    }
}
