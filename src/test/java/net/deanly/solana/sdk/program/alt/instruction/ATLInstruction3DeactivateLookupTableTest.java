package net.deanly.solana.sdk.program.alt.instruction;

import net.deanly.structlayout.StructLayout;
import net.deanly.solana.sdk.crypto.PublicKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ATLInstruction3DeactivateLookupTableTest {

    @Test
    void testDeactivateLookupTableInstruction() {
        // Test data
        PublicKey lookupTable = new PublicKey("LookupTable111111111111111111111111111111");
        PublicKey authority = new PublicKey("Authority111111111111111111111111111111");

        // Create instruction
        ATLInstruction3DeactivateLookupTable instruction = new ATLInstruction3DeactivateLookupTable();
        instruction.setKeys(lookupTable, authority);

        // Validate instruction data
        byte[] encodedData = instruction.getData();
        assertNotNull(encodedData);
        ATLInstruction3DeactivateLookupTable decodedInstruction = StructLayout.decode(encodedData, ATLInstruction3DeactivateLookupTable.class);

        // Validate keys
        assertEquals(2, instruction.getKeys().size());

        // LookupTable key validation
        assertEquals(lookupTable.toBase58(), instruction.getKeys().get(0).getPublicKey().toBase58());
        assertTrue(instruction.getKeys().get(0).isWritable());
        assertFalse(instruction.getKeys().get(0).isSigner());

        // Authority key validation
        assertEquals(authority.toBase58(), instruction.getKeys().get(1).getPublicKey().toBase58());
        assertFalse(instruction.getKeys().get(1).isWritable());
        assertTrue(instruction.getKeys().get(1).isSigner());
    }
}