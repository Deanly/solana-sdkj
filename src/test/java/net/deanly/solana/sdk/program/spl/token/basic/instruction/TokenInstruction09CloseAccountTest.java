package net.deanly.solana.sdk.program.spl.token.basic.instruction;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.spl.token.instruction.TokenInstruction09CloseAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TokenInstruction09CloseAccountTest {

    @Test
    public void testSingleOwnerCloseAccountInstruction() {
        // Prepare test data
        PublicKey accountToClose = new PublicKey("AccountToClosePublicKey");
        PublicKey destination = new PublicKey("DestinationAccountPublicKey");
        PublicKey owner = new PublicKey("OwnerPublicKey");

        // Create instruction
        TokenInstruction09CloseAccount instruction = new TokenInstruction09CloseAccount();
        instruction.setKeys(accountToClose, destination, owner, null);

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(3, instruction.getKeys().size());

        Assertions.assertEquals(accountToClose, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(destination, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(1).isWritable());
        Assertions.assertEquals(owner, instruction.getKeys().get(2).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(2).isSigner());

        // Verify encoded data
        byte[] encoded = instruction.getData();
        Assertions.assertNotNull(encoded);
        Assertions.assertTrue(encoded.length > 0);
    }

    @Test
    public void testMultisigOwnerCloseAccountInstruction() {
        // Prepare test data
        PublicKey accountToClose = new PublicKey("AccountToClosePublicKey");
        PublicKey destination = new PublicKey("DestinationAccountPublicKey");
        PublicKey delegate = new PublicKey("MultisigDelegatePublicKey");
        PublicKey signer1 = new PublicKey("Signer1PublicKey");
        PublicKey signer2 = new PublicKey("Signer2PublicKey");

        // Create instruction
        TokenInstruction09CloseAccount instruction = new TokenInstruction09CloseAccount();
        instruction.setKeys(accountToClose, destination, delegate, Arrays.asList(signer1, signer2));

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(5, instruction.getKeys().size());

        Assertions.assertEquals(accountToClose, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(destination, instruction.getKeys().get(1).getPublicKey());
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
    }
}