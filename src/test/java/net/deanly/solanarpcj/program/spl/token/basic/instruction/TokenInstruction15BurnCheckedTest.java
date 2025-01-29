package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.spl.token.basic.instruction.TokenInstruction15BurnChecked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TokenInstruction15BurnCheckedTest {

    @Test
    public void testSingleOwnerBurnChecked() {
        // Prepare test data
        PublicKey account = new PublicKey("TokenAccountPublicKey");
        PublicKey mint = new PublicKey("MintPublicKey");
        PublicKey owner = new PublicKey("OwnerPublicKey");
        long amount = 200_000L; // Burn 200,000 units
        int decimals = 6;       // Token decimals: 6

        // Create instruction
        TokenInstruction15BurnChecked instruction = new TokenInstruction15BurnChecked();
        instruction.setKeys(account, mint, owner, null);
        instruction.setAmount(amount);
        instruction.setDecimals(decimals);

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(3, instruction.getKeys().size());

        Assertions.assertEquals(account, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(owner, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(1).isSigner());
        Assertions.assertEquals(mint, instruction.getKeys().get(2).getPublicKey());
        Assertions.assertFalse(instruction.getKeys().get(2).isWritable());

        // Verify encoded data
        byte[] encoded = instruction.getData();
        Assertions.assertNotNull(encoded);
        Assertions.assertTrue(encoded.length > 0);
    }

    @Test
    public void testMultisigOwnerBurnChecked() {
        // Prepare test data
        PublicKey account = new PublicKey("TokenAccountPublicKey");
        PublicKey mint = new PublicKey("MintPublicKey");
        PublicKey multisig = new PublicKey("MultisigOwnerPublicKey");
        PublicKey signer1 = new PublicKey("Signer1PublicKey");
        PublicKey signer2 = new PublicKey("Signer2PublicKey");
        long amount = 100_000L;  // Burn 100,000 units
        int decimals = 6;        // Token decimals: 6

        // Create instruction
        TokenInstruction15BurnChecked instruction = new TokenInstruction15BurnChecked();
        instruction.setKeys(account, mint, multisig, Arrays.asList(signer1, signer2));
        instruction.setAmount(amount);
        instruction.setDecimals(decimals);

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(5, instruction.getKeys().size());

        Assertions.assertEquals(account, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(multisig, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertFalse(instruction.getKeys().get(1).isWritable());
        Assertions.assertEquals(mint, instruction.getKeys().get(2).getPublicKey());
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