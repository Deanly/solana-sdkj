package net.deanly.solanarpcj.program.system.account.instruction;

import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemInstruction6InitializeNonceAccountTest {

    @Test
    void testInitializeNonceAccountInstructionEncoding() throws Exception {
        // Arrange: Input data for test
        PublicKey nonceAccount = new PublicKey("11111111111111111111111111111111");
        PublicKey authorityAccount = new PublicKey("SecondPubey22222222222222222222222222222222");

        List<AccountMeta> keys = Arrays.asList(
                new AccountMeta(nonceAccount, true, true), // Nonce account
                new AccountMeta(authorityAccount, false, true) // Authority account
        );

        PublicKey authority = new PublicKey("ThirdPubkey33333333333333333333333333333333");

        // Create instruction instance
        SystemInstruction6InitializeNonceAccount instruction = new SystemInstruction6InitializeNonceAccount(
                keys, authority
        );

        // Act: Encode the instruction
        byte[] encodedData = instruction.getData();

        // Assert: Validate encoding is correct
        assertNotNull(encodedData);
        // Expected size: u32 (4 bytes - instruction index) + PublicKey (32 bytes)
        assertEquals(36, encodedData.length);

        // Decode the instruction back to verify round-trip
        SystemInstruction6InitializeNonceAccount decodedInstruction = StructLayout.decode(
                encodedData, SystemInstruction6InitializeNonceAccount.class
        );

        // Validate the decoded instruction matches the original
        assertEquals(instruction.getAuthority(), decodedInstruction.getAuthority());
        assertEquals(instruction.getKeys().size(), decodedInstruction.getKeys().size());
        assertEquals(instruction.getKeys().get(0).getPublicKey(), decodedInstruction.getKeys().get(0).getPublicKey());
        assertEquals(instruction.getKeys().get(1).getPublicKey(), decodedInstruction.getKeys().get(1).getPublicKey());
    }
}