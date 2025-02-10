package net.deanly.solana.sdk.layout;

import net.deanly.structlayout.StructLayout;

/**
 * Represents the base structure for encoding and decoding structured binary data.
 * <p>
 * This abstract class provides a foundation for defining structured layouts that
 * can be used for serialization and deserialization of binary data. It allows
 * developers to define structured formats that can be efficiently transformed into
 * byte arrays or reconstructed from byte arrays.
 * </p>
 *
 * <p>
 * The serialization process follows a structured approach where data is encoded
 * in a predefined format. By extending this class, developers can define custom
 * structures using annotations such as @StructField, ensuring consistency
 * in binary transformations.
 * </p>
 *
 * Usage Example:
 * <pre>
 * {@code
 *      public class ExampleStruct extends Struct {
 *          @StructField(order = 1, type = UInt32LEField.class)
 *          private int exampleField;
 *      }
 * }
 * </pre>
 * @see <a href="https://github.com/Deanly/struct-layout">Struct Layout Github</a>
 */
public abstract class Struct {

    public byte[] serialize() {
        return StructLayout.encode(this);
    }

}
