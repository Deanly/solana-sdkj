package net.deanly.solana.sdk.program.spl.token.basic.instruction;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.spl.token.instruction.TokenInstruction05Revoke;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TokenInstruction05RevokeTest {

    @Test
    public void testSingleOwnerRevokeInstruction() {
        // Prepare test data
        PublicKey source = new PublicKey("SourcePublicKey");
        PublicKey owner = new PublicKey("OwnerPublicKey");

        // Create instruction
        TokenInstruction05Revoke instruction = new TokenInstruction05Revoke();
        instruction.setKeys(source, owner, null);

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(2, instruction.getKeys().size());
        Assertions.assertEquals(source, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(owner, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(1).isSigner());

        // Verify encoded data
        byte[] encoded = instruction.getData();
        Assertions.assertNotNull(encoded);
        Assertions.assertTrue(encoded.length > 0);

        // Decode and verify
        TokenInstruction05Revoke decoded = new TokenInstruction05Revoke();
        Assertions.assertEquals(instruction.getDiscriminator(), decoded.getDiscriminator());
    }

    @Test
    public void testMultisigOwnerRevokeInstruction() {
        // Prepare test data
        PublicKey source = new PublicKey("SourcePublicKey");
        PublicKey multisig = new PublicKey("MultisigPublicKey");
        PublicKey signer1 = new PublicKey("Signer1PublicKey");
        PublicKey signer2 = new PublicKey("Signer2PublicKey");

        // Create instruction
        TokenInstruction05Revoke instruction = new TokenInstruction05Revoke();
        instruction.setKeys(source, multisig, Arrays.asList(signer1, signer2));

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(4, instruction.getKeys().size());
        Assertions.assertEquals(source, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(multisig, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertFalse(instruction.getKeys().get(1).isWritable());
        Assertions.assertEquals(signer1, instruction.getKeys().get(2).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(2).isSigner());
        Assertions.assertEquals(signer2, instruction.getKeys().get(3).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(3).isSigner());

        // Verify encoded data
        byte[] encoded = instruction.getData();
        Assertions.assertNotNull(encoded);
        Assertions.assertTrue(encoded.length > 0);

        // Decode and verify
        TokenInstruction05Revoke decoded = new TokenInstruction05Revoke();
        Assertions.assertEquals(instruction.getDiscriminator(), decoded.getDiscriminator());
    }
}