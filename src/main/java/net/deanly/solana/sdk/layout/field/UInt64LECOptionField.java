package net.deanly.solana.sdk.layout.field;

import net.deanly.structlayout.type.borsh.AbstractBorshOptionField;
import net.deanly.structlayout.type.basic.UInt64LEField;

import java.math.BigInteger;

public class UInt64LECOptionField extends AbstractBorshOptionField<BigInteger, UInt64LEField> {
    @Override
    protected UInt64LEField createField() {
        return new UInt64LEField();
    }
}
