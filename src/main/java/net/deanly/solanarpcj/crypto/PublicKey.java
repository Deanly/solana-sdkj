package net.deanly.solanarpcj.crypto;

//import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.deanly.solanarpcj.crypto.Base58;
//import net.deanly.solanarpcj.crypto.json.PublicKeySerializer;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//@JsonSerialize(using = PublicKeySerializer.class)
@NoArgsConstructor
public class PublicKey {
    public static final int PUBLIC_KEY_LENGTH = 32;

    @Getter
    private Ed25519PublicKeyParameters publicKeyParams;

    public PublicKey(String pubkey) {
        byte[] decoded = Base58.decode(pubkey);
        if (decoded.length != PUBLIC_KEY_LENGTH) {
            throw new IllegalArgumentException("Invalid public key input: length must be exactly " + PUBLIC_KEY_LENGTH);
        }
        this.publicKeyParams = new Ed25519PublicKeyParameters(decoded, 0);
    }

    public PublicKey(byte[] pubkey) {
        if (pubkey.length != PUBLIC_KEY_LENGTH) {
            throw new IllegalArgumentException("Invalid public key input: length must be exactly " + PUBLIC_KEY_LENGTH);
        }
        this.publicKeyParams = new Ed25519PublicKeyParameters(pubkey, 0);
    }

    public static PublicKey readPubkey(byte[] bytes, int offset) {
        if (bytes.length < offset + PUBLIC_KEY_LENGTH) {
            throw new IllegalArgumentException("Invalid offset or insufficient bytes");
        }
        byte[] pubkeyBytes = Arrays.copyOfRange(bytes, offset, offset + PUBLIC_KEY_LENGTH);
        return new PublicKey(pubkeyBytes);
    }

    public byte[] toByteArray() {
        return publicKeyParams.getEncoded();
    }

    public String toBase58() {
        return Base58.encode(toByteArray());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PublicKey)) return false;
        PublicKey other = (PublicKey) o;
        return Arrays.equals(this.toByteArray(), other.toByteArray());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(toByteArray());
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
     * Ed25519 공개키가 타원곡선 위에 있는지 확인하는 메서드
     *
     * @param pubkey 검증할 공개키 바이트 배열
     * @return 타원곡선 위에 있으면 true, 그렇지 않으면 false
     */
    public static boolean isOnCurve(byte[] pubkey) {
        if (pubkey.length != PUBLIC_KEY_LENGTH) {
            return false;
        }
        // Ed25519에서 공개키의 마지막 바이트가 0x80 이상이면 Curve 위에 있는 것
        return (pubkey[31] & 0x80) == 0;
    }

    @Getter
    public static class ProgramDerivedAddress {
        private PublicKey address;
        private int nonce;

        public ProgramDerivedAddress(PublicKey address, int nonce) {
            this.address = address;
            this.nonce = nonce;
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
}