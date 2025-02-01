package net.deanly.solana.sdk.program.spl.token.basic.instruction;

import net.deanly.solana.sdk.crypto.PublicKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TokenInstruction08BurnTest {

    @Test
    public void testSingleOwnerBurnInstruction() {
        // Prepare test data
        PublicKey account = new PublicKey("AccountPublicKey");
        PublicKey mint = new PublicKey("MintPublicKey");
        PublicKey owner = new PublicKey("OwnerPublicKey");
        long amountToBurn = 500;

        // Create instruction
        TokenInstruction08Burn instruction = new TokenInstruction08Burn();
        instruction.setAmount(amountToBurn);
        instruction.setKeys(account, mint, owner, null);

        // Verify amount
        Assertions.assertEquals(amountToBurn, instruction.getAmount());

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(3, instruction.getKeys().size());
        Assertions.assertEquals(account, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(mint, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(1).isWritable());
        Assertions.assertEquals(owner, instruction.getKeys().get(2).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(2).isSigner());

        // Verify encoded data
        byte[] encoded = instruction.getData();
        Assertions.assertNotNull(encoded);
        Assertions.assertTrue(encoded.length > 0);

        // Decode and verify
        TokenInstruction08Burn decoded = new TokenInstruction08Burn();
        decoded.setData(encoded);
        Assertions.assertEquals(amountToBurn, decoded.getAmount());
    }

    @Test
    public void testMultisigOwnerBurnInstruction() {
        // Prepare test data
        PublicKey account = new PublicKey("AccountPublicKey");
        PublicKey mint = new PublicKey("MintPublicKey");
        PublicKey delegate = new PublicKey("DelegatePublicKey");
        PublicKey signer1 = new PublicKey("Signer1PublicKey");
        PublicKey signer2 = new PublicKey("Signer2PublicKey");
        long amountToBurn = 1000;

        // Create instruction
        TokenInstruction08Burn instruction = new TokenInstruction08Burn();
        instruction.setAmount(amountToBurn);
        instruction.setKeys(account, mint, delegate, Arrays.asList(signer1, signer2));

        // Verify amount
        Assertions.assertEquals(amountToBurn, instruction.getAmount());

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(5, instruction.getKeys().size());
        Assertions.assertEquals(account, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(mint, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(1).isWritable());
        Assertions.assertEquals(delegate, instruction.getKeys().get(2).getPublicKey());
        Assertions.assertFalse(instruction.getKeys().get(2).isWritable());
        Assertions.assertEquals(signer1, instruction.getKeys().get(3).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(3).isSigner());
        Assertions.assertEquals(signer2, instruction.getKeys().get(4).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(4).isSigner());

        // Verify encoded data
        byte[] encoded = instruction.getData();
        Assertions.assertNotNull(encoded);
        Assertions.assertTrue(encoded.length > 0);

        // Decode and verify
        TokenInstruction08Burn decoded = new TokenInstruction08Burn();
        decoded.setData(encoded);
        Assertions.assertEquals(amountToBurn, decoded.getAmount());
    }
}