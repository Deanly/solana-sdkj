package net.deanly.solana.sdk.transaction.message.compiler;

import net.deanly.solana.sdk.transaction.instruction.TransactionInstructionImpl;
import net.deanly.solana.sdk.transaction.message.Message;
import net.deanly.solana.sdk.transaction.message.MessageV0;
import org.junit.jupiter.api.Test;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.solana.sdk.transaction.message.meta.MessageAddressTableLookup;
import net.deanly.solana.sdk.program.alt.state.AddressLookupTableAccount;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageCompilerTest {

    @Test
    void compileLegacy_withValidInputs_createsCorrectMessage() {
        PublicKey payer = new PublicKey("4kYtSEvtkPh3dgb1uxnUd3JW1CAkf5S6ACe7FkmkxLoE");

        PublicKey programId = new PublicKey("ThirdPubkey33333333333333333333333333333333");

        PublicKey accountKey = new PublicKey("H8DSBFkrAC5HBBF5YNAGtNg3YPr4iwRbQbV7wqrUk7Yp");

        AccountMeta accountMeta = new AccountMeta(accountKey, true, true);
        TransactionInstruction instruction = new TransactionInstructionImpl(
                programId, List.of(accountMeta), new byte[]{}
        );
        List<TransactionInstruction> instructions = List.of(instruction);
        String recentBlockhash = "7dA2H9LGYhFNco7DAFS52trcmCQt6tLdqiy4ApXPtGBF";

        // Execute
        Message legacyMessage = MessageCompiler.compileLegacy(payer, instructions, recentBlockhash);

        // Assertions
        assertNotNull(legacyMessage);
        assertEquals(1, legacyMessage.getInstructions().size());
        assertEquals(accountKey, legacyMessage.getStaticAccountKeys().get(1));
        assertEquals(programId, legacyMessage.getStaticAccountKeys().get(2));
    }

    @Test
    void compileV0_withValidInputs_createsCorrectMessageV0() {
        // Prepare inputs
        PublicKey payer = new PublicKey("4kYtSEvtkPh3dgb1uxnUd3JW1CAkf5S6ACe7FkmkxLoE");

        PublicKey programId = new PublicKey("SecondPubey22222222222222222222222222222222");

        PublicKey accountKey1 = new PublicKey("H8DSBFkrAC5HBBF5YNAGtNg3YPr4iwRbQbV7wqrUk7Yp");
        PublicKey accountKey2 = new PublicKey("FourthPubke44444444444444444444444444444444");

        AccountMeta accountMeta1 = new AccountMeta(accountKey1, true, true); // Writable account
        AccountMeta accountMeta2 = new AccountMeta(accountKey2, false, true); // Writable account
        TransactionInstruction instruction = new TransactionInstructionImpl(
                programId, List.of(accountMeta1, accountMeta2), new byte[]{}
        );
        List<TransactionInstruction> instructions = List.of(instruction);

        // Valid blockhash as a 32-character string
        String recentBlockhash = "3nvbfqeBa1Fs57HRXeg1uZ1UFsr7KRZqzVFkMRF4QFx4";

        // Define dummy Address Lookup Table for the test
        AddressLookupTableAccount lookupTableAccount = new AddressLookupTableAccount(
                new PublicKey("ThirdPubkey33333333333333333333333333333333"),
                new AddressLookupTableAccount.State(
                        1,
                        new BigInteger("FFFFFFFFFFFFFFFF", 16),
                        0L,
                        0,
                        null,
                        List.of(accountKey1, accountKey2)
                )
        );

        // Compile versioned message
        MessageV0 messageV0 = MessageCompiler.compileV0(payer, instructions, recentBlockhash, List.of(lookupTableAccount));

        // Verify MessageV0 object structure
        assertNotNull(messageV0, "MessageV0 should not be null");
        assertEquals(1, messageV0.getInstructions().size(), "Expected exactly one instruction");

        // Validate static account keys
        List<PublicKey> accountKeys = messageV0.getStaticAccountKeys();
        assertTrue(accountKeys.contains(accountKey1), "Expected accountKey to be part of the static keys");

        // Verify the Address Lookup Table key
        List<MessageAddressTableLookup> addressTableLookups = messageV0.getAddressTableLookups();
        assertEquals(1, addressTableLookups.size(), "Expected exactly one address table lookup");
        assertEquals("ThirdPubkey33333333333333333333333333333333",
                addressTableLookups.get(0).getAccountKey().toBase58(),
                "Expected Address Lookup Table Account Key to match");
        assertEquals(1, addressTableLookups.get(0).getWritableIndexes().size(), "Expected exactly one writable index");
        assertEquals(1, addressTableLookups.get(0).getWritableIndexes().get(0), "Expected exactly drained writable index to be 1");

        // Ensure accounts in Address Lookup Table are mapped correctly
        assertTrue(
                lookupTableAccount.getState().getAddresses().contains(accountKey2),
                "Expected accountKey to resolve correctly through address table"
        );
    }

    @Test
    void compileLegacy_withEmptyInstructions_throwsException() {
        PublicKey payer = new PublicKey("4kYtSEvtkPh3dgb1uxnUd3JW1CAkf5S6ACe7FkmkxLoE");

        String recentBlockhash = "7dA2H9LGYhFNco7DAFS52trcmCQt6tLdqiy4ApXPtGBF";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            MessageCompiler.compileLegacy(payer, Collections.emptyList(), recentBlockhash);
        });

        assertEquals("Instructions cannot be empty", exception.getMessage());
    }

    @Test
    void compileV0_withNullAddressLookupTableAccounts_handlesGracefully() {
        PublicKey payer = new PublicKey("4kYtSEvtkPh3dgb1uxnUd3JW1CAkf5S6ACe7FkmkxLoE");

        PublicKey programId = new PublicKey("ThirdPubkey33333333333333333333333333333333");

        PublicKey accountKey = new PublicKey("H8DSBFkrAC5HBBF5YNAGtNg3YPr4iwRbQbV7wqrUk7Yp");

        AccountMeta accountMeta = new AccountMeta(accountKey, true, true);
        TransactionInstruction instruction = new TransactionInstructionImpl(
                programId, List.of(accountMeta), new byte[]{}
        );
        List<TransactionInstruction> instructions = List.of(instruction);
        String recentBlockhash = "7dA2H9LGYhFNco7DAFS52trcmCQt6tLdqiy4ApXPtGBF";

        // Execute
        MessageV0 messageV0 = MessageCompiler.compileV0(payer, instructions, recentBlockhash, null);

        // Assertions
        assertNotNull(messageV0);
        assertEquals(1, messageV0.getInstructions().size());
        assertTrue(messageV0.getAddressTableLookups().isEmpty());
    }

    @Test
    void compileV0_withMixedAccountIndexes_createsCorrectLookupIndexes() {
        // Setup inputs
        PublicKey payer = new PublicKey("4kYtSEvtkPh3dgb1uxnUd3JW1CAkf5S6ACe7FkmkxLoE");

        PublicKey programId = new PublicKey("SecondProgram111111111111111111111111111111");

        PublicKey writableKey = new PublicKey("ThirdPubkey33333333333333333333333333333333");
        PublicKey readonlyKey = new PublicKey("H8DSBFkrAC5HBBF5YNAGtNg3YPr4iwRbQbV7wqrUk7Yp");
        PublicKey nonALTKey = new PublicKey("SecondPubey22222222222222222222222222222222");

        AccountMeta writableMeta = new AccountMeta(writableKey, false, true);
        AccountMeta readonlyMeta = new AccountMeta(readonlyKey, false, false);
        AccountMeta nonALTMeta = new AccountMeta(nonALTKey, false, false);

        TransactionInstruction instruction = new TransactionInstructionImpl(
                programId, List.of(writableMeta, readonlyMeta, nonALTMeta), new byte[]{}
        );

        List<TransactionInstruction> instructions = List.of(instruction);
        String recentBlockhash = "7dA2H9LGYhFNco7DAFS52trcmCQt6tLdqiy4ApXPtGBF";

        // Create Address Lookup Table
        AddressLookupTableAccount lookupTableAccount = new AddressLookupTableAccount(
                new PublicKey("FourthPubke44444444444444444444444444444444"),
                new AddressLookupTableAccount.State(
                        1,
                        new BigInteger("FFFFFFFFFFFFFFFF", 16),
                        0L,
                        0,
                        null,
                        List.of(writableKey, readonlyKey) // Only includes writableKey & readonlyKey
                )
        );

        // Execute
        MessageV0 messageV0 = MessageCompiler.compileV0(
                payer, instructions, recentBlockhash, List.of(lookupTableAccount)
        );

        // Assertions
        List<MessageAddressTableLookup> addressTableLookups = messageV0.getAddressTableLookups();

        assertEquals(1, addressTableLookups.size());
        MessageAddressTableLookup lookup = addressTableLookups.get(0);

        // Verify writableIndexes & readonlyIndexes
        assertEquals(1, lookup.getWritableIndexes().size());
        assertEquals(0, lookup.getWritableIndexes().get(0)); // writableKey is first in ALT

        assertEquals(1, lookup.getReadonlyIndexes().size());
        assertEquals(1, lookup.getReadonlyIndexes().get(0)); // readonlyKey is second in ALT

        // Ensure nonALTKey is NOT included in lookups
        assertTrue(messageV0.getStaticAccountKeys().contains(nonALTKey));
    }

    @Test
    void compileV0_withEmptyAddressLookupTable_handlesGracefully() {
        // Setup inputs
        PublicKey payer = new PublicKey("4kYtSEvtkPh3dgb1uxnUd3JW1CAkf5S6ACe7FkmkxLoE");

        PublicKey programId = new PublicKey("SecondProgram111111111111111111111111111111");

        PublicKey accountKey = new PublicKey("SecondPubey22222222222222222222222222222222");
        AccountMeta accountMeta = new AccountMeta(accountKey, true, false);

        TransactionInstruction instruction = new TransactionInstructionImpl(
                programId, List.of(accountMeta), new byte[]{}
        );

        List<TransactionInstruction> instructions = List.of(instruction);
        String recentBlockhash = "7dA2H9LGYhFNco7DAFS52trcmCQt6tLdqiy4ApXPtGBF";

        // Create an empty Address Lookup Table
        AddressLookupTableAccount lookupTableAccount = new AddressLookupTableAccount(
                new PublicKey("FourthPubke44444444444444444444444444444444"),
                new AddressLookupTableAccount.State(
                        1,
                        new BigInteger("FFFFFFFFFFFFFFFF", 16),
                        0L,
                        0,
                        null,
                        List.of() // Empty ALT
                )
        );

        // Execute
        MessageV0 messageV0 = MessageCompiler.compileV0(
                payer, instructions, recentBlockhash, List.of(lookupTableAccount)
        );

        // Verify MessageV0 object structure
        assertNotNull(messageV0, "MessageV0 should not be null");

        // Verify the Address Lookup Table key
        List<MessageAddressTableLookup> addressTableLookups = messageV0.getAddressTableLookups();
        assertEquals(0, addressTableLookups.size(), "Expected Address Lookup Table to be present");
    }
}