package net.deanly.solana.sdk.types;

import lombok.EqualsAndHashCode;
import net.deanly.solana.sdk.types.codec.Base58;

import java.lang.reflect.Method;

@EqualsAndHashCode
public class Blockhash implements Comparable<Blockhash> {
    private final String blockhash;

    public Blockhash(String blockhash) {
        if (isTestModeEnabled()) {
            blockhash = applyTestModeTransformation(blockhash);
        }
        if (!isValidBase58(blockhash)) {
            throw new IllegalArgumentException("Invalid Blockhash format");
        }
        this.blockhash = blockhash;
    }

    public static Blockhash of(String blockhash) {
        return new Blockhash(blockhash);
    }

    private boolean isValidBase58(String input) {
        if (input.length() != 32 && (input.length() < 43 || input.length() > 44)) {
            return false;
        }
        return Base58.isValidBase58Char(input);
    }

    public String getValue() {
        return blockhash;
    }

    public byte[] toByteArray() {
        return Base58.decode(blockhash);
    }

    @Override
    public String toString() {
        return blockhash;
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
    public int compareTo(Blockhash other) {
        return this.blockhash.compareTo(other.blockhash);
    }
}