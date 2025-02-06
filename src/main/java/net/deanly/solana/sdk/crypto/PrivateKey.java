package net.deanly.solana.sdk.crypto;

import net.deanly.solana.sdk.types.codec.Base58;

import java.util.Arrays;

public class PrivateKey {
    private boolean isCleared = false;
    private final byte[] encoded;

    public PrivateKey(byte[] encoded) {
        if (encoded == null || encoded.length != 64) {
            throw new IllegalArgumentException("Invalid private key length. Expected 64 bytes.");
        }
        this.encoded = encoded.clone();
    }

    public byte[] getEncoded() {
        if (this.isCleared) {
            throw new IllegalStateException("Private key has been cleared.");
        }
        return encoded.clone();
    }

    public String toBase58() {
        if (this.isCleared) {
            throw new IllegalStateException("Private key has been cleared.");
        }
        return Base58.encode(encoded);
    }

    public void clear() {
        Arrays.fill(encoded, (byte) 0);
        this.isCleared = true;
    }

    @Override
    public String toString() {
        return "PublicKey{***}";
    }
}