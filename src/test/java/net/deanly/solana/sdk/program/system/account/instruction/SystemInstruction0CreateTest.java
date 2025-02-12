package net.deanly.solana.sdk.program.system.account.instruction;

import net.deanly.solana.sdk.program.core.system.instruction.SystemInstruction0Create;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemInstruction0CreateTest {

    @Test
    void testCreateInstructionEncoding() throws Exception {
        // Arrange: Input data
        PublicKey fromAccount = new PublicKey("11111111111111111111111111111111");
        PublicKey newAccount = new PublicKey("SecondPubey22222222222222222222222222222222");
        PublicKey programId = new PublicKey("ThirdPubkey33333333333333333333333333333333");

        List<AccountMeta> keys = Arrays.asList(
                new AccountMeta(fromAccount, true, true), // Payer (funding account)
                new AccountMeta(newAccount, false, true) // New account
        );

        long lamports = 1_000_000;
        long space = 1024;

        SystemInstruction0Create instruction = new SystemInstruction0Create(keys, lamports, space, programId);

        // Act: Encode the instruction
        byte[] encodedData = instruction.getData();

        // Assert: Validate encoding
        assertNotNull(encodedData);
        assertEquals(52, encodedData.length); // u32 (4 bytes) + s64 (8 bytes) + s64 (8 bytes) + PublicKey (32 bytes)

        // Decode the instruction back
        SystemInstruction0Create decodedInstruction = StructLayout.decode(encodedData, SystemInstruction0Create.class);

        // Assert: Ensure decoded instruction matches original
        assertEquals(instruction.getLamports(), decodedInstruction.getLamports());
        assertEquals(instruction.getSpace(), decodedInstruction.getSpace());
        assertEquals(instruction.getNewProgramId(), decodedInstruction.getNewProgramId());
    }
}