package net.deanly.solana.sdk.program.spl.token.basic.instruction;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.system.Sysvar;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TokenInstruction16InitializeAccount2Test {

    @Test
    public void testInitializeAccount2() {
        // Step 1: Prepare necessary data
        PublicKey account = new PublicKey("AccountPublicKey");
        PublicKey mint = new PublicKey("MintPublicKey");
        PublicKey owner = new PublicKey("OwnerPublicKey");

        // Step 2: Create instruction
        TokenInstruction16InitializeAccount2 instruction = new TokenInstruction16InitializeAccount2();
        instruction.setOwner(owner);  // Set the owner
        instruction.setKeys(account, mint, null);  // Set account and mint

        // Step 3: Verify metadata (keys)
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(3, instruction.getKeys().size());

        // Account to initialize
        Assertions.assertEquals(account, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertFalse(instruction.getKeys().get(0).isSigner());

        // Mint
        Assertions.assertEquals(mint, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertFalse(instruction.getKeys().get(1).isWritable());
        Assertions.assertFalse(instruction.getKeys().get(1).isSigner());

        // Rent Sysvar
        Assertions.assertEquals(Sysvar.SYSVAR_RENT_ADDRESS, instruction.getKeys().get(2).getPublicKey());
        Assertions.assertFalse(instruction.getKeys().get(2).isWritable());
        Assertions.assertFalse(instruction.getKeys().get(2).isSigner());

        // Step 4: Verify encoded data
        byte[] encodedData = instruction.getData();
        Assertions.assertNotNull(encodedData);
        Assertions.assertTrue(encodedData.length > 0);

        // Step 5: Decode data to verify it
        TokenInstruction16InitializeAccount2 decodedInstruction = new TokenInstruction16InitializeAccount2();
        decodedInstruction.setData(encodedData);
        Assertions.assertEquals(owner, decodedInstruction.getOwner());
    }
}