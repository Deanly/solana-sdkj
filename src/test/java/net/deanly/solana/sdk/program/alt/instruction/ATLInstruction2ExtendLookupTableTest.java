package net.deanly.solana.sdk.program.alt.instruction;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ATLInstruction2ExtendLookupTableTest {

    @Test
    void testExtendLookupTableInstruction() {
        // Test data
        PublicKey lookupTable = new PublicKey("LookupTable111111111111111111111111111111");
        PublicKey authority = new PublicKey("Authority111111111111111111111111111111");
        PublicKey payer = new PublicKey("Payer111111111111111111111111111111111");
        List<PublicKey> addresses = List.of(
                new PublicKey("Address111111111111111111111111111111111"),
                new PublicKey("Address222222222222222222222222222222222"),
                new PublicKey("Address333333333333333333333333333333333")
        );

        // Create instruction
        ATLInstruction2ExtendLookupTable instruction = new ATLInstruction2ExtendLookupTable();
        instruction.setKeys(lookupTable, authority, payer);
        instruction.setAddresses(addresses);

        // Validate instruction data
        byte[] encodedData = instruction.getData();
        assertNotNull(encodedData);

        ATLInstruction2ExtendLookupTable decodedInstruction =
                StructLayout.decode(encodedData, ATLInstruction2ExtendLookupTable.class);

        // Validate addresses
        assertNotNull(decodedInstruction.getAddresses());
        assertEquals(3, decodedInstruction.getAddresses().size());
        assertEquals(addresses, decodedInstruction.getAddresses());

        // Validate keys
        assertEquals(4, instruction.getKeys().size());

        // Validate Lookup Table key
        assertEquals(lookupTable.toBase58(), instruction.getKeys().get(0).getPublicKey().toBase58());
        assertTrue(instruction.getKeys().get(0).isWritable());
        assertFalse(instruction.getKeys().get(0).isSigner());

        // Validate Authority key
        assertEquals(authority.toBase58(), instruction.getKeys().get(1).getPublicKey().toBase58());
        assertFalse(instruction.getKeys().get(1).isWritable());
        assertTrue(instruction.getKeys().get(1).isSigner());

        // Validate Payer key
        assertEquals(payer.toBase58(), instruction.getKeys().get(2).getPublicKey().toBase58());
        assertTrue(instruction.getKeys().get(2).isWritable());
        assertTrue(instruction.getKeys().get(2).isSigner());
    }
}