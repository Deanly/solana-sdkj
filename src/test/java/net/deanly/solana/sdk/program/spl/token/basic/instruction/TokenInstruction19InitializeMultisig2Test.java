package net.deanly.solana.sdk.program.spl.token.basic.instruction;

import net.deanly.solana.sdk.program.spl.token.instruction.TokenInstruction19InitializeMultisig2;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class TokenInstruction19InitializeMultisig2Test {

    @Test
    public void testSuccessfulCase() {
        // Example inputs
        PublicKey multisigAccount = new PublicKey("MultisigAccountPublicKey");
        PublicKey signer1 = new PublicKey("Signer1PublicKey");
        PublicKey signer2 = new PublicKey("Signer2PublicKey");
        PublicKey signer3 = new PublicKey("Signer3PublicKey");
        List<PublicKey> signers = Arrays.asList(signer1, signer2, signer3);

        // Create the instruction object
        TokenInstruction19InitializeMultisig2 instruction = new TokenInstruction19InitializeMultisig2();
        instruction.setKeys(multisigAccount, signers); // Set the keys

        // Assert the number of keys
        List<AccountMeta> keys = instruction.getKeys();
        Assertions.assertNotNull(keys);
        Assertions.assertEquals(4, keys.size()); // 1 multisig + 3 signers

        // Check the multisig account
        AccountMeta multisigMeta = keys.get(0);
        Assertions.assertEquals(multisigAccount, multisigMeta.getPublicKey());
        Assertions.assertTrue(multisigMeta.isWritable());
        Assertions.assertFalse(multisigMeta.isSigner());

        // Check the signers
        for (int i = 0; i < signers.size(); i++) {
            AccountMeta signerMeta = keys.get(i + 1);
            Assertions.assertEquals(signers.get(i), signerMeta.getPublicKey());
            Assertions.assertFalse(signerMeta.isWritable());
            Assertions.assertTrue(signerMeta.isSigner());
        }

        // Check the encoded data
        byte[] data = instruction.getData();
        Assertions.assertNotNull(data);
        Assertions.assertTrue(data.length > 0);
    }

    @Test
    public void testInvalidSignersCount() {
        // Example inputs
        PublicKey multisigAccount = new PublicKey("MultisigAccountPublicKey");

        // Signer list with 12 signers, exceeding the limit (max 11)
        List<PublicKey> signers = Arrays.asList(
                new PublicKey("Signer1PublicKey"), new PublicKey("Signer2PublicKey"),
                new PublicKey("Signer3PublicKey"), new PublicKey("Signer4PublicKey"),
                new PublicKey("Signer5PublicKey"), new PublicKey("Signer6PublicKey"),
                new PublicKey("Signer7PublicKey"), new PublicKey("Signer8PublicKey"),
                new PublicKey("Signer9PublicKey"), new PublicKey("Signer10PublicKey"),
                new PublicKey("Signer11PublicKey"), new PublicKey("Signer12PublicKey")
        );

        TokenInstruction19InitializeMultisig2 instruction = new TokenInstruction19InitializeMultisig2();

        // Expect exception due to too many signers
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            instruction.setKeys(multisigAccount, signers);
        });
    }
}