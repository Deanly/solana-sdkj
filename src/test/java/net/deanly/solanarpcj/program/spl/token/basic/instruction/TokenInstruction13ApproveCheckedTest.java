package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.spl.token.basic.instruction.TokenInstruction13ApproveChecked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TokenInstruction13ApproveCheckedTest {

    @Test
    public void testSingleOwnerApproveChecked() {
        // Prepare test data
        PublicKey source = new PublicKey("SourceAccountPublicKey");
        PublicKey mint = new PublicKey("MintPublicKey");
        PublicKey delegate = new PublicKey("DelegateAccountPublicKey");
        PublicKey owner = new PublicKey("OwnerPublicKey");
        long amount = 1_000_000L; // Amount to approve: 1,000,000 units
        int decimals = 6;         // Token decimals: 6

        // Create instruction
        TokenInstruction13ApproveChecked instruction = new TokenInstruction13ApproveChecked();
        instruction.setKeys(source, mint, delegate, owner, null);
        instruction.setAmount(amount);
        instruction.setDecimals(decimals);

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(4, instruction.getKeys().size());

        Assertions.assertEquals(source, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(mint, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertFalse(instruction.getKeys().get(1).isWritable());
        Assertions.assertEquals(delegate, instruction.getKeys().get(2).getPublicKey());
        Assertions.assertFalse(instruction.getKeys().get(2).isWritable());
        Assertions.assertEquals(owner, instruction.getKeys().get(3).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(3).isSigner());

        // Verify encoded data
        byte[] encoded = instruction.getData();
        Assertions.assertNotNull(encoded);
        Assertions.assertTrue(encoded.length > 0);
    }

    @Test
    public void testMultisigOwnerApproveChecked() {
        // Prepare test data
        PublicKey source = new PublicKey("SourceAccountPublicKey");
        PublicKey mint = new PublicKey("MintPublicKey");
        PublicKey delegate = new PublicKey("DelegateAccountPublicKey");
        PublicKey multisig = new PublicKey("MultisigOwnerPublicKey");
        PublicKey signer1 = new PublicKey("Signer1PublicKey");
        PublicKey signer2 = new PublicKey("Signer2PublicKey");
        long amount = 500_000L;  // Approve 500,000 units
        int decimals = 6;        // Token decimals: 6

        // Create instruction
        TokenInstruction13ApproveChecked instruction = new TokenInstruction13ApproveChecked();
        instruction.setKeys(source, mint, delegate, multisig, Arrays.asList(signer1, signer2));
        instruction.setAmount(amount);
        instruction.setDecimals(decimals);

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(6, instruction.getKeys().size());

        Assertions.assertEquals(source, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(mint, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertFalse(instruction.getKeys().get(1).isWritable());
        Assertions.assertEquals(delegate, instruction.getKeys().get(2).getPublicKey());
        Assertions.assertFalse(instruction.getKeys().get(2).isWritable());
        Assertions.assertEquals(multisig, instruction.getKeys().get(3).getPublicKey());
        Assertions.assertFalse(instruction.getKeys().get(3).isWritable());
        Assertions.assertEquals(signer1, instruction.getKeys().get(4).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(4).isSigner());
        Assertions.assertEquals(signer2, instruction.getKeys().get(5).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(5).isSigner());

        // Verify encoded data
        byte[] encoded = instruction.getData();
        Assertions.assertNotNull(encoded);
        Assertions.assertTrue(encoded.length > 0);
    }
}