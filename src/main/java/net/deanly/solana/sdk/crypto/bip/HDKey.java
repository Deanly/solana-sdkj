package net.deanly.solana.sdk.crypto.bip;


import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;

public class HDKey {

    private final byte[] privateKey; // 32바이트 Ed25519 개인키
    private final byte[] chainCode;  // 32바이트 체인 코드 (BIP-32 확장성 제공)

    /**
     * BIP-32에서 파생된 Master Private Key 와 Chain Code를 저장하는 객체
     * @param privateKey 32바이트 Ed25519 개인키
     * @param chainCode 32바이트 체인 코드
     */
    public HDKey(byte[] privateKey, byte[] chainCode) {
        if (privateKey.length != 32) {
            throw new IllegalArgumentException("Private key must be 32 bytes");
        }
        if (chainCode.length != 32) {
            throw new IllegalArgumentException("Chain code must be 32 bytes");
        }
        this.privateKey = Arrays.copyOf(privateKey, 32);
        this.chainCode = Arrays.copyOf(chainCode, 32);
    }

    /**
     * 개인키 반환 (32바이트)
     * @return Ed25519 개인키 바이트 배열
     */
    public byte[] getPrivateKey() {
        return privateKey;
    }

    /**
     * 체인 코드 반환 (32바이트)
     * @return BIP-32 체인 코드
     */
    public byte[] getChainCode() {
        return chainCode;
    }

    /**
     * Ed25519 KeyPair 객체 생성 (PublicKey 자동 계산)
     * @return Java `KeyPair` 객체
     */
    public KeyPair toKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("Ed25519");
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate Ed25519 KeyPair", e);
        }
    }

    /**
     * Ed25519 공개키를 계산하여 반환
     * @return Ed25519 공개키 (32바이트)
     */
    public byte[] getPublicKey() {
        Ed25519PrivateKeyParameters privateKeyParams = new Ed25519PrivateKeyParameters(privateKey, 0);
        Ed25519PublicKeyParameters publicKeyParams = privateKeyParams.generatePublicKey();
        return publicKeyParams.getEncoded();
    }

    @Override
    public String toString() {
        return "HDKey{" +
                "privateKey=" + Arrays.toString(privateKey) +
                ", chainCode=" + Arrays.toString(chainCode) +
                '}';
    }
}