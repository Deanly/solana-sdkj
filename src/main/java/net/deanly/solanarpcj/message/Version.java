package net.deanly.solanarpcj.message;

import lombok.Getter;

import java.nio.ByteBuffer;
import java.util.function.Function;

@Getter
public enum Version {
    LEGACY(null, Message::deserialize),
    V0((1 << 7), MessageV0::deserialize);

    private final Integer versionPrefix;
    private final Function<byte[], VersionedMessage> deserializer;

    Version(Integer versionPrefix, Function<byte[], VersionedMessage> deserializer) {
        this.versionPrefix = versionPrefix;
        this.deserializer = deserializer;
    }

    public static Version fromPrefix(Integer prefix) {
        if (prefix == null) {
            return LEGACY; // Handle Legacy messages explicitly
        }

        int version = prefix & 0x7F;
        for (Version v : values()) {
            if (v.versionPrefix != null && (v.versionPrefix & 0x7F) == version) {
                return v;
            }
        }
        throw new IllegalArgumentException(String.format("Unsupported message version prefix: %d", prefix));
    }

    public static Version detectVersion(byte[] serializedMessage) {
        if (serializedMessage.length == 0) {
            throw new IllegalArgumentException("Message data is empty or corrupted");
        }

        int prefix = Byte.toUnsignedInt(serializedMessage[0]);

        if ((prefix & 0x80) == 0) { // No version-prefix bit set
            return LEGACY;
        }

        return fromPrefix(prefix);
    }
}