package net.deanly.solanarpcj.message.compiler;

import net.deanly.solanarpcj.message.Message;
import net.deanly.solanarpcj.message.MessageV0;
import org.junit.jupiter.api.Test;
import net.deanly.solanarpcj.core.AccountMeta;
import net.deanly.solanarpcj.core.PublicKey;
import net.deanly.solanarpcj.core.TransactionInstruction;
import net.deanly.solanarpcj.message.meta.MessageAddressTableLookup;
import net.deanly.solanarpcj.alt.AddressLookupTableAccount;

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
        TransactionInstruction instruction = new TransactionInstruction(
                programId, List.of(accountMeta), new byte[]{}
        );
        List<TransactionInstruction> instructions = List.of(instruction);
        String recentBlockhash = "7dA2H9LGYhFNco7DAFS52trcmCQt6tLdqiy4ApXPtGBF";

        // Execute
        Message legacyMessage = MessageCompiler.compileLegacy(payer, instructions, recentBlockhash);

        // Assertions
        assertNotNull(legacyMessage);
        assertEquals(1, legacyMessage.getInstructions().size());
        assertEquals(accountKey, legacyMessage.getAccountKeys().get(1));
        assertEquals(programId, legacyMessage.getAccountKeys().get(2));
    }

    @Test
    void compileV0_withValidInputs_createsCorrectMessageV0() {
        // Prepare inputs
        PublicKey payer = new PublicKey("4kYtSEvtkPh3dgb1uxnUd3JW1CAkf5S6ACe7FkmkxLoE");

        PublicKey programId = new PublicKey("SecondPubey22222222222222222222222222222222");

        PublicKey accountKey = new PublicKey("H8DSBFkrAC5HBBF5YNAGtNg3YPr4iwRbQbV7wqrUk7Yp");

        AccountMeta accountMeta = new AccountMeta(accountKey, true, true); // Writable account
        TransactionInstruction instruction = new TransactionInstruction(
                programId, List.of(accountMeta), new byte[]{}
        );
        List<TransactionInstruction> instructions = List.of(instruction);

        // Valid blockhash as a 32-character string
        String recentBlockhash = "3nvbfqeBa1Fs57HRXeg1uZ1UFsr7KRZqzVFkMRF4QFx4";

        // Define dummy Address Lookup Table for the test
        AddressLookupTableAccount lookupTableAccount = new AddressLookupTableAccount(
                new PublicKey("ThirdPubkey33333333333333333333333333333333"),
                new AddressLookupTableAccount.State(
                        1,
                        0xFFFFFFFFFFFFFFFFL,
                        0L,
                        0,
                        null,
                        List.of(accountKey)
                )
        );

        // Compile versioned message
        MessageV0 messageV0 = MessageCompiler.compileV0(payer, instructions, recentBlockhash, List.of(lookupTableAccount));

        // Verify MessageV0 object structure
        assertNotNull(messageV0, "MessageV0 should not be null");
        assertEquals(1, messageV0.getInstructions().size(), "Expected exactly one instruction");

        // Validate static account keys
        List<PublicKey> accountKeys = messageV0.getAccountKeys();
        assertTrue(accountKeys.contains(accountKey), "Expected accountKey to be part of the static keys");

        // Verify the Address Lookup Table key
        List<MessageAddressTableLookup> addressTableLookups = messageV0.getAddressTableLookups();
        assertEquals(1, addressTableLookups.size(), "Expected exactly one address table lookup");
        assertEquals("LookupTable11111111111111111111111111111",
                addressTableLookups.get(0).getAccountKey().toBase58(),
                "Expected Address Lookup Table Account Key to match");

        // Ensure accounts in Address Lookup Table are mapped correctly
        assertTrue(
                lookupTableAccount.getState().getAddresses().contains(accountKey),
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
        TransactionInstruction instruction = new TransactionInstruction(
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
}