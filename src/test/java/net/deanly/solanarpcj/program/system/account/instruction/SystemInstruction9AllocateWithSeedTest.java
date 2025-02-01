package net.deanly.solanarpcj.program.system.account.instruction;

import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemInstruction9AllocateWithSeedTest {

    @Test
    void testAllocateWithSeedInstructionEncoding() throws Exception {
        // Arrange: Input data for test
        PublicKey derivedAccount = new PublicKey("11111111111111111111111111111111");
        PublicKey base = new PublicKey("SecondPubey22222222222222222222222222222222");
        String seed = "custom-seed-value";
        long space = 2048L; // Allocate 2048 bytes
        PublicKey programId = new PublicKey("ThirdPubkey33333333333333333333333333333333");

        List<AccountMeta> keys = Collections.singletonList(
                new AccountMeta(derivedAccount, true, true) // Derived account
        );

        // Create instruction instance
        SystemInstruction9AllocateWithSeed instruction = SystemInstruction9AllocateWithSeed.create(
                derivedAccount, base, seed, space, programId
        );

        assertEquals(2, instruction.getKeys().size());
        assertEquals(derivedAccount, instruction.getKeys().get(0).getPublicKey());
        assertEquals(base, instruction.getKeys().get(1).getPublicKey());

        // Act: Encode the instruction
        byte[] encodedData = instruction.getData();

        // Assert: Check encoding
        assertNotNull(encodedData);
        // Calculate minimum size: 4 bytes (instruction) + 32 bytes (base) + variable seed size +
        // 8 bytes (space) + 32 bytes (programId)
        assertTrue(encodedData.length > 76); // Approximate minimum (seed is variable)

        // Decode and verify
        SystemInstruction9AllocateWithSeed decodedInstruction = StructLayout.decode(
                encodedData, SystemInstruction9AllocateWithSeed.class
        );

        // Assert: Verify decoded data matches original
        assertEquals(instruction.getBase(), decodedInstruction.getBase());
        assertEquals(instruction.getSeed(), decodedInstruction.getSeed());
        assertEquals(instruction.getSpace(), decodedInstruction.getSpace());
        assertEquals(instruction.getProgramId(), decodedInstruction.getProgramId());
    }
}