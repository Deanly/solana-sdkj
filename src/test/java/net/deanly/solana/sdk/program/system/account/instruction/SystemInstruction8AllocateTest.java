package net.deanly.solana.sdk.program.system.account.instruction;

import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemInstruction8AllocateTest {

    @Test
    void testAllocateInstructionEncoding() throws Exception {
        // Arrange: Input data for test
        PublicKey targetAccount = new PublicKey("11111111111111111111111111111111");

        List<AccountMeta> keys = Collections.singletonList(
                new AccountMeta(targetAccount, true, true) // Target account for space allocation
        );

        long space = 4096L; // Allocate 4096 bytes of space

        // Create instruction instance
        SystemInstruction8Allocate instruction = SystemInstruction8Allocate.create(
                targetAccount, space
        );

        assertEquals(1, instruction.getKeys().size());
        assertEquals(targetAccount, instruction.getKeys().get(0).getPublicKey());

        // Act: Encode the instruction
        byte[] encodedData = instruction.getData();

        // Assert: Validate encoding is correct
        assertNotNull(encodedData);
        // Expected size: u32 (4 bytes - instruction index) + s64 (8 bytes - space)
        assertEquals(12, encodedData.length);

        // Decode the instruction back to verify round-trip
        SystemInstruction8Allocate decodedInstruction = StructLayout.decode(
                encodedData, SystemInstruction8Allocate.class
        );

        // Validate the decoded instruction matches the original
        assertEquals(instruction.getSpace(), decodedInstruction.getSpace());
    }
}