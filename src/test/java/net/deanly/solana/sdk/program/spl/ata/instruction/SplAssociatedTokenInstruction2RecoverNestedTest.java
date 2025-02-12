package net.deanly.solana.sdk.program.spl.ata.instruction;

import net.deanly.solana.sdk.program.spl.token.SplTokenProgram;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SplAssociatedTokenInstruction2RecoverNestedTest {

    @Test
    public void instructionTest() {
        PublicKey nestedAssociatedAccount = new PublicKey("NestedAccountPublicKey");
        PublicKey nestedTokenMint = new PublicKey("NestedTokenMintKey");
        PublicKey destinationAccount = new PublicKey("DestinationAccountKey");
        PublicKey ownerAssociatedAccount = new PublicKey("OwnerAccountKey");
        PublicKey ownerTokenMint = new PublicKey("OwnerMintKey");
        PublicKey walletKey = new PublicKey("WalletKey");
        PublicKey tokenProgramKey = SplTokenProgram.PROGRAM_ID;

        // Create instruction
        SplAssociatedTokenInstruction2RecoverNested instruction =
                SplAssociatedTokenInstruction2RecoverNested.create(
                        nestedAssociatedAccount,
                        nestedTokenMint,
                        destinationAccount,
                        ownerAssociatedAccount,
                        ownerTokenMint,
                        walletKey
                );

        // Validate set keys
        List<AccountMeta> keys = instruction.getKeys();
        assertEquals(7, keys.size());
        assertEquals(2, instruction.getDiscriminator(), "Discriminator should match expected value.");
        assertEquals(walletKey, keys.get(5).getPublicKey(), "Wallet key should match expected value.");
        assertEquals(ownerTokenMint, keys.get(4).getPublicKey(), "Owner token mint key should match expected value.");
        assertEquals(ownerAssociatedAccount, keys.get(3).getPublicKey(), "Owner associated account key should match expected value.");
        assertEquals(destinationAccount, keys.get(2).getPublicKey(), "Destination account key should match expected value.");
        assertEquals(nestedTokenMint, keys.get(1).getPublicKey(), "Nested token mint key should match expected value.");
        assertEquals(nestedAssociatedAccount, keys.get(0).getPublicKey(), "Nested associated account key should match expected value.");
        assertEquals(tokenProgramKey, keys.get(6).getPublicKey(), "Token program key should match expected value.");

        // Validate data encoding
        byte[] encodedData = instruction.getData();
        assertEquals(1, encodedData.length, "Data length should match expected value.");
        assertEquals(2, encodedData[0], "Data value should match expected value.");
    }
}
