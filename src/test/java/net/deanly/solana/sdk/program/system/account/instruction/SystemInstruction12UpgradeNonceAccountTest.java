package net.deanly.solana.sdk.program.system.account.instruction;

import net.deanly.solana.sdk.program.core.system.instruction.SystemInstruction12UpgradeNonceAccount;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemInstruction12UpgradeNonceAccountTest {

    @Test
    void testUpgradeNonceAccountInstructionEncoding() throws Exception {
        // Arrange: Input data for test
        PublicKey nonceAccount = new PublicKey("11111111111111111111111111111111");
        PublicKey authorityAccount = new PublicKey("SecondPubey22222222222222222222222222222222");

        List<AccountMeta> keys = Arrays.asList(
                new AccountMeta(nonceAccount, true, true), // Nonce Account
                new AccountMeta(authorityAccount, true, false) // Authority
        );

        // Create instance
        SystemInstruction12UpgradeNonceAccount instruction = SystemInstruction12UpgradeNonceAccount.create(
            nonceAccount
        );

        assertEquals(1, instruction.getKeys().size());
        assertEquals(keys.get(0).getPublicKey(), instruction.getKeys().get(0).getPublicKey());


        // Act: Encode the instruction
        byte[] encodedData = instruction.getData();

        // Assert: Check encoding
        assertNotNull(encodedData);
        // Expected length: u32 (4 bytes for instruction)
        assertEquals(4, encodedData.length); // Only contains instruction index

        // Decode and verify
        SystemInstruction12UpgradeNonceAccount decodedInstruction = StructLayout.decode(
                encodedData, SystemInstruction12UpgradeNonceAccount.class
        );
    }
}