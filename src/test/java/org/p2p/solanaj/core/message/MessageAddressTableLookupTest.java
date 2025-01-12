package org.p2p.solanaj.core.message;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

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
        log.info("AccountKey: " + lookup.getAccountKey());
        log.info("WritableIndexes: " + lookup.getWritableIndexes());
        log.info("ReadonlyIndexes: " + lookup.getReadonlyIndexes());
    }
}
