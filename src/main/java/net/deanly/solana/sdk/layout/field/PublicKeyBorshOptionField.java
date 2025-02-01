package net.deanly.solana.sdk.layout.field;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.structlayout.type.borsh.AbstractBorshOptionField;

public class PublicKeyBorshOptionField extends AbstractBorshOptionField<PublicKey, PublicKeyField> {
    @Override
    protected PublicKeyField createField() {
        return new PublicKeyField();
    }
}
