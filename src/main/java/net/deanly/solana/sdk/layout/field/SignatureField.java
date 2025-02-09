package net.deanly.solana.sdk.layout.field;

import net.deanly.solana.sdk.types.Signature;
import net.deanly.solana.sdk.types.codec.Base58;
import net.deanly.structlayout.type.FieldBase;

public class SignatureField extends FieldBase<Signature> {

    private static final int LENGTH = 64; // 64 bytes

    public SignatureField() {
        super(LENGTH);
    }

    @Override
    public byte[] encode(Signature value) {
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null.");
        }
        byte[] bytes = Base58.decode(value.toString());

        if (bytes.length != LENGTH) {
            throw new IllegalArgumentException("must be exactly " + LENGTH + " bytes.");
        }

        return bytes;
    }

    @Override
    public Signature decode(byte[] buffer, int offset) {
        if (buffer == null || buffer.length - offset < LENGTH) {
            throw new IllegalArgumentException("Buffer does not contain enough data for a base58 field. offset=" + offset + ", " +  (buffer != null ? buffer.length : 0) + " bytes remaining.");
        }

        byte[] blockhashBytes = new byte[LENGTH];
        System.arraycopy(buffer, offset, blockhashBytes, 0, LENGTH);

        return Signature.of(Base58.encode(blockhashBytes));
    }


}
