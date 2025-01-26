package net.deanly.solanarpcj.message.meta;

import lombok.*;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solanarpcj.core.PublicKey;
import net.deanly.solanarpcj.layout.PublicKeyField;
import net.deanly.solanarpcj.layout.ShortVecField;
import net.deanly.solanarpcj.utils.ShortvecEncoding;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Represents a MessageAddressTableLookup.
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class MessageAddressTableLookup {

    @StructField(order = 1, type = PublicKeyField.class)
    private PublicKey accountKey;

    @StructSequenceField(order = 2, elementType = UInt8Field.class, lengthType = ShortVecField.class)
    private List<Integer> writableIndexes;

    @StructSequenceField(order = 3, elementType = UInt8Field.class, lengthType = ShortVecField.class)
    private List<Integer> readonlyIndexes;

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
        return StructLayout.encode(this);
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
        return StructLayout.decode(buffer.array(), MessageAddressTableLookup.class);
    }

    public int getTotalKeyCount() {
        Set<Integer> totalIndexes = new HashSet<>();
        totalIndexes.addAll(writableIndexes);
        totalIndexes.addAll(readonlyIndexes);
        return totalIndexes.size();
    }
}