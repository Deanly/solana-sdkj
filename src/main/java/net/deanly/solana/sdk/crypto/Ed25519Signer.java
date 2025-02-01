package net.deanly.solana.sdk.crypto;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

/**
 * Utility class for signing data using Ed25519 algorithm.
 */
public class Ed25519Signer {

    // 서명(Sign)
    public static byte[] sign(byte[] message, byte[] privateKey) {
        Ed25519PrivateKeyParameters privateKeyParams = new Ed25519PrivateKeyParameters(privateKey, 0);
        org.bouncycastle.crypto.signers.Ed25519Signer signer = new org.bouncycastle.crypto.signers.Ed25519Signer();
        signer.init(true, privateKeyParams);
        signer.update(message, 0, message.length);
        return signer.generateSignature(); // 64바이트 서명 반환
    }

    // 서명 검증(Verify)
    public static boolean verify(byte[] message, byte[] signature, byte[] publicKey) {
        Ed25519PublicKeyParameters publicKeyParams = new Ed25519PublicKeyParameters(publicKey, 0);
        org.bouncycastle.crypto.signers.Ed25519Signer verifier = new org.bouncycastle.crypto.signers.Ed25519Signer();
        verifier.init(false, publicKeyParams);
        verifier.update(message, 0, message.length);
        return verifier.verifySignature(signature);
    }


//    /**
//     * Signs the given message using the provided Ed25519 private key.
//     *
//     * @param message    The message to sign.
//     * @param secretKey  The 64-byte secret key (32-byte private scalar + 32-byte public key).
//     * @return The generated signature.
//     */
//    public static byte[] sign(byte[] message, byte[] secretKey) {
//        if (secretKey.length != 64) {
//            throw new IllegalArgumentException("Invalid Ed25519 private key length. Expected 64 bytes.");
//        }
//        byte[] privateScalar = Arrays.copyOfRange(secretKey, 0, 32);
//        byte[] publicKey = Arrays.copyOfRange(secretKey, 32, 64);
//
//        // Create a SHA-512 digest
//        byte[] r;
//        try {
//            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
//            sha512.update(privateScalar);
//            sha512.update(message);
//            r = sha512.digest();
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException("SHA-512 is not available", e);
//        }
//
//        // Reduce r mod L (where L is Ed25519's base order)
//        r = Ed25519.reduceModL(r);
//
//        // Compute R = r * B (where B is Ed25519's generator point)
//        byte[] R = Ed25519.scalarMultiplyBase(r);
//
//        // Compute H(R || publicKey || message)
//        byte[] h;
//        try {
//            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
//            sha512.update(R);
//            sha512.update(publicKey);
//            sha512.update(message);
//            h = sha512.digest();
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException("SHA-512 is not available", e);
//        }
//
//        // Reduce h mod L
//        h = Ed25519.reduceModL(h);
//
//        // Compute S = r + h * privateScalar mod L
//        byte[] S = Ed25519.computeS(r, h, privateScalar);
//
//        // Signature is R || S
//        byte[] signature = new byte[64];
//        System.arraycopy(R, 0, signature, 0, 32);
//        System.arraycopy(S, 0, signature, 32, 32);
//
//        return signature;
//    }
//
//    /**
//     * Signs the given message using the provided Ed25519 private key.
//     *
//     * @param message   The message to be signed.
//     * @param privateKey The private key for signing.
//     * @return The generated signature.
//     * @throws GeneralSecurityException if signing fails.
//     */
//    public static byte[] sign(byte[] message, byte[] privateKey) throws GeneralSecurityException {
//        if (privateKey.length != 64) {
//            throw new IllegalArgumentException("Invalid Ed25519 private key length. Expected 64 bytes.");
//        }
//
//        // Convert 64-byte secret key to `PrivateKey` object
//        KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
//        java.security.PrivateKey key = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(convertToPKCS8(privateKey)));
//
//        // Sign the message
//        Signature signature = Signature.getInstance("Ed25519");
//        signature.initSign(key);
//        signature.update(message);
//        return signature.sign();
//    }

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