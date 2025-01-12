package org.p2p.solanaj.core.message;

import org.p2p.solanaj.core.PublicKey;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public interface VersionedMessage {
    Version getVersion();
    byte[] serialize();
    MessageHeader getHeader();
    List<PublicKey> getSigners();

    /**
     * Deserialize a versioned message from a byte array.
     */
    static VersionedMessage deserialize(byte[] serializedMessage) {
        ByteBuffer buffer = ByteBuffer.wrap(serializedMessage).order(ByteOrder.LITTLE_ENDIAN);
        return deserialize(buffer);
    }

    /**
     * Deserialize a versioned message from a ByteBuffer.
     */
    static VersionedMessage deserialize(ByteBuffer buffer) {
        // Mark the buffer's current position and read the prefix
        buffer.mark();
        int prefix = Byte.toUnsignedInt(buffer.get());
        buffer.reset();

        // Use Version enum to determine the correct deserialization logic
        Version version = Version.fromPrefix(prefix);
        return version.getDeserializer().apply(buffer);
    }
}