package net.deanly.solana.sdk.program.spl.token.basic.instruction;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.spl.token.instruction.TokenInstruction07MintTo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TokenInstruction07MintToTest {

    @Test
    public void testSingleAuthorityMintToInstruction() {
        // Prepare test data
        PublicKey mint = new PublicKey("MintPublicKey");
        PublicKey destination = new PublicKey("DestinationPublicKey");
        PublicKey mintAuthority = new PublicKey("MintAuthorityPublicKey");
        long amountToMint = 1000;

        // Create instruction
        TokenInstruction07MintTo instruction = new TokenInstruction07MintTo();
        instruction.setAmount(amountToMint);
        instruction.setKeys(mint, destination, mintAuthority, null);

        // Verify amount
        Assertions.assertEquals(amountToMint, instruction.getAmount());

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(3, instruction.getKeys().size());
        Assertions.assertEquals(mint, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(destination, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(1).isWritable());
        Assertions.assertEquals(mintAuthority, instruction.getKeys().get(2).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(2).isSigner());

        // Verify encoded data
        byte[] encoded = instruction.getData();
        Assertions.assertNotNull(encoded);
        Assertions.assertTrue(encoded.length > 0);

        // Decode and verify
        TokenInstruction07MintTo decoded = new TokenInstruction07MintTo();
        decoded.setData(encoded);
        Assertions.assertEquals(amountToMint, decoded.getAmount());
    }

    @Test
    public void testMultisigAuthorityMintToInstruction() {
        // Prepare test data
        PublicKey mint = new PublicKey("MintPublicKey");
        PublicKey destination = new PublicKey("DestinationPublicKey");
        PublicKey multisigAuthority = new PublicKey("MultisigAuthorityPublicKey");
        PublicKey signer1 = new PublicKey("Signer1PublicKey");
        PublicKey signer2 = new PublicKey("Signer2PublicKey");
        long amountToMint = 2000;

        // Create instruction
        TokenInstruction07MintTo instruction = new TokenInstruction07MintTo();
        instruction.setAmount(amountToMint);
        instruction.setKeys(mint, destination, multisigAuthority, Arrays.asList(signer1, signer2));

        // Verify amount
        Assertions.assertEquals(amountToMint, instruction.getAmount());

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(5, instruction.getKeys().size());
        Assertions.assertEquals(mint, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(destination, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(1).isWritable());
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

        // Decode and verify
        TokenInstruction07MintTo decoded = new TokenInstruction07MintTo();
        decoded.setData(encoded);
        Assertions.assertEquals(amountToMint, decoded.getAmount());
    }
}