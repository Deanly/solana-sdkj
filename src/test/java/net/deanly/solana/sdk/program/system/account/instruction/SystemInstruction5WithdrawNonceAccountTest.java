package net.deanly.solana.sdk.program.system.account.instruction;

import net.deanly.solana.sdk.program.core.system.instruction.SystemInstruction5WithdrawNonceAccount;
import net.deanly.solana.sdk.program.core.Sysvar;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemInstruction5WithdrawNonceAccountTest {

    @Test
    void testWithdrawNonceAccountInstructionEncoding() throws Exception {
        // Arrange: Input data for test
        PublicKey nonceAccount = new PublicKey("11111111111111111111111111111111");
        PublicKey authorityAccount = new PublicKey("SecondPubey22222222222222222222222222222222");
        PublicKey destinationAccount = new PublicKey("ThirdPubkey33333333333333333333333333333333");

        List<AccountMeta> keys = Arrays.asList(
                new AccountMeta(nonceAccount, true, true), // Nonce account
                new AccountMeta(authorityAccount, false, true), // Authority account
                new AccountMeta(destinationAccount, true, false) // Destination account
        );

        long lamports = 500_000L;

        // Create instruction instance
        SystemInstruction5WithdrawNonceAccount instruction = SystemInstruction5WithdrawNonceAccount.create(
                nonceAccount, authorityAccount, destinationAccount, lamports
        );

        assertEquals(5, instruction.getKeys().size());
        assertEquals(nonceAccount, instruction.getKeys().get(0).getPublicKey());
        assertEquals(destinationAccount, instruction.getKeys().get(1).getPublicKey());
        assertEquals(Sysvar.SYSVAR_RECENT_BLOCKHASHES_ADDRESS, instruction.getKeys().get(2).getPublicKey());
        assertEquals(Sysvar.SYSVAR_RENT_ADDRESS, instruction.getKeys().get(3).getPublicKey());
        assertEquals(authorityAccount, instruction.getKeys().get(4).getPublicKey());

        // Act: Encode the instruction
        byte[] encodedData = instruction.getData();

        // Assert: Check encoding correctness
        assertNotNull(encodedData);
        // Expected size: u32 (4 bytes - instruction index) + s64 (8 bytes - lamports)
        assertEquals(12, encodedData.length);

        // Decode the instruction back to verify round-trip
        SystemInstruction5WithdrawNonceAccount decodedInstruction = StructLayout.decode(
                encodedData, SystemInstruction5WithdrawNonceAccount.class
        );

        // Validate the decoded instruction matches the original
        assertEquals(instruction.getLamports(), decodedInstruction.getLamports());
    }
}