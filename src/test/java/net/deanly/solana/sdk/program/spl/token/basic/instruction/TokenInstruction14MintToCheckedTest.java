package net.deanly.solana.sdk.program.spl.token.basic.instruction;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.spl.token.instruction.TokenInstruction14MintToChecked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TokenInstruction14MintToCheckedTest {

    @Test
    public void testSingleAuthorityMintToChecked() {
        // Prepare test data
        PublicKey destination = new PublicKey("DestinationAccountPublicKey");
        PublicKey mint = new PublicKey("MintPublicKey");
        PublicKey authority = new PublicKey("AuthorityPublicKey");
        long amount = 1_000_000L; // Mint 1,000,000 units
        int decimals = 6;         // Token decimals: 6

        // Create instruction
        TokenInstruction14MintToChecked instruction = new TokenInstruction14MintToChecked();
        instruction.setKeys(destination, mint, authority, null);
        instruction.setAmount(amount);
        instruction.setDecimals(decimals);

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(3, instruction.getKeys().size());

        Assertions.assertEquals(destination, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(mint, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(1).isWritable());
        Assertions.assertEquals(authority, instruction.getKeys().get(2).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(2).isSigner());

        // Verify encoded data
        byte[] encoded = instruction.getData();
        Assertions.assertNotNull(encoded);
        Assertions.assertTrue(encoded.length > 0);
    }

    @Test
    public void testMultisigAuthorityMintToChecked() {
        // Prepare test data
        PublicKey destination = new PublicKey("DestinationAccountPublicKey");
        PublicKey mint = new PublicKey("MintPublicKey");
        PublicKey multisig = new PublicKey("MultisigAuthorityPublicKey");
        PublicKey signer1 = new PublicKey("Signer1PublicKey");
        PublicKey signer2 = new PublicKey("Signer2PublicKey");
        long amount = 500_000L;  // Mint 500,000 units
        int decimals = 6;        // Token decimals: 6

        // Create instruction
        TokenInstruction14MintToChecked instruction = new TokenInstruction14MintToChecked();
        instruction.setKeys(destination, mint, multisig, Arrays.asList(signer1, signer2));
        instruction.setAmount(amount);
        instruction.setDecimals(decimals);

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(5, instruction.getKeys().size());

        Assertions.assertEquals(destination, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(mint, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(1).isWritable());
        Assertions.assertEquals(multisig, instruction.getKeys().get(2).getPublicKey());
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