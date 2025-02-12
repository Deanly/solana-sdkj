package net.deanly.solana.sdk.program.spl.token.basic.instruction;

import net.deanly.solana.sdk.program.spl.token.instruction.TokenInstruction22InitializeImmutableOwner;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TokenInstruction22InitializeImmutableOwnerTest {

    @Test
    public void testSuccessfulCase() {
        // Example inputs
        PublicKey account = new PublicKey("TokenAccountPublicKeyExample");

        // Create the instruction object
        TokenInstruction22InitializeImmutableOwner instruction = new TokenInstruction22InitializeImmutableOwner();
        instruction.setKeys(account);

        // Validate keys setup
        List<AccountMeta> keys = instruction.getKeys();
        Assertions.assertNotNull(keys);
        Assertions.assertEquals(1, keys.size()); // Only the token account is required.

        // Check the token account
        AccountMeta accountMeta = keys.get(0);
        Assertions.assertEquals(account, accountMeta.getPublicKey());
        Assertions.assertTrue(accountMeta.isWritable());
        Assertions.assertFalse(accountMeta.isSigner());

        // Check the encoded data
        byte[] data = instruction.getData();
        Assertions.assertNotNull(data);
        Assertions.assertEquals(1, data.length); // Only discriminator should be present.
        Assertions.assertEquals(22, data[0]);

    }

    @Test
    public void testEmptyKeys() {
        // Create instruction without setting keys
        TokenInstruction22InitializeImmutableOwner instruction = new TokenInstruction22InitializeImmutableOwner();

        // Check keys - should be empty (user guidance)
        List<AccountMeta> keys = instruction.getKeys();
        Assertions.assertTrue(keys.isEmpty());
    }

}