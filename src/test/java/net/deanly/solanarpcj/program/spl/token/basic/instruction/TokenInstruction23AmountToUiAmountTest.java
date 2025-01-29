package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.spl.token.basic.instruction.TokenInstruction23AmountToUiAmount;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Unit tests for TokenInstruction23AmountToUiAmount.
 */
class TokenInstruction23AmountToUiAmountTest {

    @Test
    void testSetKeysAndGetKeys() {
        // Arrange
        PublicKey mint = new PublicKey("MintPublicKeyExample");
        TokenInstruction23AmountToUiAmount instruction = new TokenInstruction23AmountToUiAmount();

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
        long amount = 123456789L;
        TokenInstruction23AmountToUiAmount instruction = new TokenInstruction23AmountToUiAmount();
        instruction.setAmount(amount);

        // Act
        byte[] data = instruction.getData();

        // Assert
        Assertions.assertNotNull(data);
        Assertions.assertEquals(9, data.length); // 1 byte for discriminator + 8 bytes for amount
        Assertions.assertEquals(23, data[0]); // discriminator is the first byte
        long extractedAmount = StructLayout.decode(data, TokenInstruction23AmountToUiAmount.class).getAmount();
        Assertions.assertEquals(amount, extractedAmount);
    }

    @Test
    void testSetData() {
        // Arrange
        long amount = 987654321L;
        TokenInstruction23AmountToUiAmount instruction = new TokenInstruction23AmountToUiAmount();
        instruction.setAmount(amount);

        byte[] data = instruction.getData();

        TokenInstruction23AmountToUiAmount decodedInstruction = new TokenInstruction23AmountToUiAmount();

        // Act
        decodedInstruction.setData(data);

        // Assert
        Assertions.assertEquals(amount, decodedInstruction.getAmount());
    }

    @Test
    void testInvalidKeySetting() {
        // Arrange
        TokenInstruction23AmountToUiAmount instruction = new TokenInstruction23AmountToUiAmount();

        // Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> instruction.setKeys(null));
    }
}