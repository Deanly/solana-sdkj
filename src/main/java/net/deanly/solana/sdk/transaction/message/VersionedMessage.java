package net.deanly.solana.sdk.transaction.message;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.solana.sdk.transaction.message.meta.MessageAddressTableLookup;
import net.deanly.solana.sdk.transaction.message.meta.MessageCompiledInstruction;
import net.deanly.solana.sdk.transaction.message.meta.MessageHeader;
import net.deanly.solana.sdk.types.Blockhash;
import net.deanly.structlayout.annotation.StructTypeSelector;
import net.deanly.structlayout.codec.helpers.ByteArrayHelper;
import net.deanly.structlayout.dispatcher.StructTypeDispatcher;

import java.nio.ByteBuffer;
import java.util.List;

@StructTypeSelector(dispatcher = VersionedMessage.Dispatcher.class)
public interface VersionedMessage {
    Version getVersion();
    byte[] serialize();
    MessageHeader getHeader();
    List<PublicKey> getSigners();
    List<PublicKey> getStaticAccountKeys();
    Blockhash getRecentBlockhash();
    List<MessageCompiledInstruction> getInstructions();
    List<MessageAddressTableLookup> getAddressTableLookups();

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

    class Dispatcher implements StructTypeDispatcher {
        @Override
        public Class<?> dispatch(byte[] data, int startOffset) {
            Version version = Version.detectVersion(new byte[]{data[startOffset]});

            if (version == null) {
                throw new IllegalArgumentException("Message data is corrupted");
            }

            return switch (version) {
                case LEGACY -> Message.class;
                case V0 -> MessageV0.class;
            };

        }

        @Override
        public int getNoDataSpan() {
            return 0;
        }
    }
}