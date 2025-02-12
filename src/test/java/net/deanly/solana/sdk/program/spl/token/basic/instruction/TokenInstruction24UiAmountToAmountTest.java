package net.deanly.solana.sdk.program.spl.token.basic.instruction;

import net.deanly.solana.sdk.program.spl.token.instruction.TokenInstruction24UiAmountToAmount;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Unit tests for TokenInstruction24UiAmountToAmount.
 */
class TokenInstruction24UiAmountToAmountTest {

    @Test
    void testSetKeysAndGetKeys() {
        // Arrange
        PublicKey mint = new PublicKey("AnotherMintPublicKeyExample");
        TokenInstruction24UiAmountToAmount instruction = new TokenInstruction24UiAmountToAmount();

        // Act
        instruction.setKeys(mint);
        List<AccountMeta> keys = instruction.getKeys();

        // Assert
        Assertions.assertNotNull(keys);
        Assertions.assertEquals(1, keys.size());
        Assertions.assertEquals(mint, keys.get(0).getPublicKey());
        Assertions.assertFalse(keys.get(0).isSigner());
        Assertions.assertFalse(keys.get(0).isWritable());
    }

    @Test
    void testGetData() {
        // Arrange
        String uiAmount = "1234.5678";
        TokenInstruction24UiAmountToAmount instruction = new TokenInstruction24UiAmountToAmount();
        instruction.setUiAmount(uiAmount);

        // Act
        byte[] data = instruction.getData();

        // Assert
        Assertions.assertNotNull(data);
        Assertions.assertTrue(data.length > 1); // Minimum length: discriminator + UTF-8 encoded data
        Assertions.assertEquals(24, data[0]); // The first byte must be the discriminator
        String extractedUiAmount = StructLayout.decode(data, TokenInstruction24UiAmountToAmount.class).getUiAmount();
        Assertions.assertEquals(uiAmount, extractedUiAmount);
    }

    @Test
    void testSetData() {
        // Arrange
        String uiAmount = "9876.5432";
        TokenInstruction24UiAmountToAmount instruction = new TokenInstruction24UiAmountToAmount();
        instruction.setUiAmount(uiAmount);

        byte[] data = instruction.getData();
        TokenInstruction24UiAmountToAmount decodedInstruction = new TokenInstruction24UiAmountToAmount();

        // Act
        decodedInstruction.setData(data);

        // Assert
        Assertions.assertEquals(uiAmount, decodedInstruction.getUiAmount());
    }

    @Test
    void testInvalidKeySetting() {
        // Arrange
        TokenInstruction24UiAmountToAmount instruction = new TokenInstruction24UiAmountToAmount();

        // Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> instruction.setKeys(null));
    }
}