package org.p2p.solanaj.core.message;

import lombok.Value;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.utils.ShortvecEncoding;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a MessageAddressTableLookup.
 */
@Value
public class MessageAddressTableLookup {
    PublicKey accountKey;
    List<Integer> writableIndexes;
    List<Integer> readonlyIndexes;

    /**
     * Constructs a MessageAddressTableLookup with the given parameters.
     *
     * @param accountKey      The public key of the address lookup table
     * @param writableIndexes The list of writable address indexes
     * @param readonlyIndexes The list of readonly address indexes
     */
    public MessageAddressTableLookup(PublicKey accountKey, List<Integer> writableIndexes, List<Integer> readonlyIndexes) {
        this.accountKey = Objects.requireNonNull(accountKey, "AccountKey cannot be null");
        this.writableIndexes = validateIndexes(writableIndexes, "Writable");
        this.readonlyIndexes = validateIndexes(readonlyIndexes, "Readonly");
    }

    private List<Integer> validateIndexes(List<Integer> indexes, String type) {
        List<Integer> validatedIndexes = new ArrayList<>();
        for (Integer index : indexes) {
            if (index < 0 || index > 255) {
                throw new IllegalArgumentException(type + " index must be between 0 and 255");
            }
            validatedIndexes.add(index);
        }
        return validatedIndexes;
    }

    /**
     * Serializes the MessageAddressTableLookup into a byte array.
     *
     * @return Byte array representing the serialized lookup table
     */
    public byte[] serialize() {
        byte[] accountKeyBytes = accountKey.toByteArray();
        byte[] writableLength = ShortvecEncoding.encodeLength(writableIndexes.size());
        byte[] readonlyLength = ShortvecEncoding.encodeLength(readonlyIndexes.size());

        ByteBuffer buffer = ByteBuffer.allocate(getSerializedLength());
        buffer.put(accountKeyBytes);
        buffer.put(writableLength);
        for (Integer index : writableIndexes) {
            buffer.put(index.byteValue());
        }
        buffer.put(readonlyLength);
        for (Integer index : readonlyIndexes) {
            buffer.put(index.byteValue());
        }
        return buffer.array();
    }

    /**
     * Gets the serialized length of the MessageAddressTableLookup.
     *
     * @return Length in bytes
     */
    public int getSerializedLength() {
        return 32 + // accountKey
                ShortvecEncoding.encodeLength(writableIndexes.size()).length +
                writableIndexes.size() +
                ShortvecEncoding.encodeLength(readonlyIndexes.size()).length +
                readonlyIndexes.size();
    }

    /**
     * Deserializes a MessageAddressTableLookup from a byte array.
     *
     * @return The deserialized MessageAddressTableLookup object.
     */
    public static MessageAddressTableLookup deserialize(ByteBuffer buffer) {
        // Deserialize the account key
        byte[] accountKeyBytes = new byte[PublicKey.PUBLIC_KEY_LENGTH];
        buffer.get(accountKeyBytes);
        PublicKey accountKey = new PublicKey(accountKeyBytes);

        // Deserialize writable indexes
        int writableIndexesLength = ShortvecEncoding.decodeLength(buffer);
        List<Integer> writableIndexes = new ArrayList<>();
        for (int i = 0; i < writableIndexesLength; i++) {
            writableIndexes.add(Byte.toUnsignedInt(buffer.get()));
        }

        // Deserialize readonly indexes
        int readonlyIndexesLength = ShortvecEncoding.decodeLength(buffer);
        List<Integer> readonlyIndexes = new ArrayList<>();
        for (int i = 0; i < readonlyIndexesLength; i++) {
            readonlyIndexes.add(Byte.toUnsignedInt(buffer.get()));
        }

        // Create and return the deserialized object
        return new MessageAddressTableLookup(accountKey, writableIndexes, readonlyIndexes);
    }
}