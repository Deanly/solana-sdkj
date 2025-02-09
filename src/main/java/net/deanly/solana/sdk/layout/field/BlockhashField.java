package net.deanly.solana.sdk.layout.field;

import net.deanly.solana.sdk.types.Blockhash;
import net.deanly.structlayout.type.FieldBase;
import net.deanly.solana.sdk.types.codec.Base58;

public class BlockhashField extends FieldBase<Blockhash> {

    private static final int LENGTH = 32; // 32 bytes

    public BlockhashField() {
        super(LENGTH);
    }

    @Override
    public byte[] encode(Blockhash value) {
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null.");
        }
        byte[] bytes = value.toByteArray();

        if (bytes.length != LENGTH) {
            throw new IllegalArgumentException("must be exactly " + LENGTH + " bytes.");
        }

        return bytes;
    }

    @Override
    public Blockhash decode(byte[] buffer, int offset) {
        if (buffer == null || buffer.length - offset < LENGTH) {
            throw new IllegalArgumentException("Buffer does not contain enough data for a base58 field. ");
        }

        byte[] blockhashBytes = new byte[LENGTH];
        System.arraycopy(buffer, offset, blockhashBytes, 0, LENGTH);

        return Blockhash.of(Base58.encode(blockhashBytes));
    }


}

