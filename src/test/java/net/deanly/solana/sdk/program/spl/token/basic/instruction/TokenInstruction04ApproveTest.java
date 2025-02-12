package net.deanly.solana.sdk.program.spl.token.basic.instruction;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.spl.token.instruction.TokenInstruction04Approve;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TokenInstruction04ApproveTest {

    @Test
    public void testSingleOwnerApproveInstruction() {
        // Prepare test data
        PublicKey source = new PublicKey("SourcePublicKey");
        PublicKey delegate = new PublicKey("DelegatePublicKey");
        PublicKey owner = new PublicKey("OwnerPublicKey");
        long amount = 500;

        // Create instruction
        TokenInstruction04Approve instruction = new TokenInstruction04Approve();
        instruction.setAmount(amount);
        instruction.setKeys(source, delegate, owner, null);

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(3, instruction.getKeys().size());
        Assertions.assertEquals(source, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(delegate, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertFalse(instruction.getKeys().get(1).isWritable());
        Assertions.assertEquals(owner, instruction.getKeys().get(2).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(2).isSigner());

        // Encode and Decode
        byte[] encoded = instruction.getData();
        TokenInstruction04Approve decoded = new TokenInstruction04Approve();
        decoded.setData(encoded);

        // Verify data integrity
        Assertions.assertEquals(instruction.getDiscriminator(), decoded.getDiscriminator());
        Assertions.assertEquals(instruction.getAmount(), decoded.getAmount());
    }

    @Test
    public void testMultisigOwnerApproveInstruction() {
        // Prepare test data
        PublicKey source = new PublicKey("SourceMultisigPublicKey");
        PublicKey delegate = new PublicKey("DelegateMultisigPublicKey");
        PublicKey multisig = new PublicKey("MultisigOwnerPublicKey");
        PublicKey signer1 = new PublicKey("Signer1PublicKey");
        PublicKey signer2 = new PublicKey("Signer2PublicKey");
        long amount = 1000;

        // Create instruction
        TokenInstruction04Approve instruction = new TokenInstruction04Approve();
        instruction.setAmount(amount);
        instruction.setKeys(source, delegate, multisig, Arrays.asList(signer1, signer2));

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(5, instruction.getKeys().size());
        Assertions.assertEquals(source, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(delegate, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertFalse(instruction.getKeys().get(1).isWritable());
        Assertions.assertEquals(multisig, instruction.getKeys().get(2).getPublicKey());
        Assertions.assertFalse(instruction.getKeys().get(2).isWritable());
        Assertions.assertEquals(signer1, instruction.getKeys().get(3).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(3).isSigner());
        Assertions.assertEquals(signer2, instruction.getKeys().get(4).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(4).isSigner());

        // Encode and Decode
        byte[] encoded = instruction.getData();
        TokenInstruction04Approve decoded = new TokenInstruction04Approve();
        decoded.setData(encoded);

        // Verify data integrity
        Assertions.assertEquals(instruction.getDiscriminator(), decoded.getDiscriminator());
        Assertions.assertEquals(instruction.getAmount(), decoded.getAmount());
    }
}