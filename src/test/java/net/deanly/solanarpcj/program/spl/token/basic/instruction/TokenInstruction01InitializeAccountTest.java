package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.system.Sysvar;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for TokenInstruction01InitializeAccount.
 */
class TokenInstruction01InitializeAccountTest {

    @Test
    void testInitializeAccountDataEncoding() {
        // Given
        TokenInstruction01InitializeAccount instruction = new TokenInstruction01InitializeAccount();

        PublicKey account = new PublicKey("Account11111111111111111111111111111111");
        PublicKey mint = new PublicKey("Mint11111111111111111111111111111111111");
        PublicKey owner = new PublicKey("Owner1111111111111111111111111111111111");

        instruction.setKeys(account, mint, owner, null, null);

        // When
        byte[] encodedData = instruction.getData();

        // Decode back into a new instruction for verification
        TokenInstruction01InitializeAccount decoded = StructLayout.decode(encodedData, TokenInstruction01InitializeAccount.class);

        // Then
        assertNotNull(encodedData, "Encoded data should not be null");
        assertEquals(1, encodedData[0], "First byte should be discriminator (1)");
        assertNotNull(decoded, "Decoded instruction should not be null");
        assertEquals(1, decoded.getDiscriminator(), "Decoded discriminator should be 1");
    }

    @Test
    void testSetKeysForInitializeAccount() {
        // Given
        TokenInstruction01InitializeAccount instruction = new TokenInstruction01InitializeAccount();

        PublicKey account = new PublicKey("Account11111111111111111111111111111111");
        PublicKey mint = new PublicKey("Mint11111111111111111111111111111111111");
        PublicKey owner = new PublicKey("Owner1111111111111111111111111111111111");

        // Set keys
        instruction.setKeys(account, mint, owner, null, null);

        // When
        List<AccountMeta> keys = instruction.getKeys();

        // Then
        assertNotNull(keys, "Keys should not be null");
        assertEquals(4, keys.size(), "Should have exactly 4 accounts (account, mint, owner, rent)");

        // Validate Account
        AccountMeta accountMeta = keys.get(0);
        assertEquals(account.toBase58(), accountMeta.getPublicKey().toBase58(), "Account public key mismatch");
        assertTrue(accountMeta.isWritable(), "Account should be writable");
        assertFalse(accountMeta.isSigner(), "Account should not be a signer");

        // Validate Mint
        AccountMeta mintMeta = keys.get(1);
        assertEquals(mint.toBase58(), mintMeta.getPublicKey().toBase58(), "Mint public key mismatch");
        assertFalse(mintMeta.isWritable(), "Mint should not be writable");
        assertFalse(mintMeta.isSigner(), "Mint should not be a signer");

        // Validate Owner
        AccountMeta ownerMeta = keys.get(2);
        assertEquals(owner.toBase58(), ownerMeta.getPublicKey().toBase58(), "Owner public key mismatch");
        assertFalse(ownerMeta.isWritable(), "Owner should not be writable");
        assertFalse(ownerMeta.isSigner(), "Owner should not be a signer");

        // Validate Rent Sysvar
        AccountMeta rentMeta = keys.get(3);
        assertEquals(Sysvar.SYSVAR_RENT_ADDRESS.toBase58(), rentMeta.getPublicKey().toBase58(), "Rent public key mismatch");
        assertFalse(rentMeta.isWritable(), "Rent Sysvar should not be writable");
        assertFalse(rentMeta.isSigner(), "Rent Sysvar should not be a signer");
    }

    @Test
    void testSetKeysWithIncorrectInputs() {
        // Given
        TokenInstruction01InitializeAccount instruction = new TokenInstruction01InitializeAccount();
        PublicKey account = new PublicKey("Account11111111111111111111111111111111");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            instruction.setKeys(account, null, null, null, null);
        }, "IllegalArgumentException should be thrown when inputs are invalid");

        assertNotNull(exception.getMessage(), "Exception message should not be null");
    }
}