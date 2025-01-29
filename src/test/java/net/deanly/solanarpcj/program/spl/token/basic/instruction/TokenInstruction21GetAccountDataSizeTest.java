package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.spl.token.basic.instruction.TokenInstruction21GetAccountDataSize;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TokenInstruction21GetAccountDataSizeTest {

    @Test
    public void testSuccessfulCase() {
        // Example inputs
        PublicKey mint = new PublicKey("MintPublicKeyExample");

        // Create the instruction object
        TokenInstruction21GetAccountDataSize instruction = new TokenInstruction21GetAccountDataSize();
        instruction.setKeys(mint);

        // Validate keys setup
        List<AccountMeta> keys = instruction.getKeys();
        Assertions.assertNotNull(keys);
        Assertions.assertEquals(1, keys.size()); // Only the mint account is required.

        // Check the mint account
        AccountMeta mintMeta = keys.get(0);
        Assertions.assertEquals(mint, mintMeta.getPublicKey());
        Assertions.assertFalse(mintMeta.isWritable());
        Assertions.assertFalse(mintMeta.isSigner());

        // Check the encoded data
        byte[] data = instruction.getData();
        Assertions.assertNotNull(data);
        Assertions.assertEquals(1, data.length); // Only discriminator should be present.
        Assertions.assertEquals(21, data[0]);

    }

    @Test
    public void testEmptyKeys() {
        // Create instruction without setting keys
        TokenInstruction21GetAccountDataSize instruction = new TokenInstruction21GetAccountDataSize();

        // Check keys - should be empty (user guidance)
        List<AccountMeta> keys = instruction.getKeys();
        Assertions.assertTrue(keys.isEmpty());
    }

}