package net.deanly.solana.sdk.program.spl.token.basic.instruction;

import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

class TokenInstruction03TransferTest {

    @Test
    void testSetKeysForSingleOwner() {
        // Arrange
        PublicKey source = new PublicKey("Source111111111111111111111111111111111");
        PublicKey destination = new PublicKey("Destination1111111111111111111111111");
        PublicKey ownerDelegate = new PublicKey("Owner111111111111111111111111111111111");
        TokenInstruction03Transfer instruction = new TokenInstruction03Transfer();

        // Act
        instruction.setKeys(source, destination, ownerDelegate, null);

        // Assert
        List<AccountMeta> keys = instruction.getKeys();
        assertEquals(3, keys.size());
        assertEquals(source, keys.get(0).getPublicKey());
        assertTrue(keys.get(0).isWritable());
        assertFalse(keys.get(0).isSigner());

        assertEquals(destination, keys.get(1).getPublicKey());
        assertTrue(keys.get(1).isWritable());
        assertFalse(keys.get(1).isSigner());

        assertEquals(ownerDelegate, keys.get(2).getPublicKey());
        assertFalse(keys.get(2).isWritable());
        assertTrue(keys.get(2).isSigner());
    }

    @Test
    void testSetKeysForMultisigOwner() {
        // Arrange
        PublicKey source = new PublicKey("Source222222222222222222222222222222222");
        PublicKey destination = new PublicKey("Destination2222222222222222222222222");
        PublicKey delegate = new PublicKey("Delegate2222222222222222222222222222222");
        List<PublicKey> signers = Arrays.asList(
                new PublicKey("Signer111111111111111111111111111111111"),
                new PublicKey("Signer222222222222222222222222222222222")
        );
        TokenInstruction03Transfer instruction = new TokenInstruction03Transfer();

        // Act
        instruction.setKeys(source, destination, delegate, signers);

        // Assert
        List<AccountMeta> keys = instruction.getKeys();
        assertEquals(5, keys.size()); // Source, destination, delegate, and 2 signers

        assertEquals(source, keys.get(0).getPublicKey());
        assertTrue(keys.get(0).isWritable());
        assertFalse(keys.get(0).isSigner());

        assertEquals(destination, keys.get(1).getPublicKey());
        assertTrue(keys.get(1).isWritable());
        assertFalse(keys.get(1).isSigner());

        assertEquals(delegate, keys.get(2).getPublicKey());
        assertFalse(keys.get(2).isWritable());
        assertFalse(keys.get(2).isSigner());

        assertEquals(signers.get(0), keys.get(3).getPublicKey());
        assertFalse(keys.get(3).isWritable());
        assertTrue(keys.get(3).isSigner());

        assertEquals(signers.get(1), keys.get(4).getPublicKey());
        assertFalse(keys.get(4).isWritable());
        assertTrue(keys.get(4).isSigner());
    }

    @Test
    void testGetDataEncoding() {
        // Arrange
        TokenInstruction03Transfer instruction = new TokenInstruction03Transfer();
        instruction.setAmount(1000L);

        PublicKey source = new PublicKey("Source111111111111111111111111111111111");
        PublicKey destination = new PublicKey("Destination1111111111111111111111111");
        PublicKey ownerDelegate = new PublicKey("Owner111111111111111111111111111111111");
        instruction.setKeys(source, destination, ownerDelegate, null);

        // Act
        byte[] encodedData = instruction.getData();

        // Assert
        assertNotNull(encodedData);
        assertEquals(9, encodedData.length); // Discriminator (1 byte) + Amount (8 bytes)
        assertEquals(3, encodedData[0]); // Discriminator should be 3
        assertEquals(1000L, StructLayout.decode(encodedData, TokenInstruction03Transfer.class).getAmount());
    }

    @Test
    void testSetDataDecoding() {
        // Arrange
        TokenInstruction03Transfer instruction = new TokenInstruction03Transfer();
        byte[] encodedData = new byte[] {
                3,                     // Discriminator
                -24, 3, 0, 0, 0, 0, 0, 0 // Amount = 1000 (in little-endian)
        };

        // Act
        instruction.setData(encodedData);

        // Assert
        assertEquals(3, instruction.getDiscriminator());
        assertEquals(1000L, instruction.getAmount());
    }
}