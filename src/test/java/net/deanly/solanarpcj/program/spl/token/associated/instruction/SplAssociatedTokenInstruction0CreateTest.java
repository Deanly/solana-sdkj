package net.deanly.solanarpcj.program.spl.token.associated.instruction;

import net.deanly.solanarpcj.program.spl.token.basic.SplTokenProgram;
import net.deanly.solanarpcj.program.system.account.SystemProgram;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class SplAssociatedTokenInstruction0CreateTest {

    @Test
    public void testCreateInstruction() {
        // Test PublicKey mock objects
        PublicKey payer = new PublicKey("ExamplePayerPublicKey");
        PublicKey associatedTokenAccount = new PublicKey("6rpG9aVgqoQ5hdWsBvu7VgBScFpGrJ6jh6NAX7HDS7h6");
        PublicKey owner = new PublicKey("ExampleOwnerPublicKey");
        PublicKey mint = new PublicKey("ExampleMintPublicKey");
        PublicKey systemProgram = SystemProgram.PROGRAM_ID;
        PublicKey tokenProgram = SplTokenProgram.PROGRAM_ID;

        // Create instruction
        SplAssociatedTokenInstruction0Create instruction =
                SplAssociatedTokenInstruction0Create.create(
                        payer,
                        owner,
                        mint
                );

        // Validate discriminator
        assertEquals(0, instruction.getDiscriminator(), "Discriminator should match expected value.");

        // Validate keys metadata
        List<AccountMeta> keys = instruction.getKeys();
        assertEquals(6, keys.size(), "Keys size should match expected value.");

        // Validate Payer
        assertEquals(payer, keys.get(0).getPublicKey(), "Payer PublicKey mismatch.");
        assertTrue(keys.get(0).isWritable(), "Payer should be writable.");
        assertTrue(keys.get(0).isSigner(), "Payer should be a signer.");

        // Validate AssociatedTokenAccount
        assertEquals(associatedTokenAccount, keys.get(1).getPublicKey(), "Associated Token Account PublicKey mismatch.");
        assertTrue(keys.get(1).isWritable(), "AssociatedTokenAccount should be writable.");
        assertFalse(keys.get(1).isSigner(), "AssociatedTokenAccount should not be a signer.");

        // Validate Owner
        assertEquals(owner, keys.get(2).getPublicKey(), "Owner PublicKey mismatch.");
        assertFalse(keys.get(2).isWritable(), "Owner should not be writable.");
        assertFalse(keys.get(2).isSigner(), "Owner should not be a signer.");

        // Validate Mint
        assertEquals(mint, keys.get(3).getPublicKey(), "Mint PublicKey mismatch.");
        assertFalse(keys.get(3).isWritable(), "Mint should not be writable.");
        assertFalse(keys.get(3).isSigner(), "Mint should not be a signer.");

        // Validate SystemProgram
        assertEquals(systemProgram, keys.get(4).getPublicKey(), "SystemProgram PublicKey mismatch.");
        assertFalse(keys.get(4).isWritable(), "SystemProgram should not be writable.");
        assertFalse(keys.get(4).isSigner(), "SystemProgram should not be a signer.");

        // Validate TokenProgram
        assertEquals(tokenProgram, keys.get(5).getPublicKey(), "TokenProgram PublicKey mismatch.");
        assertFalse(keys.get(5).isWritable(), "TokenProgram should not be writable.");
        assertFalse(keys.get(5).isSigner(), "TokenProgram should not be a signer.");
    }
}