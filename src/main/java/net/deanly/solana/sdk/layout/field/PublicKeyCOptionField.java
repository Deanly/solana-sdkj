package net.deanly.solana.sdk.layout.field;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.structlayout.type.rust.AbstractRustCOptionField;

public class PublicKeyCOptionField extends AbstractRustCOptionField<PublicKey, PublicKeyField> {
    @Override
    protected PublicKeyField createField() {
        return new PublicKeyField();
    }
}
