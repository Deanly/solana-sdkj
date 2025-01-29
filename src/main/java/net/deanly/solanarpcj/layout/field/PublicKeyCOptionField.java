package net.deanly.solanarpcj.layout.field;

import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.structlayout.type.advanced.AbstractCOptionField;

public class PublicKeyCOptionField extends AbstractCOptionField<PublicKey, PublicKeyField> {
    @Override
    protected PublicKeyField createField() {
        return new PublicKeyField();
    }
}
