package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.spl.token.basic.instruction.TokenInstruction10FreezeAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TokenInstruction10FreezeAccountTest {

    @Test
    public void testSingleOwnerFreezeAccountInstruction() {
        // Prepare test data
        PublicKey accountToFreeze = new PublicKey("AccountToFreezePublicKey");
        PublicKey mint = new PublicKey("MintPublicKey");
        PublicKey freezeAuthority = new PublicKey("FreezeAuthorityPublicKey");

        // Create instruction
        TokenInstruction10FreezeAccount instruction = new TokenInstruction10FreezeAccount();
        instruction.setKeys(accountToFreeze, mint, freezeAuthority, null);

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(3, instruction.getKeys().size());

        Assertions.assertEquals(accountToFreeze, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(mint, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertFalse(instruction.getKeys().get(1).isWritable());
        Assertions.assertEquals(freezeAuthority, instruction.getKeys().get(2).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(2).isSigner());

        // Verify encoded data
        byte[] encoded = instruction.getData();
        Assertions.assertNotNull(encoded);
        Assertions.assertTrue(encoded.length > 0);
    }

    @Test
    public void testMultisigOwnerFreezeAccountInstruction() {
        // Prepare test data
        PublicKey accountToFreeze = new PublicKey("AccountToFreezePublicKey");
        PublicKey mint = new PublicKey("MintPublicKey");
        PublicKey multisigAuthority = new PublicKey("MultisigAuthorityPublicKey");
        PublicKey signer1 = new PublicKey("Signer1PublicKey");
        PublicKey signer2 = new PublicKey("Signer2PublicKey");

        // Create instruction
        TokenInstruction10FreezeAccount instruction = new TokenInstruction10FreezeAccount();
        instruction.setKeys(accountToFreeze, mint, multisigAuthority, Arrays.asList(signer1, signer2));

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(5, instruction.getKeys().size());

        Assertions.assertEquals(accountToFreeze, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(mint, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertFalse(instruction.getKeys().get(1).isWritable());
        Assertions.assertEquals(multisigAuthority, instruction.getKeys().get(2).getPublicKey());
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