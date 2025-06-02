package net.deanly.solana.sdk.program.metaplex.tokenmetadata.layout;

import net.deanly.solana.sdk.program.metaplex.tokenmetadata.type.CollectionDetails;
import net.deanly.structlayout.Field;
import net.deanly.structlayout.type.FieldBase;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class CollectionDetailsField extends FieldBase<CollectionDetails> implements Field<CollectionDetails> {

    public CollectionDetailsField() {
        super(-1, CollectionDetails.class); // dynamic size
    }

    @Override
    public CollectionDetails decode(byte[] buffer, int offset) {
        if (buffer == null || buffer.length <= offset) {
            throw new IllegalArgumentException("Buffer underflow at offset: " + offset);
        }

        int tag = Byte.toUnsignedInt(buffer[offset]);

        switch (tag) {
            case CollectionDetails.V1: {
                if (buffer.length - offset < 9) {
                    throw new IllegalArgumentException("Insufficient bytes for CollectionDetails.V1");
                }
                long size = ByteBuffer.wrap(buffer, offset + 1, 8).order(ByteOrder.LITTLE_ENDIAN).getLong();
                return new CollectionDetails.V1(size);
            }
            case CollectionDetails.V2: {
                if (buffer.length - offset < 9) {
                    throw new IllegalArgumentException("Insufficient bytes for CollectionDetails.V2");
                }
                byte[] padding = Arrays.copyOfRange(buffer, offset + 1, offset + 9);
                return new CollectionDetails.V2(padding);
            }
            default:
                throw new IllegalArgumentException("Unknown CollectionDetails variant tag: " + tag);
        }
    }

    @Override
    public byte[] encode(CollectionDetails value) {
        if (value instanceof CollectionDetails.V1) {
            CollectionDetails.V1 v1 = (CollectionDetails.V1) value;
            ByteBuffer buf = ByteBuffer.allocate(9).order(ByteOrder.LITTLE_ENDIAN);
            buf.put((byte) CollectionDetails.V1);
            buf.putLong(v1.getSize());
            return buf.array();
        } else if (value instanceof CollectionDetails.V2) {
            CollectionDetails.V2 v2 = (CollectionDetails.V2) value;
            ByteBuffer buf = ByteBuffer.allocate(9);
            buf.put((byte) CollectionDetails.V2);
            buf.put(v2.getPadding());
            return buf.array();
        } else {
            throw new IllegalArgumentException("Unsupported CollectionDetails variant: " + value.getClass());
        }
    }
}
