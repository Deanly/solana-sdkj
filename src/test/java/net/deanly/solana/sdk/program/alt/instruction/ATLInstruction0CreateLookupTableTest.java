package net.deanly.solana.sdk.program.alt.instruction;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.core.system.SystemProgram;
import net.deanly.solana.sdk.program.spl.alt.instruction.ATLInstruction0CreateLookupTable;
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
        instruction.setKeys(authority, payer, recentSlot);

        // Validate instruction data
        byte[] encodedData = instruction.getData();
        assertNotNull(encodedData);
        ATLInstruction0CreateLookupTable decodedInstruction = StructLayout.decode(encodedData, ATLInstruction0CreateLookupTable.class);
        assertEquals(recentSlot, decodedInstruction.getRecentSlot());
        assertEquals(bumpSeed, decodedInstruction.getBumpSeed());

        // Validate keys
        assertEquals(4, instruction.getKeys().size());
        assertEquals("8SukFg2JSCWfCes7XJBB6nwWdXyDuGDW53owC4QcdAXW", instruction.getKeys().get(0).getPublicKey().toBase58());
        assertTrue(instruction.getKeys().get(0).isWritable());
        assertFalse(instruction.getKeys().get(0).isSigner());

        assertEquals(authority.toBase58(), instruction.getKeys().get(1).getPublicKey().toBase58());
        assertFalse(instruction.getKeys().get(1).isWritable());
        assertTrue(instruction.getKeys().get(1).isSigner());

        assertEquals(payer.toBase58(), instruction.getKeys().get(2).getPublicKey().toBase58());
        assertTrue(instruction.getKeys().get(2).isWritable());
        assertTrue(instruction.getKeys().get(2).isSigner());

        assertEquals(SystemProgram.PROGRAM_ID, instruction.getKeys().get(3).getPublicKey());
        assertFalse(instruction.getKeys().get(3).isWritable());
        assertFalse(instruction.getKeys().get(3).isSigner());
    }
}