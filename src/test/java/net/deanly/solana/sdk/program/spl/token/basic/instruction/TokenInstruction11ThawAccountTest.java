package net.deanly.solana.sdk.program.spl.token.basic.instruction;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.spl.token.instruction.TokenInstruction11ThawAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TokenInstruction11ThawAccountTest {

    @Test
    public void testSingleOwnerThawAccountInstruction() {
        // Prepare test data
        PublicKey accountToThaw = new PublicKey("AccountToThawPublicKey");
        PublicKey mint = new PublicKey("MintPublicKey");
        PublicKey freezeAuthority = new PublicKey("FreezeAuthorityPublicKey");

        // Create instruction
        TokenInstruction11ThawAccount instruction = new TokenInstruction11ThawAccount();
        instruction.setKeys(accountToThaw, mint, freezeAuthority, null);

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(3, instruction.getKeys().size());

        Assertions.assertEquals(accountToThaw, instruction.getKeys().get(0).getPublicKey());
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
    public void testMultisigOwnerThawAccountInstruction() {
        // Prepare test data
        PublicKey accountToThaw = new PublicKey("AccountToThawPublicKey");
        PublicKey mint = new PublicKey("MintPublicKey");
        PublicKey multisigAuthority = new PublicKey("MultisigAuthorityPublicKey");
        PublicKey signer1 = new PublicKey("Signer1PublicKey");
        PublicKey signer2 = new PublicKey("Signer2PublicKey");

        // Create instruction
        TokenInstruction11ThawAccount instruction = new TokenInstruction11ThawAccount();
        instruction.setKeys(accountToThaw, mint, multisigAuthority, Arrays.asList(signer1, signer2));

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(5, instruction.getKeys().size());

        Assertions.assertEquals(accountToThaw, instruction.getKeys().get(0).getPublicKey());
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