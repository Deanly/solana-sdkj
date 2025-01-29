package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.spl.token.basic.instruction.TokenInstruction18InitializeAccount3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TokenInstruction18InitializeAccount3Test {

    @Test
    public void testInitializeAccount3() {
        // Step 1: Define necessary inputs
        PublicKey account = new PublicKey("AccountPublicKey");
        PublicKey mint = new PublicKey("MintPublicKey");
        PublicKey owner = new PublicKey("OwnerPublicKey");

        // Step 2: Create instruction
        TokenInstruction18InitializeAccount3 instruction = new TokenInstruction18InitializeAccount3();
        instruction.setOwner(owner); // Set the account's new owner
        instruction.setKeys(account, mint); // Associate the instruction with the necessary keys

        // Step 3: Validate keys
        List<AccountMeta> keys = instruction.getKeys();
        Assertions.assertNotNull(keys);
        Assertions.assertEquals(2, keys.size());

        // Account to initialize
        AccountMeta accountMeta = keys.get(0);
        Assertions.assertEquals(account, accountMeta.getPublicKey());
        Assertions.assertTrue(accountMeta.isWritable());
        Assertions.assertFalse(accountMeta.isSigner());

        // Mint
        AccountMeta mintMeta = keys.get(1);
        Assertions.assertEquals(mint, mintMeta.getPublicKey());
        Assertions.assertFalse(mintMeta.isWritable());
        Assertions.assertFalse(mintMeta.isSigner());

        // Step 4: Validate encoded data
        byte[] encodedData = instruction.getData();
        Assertions.assertNotNull(encodedData);
        Assertions.assertTrue(encodedData.length > 0);

        // Step 5: Validate decoding data
        TokenInstruction18InitializeAccount3 decodedInstruction = new TokenInstruction18InitializeAccount3();
        decodedInstruction.setData(encodedData);

        Assertions.assertEquals(18, decodedInstruction.getDiscriminator());
        Assertions.assertEquals(owner, decodedInstruction.getOwner());
    }
}