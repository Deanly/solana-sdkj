package net.deanly.solana.sdk.program.spl.token.associated.instruction;

import net.deanly.solana.sdk.program.spl.token.basic.SplTokenProgram;
import net.deanly.solana.sdk.program.system.account.SystemProgram;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class SplAssociatedTokenInstruction1CreateIdempotentTest {

    @Test
    public void testCreateInstruction() {
        // Define mock objects for PublicKeys
        PublicKey payer = new PublicKey("DummyPayerPublicKey");
        PublicKey ata = new PublicKey("6M8tvLDtG47BmPJj1nn1tkUpcHm4o2jDDqeZWkRp2amR");
        PublicKey owner = new PublicKey("DummyOwnerPublicKey");
        PublicKey mint = new PublicKey("DummyMintPublicKey");
        PublicKey systemProgram = SystemProgram.PROGRAM_ID;
        PublicKey tokenProgram = SplTokenProgram.PROGRAM_ID;

        // Generate the instruction
        SplAssociatedTokenInstruction1CreateIdempotent instruction =
                SplAssociatedTokenInstruction1CreateIdempotent.create(
                        payer, owner, mint
                );

        // Validate discriminator
        assertEquals(1, instruction.getDiscriminator(), "Discriminator should match the expected value.");

        // Validate account keys
        List<AccountMeta> keys = instruction.getKeys();
        assertEquals(6, keys.size(), "Expected exactly 6 keys.");

        // Validate each key
        assertEquals(payer, keys.get(0).getPublicKey(), "Payer public key mismatch.");
        assertTrue(keys.get(0).isWritable(), "Payer should be writable.");
        assertTrue(keys.get(0).isSigner(), "Payer should be a signer.");

        assertEquals(ata, keys.get(1).getPublicKey(), "ATA public key mismatch.");
        assertTrue(keys.get(1).isWritable(), "ATA should be writable.");
        assertFalse(keys.get(1).isSigner(), "ATA should not be a signer.");

        assertEquals(owner, keys.get(2).getPublicKey(), "Owner public key mismatch.");
        assertFalse(keys.get(2).isWritable(), "Owner should not be writable.");
        assertFalse(keys.get(2).isSigner(), "Owner should not be a signer.");

        assertEquals(mint, keys.get(3).getPublicKey(), "Mint public key mismatch.");
        assertFalse(keys.get(3).isWritable(), "Mint should not be writable.");
        assertFalse(keys.get(3).isSigner(), "Mint should not be a signer.");

        assertEquals(systemProgram, keys.get(4).getPublicKey(), "SystemProgram public key mismatch.");
        assertFalse(keys.get(4).isWritable(), "SystemProgram should not be writable.");
        assertFalse(keys.get(4).isSigner(), "SystemProgram should not be a signer.");

        assertEquals(tokenProgram, keys.get(5).getPublicKey(), "TokenProgram public key mismatch.");
        assertFalse(keys.get(5).isWritable(), "TokenProgram should not be writable.");
        assertFalse(keys.get(5).isSigner(), "TokenProgram should not be a signer.");
    }
}