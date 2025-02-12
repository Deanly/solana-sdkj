package net.deanly.solana.sdk.program.spl.token.basic.instruction;

import net.deanly.solana.sdk.program.spl.token.instruction.TokenInstruction20InitializeMint2;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TokenInstruction20InitializeMint2Test {

    @Test
    public void testSuccessfulCase() {
        // Example inputs
        PublicKey mint = new PublicKey("MintAccountPublicKeyExample");
        PublicKey mintAuthority = new PublicKey("MintAuthorityPublicKeyExample");
        PublicKey freezeAuthority = new PublicKey("FreezeAuthorityPublicKeyExample");
        int decimals = 8;

        // Create the instruction object
        TokenInstruction20InitializeMint2 instruction = new TokenInstruction20InitializeMint2();
        instruction.setDecimals(decimals);
        instruction.setMintAuthority(mintAuthority);
        instruction.setFreezeAuthority(freezeAuthority);
        instruction.setKeys(mint);

        // Assert the number of keys
        List<AccountMeta> keys = instruction.getKeys();
        Assertions.assertNotNull(keys);
        Assertions.assertEquals(1, keys.size()); // Only the mint account is required.

        // Check the mint account
        AccountMeta mintMeta = keys.get(0);
        Assertions.assertEquals(mint, mintMeta.getPublicKey());
        Assertions.assertTrue(mintMeta.isWritable());
        Assertions.assertFalse(mintMeta.isSigner());

        // Check the encoded data
        byte[] data = instruction.getData();
        Assertions.assertNotNull(data);
        Assertions.assertTrue(data.length > 0);

        // Decode the data
        TokenInstruction20InitializeMint2 decodedInstruction = new TokenInstruction20InitializeMint2();
        decodedInstruction.setData(data);

        // Validate decoded fields
        Assertions.assertEquals(20, decodedInstruction.getDiscriminator()); // Must match the index.
        Assertions.assertEquals(decimals, decodedInstruction.getDecimals());
        Assertions.assertEquals(mintAuthority, decodedInstruction.getMintAuthority());
        Assertions.assertEquals(freezeAuthority, decodedInstruction.getFreezeAuthority());
    }

    @Test
    public void testEmptyKeys() {
        // Create instruction without setting keys
        TokenInstruction20InitializeMint2 instruction = new TokenInstruction20InitializeMint2();

        // Check keys - should be empty (user guide)
        List<AccountMeta> keys = instruction.getKeys();
        Assertions.assertTrue(keys.isEmpty());
    }
}