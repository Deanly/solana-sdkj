package net.deanly.solanarpcj.crypto.bip;

import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import java.util.Arrays;

public class HDKeyDerivation {

    /**
     * BIP-32 규격에 맞는 Master Private Key 생성
     * @param seed 512비트(64바이트) 시드
     * @return (PrivateKey, ChainCode)
     */
    public static HDKey generateMasterKey(byte[] seed) {
        byte[] key = "ed25519 seed".getBytes();
        byte[] master = hmacSha512(key, seed);
        byte[] privateKey = Arrays.copyOfRange(master, 0, 32);
        byte[] chainCode = Arrays.copyOfRange(master, 32, 64);
        return new HDKey(privateKey, chainCode);
    }

    /**
     * HMAC-SHA512 함수
     */
    static byte[] hmacSha512(byte[] key, byte[] data) {
        HMac hmac = new HMac(new SHA512Digest());
        hmac.init(new KeyParameter(key));
        hmac.update(data, 0, data.length);
        byte[] out = new byte[64];
        hmac.doFinal(out, 0);
        return out;
    }
}
