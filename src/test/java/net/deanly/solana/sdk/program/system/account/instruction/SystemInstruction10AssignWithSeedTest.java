package net.deanly.solana.sdk.program.system.account.instruction;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.core.system.instruction.SystemInstruction10AssignWithSeed;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SystemInstruction10AssignWithSeedTest {

    @Test
    void testAssignWithSeedInstructionEncoding() throws Exception {
        // Arrange: Input data for test
        PublicKey derivedAccount = new PublicKey("11111111111111111111111111111111");
        PublicKey base = new PublicKey("SecondPubey22222222222222222222222222222222");
        String seed = "unique-seed";
        PublicKey programId = new PublicKey("ThirdPubkey33333333333333333333333333333333");

        // Create instruction instance
        SystemInstruction10AssignWithSeed instruction = SystemInstruction10AssignWithSeed.create(
                derivedAccount, base, seed, programId
        );

        assertEquals(2, instruction.getKeys().size());
        assertEquals(derivedAccount, instruction.getKeys().get(0).getPublicKey());
        assertEquals(base, instruction.getKeys().get(1).getPublicKey());

        // Act: Encode the instruction
        byte[] encodedData = instruction.getData();

        // Assert: Check encoding
        assertNotNull(encodedData);
        // Expected minimum size: 4 bytes (instruction) + 32 bytes (base) + variable seed size + 32 bytes (programId)
        assertTrue(encodedData.length > 68); // Approximate size (seed is variable)

        // Decode and verify
        SystemInstruction10AssignWithSeed decodedInstruction = StructLayout.decode(
                encodedData, SystemInstruction10AssignWithSeed.class
        );

        // Assert: Verify decoded matches original
        assertEquals(instruction.getBase(), decodedInstruction.getBase());
        assertEquals(instruction.getSeed(), decodedInstruction.getSeed());
        assertEquals(instruction.getProgramId(), decodedInstruction.getProgramId());
    }
}