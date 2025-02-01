package net.deanly.solana.sdk.crypto;

import lombok.Getter;
import net.deanly.solana.sdk.crypto.bip.HDKey;
import net.deanly.solana.sdk.crypto.bip.HDKeyDerivation;
import net.deanly.solana.sdk.crypto.bip.MnemonicGenerator;
import net.deanly.solana.sdk.crypto.bip.SolanaKeyDerivation;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;

import java.security.SecureRandom;
import java.util.List;

/**
 * Represents a Solana Ed25519 KeyPair, providing utility methods for
 * key generation and encoding.
 */
public class KeyPair {

    private final Ed25519PrivateKeyParameters privateKeyParams;

    @Getter
    private final PublicKey publicKey;

    /**
     * Generates a new random Ed25519 key pair.
     */
    public KeyPair() {
        SecureRandom random = new SecureRandom();
        this.privateKeyParams = new Ed25519PrivateKeyParameters(random);
        this.publicKey = new PublicKey(privateKeyParams.generatePublicKey().getEncoded());
    }

    /**
     * Creates a Solana key pair from a given 64-byte secret key.
     *
     * @param secretKey The 64-byte private key
     */
    public KeyPair(byte[] secretKey) {
        if (secretKey.length != 64) {
            throw new IllegalArgumentException("Invalid secret key length. Expected 64 bytes.");
        }
        this.privateKeyParams = new Ed25519PrivateKeyParameters(secretKey, 0);
        this.publicKey = new PublicKey(privateKeyParams.generatePublicKey().getEncoded());
    }

    public KeyPair(PrivateKey privateKey) {
        this(privateKey.getEncoded());
        privateKey.clear();
    }

    public PrivateKey getPrivateKey() {
        return new PrivateKey(privateKeyParams.getEncoded());
    }

    /**
     * Returns the full 64-byte private key (private scalar + public key).
     *
     * @return The 64-byte private key.
     */
    public byte[] toByteArray() {
        byte[] privateScalar = privateKeyParams.getEncoded(); // 32 bytes
        byte[] publicKeyBytes = publicKey.toByteArray(); // 32 bytes

        byte[] fullKey = new byte[64];
        System.arraycopy(privateScalar, 0, fullKey, 0, 32);
        System.arraycopy(publicKeyBytes, 0, fullKey, 32, 32);

        return fullKey;
    }

    public String getPublicKeyBase58() {
        return publicKey.toBase58();
    }

    public String getPrivateKeyBase58() {
        return Base58.encode(toByteArray());
    }

    public static KeyPair fromMnemonic(List<String> words, String passphrase) {
        byte[] seed = MnemonicGenerator.toSeed(words, passphrase);
        HDKey masterKey = HDKeyDerivation.generateMasterKey(seed);
        byte[] privateKey = SolanaKeyDerivation.deriveSolanaKey(masterKey.getPrivateKey(), masterKey.getChainCode());
        return new KeyPair(privateKey);
    }

    public static KeyPair fromBase58PrivateKey(String base58PrivateKey) {
        byte[] privateKey = Base58.decode(base58PrivateKey);
        return new KeyPair(privateKey);
    }

    @Override
    public String toString() {
        return "KeyPair{" +
                "publicKey=" + getPublicKeyBase58() +
                ", privateKey=****" +
                '}';
    }
}