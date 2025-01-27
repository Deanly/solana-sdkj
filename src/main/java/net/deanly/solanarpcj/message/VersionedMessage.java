package net.deanly.solanarpcj.message;

import net.deanly.solanarpcj.account.PublicKey;
import net.deanly.solanarpcj.message.meta.MessageHeader;

import java.nio.ByteBuffer;
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
        if (serializedMessage.length == 0) {
            throw new IllegalArgumentException("Message data is empty or corrupted");
        }

        Version version = Version.detectVersion(serializedMessage);
        return version.getDeserializer().apply(serializedMessage);
    }

    /**
     * Deserialize a versioned message from a ByteBuffer.
     */
    static VersionedMessage deserialize(ByteBuffer buffer) {
        return deserialize(buffer.array());
    }
}