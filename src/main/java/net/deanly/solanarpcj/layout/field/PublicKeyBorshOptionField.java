package net.deanly.solanarpcj.layout.field;

import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.structlayout.type.borsh.AbstractBorshOptionField;

public class PublicKeyBorshOptionField extends AbstractBorshOptionField<PublicKey, PublicKeyField> {
    @Override
    protected PublicKeyField createField() {
        return new PublicKeyField();
    }
}
