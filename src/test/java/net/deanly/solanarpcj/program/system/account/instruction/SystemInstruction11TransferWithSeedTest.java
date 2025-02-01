package net.deanly.solanarpcj.program.system.account.instruction;

import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemInstruction11TransferWithSeedTest {

    @Test
    void testTransferWithSeedInstructionEncoding() throws Exception {
        // Arrange: Input data for test
        PublicKey sourceAccount = new PublicKey("11111111111111111111111111111111");
        PublicKey destinationAccount = new PublicKey("SecondPubey22222222222222222222222222222222");
        PublicKey base = new PublicKey("ThirdPubkey33333333333333333333333333333333");
        String seed = "transfer-seed";
        long lamports = 1000000L;
        PublicKey programId = new PublicKey("FourthPubke44444444444444444444444444444444");

        // Create instruction instance
        SystemInstruction11TransferWithSeed instruction = SystemInstruction11TransferWithSeed.create(
                sourceAccount, base, destinationAccount, lamports, seed, programId
        );

        instruction.setKeys(sourceAccount, base, destinationAccount);

        assertEquals(3, instruction.getKeys().size());
        assertEquals(sourceAccount, instruction.getKeys().get(0).getPublicKey());
        assertEquals(base, instruction.getKeys().get(1).getPublicKey());
        assertEquals(destinationAccount, instruction.getKeys().get(2).getPublicKey());

        // Act: Encode the instruction
        byte[] encodedData = instruction.getData();

        // Assert: Check the encoding
        assertNotNull(encodedData);
        // Expected minimum size: u32 (4 bytes) + u64 (8 bytes) + 32 bytes (base) + variable seed length + 32 bytes (programId)
        assertTrue(encodedData.length > 45); // Minimum size, actual depends on seed length

        // Decode and verify
        SystemInstruction11TransferWithSeed decodedInstruction = StructLayout.decode(
                encodedData, SystemInstruction11TransferWithSeed.class
        );

        // Assert: Verify decoded matches original data
        assertEquals(instruction.getLamports(), decodedInstruction.getLamports());
        assertEquals(instruction.getSeed(), decodedInstruction.getSeed());
        assertEquals(instruction.getProgramId(), decodedInstruction.getProgramId());
    }
}