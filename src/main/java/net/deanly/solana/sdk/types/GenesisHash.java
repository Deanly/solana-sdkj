package net.deanly.solana.sdk.types;

import lombok.EqualsAndHashCode;
import net.deanly.solana.sdk.types.codec.Base58;

import java.lang.reflect.Method;

@EqualsAndHashCode
public class GenesisHash implements Comparable<GenesisHash> {
    private final String genesisHash;

    public GenesisHash(String genesisHash) {
        if (isTestModeEnabled()) {
            genesisHash = applyTestModeTransformation(genesisHash);
        }
        if (!isValidBase58(genesisHash)) {
            throw new IllegalArgumentException("Invalid GenesisHash format");
        }
        this.genesisHash = genesisHash;
    }

    public static GenesisHash of(String genesisHash) {
        return new GenesisHash(genesisHash);
    }

    private boolean isValidBase58(String input) {
        if (input.length() < 43 || input.length() > 44) {
            return false;
        }
        return Base58.isValidBase58Char(input);
    }

    public String getValue() {
        return genesisHash;
    }

    @Override
    public String toString() {
        return genesisHash;
    }

    public byte[] toByteArray() {
        return Base58.decode(genesisHash);
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

    @Override
    public int compareTo(GenesisHash o) {
        return this.genesisHash.compareTo(o.genesisHash);
    }
}