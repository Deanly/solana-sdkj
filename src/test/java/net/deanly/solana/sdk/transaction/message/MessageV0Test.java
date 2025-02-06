package net.deanly.solana.sdk.transaction.message;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstructionImpl;
import net.deanly.solana.sdk.types.Blockhash;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.solana.sdk.transaction.message.meta.LoadedAddresses;
import net.deanly.solana.sdk.transaction.message.meta.MessageAddressTableLookup;
import net.deanly.solana.sdk.transaction.message.meta.MessageHeader;
import net.deanly.solana.sdk.program.alt.state.AddressLookupTableAccount;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static net.deanly.solana.sdk.program.alt.state.AddressLookupTableAccountTest.createMockAddressLookupTableAccount;

class MessageV0Test {

    // Helper method to create a TransactionInstruction with test Base58 keys
    private TransactionInstruction createMockInstruction() {
        PublicKey programId = new PublicKey("11111111111111111111111111111111");
        AccountMeta meta = new AccountMeta(
                new PublicKey("SecondPubey22222222222222222222222222222222"),
                false, true    // signer
                // writable
        );

        byte[] data = new byte[]{1, 2, 3};
        return new TransactionInstructionImpl(programId, Collections.singletonList(meta), data);
    }

    @Test
    void testSerializationAndDeserialization() {
        // Arrange
        PublicKey payerKey = new PublicKey("11111111111111111111111111111111");
        List<TransactionInstruction> instructions = List.of(createMockInstruction());
        String recentBlockhash = "SecondPubey22222222222222222222222222222222";
        List<AddressLookupTableAccount> addressLookupTableAccounts = List.of(
                createMockAddressLookupTableAccount(new PublicKey("ThirdPubkey33333333333333333333333333333333"))
        );

        // Compile to create MessageV0
        MessageV0 originalMessage = MessageV0.compile(payerKey, instructions,  Blockhash.of(recentBlockhash), addressLookupTableAccounts);

        // Act
        byte[] serialized = originalMessage.serialize();
        MessageV0 deserializedMessage = MessageV0.deserialize(serialized);

        StructLayout.debug(originalMessage);
        StructLayout.debug(serialized);
        StructLayout.debug(deserializedMessage);

        // Assert: Ensure the deserialized object matches the original one
        assertEquals(originalMessage, deserializedMessage);
        assertTrue(MessageV0.validateSerialization(originalMessage));
    }

    @Test
    void testAddressLookupTableKeysResolution() {
        // Arrange
        PublicKey tableKey = new PublicKey("ThirdPubkey33333333333333333333333333333333");
        List<MessageAddressTableLookup> tableLookups = List.of(new MessageAddressTableLookup(
                tableKey,
                List.of(0), // writable index
                List.of(1)  // readonly index
        ));

        AddressLookupTableAccount tableAccount = createMockAddressLookupTableAccount(tableKey);

        // Act
        LoadedAddresses loadedAddresses =
                MessageV0.resolveAddressTableLookups(tableLookups, List.of(tableAccount));

        // Assert: Check resolved writable and readonly keys
        assertEquals(1, loadedAddresses.getWritable().size());
        assertEquals(new PublicKey("11111111111111111111111111111111"), loadedAddresses.getWritable().get(0));
        assertEquals(1, loadedAddresses.getReadonly().size());
        assertEquals(new PublicKey("ThirdPubkey33333333333333333333333333333333"), loadedAddresses.getReadonly().get(0));
    }

