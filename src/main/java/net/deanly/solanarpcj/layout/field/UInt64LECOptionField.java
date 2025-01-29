package net.deanly.solanarpcj.layout.field;

import net.deanly.structlayout.type.advanced.AbstractCOptionField;
import net.deanly.structlayout.type.basic.UInt64LEField;

import java.math.BigInteger;

public class UInt64LECOptionField extends AbstractCOptionField<BigInteger, UInt64LEField> {
    @Override
    protected UInt64LEField createField() {
        return new UInt64LEField();
    }
}
