package net.deanly.solanarpcj.program.system.account.instruction;

import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemInstruction2TransferTest {

    @Test
    void testTransferInstructionEncoding() throws Exception {
        // Arrange: Input data for test
        PublicKey fromAccount = new PublicKey("11111111111111111111111111111111");
        PublicKey toAccount = new PublicKey("SecondPubey22222222222222222222222222222222");

        List<AccountMeta> keys = Arrays.asList(
                new AccountMeta(fromAccount, true, true), // Sender account
                new AccountMeta(toAccount, false, true)  // Receiver account
        );

        long lamports = 1_000_000L;

        // Create instruction instance
        SystemInstruction2Transfer instruction = new SystemInstruction2Transfer(keys, lamports);

        // Act: Encode the instruction
        byte[] encodedData = instruction.getData();

        // Assert: Validate encoding is correct
        assertNotNull(encodedData);
        assertEquals(12, encodedData.length); // u32 (4 bytes) + s64 (8 bytes)

        // Decode the instruction back to verify round-trip
        SystemInstruction2Transfer decodedInstruction = StructLayout.decode(encodedData, SystemInstruction2Transfer.class);

        // Validate the decoded instruction matches the original
        assertEquals(instruction.getLamports(), decodedInstruction.getLamports());
        assertEquals(instruction.getKeys().size(), decodedInstruction.getKeys().size());
        assertEquals(instruction.getKeys().get(0).getPublicKey(), decodedInstruction.getKeys().get(0).getPublicKey());
        assertEquals(instruction.getKeys().get(1).getPublicKey(), decodedInstruction.getKeys().get(1).getPublicKey());
    }
}