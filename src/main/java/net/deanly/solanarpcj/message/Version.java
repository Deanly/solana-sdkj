package net.deanly.solanarpcj.message;

import lombok.Getter;

import java.nio.ByteBuffer;
import java.util.function.Function;

@Getter
public enum Version {
    LEGACY(0x00, Message::deserialize),
    V0((1 << 7), MessageV0::deserialize);

    private final int versionPrefix;
    private final Function<ByteBuffer, VersionedMessage> deserializer;

    Version(int versionPrefix, Function<ByteBuffer, VersionedMessage> deserializer) {
        this.versionPrefix = versionPrefix;
        this.deserializer = deserializer;
    }

    public static Version fromPrefix(int prefix) {
        int version = prefix & 0x7F;
        for (Version v : values()) {
            if ((v.versionPrefix & 0x7F) == version) {
                return v;
            }
        }
        throw new IllegalArgumentException(String.format("Unsupported message version prefix: %d", prefix));
    }
}