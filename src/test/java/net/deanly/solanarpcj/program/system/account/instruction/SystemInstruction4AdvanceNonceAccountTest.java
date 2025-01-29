package net.deanly.solanarpcj.program.system.account.instruction;

import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemInstruction4AdvanceNonceAccountTest {

    @Test
    void testAdvanceNonceAccountInstructionEncoding() throws Exception {
        // Arrange: Input data for test
        PublicKey nonceAccount = new PublicKey("11111111111111111111111111111111");
        PublicKey authorityAccount = new PublicKey("SecondPubey22222222222222222222222222222222");

        List<AccountMeta> keys = Arrays.asList(
                new AccountMeta(nonceAccount, true, true), // Nonce account
                new AccountMeta(authorityAccount, false, true) // Authority account
        );

        // Create instruction instance
        SystemInstruction4AdvanceNonceAccount instruction = new SystemInstruction4AdvanceNonceAccount(keys);

        // Act: Encode the instruction
        byte[] encodedData = instruction.getData();

        // Assert: Validate encoding is correct
        assertNotNull(encodedData);
        // Expected size: u32 (4 bytes - instruction index)
        assertEquals(4, encodedData.length);

        // Decode the instruction back to verify round-trip
        SystemInstruction4AdvanceNonceAccount decodedInstruction = StructLayout.decode(encodedData, SystemInstruction4AdvanceNonceAccount.class);

        // Validate the decoded instruction matches the original
        assertEquals(instruction.getKeys().size(), decodedInstruction.getKeys().size());
        assertEquals(instruction.getKeys().get(0).getPublicKey(), decodedInstruction.getKeys().get(0).getPublicKey());
        assertEquals(instruction.getKeys().get(1).getPublicKey(), decodedInstruction.getKeys().get(1).getPublicKey());
    }
}