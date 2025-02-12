package net.deanly.solana.sdk.program.spl.memo;

import net.deanly.solana.sdk.rpc.client.Network;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class SplMemoProgramTest {

    @Test
    public void testWriteUtf8_ValidInput() {
        PublicKey account = new PublicKey("11111111111111111111111111111111");
        String memo = "Test memo";

        TransactionInstruction instruction = SplMemoProgram.write(memo, List.of(account));

        assertNotNull(instruction);
        assertEquals(SplMemoProgram.PROGRAM_ID, instruction.getProgramId(Network.DEVNET));
        assertEquals(1, instruction.getKeys().size());
        assertEquals(account, instruction.getKeys().get(0).getPublicKey());
        assertTrue(instruction.getKeys().get(0).isSigner());
        assertFalse(instruction.getKeys().get(0).isWritable());
        assertArrayEquals(memo.getBytes(StandardCharsets.UTF_8), instruction.getData());
    }

    @Test
    public void testWriteUtf8_NullAccount() {
        SplMemoProgram.write("Test memo", null);
    }

    @Test
    public void testWriteUtf8_NullMemo() {
        PublicKey account = new PublicKey("11111111111111111111111111111111");
        assertThrows(IllegalArgumentException.class, () -> SplMemoProgram.write(null, List.of(account)));
    }

    @Test
    public void testWriteUtf8_EmptyMemo() {
        PublicKey account = new PublicKey("11111111111111111111111111111111");
        assertThrows(IllegalArgumentException.class, () -> SplMemoProgram.write("", List.of(account)));
    }

    @Test
    public void testWriteUtf8_LongMemo() {
        PublicKey account = new PublicKey("11111111111111111111111111111111");
        String longMemo = String.join("", java.util.Collections.nCopies(1000, "A"));

        TransactionInstruction instruction = SplMemoProgram.write(longMemo, List.of(account));

        assertNotNull(instruction);
        assertEquals(SplMemoProgram.PROGRAM_ID, instruction.getProgramId(Network.DEVNET));
        assertArrayEquals(longMemo.getBytes(StandardCharsets.UTF_8), instruction.getData());
    }
}
