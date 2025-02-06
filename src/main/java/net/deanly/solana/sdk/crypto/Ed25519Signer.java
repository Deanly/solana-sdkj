package net.deanly.solana.sdk.crypto;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;

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

}