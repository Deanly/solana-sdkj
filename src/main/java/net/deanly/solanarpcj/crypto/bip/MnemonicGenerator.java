package net.deanly.solanarpcj.crypto.bip;

import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;

public class MnemonicGenerator {

    /**
     * BIP-39 Mnemonic에서 Seed 생성 (PBKDF2-HMAC-SHA512 사용)
     * @param mnemonic 단어 리스트 (12~24 단어)
     * @param passphrase 추가 패스프레이즈 (선택사항)
     * @return 512비트(64바이트) 시드
     */
    public static byte[] toSeed(List<String> mnemonic, String passphrase) {
        String mnemonicStr = String.join(" ", mnemonic);
        String salt = "mnemonic" + passphrase;
        byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
        byte[] mnemonicBytes = mnemonicStr.getBytes(StandardCharsets.UTF_8);

        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA512Digest());
        gen.init(mnemonicBytes, saltBytes, 2048);
        return ((KeyParameter) gen.generateDerivedMacParameters(512)).getKey();
    }
}
