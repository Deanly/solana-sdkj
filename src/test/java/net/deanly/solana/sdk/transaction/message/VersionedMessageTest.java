package net.deanly.solana.sdk.transaction.message;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.transaction.message.meta.MessageAddressTableLookup;
import net.deanly.solana.sdk.transaction.message.meta.MessageCompiledInstruction;
import net.deanly.solana.sdk.transaction.message.meta.MessageHeader;
import net.deanly.solana.sdk.types.Blockhash;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.exception.StructDecodingException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VersionedMessageTest {

    @Test
    void deserializeLegacyMessage_correctlyDeserializes() {
        // Create a mock Legacy message
        MessageHeader header = new MessageHeader(1, 0, 1);
        List<PublicKey> staticAccountKeys = List.of(
                new PublicKey("2USZwUvRmHimVfzde7EY5AyfEsqLUdzQWWxuMiz77n1u")
        );
        String recentBlockhash = "7dA2H9LGYhFNco7DAFS52trcmCQt6tLdqiy4ApXPtGBF";
        List<MessageCompiledInstruction> instructions = List.of();

        Message originalMessage = new Message(header, staticAccountKeys,  Blockhash.of(recentBlockhash), instructions);

        // Serialize the message
        byte[] serializedMessage = originalMessage.serialize();
        StructLayout.debug(serializedMessage);

        // Deserialize the message
        VersionedMessage deserializedMessage = VersionedMessage.deserialize(serializedMessage);
        StructLayout.debug(deserializedMessage);

        // Validate deserialized instance
        assertTrue(deserializedMessage instanceof Message, "Deserialized message should be of type Message");
        assertEquals(originalMessage, deserializedMessage, "Original and deserialized messages should match");
    }

    @Test
    void deserializeVersionedMessage_correctlyDeserializes() {
        // Create a mock Versioned (V0) message
        MessageHeader header = new MessageHeader(1, 0, 1);
        List<PublicKey> staticAccountKeys = List.of(
                new PublicKey("3bvGkg7vCdUdKgdataTukWNcniVZZ7a44W5d5psE5tZt")
        );
        String recentBlockhash = "4dA1H9LG5oFN1o7DAFS52trcmCQt6tLdqiy4ApXxx3mt";
        List<MessageCompiledInstruction> instructions = List.of();

        MessageAddressTableLookup addressTableLookup = new MessageAddressTableLookup(
                new PublicKey("11111111111111111111111111111111"),
                List.of(0), // writable indexes
                List.of(1) // readonly indexes
        );

        List<MessageAddressTableLookup> addressTableLookups = List.of(addressTableLookup);

        MessageV0 originalMessageV0 = new MessageV0(header, staticAccountKeys,  Blockhash.of(recentBlockhash), instructions, addressTableLookups);

        // Serialize the message
        byte[] serializedMessageV0 = originalMessageV0.serialize();

        // Deserialize the message
        VersionedMessage deserializedMessage = VersionedMessage.deserialize(serializedMessageV0);

        // Validate deserialized instance
        assertTrue(deserializedMessage instanceof MessageV0, "Deserialized message should be of type MessageV0");
        assertEquals(originalMessageV0, deserializedMessage, "Original and deserialized messages should match");
    }

    @Test
    void deserialize_invalidMessage_throwsException() {
        // Use a completely invalid byte array
        byte[] invalidMessage = new byte[]{0, 1, 2, 3, 4};

        // Attempt deserialization and verify an exception is thrown
        Exception exception = assertThrows(StructDecodingException.class, () -> {
            VersionedMessage.deserialize(invalidMessage);
        });
    }

    @Test
    void deserialize_emptyByteArray_throwsException() {
        // Test with an empty byte array
        byte[] emptyMessage = new byte[0];

        // Attempt deserialization and verify an exception is thrown
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            VersionedMessage.deserialize(emptyMessage);
        });

        assertEquals("Message data is empty or corrupted", exception.getMessage());
    }

    @Test
    void serializeAndDeserializeRoundTrip_preservesMessageEquality() {
        // Create a mock V0 message to test serialization round-trip
        MessageHeader header = new MessageHeader(1, 0, 1);
        List<PublicKey> staticAccountKeys = List.of(
                new PublicKey("5voVkg8vDnDdTozRtaktneGiYghjkPwcKRKwWEHdtiZz")
        );
        String recentBlockhash = "BbSHyzer9JZ1FNco7DA1UfmcmUCqLtLdqiy4AxPfEd74";
        List<MessageCompiledInstruction> instructions = List.of();

        MessageAddressTableLookup addressTableLookup = new MessageAddressTableLookup(
                new PublicKey("11111111111111111111111111111112"),
                List.of(0), // writable indexes
                List.of(1) // readonly indexes
        );

        List<MessageAddressTableLookup> addressTableLookups = List.of(addressTableLookup);

        MessageV0 originalMessageV0 = new MessageV0(header, staticAccountKeys,  Blockhash.of(recentBlockhash), instructions, addressTableLookups);

        // Serialize the original message
        byte[] serializedMessage = originalMessageV0.serialize();

        // Deserialize the message back
        VersionedMessage deserializedMessage = VersionedMessage.deserialize(serializedMessage);

        // Ensure the deserialized message matches the original
        assertEquals(originalMessageV0, deserializedMessage, "Deserialized message should match the original after a round-trip");
    }
}