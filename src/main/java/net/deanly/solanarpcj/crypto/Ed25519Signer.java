package net.deanly.solanarpcj.crypto;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Utility class for signing data using Ed25519 algorithm.
 */
public class Ed25519Signer {

    /**
     * Signs the given message using the provided Ed25519 private key.
     *
     * @param message   The message to be signed.
     * @param privateKey The private key for signing.
     * @return The generated signature.
     * @throws GeneralSecurityException if signing fails.
     */
    public static byte[] sign(byte[] message, byte[] privateKey) throws GeneralSecurityException {
        if (privateKey.length != 64) {
            throw new IllegalArgumentException("Invalid Ed25519 private key length. Expected 64 bytes.");
        }

        // Convert 64-byte secret key to `PrivateKey` object
        KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
        java.security.PrivateKey key = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(convertToPKCS8(privateKey)));

        // Sign the message
        Signature signature = Signature.getInstance("Ed25519");
        signature.initSign(key);
        signature.update(message);
        return signature.sign();
    }

    /**
     * Converts a 64-byte Ed25519 private key into a PKCS#8 encoded format.
     *
     * @param rawPrivateKey The 64-byte Ed25519 private key.
     * @return PKCS#8 encoded private key.
     */
    private static byte[] convertToPKCS8(byte[] rawPrivateKey) {
        byte[] prefix = new byte[]{
                0x30, 0x2e, // SEQUENCE (46 bytes)
                0x02, 0x01, 0x00, // INTEGER (0)
                0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x70, // OID 1.3.101.112 (Ed25519)
                0x04, 0x22, // OCTET STRING (34 bytes)
                0x04, 0x20  // Private key (32 bytes)
        };

        byte[] pkcs8Key = new byte[prefix.length + 32];
        System.arraycopy(prefix, 0, pkcs8Key, 0, prefix.length);
        System.arraycopy(rawPrivateKey, 0, pkcs8Key, prefix.length, 32);
        return pkcs8Key;
    }
}