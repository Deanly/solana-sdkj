package net.deanly.solanarpcj.layout;

import net.deanly.structlayout.type.FieldBase;
import org.bitcoinj.core.Base58;

public class BlockhashField extends FieldBase<String> {

    private static final int BLOCKHASH_LENGTH = 32; // 32 bytes

    public BlockhashField() {
        super(BLOCKHASH_LENGTH);
    }

    @Override
    public byte[] encode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Blockhash value cannot be null.");
        }
        byte[] bytes = Base58.decode(value);

        if (bytes.length != BLOCKHASH_LENGTH) {
            throw new IllegalArgumentException("Blockhash must be exactly " + BLOCKHASH_LENGTH + " bytes.");
        }

        return bytes;
    }

    @Override
    public String decode(byte[] buffer, int offset) {
        if (buffer == null || buffer.length - offset < BLOCKHASH_LENGTH) {
            throw new IllegalArgumentException("Buffer does not contain enough data for a Blockhash.");
        }

        byte[] blockhashBytes = new byte[BLOCKHASH_LENGTH];
        System.arraycopy(buffer, offset, blockhashBytes, 0, BLOCKHASH_LENGTH);

        return Base58.encode(blockhashBytes);
    }


}

