package net.deanly.solana.sdk.program.system.account.instruction;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.core.system.instruction.SystemInstruction3CreateWithSeed;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SystemInstruction3CreateWithSeedTest {

    @Test
    void testCreateWithSeedInstructionEncoding() throws Exception {
        // Arrange: Input data for test
        PublicKey payerAccount = new PublicKey("11111111111111111111111111111111");
        PublicKey newAccount = new PublicKey("SecondPubey22222222222222222222222222222222");
        PublicKey baseAccount = new PublicKey("ThirdPubkey33333333333333333333333333333333");
        PublicKey ownerProgramId = new PublicKey("FourthPubke44444444444444444444444444444444");

        String seed = "sample-seed";
        long lamports = 1_000_000L;
        long space = 2048L;

        // Create instruction instance
        SystemInstruction3CreateWithSeed instruction = SystemInstruction3CreateWithSeed.create(
                payerAccount, newAccount, baseAccount, seed, lamports, space, ownerProgramId
        );

        assertEquals(3, instruction.getKeys().size());
        assertEquals(instruction.getKeys().get(0).getPublicKey(), payerAccount);
        assertEquals(instruction.getKeys().get(1).getPublicKey(), newAccount);
        assertEquals(instruction.getKeys().get(2).getPublicKey(), baseAccount);

        // Act: Encode the instruction
        byte[] encodedData = instruction.getData();

        // Assert: Validate encoding is correct
        assertNotNull(encodedData);
        // Calculate expected size: u32 (4) + PublicKey (32) + seed (13 with length prefix) + s64 (8) + s64 (8) + PublicKey (32)
        assertTrue(85 < encodedData.length);

        // Decode the instruction back to verify round-trip
        SystemInstruction3CreateWithSeed decodedInstruction = StructLayout.decode(
                encodedData, SystemInstruction3CreateWithSeed.class
        );

        // Validate the decoded instruction matches the original
        assertEquals(instruction.getBase(), decodedInstruction.getBase());
        assertEquals(instruction.getSeed(), decodedInstruction.getSeed());
        assertEquals(instruction.getLamports(), decodedInstruction.getLamports());
        assertEquals(instruction.getSpace(), decodedInstruction.getSpace());
        assertEquals(instruction.getProgramId(), decodedInstruction.getProgramId());
    }
}