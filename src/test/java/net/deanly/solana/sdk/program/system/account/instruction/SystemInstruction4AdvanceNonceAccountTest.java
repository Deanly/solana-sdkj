package net.deanly.solana.sdk.program.system.account.instruction;

import net.deanly.solana.sdk.program.core.system.instruction.SystemInstruction4AdvanceNonceAccount;
import net.deanly.solana.sdk.program.core.Sysvar;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
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
                new AccountMeta(authorityAccount, true, false) // Authority account
        );

        // Create instruction instance
        SystemInstruction4AdvanceNonceAccount instruction = SystemInstruction4AdvanceNonceAccount.create(
                nonceAccount, authorityAccount
        );

        assertEquals(3, instruction.getKeys().size());
        assertEquals(nonceAccount, instruction.getKeys().get(0).getPublicKey());
        assertEquals(Sysvar.SYSVAR_RECENT_BLOCKHASHES_ADDRESS, instruction.getKeys().get(1).getPublicKey());
        assertEquals(authorityAccount, instruction.getKeys().get(2).getPublicKey());

        // Act: Encode the instruction
        byte[] encodedData = instruction.getData();

        // Assert: Validate encoding is correct
        assertNotNull(encodedData);
        // Expected size: u32 (4 bytes - instruction index)
        assertEquals(4, encodedData.length);

        // Decode the instruction back to verify round-trip
        SystemInstruction4AdvanceNonceAccount decodedInstruction = StructLayout.decode(encodedData, SystemInstruction4AdvanceNonceAccount.class);
    }
}