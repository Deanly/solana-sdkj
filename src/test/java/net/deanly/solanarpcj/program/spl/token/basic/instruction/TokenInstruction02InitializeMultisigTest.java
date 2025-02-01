package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.system.Sysvar;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for TokenInstruction02InitializeMultisig.
 */
class TokenInstruction02InitializeMultisigTest {

    @Test
    void testInitializeMultisigDataEncoding() {
        // Given
        TokenInstruction02InitializeMultisig instruction = new TokenInstruction02InitializeMultisig();

        PublicKey multisigAccount = new PublicKey("Multisig11111111111111111111111111111");
        PublicKey signer1 = new PublicKey("Signer1111111111111111111111111111111");
        PublicKey signer2 = new PublicKey("Signer2222222222222222222222222222222");
        PublicKey signer3 = new PublicKey("Signer3333333333333333333333333333333");

        instruction.setKeys(multisigAccount, List.of(signer1, signer2, signer3), Sysvar.SYSVAR_RENT_ADDRESS);

        // When
        byte[] encodedData = instruction.getData();

        // Decode back into a new instruction for verification
        TokenInstruction02InitializeMultisig decoded = StructLayout.decode(encodedData, TokenInstruction02InitializeMultisig.class);

        // Then
        assertNotNull(encodedData, "Encoded data should not be null");
        assertEquals(2, encodedData[0], "First byte should be discriminator (2)");
        assertEquals(3, decoded.getM(), "Decoded 'm' should match the required signers count");
    }

    @Test
    void testSetKeysForInitializeMultisig() {
        // Given
        TokenInstruction02InitializeMultisig instruction = new TokenInstruction02InitializeMultisig();
        PublicKey multisigAccount = new PublicKey("Multisig11111111111111111111111111111");

        PublicKey signer1 = new PublicKey("Signer1111111111111111111111111111111");
        PublicKey signer2 = new PublicKey("Signer2222222222222222222222222222222");
        PublicKey signer3 = new PublicKey("Signer3333333333333333333333333333333");

        // Set keys with multisig account and signers
        instruction.setKeys(multisigAccount, List.of(signer1, signer2, signer3), null);

        // When
        List<AccountMeta> keys = instruction.getKeys();

        // Then
        assertNotNull(keys, "Keys should not be null");
        assertEquals(5, keys.size(), "Should have exactly 5 accounts (1 multisig, 1 rent, 3 signers)");

        // Validate Multisig account
        AccountMeta multisigMeta = keys.get(0);
        assertEquals(multisigAccount.toBase58(), multisigMeta.getPublicKey().toBase58(), "Multisig account mismatch");
        assertTrue(multisigMeta.isWritable(), "Multisig account should be writable");
        assertFalse(multisigMeta.isSigner(), "Multisig account should not be a signer");

        // Validate Rent sysvar
        AccountMeta rentMeta = keys.get(1);
        assertEquals(Sysvar.SYSVAR_RENT_ADDRESS.toBase58(), rentMeta.getPublicKey().toBase58(), "Rent account mismatch");
        assertFalse(rentMeta.isWritable(), "Rent sysvar should not be writable");
        assertFalse(rentMeta.isSigner(), "Rent sysvar should not be a signer");

        // Validate Signers
        AccountMeta signer1Meta = keys.get(2);
        assertEquals(signer1.toBase58(), signer1Meta.getPublicKey().toBase58(), "Signer1 account mismatch");
        assertFalse(signer1Meta.isWritable(), "Signer1 should not be writable");
        assertTrue(signer1Meta.isSigner(), "Signer1 should be marked as signer");

        AccountMeta signer2Meta = keys.get(3);
        assertEquals(signer2.toBase58(), signer2Meta.getPublicKey().toBase58(), "Signer2 account mismatch");
        assertFalse(signer2Meta.isWritable(), "Signer2 should not be writable");
        assertTrue(signer2Meta.isSigner(), "Signer2 should be marked as signer");

        AccountMeta signer3Meta = keys.get(4);
        assertEquals(signer3.toBase58(), signer3Meta.getPublicKey().toBase58(), "Signer3 account mismatch");
        assertFalse(signer3Meta.isWritable(), "Signer3 should not be writable");
        assertTrue(signer3Meta.isSigner(), "Signer3 should be marked as signer");
    }

    @Test
    void testSetKeysWithInvalidSignerList() {
        // Given
        TokenInstruction02InitializeMultisig instruction = new TokenInstruction02InitializeMultisig();
        PublicKey multisigAccount = new PublicKey("Multisig11111111111111111111111111111");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            instruction.setKeys(multisigAccount, List.of(), null); // Pass empty signerKeys list
        });
        assertEquals("Invalid number of signer keys. Must be between 1 and 11.", exception.getMessage(), "Exception message mismatch");
    }
}