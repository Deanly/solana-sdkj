package net.deanly.solanarpcj.program.system.account.instruction;

import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemInstruction1AssignTest {

    @Test
    void testAssignInstructionEncoding() throws Exception {
        // Arrange: Input data
        PublicKey account = new PublicKey("11111111111111111111111111111111");
        PublicKey newProgramId = new PublicKey("SecondPubey22222222222222222222222222222222");

        List<AccountMeta> keys = Collections.singletonList(
                new AccountMeta(account, true, true) // Account being assigned a new owner
        );

        SystemInstruction1Assign instruction = new SystemInstruction1Assign(keys, newProgramId);

        // Act: Encode the instruction
        byte[] encodedData = instruction.getData();

        // Assert: Validate encoding
        assertNotNull(encodedData);
        assertEquals(36, encodedData.length); // u32 (4 bytes) + PublicKey (32 bytes)

        // Decode the instruction back
        SystemInstruction1Assign decodedInstruction = StructLayout.decode(encodedData, SystemInstruction1Assign.class);

        // Assert: Ensure decoded instruction matches original
        assertEquals(instruction.getProgramId(), decodedInstruction.getProgramId());
        assertEquals(instruction.getKeys().size(), decodedInstruction.getKeys().size());
        assertEquals(instruction.getKeys().get(0).getPublicKey(), decodedInstruction.getKeys().get(0).getPublicKey());
    }
}