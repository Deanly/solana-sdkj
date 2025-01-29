package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.program.system.Sysvar;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for `TokenInstruction01InitializeMint`.
 */
class TokenInstruction00InitializeMintTest {

    @Test
    void testInitializeMintEncodingAndDecoding() {
        // Given
        PublicKey mintPubkey = new PublicKey("MintPublicKey111111111111111111111111111111");
        PublicKey mintAuthority = new PublicKey("MintAuthority1111111111111111111111111111");
        PublicKey freezeAuthority = new PublicKey("FreezeAuthority111111111111111111111111");

        TokenInstruction00InitializeMint instruction = new TokenInstruction00InitializeMint();
        instruction.setDecimals(9);
        instruction.setMintAuthority(mintAuthority);
        instruction.setFreezeAuthority(freezeAuthority);
        instruction.setKeys(mintPubkey, Sysvar.SYSVAR_RENT_ADDRESS);

        // When (Encode data)
        byte[] encodedData = instruction.getData();

        // Then (Validate encoding)
        assertNotNull(encodedData, "Encoded data should not be null");
        assertEquals(36, encodedData.length, "Encoded data length should match expected size");

        TokenInstruction00InitializeMint decoded = StructLayout.decode(encodedData, TokenInstruction00InitializeMint.class);

        // Confirm decoded values match original instruction
        assertEquals(9, decoded.getDecimals());
        assertEquals(mintAuthority.toBase58(), decoded.getMintAuthority().toBase58());
        assertEquals(freezeAuthority.toBase58(), decoded.getFreezeAuthority().toBase58());
    }

    @Test
    void testGetKeys() {
        // Given
        PublicKey mintPubkey = new PublicKey("MintPublicKey111111111111111111111111111111");
        TokenInstruction00InitializeMint instruction = new TokenInstruction00InitializeMint();
        instruction.setKeys(mintPubkey, Sysvar.SYSVAR_RENT_ADDRESS);

        // When
        List<AccountMeta> keys = instruction.getKeys();

        // Then
        assertNotNull(keys, "Keys list should not be null");
        assertEquals(2, keys.size(), "Keys list should contain exactly 2 account entries");

        AccountMeta mintAccount = keys.get(0);
        assertEquals(mintPubkey, mintAccount.getPublicKey(), "Mint account PublicKey should match");
        assertTrue(mintAccount.isWritable(), "Mint account should be writable");
        assertFalse(mintAccount.isSigner(), "Mint account should not be a signer");

        AccountMeta rentSysvar = keys.get(1);
        assertEquals(Sysvar.SYSVAR_RENT_ADDRESS, rentSysvar.getPublicKey(), "Rent Sysvar PublicKey should match");
        assertFalse(rentSysvar.isWritable(), "Rent Sysvar should not be writable");
        assertFalse(rentSysvar.isSigner(), "Rent Sysvar should not be a signer");
    }
}