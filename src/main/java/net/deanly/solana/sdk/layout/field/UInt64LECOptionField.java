package net.deanly.solana.sdk.layout.field;

import net.deanly.structlayout.type.basic.UInt64LEField;
import net.deanly.structlayout.type.guava.UnsignedLong;
import net.deanly.structlayout.type.rust.AbstractRustCOptionField;

public class UInt64LECOptionField extends AbstractRustCOptionField<UnsignedLong, UInt64LEField> {
    @Override
    protected UInt64LEField createField() {
        return new UInt64LEField();
    }
}
