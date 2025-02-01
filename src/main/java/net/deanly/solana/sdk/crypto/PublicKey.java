package net.deanly.solana.sdk.crypto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.deanly.solana.sdk.program.pda.ProgramDerivedAddress;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor
public class PublicKey {
    public static final int PUBLIC_KEY_LENGTH = 32;
    public static final PublicKey DEFAULT = new PublicKey("11111111111111111111111111111111"); // None

    private final byte[] rawPublicKey = new byte[PUBLIC_KEY_LENGTH];

    @Getter
    private boolean isOnCurve;

    /**
     * Constructs a {@code PublicKey} object using a Base58-encoded string representation of the public key.
     * The string is decoded into its raw byte array representation, which is then validated and stored.
     *
     * @param pubkey A Base58-encoded string containing the public key.
     *               The decoded byte array must have a valid length and meet validation requirements.
     * @throws IllegalArgumentException If the provided string cannot be decoded or fails validation.
     */
    public PublicKey(String pubkey) {
        if (pubkey == null) {
            pubkey = DEFAULT.toBase58();
        }
        else if (isTestModeEnabled()) {
            pubkey = applyTestModeTransformation(pubkey);
        }

        byte[] decoded = Base58.decode(pubkey);
        validateAndStore(decoded);
    }

    /**
     * Constructs a {@code PublicKey} object using a byte array representation of the public key.
     *
     * @param pubkey A byte array containing the public key.
     *               The length of the array must match the expected public key length.
     * @throws IllegalArgumentException If the provided byte array does not have the required length.
     */
    public PublicKey(byte[] pubkey) {
        validateAndStore(pubkey);
    }

    private void validateAndStore(byte[] pubkey) {
        if (pubkey.length != PUBLIC_KEY_LENGTH) {
            throw new IllegalArgumentException("Invalid public key input: length must be exactly 32 bytes.");
        }
        System.arraycopy(pubkey, 0, rawPublicKey, 0, PUBLIC_KEY_LENGTH);

        this.isOnCurve = isOnCurve(pubkey);
    }

    public static PublicKey readPubkey(byte[] bytes, int offset) {
        if (bytes.length < offset + PUBLIC_KEY_LENGTH) {
            throw new IllegalArgumentException("Invalid offset or insufficient bytes");
        }
        byte[] pubkeyBytes = Arrays.copyOfRange(bytes, offset, offset + PUBLIC_KEY_LENGTH);
        return new PublicKey(pubkeyBytes);
    }

    /**
     * Converts the raw public key to a Base58-encoded string representation.
     *
     * @return A Base58-encoded string derived from the raw public key.
     */
    public String toBase58() {
        return Base58.encode(rawPublicKey);
    }

    /**
     * Converts the public key to its byte array representation.
     *
     * @return A byte array containing a clone of the raw public key.
     */
    public byte[] toByteArray() {
        return rawPublicKey.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PublicKey)) return false;
        PublicKey other = (PublicKey) o;
        return Arrays.equals(this.rawPublicKey, other.rawPublicKey);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(rawPublicKey);
    }

    @Override
    public String toString() {
        return toBase58();
    }

    public static PublicKey createProgramAddress(List<byte[]> seeds, PublicKey programId) {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            for (byte[] seed : seeds) {
                if (seed.length > 32) {
                    throw new IllegalArgumentException("Max seed length exceeded: " + seed.length);
                }
                buffer.write(seed);
            }
            buffer.write(programId.toByteArray());
            buffer.write("ProgramDerivedAddress".getBytes());

            byte[] hash = sha256(buffer.toByteArray());

            if (isOnCurve(hash)) {
                throw new IllegalStateException("Invalid seeds, address must fall off the curve");
            }

            return new PublicKey(hash);
        } catch (IOException e) {
            throw new RuntimeException("Error creating program address", e);
        }
    }

    public static PublicKey createWithSeed(PublicKey fromPublicKey, String seed, PublicKey programId) {
        return createWithSeed(fromPublicKey, Base58.decode(seed), programId);
    }

    public static PublicKey createWithSeed(PublicKey fromPublicKey, byte[] seed, PublicKey programId) {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (seed.length > 32) {
                throw new IllegalArgumentException("Max seed length exceeded: " + seed.length);
            }

            buffer.write(fromPublicKey.toByteArray());
            buffer.write(seed);
            buffer.write(programId.toByteArray());

            byte[] hash = sha256(buffer.toByteArray());
            return new PublicKey(hash);
        } catch (IOException e) {
            throw new RuntimeException("Error creating program address", e);
        }
    }

    public static byte[] sha256(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Validates whether a given byte array represents a public key that resides on the Ed25519 curve.
     *
     * @param publicKeyBytes A byte array of the public key to be validated.
     *                       It must have a length equal to the defined PUBLIC_KEY_LENGTH.
     * @return {@code true} if the provided byte array corresponds to a valid public key on the Ed25519 curve,
     *         {@code false} otherwise.
     */
    // https://docs.rs/solana-pubkey/2.1.11/src/solana_pubkey/lib.rs.html#778
    public static boolean isOnCurve(byte[] publicKeyBytes) {
        if (publicKeyBytes.length != PUBLIC_KEY_LENGTH) {
            return false;
        }

        try {
            // BouncyCastle의 Ed25519 공개 키 파라미터를 사용하여 복원 시도
            new Ed25519PublicKeyParameters(publicKeyBytes, 0);
            return true; // 정상적으로 복원되면 Ed25519 Curve 위에 있는 것
        } catch (Exception e) {
            return false; // 복원 실패 시, Ed25519 Curve 위에 있지 않음
        }
    }

    public static ProgramDerivedAddress findProgramAddress(List<byte[]> seeds, PublicKey programId) {
        for (int nonce = 255; nonce >= 0; nonce--) {
            try {
                List<byte[]> seedsWithNonce = new ArrayList<>(seeds);
                seedsWithNonce.add(new byte[]{(byte) nonce});
                PublicKey address = createProgramAddress(seedsWithNonce, programId);
                return new ProgramDerivedAddress(address, nonce);
            } catch (IllegalStateException e) {
                // Address was on the curve, try next nonce
            }
        }
        throw new IllegalStateException("Unable to find a viable program address nonce");
    }

    public static PublicKey valueOf(String publicKey) {
        return new PublicKey(publicKey);
    }

    private String applyTestModeTransformation(String pubkey) {
        try {
            // Reflectively access the PublicKeyGenerator.createDummyPublicKey method
            Class<?> generatorClass = Class.forName("net.deanly.solana.sdk.crypto.PublicKeyGenerator");
            Method generateMethod = generatorClass.getDeclaredMethod("createDummyPublicKey", String.class);

            return (String) generateMethod.invoke(null, pubkey);
        } catch (Exception e) {
            System.out.println("Failed to apply test mode transformation: " + e.getMessage());
            return pubkey;
        }
    }

    private static Boolean isTestMode = null;
    private static boolean isTestModeEnabled() {
        if (isTestMode == null) {
            // Perform reflection-based check to determine if the test class exists in the runtime
            try {
                Class.forName("net.deanly.solana.sdk.crypto.PublicKeyGenerator");
                isTestMode = true; // Class exists, indicating test mode
            } catch (ClassNotFoundException e) {
                isTestMode = false; // Class does not exist, normal mode
            }
        }
        return isTestMode;
    }
}