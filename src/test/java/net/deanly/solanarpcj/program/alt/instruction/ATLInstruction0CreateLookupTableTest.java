package net.deanly.solanarpcj.program.alt.instruction;

import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.system.Sysvar;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ATLInstruction0CreateLookupTableTest {

    @Test
    public void testCreateLookupTableInstruction() {
        // Test data
        PublicKey authority = new PublicKey("Authority111111111111111111111111111111");
        PublicKey payer = new PublicKey("Payer1111111111111111111111111111111");
        long recentSlot = 12345678L;
        int bumpSeed = 255;

        // Create instruction
        ATLInstruction0CreateLookupTable instruction = new ATLInstruction0CreateLookupTable();
        instruction.setRecentSlot(recentSlot);
        instruction.setBumpSeed(bumpSeed);
        instruction.setKeys(authority, payer);

        // Validate instruction data
        byte[] encodedData = instruction.getData();
        assertNotNull(encodedData);
        ATLInstruction0CreateLookupTable decodedInstruction = StructLayout.decode(encodedData, ATLInstruction0CreateLookupTable.class);
        assertEquals(recentSlot, decodedInstruction.getRecentSlot());
        assertEquals(bumpSeed, decodedInstruction.getBumpSeed());

        // Validate keys
        assertEquals(4, instruction.getKeys().size());
        assertEquals("Derived address", instruction.getKeys().get(0).getPublicKey().toBase58()); // Replace "Derived address" appropriately
        assertTrue(instruction.getKeys().get(0).isWritable());
        assertFalse(instruction.getKeys().get(0).isSigner());

        assertEquals(authority.toBase58(), instruction.getKeys().get(1).getPublicKey().toBase58());
        assertFalse(instruction.getKeys().get(1).isWritable());
        assertTrue(instruction.getKeys().get(1).isSigner());

        assertEquals(payer.toBase58(), instruction.getKeys().get(2).getPublicKey().toBase58());
        assertTrue(instruction.getKeys().get(2).isWritable());
        assertTrue(instruction.getKeys().get(2).isSigner());

        assertEquals(Sysvar.SYSVAR_CLOCK_ADDRESS.toBase58(), instruction.getKeys().get(3).getPublicKey().toBase58());
        assertFalse(instruction.getKeys().get(3).isWritable());
        assertFalse(instruction.getKeys().get(3).isSigner());
    }
}