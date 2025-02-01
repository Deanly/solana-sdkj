package net.deanly.solana.sdk.crypto.bip;

import java.util.Arrays;

public class SolanaKeyDerivation {

    /**
     * Solana BIP-44 Key 파생 (`m/501H/0H/0/0`)
     */
    public static byte[] deriveSolanaKey(byte[] masterPrivateKey, byte[] chainCode) {
        int[] path = {501 | 0x80000000, 0 | 0x80000000, 0 | 0x80000000, 0};  // Solana Path
        byte[] privateKey = masterPrivateKey;
        byte[] cc = chainCode;

        for (int i : path) {
            byte[] index = new byte[4];
            index[0] = (byte) (i >> 24);
            index[1] = (byte) (i >> 16);
            index[2] = (byte) (i >> 8);
            index[3] = (byte) (i);
            byte[] data = concat(new byte[]{0x00}, privateKey, index);
            byte[] derived = HDKeyDerivation.hmacSha512(cc, data);
            privateKey = Arrays.copyOfRange(derived, 0, 32);
            cc = Arrays.copyOfRange(derived, 32, 64);
        }
        return privateKey;
    }

    /**
     * 배열 합치기
     */
    private static byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] arr : arrays) {
            length += arr.length;
        }
        byte[] result = new byte[length];
        int pos = 0;
        for (byte[] arr : arrays) {
            System.arraycopy(arr, 0, result, pos, arr.length);
            pos += arr.length;
        }
        return result;
    }
}