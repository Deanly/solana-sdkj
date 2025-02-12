package net.deanly.solana.sdk.program.system.account.instruction;

import net.deanly.solana.sdk.program.core.system.SystemProgram;
import net.deanly.solana.sdk.program.core.system.instruction.SystemInstruction1Assign;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
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

        assertEquals(SystemProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(newProgramId, instruction.getOwnerProgramId());
        assertEquals(1, instruction.getKeys().size());
        assertEquals(account, instruction.getKeys().get(0).getPublicKey());

        // Act: Encode the instruction
        byte[] encodedData = instruction.getData();

        // Assert: Validate encoding
        assertNotNull(encodedData);
        assertEquals(36, encodedData.length); // u32 (4 bytes) + PublicKey (32 bytes)

        // Decode the instruction back
        SystemInstruction1Assign decodedInstruction = StructLayout.decode(encodedData, SystemInstruction1Assign.class);

        // Assert: Ensure decoded instruction matches original
        assertEquals(instruction.getProgramId(), decodedInstruction.getProgramId());
    }
}