    @Test
    void testAddressLookupTableKeyNotFound() {
        // Arrange
        PublicKey tableKey = new PublicKey("ThirdPubkey33333333333333333333333333333333");
        List<MessageAddressTableLookup> tableLookups = List.of(new MessageAddressTableLookup(
                tableKey,
                List.of(0),
                List.of(1)
        ));

        AddressLookupTableAccount differentTableAccount = createMockAddressLookupTableAccount(
                new PublicKey("11111111111111111111111111111111")
        );

        // Act & Assert: Ensure exception is thrown when lookup table key is missing
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                MessageV0.resolveAddressTableLookups(tableLookups, List.of(differentTableAccount))
        );
        assertTrue(exception.getMessage().contains("Address lookup table not found for account key"));
    }

    @Test
    void testSerializationExceedsPacketLimit() {
        // Arrange
        PublicKey payerKey = new PublicKey("11111111111111111111111111111111");

        // Create a mock instruction with a very large data payload
        byte[] largeData = new byte[1280]; // This will exceed PACKET_DATA_SIZE when serialized
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = 1; // Fill with dummy data
        }

        List<TransactionInstruction> instructions = List.of(new TransactionInstructionImpl(
                new PublicKey("SecondPubey22222222222222222222222222222222"),
                Collections.emptyList(),
                largeData
        ));
        String recentBlockhash = "ThirdPubkey33333333333333333333333333333333";

        // Act: Create a MessageV0 that will exceed the size limit when serialized
        MessageV0 message = MessageV0.compile(payerKey, instructions,  Blockhash.of(recentBlockhash), List.of(createMockAddressLookupTableAccount(new PublicKey("11111111111111111111111111111111"))));

        // Assert: Ensure exception is thrown during serialization
        IllegalStateException exception = assertThrows(IllegalStateException.class, message::serialize);
        assertTrue(exception.getMessage().contains("Serialized message exceeds allowed packet size"));
    }

    @Test
    void testDeserializeInvalidVersion() {
        // Arrange
        ByteBuffer buffer = ByteBuffer.allocate(10).put((byte) 127); // Invalid version (127)

        // Act & Assert: Ensure IllegalArgumentException is thrown for invalid version
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                MessageV0.deserialize(buffer.flip())
        );
    }

    @Test
    void testHeaderSerializationAndDeserialization() {
        // Arrange
        MessageHeader header = new MessageHeader(2, 1, 1);

        // Act
        byte[] serializedHeader = header.serialize();
        MessageHeader deserializedHeader = MessageHeader.deserialize(serializedHeader);

        // Assert: Ensure deserialization returns the original values
        assertNotNull(deserializedHeader);
        assertEquals(2, deserializedHeader.getNumRequiredSignatures());
        assertEquals(1, deserializedHeader.getNumReadonlySignedAccounts());
        assertEquals(1, deserializedHeader.getNumReadonlyUnsignedAccounts());
    }

    @Test
    void testIsAccountSigner() {
        // Arrange
        MessageHeader header = new MessageHeader(2, 1, 1); // 2 required signatures
        List<PublicKey> staticKeys = List.of(
                new PublicKey("11111111111111111111111111111111"),
                new PublicKey("SecondPubey22222222222222222222222222222222"),
                new PublicKey("ThirdPubkey33333333333333333333333333333333")
        );
        MessageV0 message = new MessageV0(header, staticKeys,  Blockhash.of("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn"), List.of(), List.of());

        // Act & Assert
        assertTrue(message.isAccountSigner(0)); // First account is a signer
        assertTrue(message.isAccountSigner(1)); // Second account is also a signer
        assertFalse(message.isAccountSigner(2)); // Third account is not a signer
    }

    @Test
    void testIsAccountWritable() {
        // Arrange
        MessageHeader header = new MessageHeader(3, 1, 1); // 3 signed, 1 readonly signed, 1 readonly unsigned
        List<PublicKey> staticKeys = List.of(
                new PublicKey("11111111111111111111111111111111"), // Signed writable
                new PublicKey("SecondPubey22222222222222222222222222222222"), // Signed writable
                new PublicKey("ThirdPubkey33333333333333333333333333333333"), // Signed readonly
                new PublicKey("FourthPubke44444444444444444444444444444444")  // Unsigned readonly
        );
        MessageV0 message = new MessageV0(header, staticKeys,  Blockhash.of("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn"), List.of(), List.of());

        // Act & Assert
        assertTrue(message.isAccountWritable(0)); // Writable signed
        assertTrue(message.isAccountWritable(1)); // Readonly signed
        assertFalse(message.isAccountWritable(2)); // Writable unsigned
        assertFalse(message.isAccountWritable(3)); // Readonly unsigned
    }

    @Test
    void testGetAddressTableLookup() {
        // Arrange
        PublicKey tableKey = new PublicKey("ThirdPubkey33333333333333333333333333333333"); // Lookup table key
        List<MessageAddressTableLookup> tableLookups = List.of(new MessageAddressTableLookup(
                tableKey,
                List.of(0), // Writable index
                List.of(1)  // Readonly index
        ));
        AddressLookupTableAccount tableAccount = createMockAddressLookupTableAccount(tableKey);

        MessageV0 message = new MessageV0(
                new MessageHeader(0, 0, 0),
                new ArrayList<>(),
                Blockhash.of("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn"),
                new ArrayList<>(),
                tableLookups
        );

        // Act
        List<MessageAddressTableLookup> resolvedLookups = message.getAddressTableLookup(List.of(tableAccount));

        // Assert
        assertNotNull(resolvedLookups);
        assertEquals(1, resolvedLookups.size());
        assertEquals(tableLookups.get(0), resolvedLookups.get(0));
    }

    @Test
    void testWritableAccountWithLookupKeys() {
        // Arrange
        PublicKey tableKey = new PublicKey("ThirdPubkey33333333333333333333333333333333");
        List<MessageAddressTableLookup> tableLookups = List.of(new MessageAddressTableLookup(
                tableKey,
                List.of(0), // Writable index
                List.of(1)  // Readonly index
        ));
        AddressLookupTableAccount tableAccount = createMockAddressLookupTableAccount(tableKey);

        List<PublicKey> staticKeys = List.of(new PublicKey("11111111111111111111111111111111"));
        MessageHeader header = new MessageHeader(1, 0, 0);

        MessageV0 message = new MessageV0(header, staticKeys,  Blockhash.of("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn"), List.of(), tableLookups);

        // Act & Assert
        assertTrue(message.isAccountWritable(1)); // Check writable index from lookup keys
        assertFalse(message.isAccountWritable(2)); // Check readonly index
    }

    @Test
    void testSerializationConsistency() {
        // Arrange
        PublicKey payerKey = new PublicKey("11111111111111111111111111111111");
        List<TransactionInstruction> instructions = List.of(createMockInstruction());
        String recentBlockhash = "SecondPubey22222222222222222222222222222222";
        List<AddressLookupTableAccount> addressLookupTableAccounts = List.of(
                createMockAddressLookupTableAccount(new PublicKey("ThirdPubkey33333333333333333333333333333333"))
        );

        // Compile to create original message
        MessageV0 originalMessage = MessageV0.compile(payerKey, instructions,  Blockhash.of(recentBlockhash), addressLookupTableAccounts);

        // Act
        byte[] serialized = originalMessage.serialize();
        MessageV0 deserializedMessage = MessageV0.deserialize(serialized);

        // Assert
        assertEquals(originalMessage, deserializedMessage);
        assertTrue(MessageV0.validateSerialization(originalMessage));
    }

    @Test
    void testAddressLookupTableKeyNotFoundThrowsException() {
        // Arrange
        PublicKey tableKey = new PublicKey("ThirdPubkey33333333333333333333333333333333");
        List<MessageAddressTableLookup> tableLookups = List.of(new MessageAddressTableLookup(
                tableKey,
                List.of(0), List.of(1)
        ));

        AddressLookupTableAccount nonMatchingTableAccount = createMockAddressLookupTableAccount(
                new PublicKey("11111111111111111111111111111111")
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                MessageV0.resolveAddressTableLookups(tableLookups, List.of(nonMatchingTableAccount))
        );
        assertTrue(exception.getMessage().contains("Address lookup table not found for account key"));
    }

    @Test
    void testDeserializing() {
        String base64Data = "gAEAAQK446O8CVwt9dRovnTkqCnE8lIzd3k9Uhkp7nPDQ4RB8gAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAJj1Cfugnrm0s9YN+IUJnIDa/iwyhT1ti+WmXqJDdXYQBAQMAAAIMAgAAAOgDAAAAAAAAAel+InGTQKJ9FGp9OmvZKHc0FUvsPB9bHJJYeHIBJTZOAQEA";
        byte[] data = Base64.getDecoder().decode(base64Data);

        MessageV0 message = MessageV0.deserialize(ByteBuffer.wrap(data));
        assertNotNull(message);
        assertEquals(1, message.getHeader().getNumRequiredSignatures());
        assertEquals(0, message.getHeader().getNumReadonlySignedAccounts());
        assertEquals(1, message.getHeader().getNumReadonlyUnsignedAccounts());
        assertEquals(1, message.getInstructions().size());
        assertEquals(2, message.getStaticAccountKeys().size());
        assertEquals(1, message.getAddressTableLookups().size());
    }

}