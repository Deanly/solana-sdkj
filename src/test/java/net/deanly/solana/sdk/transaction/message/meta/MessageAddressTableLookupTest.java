package net.deanly.solana.sdk.transaction.message.meta;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import net.deanly.solana.sdk.crypto.PublicKey;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class MessageAddressTableLookupTest {

    @Test
    public void testDeserializeByteBuffer() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{
                // PublicKey: 32 bytes
                0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
                0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18,
                0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20,
                // Writable indexes length: 2
                0x02,
                // Writable indexes: 0x01, 0x02
                0x01, 0x02,
                // Readonly indexes length: 1
                0x01,
                // Readonly index: 0x03
                0x03
        });

        MessageAddressTableLookup lookup = MessageAddressTableLookup.deserialize(buffer);

        // PublicKey expected value
        PublicKey expectedAccountKey = new PublicKey(new byte[]{
                0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
                0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18,
                0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20
        });

        // Writable indexes and readonly indexes expected values
        List<Integer> expectedWritableIndexes = List.of(1, 2);
        List<Integer> expectedReadonlyIndexes = List.of(3);

        // Assertions
        assertEquals(expectedAccountKey, lookup.getAccountKey(), "AccountKey does not match!");
        assertEquals(expectedWritableIndexes, lookup.getWritableIndexes(), "WritableIndexes do not match!");
        assertEquals(expectedReadonlyIndexes, lookup.getReadonlyIndexes(), "ReadonlyIndexes do not match!");
    }

    @Test
    public void constructor_validInputs_shouldInitializeFields() {
        PublicKey accountKey = new PublicKey("11111111111111111111111111111111");
        List<Integer> writableIndexes = List.of(10, 20);
        List<Integer> readonlyIndexes = List.of(30, 40);

        MessageAddressTableLookup lookup = new MessageAddressTableLookup(accountKey, writableIndexes, readonlyIndexes);

        assertEquals(accountKey, lookup.getAccountKey());
        assertEquals(writableIndexes, lookup.getWritableIndexes());
        assertEquals(readonlyIndexes, lookup.getReadonlyIndexes());
    }

    @Test
    public void constructor_nullAccountKey_shouldThrowException() {
        Exception exception = assertThrows(
                NullPointerException.class,
                () -> new MessageAddressTableLookup(null, List.of(10, 20), List.of(30, 40))
        );

        assertEquals("AccountKey cannot be null", exception.getMessage());
    }

    @Test
    public void constructor_invalidWritableIndex_shouldThrowException() {
        PublicKey accountKey = new PublicKey("11111111111111111111111111111111");
        List<Integer> writableIndexes = List.of(256); // Invalid index

        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> new MessageAddressTableLookup(accountKey, writableIndexes, List.of())
        );

        assertEquals("Writable index must be between 0 and 255", exception.getMessage());
    }

    @Test
    public void serialize_validData_shouldReturnCorrectByteArray() {
        PublicKey accountKey = new PublicKey("11111111111111111111111111111111");
        List<Integer> writableIndexes = List.of(10, 20);
        List<Integer> readonlyIndexes = List.of(30, 40);

        MessageAddressTableLookup lookup = new MessageAddressTableLookup(accountKey, writableIndexes, readonlyIndexes);
        byte[] serialized = lookup.serialize();

        assertNotNull(serialized);
        assertEquals(lookup.getSerializedLength(), serialized.length); // Serialized length must match
    }

    @Test
    public void deserialize_validSerializedData_shouldReturnEquivalentObject() {
        PublicKey accountKey = new PublicKey("11111111111111111111111111111111");
        List<Integer> writableIndexes = List.of(10, 20);
        List<Integer> readonlyIndexes = List.of(30, 40);

        MessageAddressTableLookup originalLookup = new MessageAddressTableLookup(accountKey, writableIndexes, readonlyIndexes);
        byte[] serialized = originalLookup.serialize();

        MessageAddressTableLookup deserializedLookup = MessageAddressTableLookup.deserialize(ByteBuffer.wrap(serialized));

        assertEquals(originalLookup, deserializedLookup);
    }

    @Test
    public void deserialize_invalidData_shouldThrowException() {
        byte[] invalidData = new byte[] {1, 2, 3};

        Exception exception = assertThrows(
                Exception.class,
                () -> MessageAddressTableLookup.deserialize(ByteBuffer.wrap(invalidData))
        );

        assertNotNull(exception);
    }

    @Test
    public void getSerializedLength_validData_shouldReturnAccurateLength() {
        PublicKey accountKey = new PublicKey("11111111111111111111111111111111");
        List<Integer> writableIndexes = List.of(10, 20);
        List<Integer> readonlyIndexes = List.of(30);

        MessageAddressTableLookup lookup = new MessageAddressTableLookup(accountKey, writableIndexes, readonlyIndexes);

        int expectedLength = 32 + // Account key length
                1 +  // Writable index length prefix
                writableIndexes.size() +
                1 +  // Readonly index length prefix
                readonlyIndexes.size();

        assertEquals(expectedLength, lookup.getSerializedLength());
    }

    @Test
    public void constructor_emptyIndexes_shouldInitializeCorrectly() {
        PublicKey accountKey = new PublicKey("11111111111111111111111111111111");

        MessageAddressTableLookup lookup = new MessageAddressTableLookup(accountKey, List.of(), List.of());

        assertNotNull(lookup);
        assertTrue(lookup.getWritableIndexes().isEmpty());
        assertTrue(lookup.getReadonlyIndexes().isEmpty());
    }
}
