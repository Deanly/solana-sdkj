package net.deanly.solanarpcj.transaction.message.compiler;

import net.deanly.solanarpcj.transaction.instruction.TransactionInstructionImpl;
import org.junit.jupiter.api.Test;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;
import net.deanly.solanarpcj.transaction.message.meta.LoadedAddresses;
import net.deanly.solanarpcj.transaction.message.meta.MessageCompiledInstruction;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MessageKeyPairKeysTest {

    @Test
    public void constructor_validInputs_shouldInitializeFields() {
        List<PublicKey> staticAccountKeys = List.of(
                new PublicKey("11111111111111111111111111111111"),
                new PublicKey("SecondPubey22222222222222222222222222222222")
        );
        LoadedAddresses loadedAddresses = new LoadedAddresses(
                List.of(new PublicKey("11111111111111111111111111111111")),
                List.of(new PublicKey("SecondPubey22222222222222222222222222222222"))
        );

        MessageAccountKeys accountKeys = new MessageAccountKeys(staticAccountKeys, loadedAddresses);

        assertEquals(staticAccountKeys, accountKeys.getStaticAccountKeys());
        assertEquals(loadedAddresses, accountKeys.getAccountKeysFromLookups());
    }

    @Test
    public void constructor_nullStaticAccountKeys_shouldThrowException() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new MessageAccountKeys(null, new LoadedAddresses(List.of(), List.of()))
        );

        assertEquals("Static account keys cannot be null", exception.getMessage());
    }

    @Test
    public void keySegments_shouldReturnOrderedSegments() {
        List<PublicKey> staticAccountKeys = List.of(
                new PublicKey("11111111111111111111111111111111"),
                new PublicKey("SecondPubey22222222222222222222222222222222")
        );
        LoadedAddresses loadedAddresses = new LoadedAddresses(
                List.of(new PublicKey("11111111111111111111111111111111")),
                List.of(new PublicKey("SecondPubey22222222222222222222222222222222"))
        );

        MessageAccountKeys accountKeys = new MessageAccountKeys(staticAccountKeys, loadedAddresses);

        List<List<PublicKey>> segments = accountKeys.keySegments();

        assertEquals(3, segments.size());
        assertEquals(staticAccountKeys, segments.get(0));
        assertEquals(loadedAddresses.getWritable(), segments.get(1));
        assertEquals(loadedAddresses.getReadonly(), segments.get(2));
    }

    @Test
    public void get_validIndex_shouldReturnPublicKey() {
        List<PublicKey> staticAccountKeys = List.of(new PublicKey("ThirdPubkey33333333333333333333333333333333"));
        LoadedAddresses loadedAddresses = new LoadedAddresses(
                List.of(new PublicKey("11111111111111111111111111111111")),
                List.of(new PublicKey("SecondPubey22222222222222222222222222222222"))
        );

        MessageAccountKeys accountKeys = new MessageAccountKeys(staticAccountKeys, loadedAddresses);

        assertEquals(new PublicKey("ThirdPubkey33333333333333333333333333333333"), accountKeys.get(0));
        assertEquals(new PublicKey("11111111111111111111111111111111"), accountKeys.get(1));
        assertEquals(new PublicKey("SecondPubey22222222222222222222222222222222"), accountKeys.get(2));
    }

    @Test
    public void get_invalidIndex_shouldReturnNull() {
        MessageAccountKeys accountKeys = new MessageAccountKeys(List.of(new PublicKey("ThirdPubkey33333333333333333333333333333333")));

        assertNull(accountKeys.get(5)); // Index out of range
    }

    @Test
    public void getLength_shouldReturnTotalCount() {
        List<PublicKey> staticAccountKeys = List.of(
                new PublicKey("ThirdPubkey33333333333333333333333333333333"),
                new PublicKey("FourthPubke44444444444444444444444444444444")
        );
        LoadedAddresses loadedAddresses = new LoadedAddresses(
                List.of(new PublicKey("11111111111111111111111111111111")),
                List.of(new PublicKey("SecondPubey22222222222222222222222222222222"))
        );

        MessageAccountKeys accountKeys = new MessageAccountKeys(staticAccountKeys, loadedAddresses);

        assertEquals(4, accountKeys.getLength());
    }

    @Test
    public void compileInstructions_validData_shouldCompileCorrectly() {
        List<PublicKey> staticAccountKeys = List.of(
                new PublicKey("ThirdPubkey33333333333333333333333333333333")
        );
        LoadedAddresses loadedAddresses = new LoadedAddresses(
                List.of(new PublicKey("11111111111111111111111111111111")),
                List.of(new PublicKey("SecondPubey22222222222222222222222222222222"))
        );
        MessageAccountKeys accountKeys = new MessageAccountKeys(staticAccountKeys, loadedAddresses);

        TransactionInstruction instruction = new TransactionInstructionImpl(
                new PublicKey("ThirdPubkey33333333333333333333333333333333"),
                List.of(new AccountMeta(new PublicKey("11111111111111111111111111111111"), false, true)),
                new byte[] { 1, 2, 3 }
        );

        List<MessageCompiledInstruction> compiledInstructions = accountKeys.compileInstructions(List.of(instruction));

        assertEquals(1, compiledInstructions.size());
        assertEquals(0, compiledInstructions.get(0).getProgramIdIndex());
        assertEquals(List.of(1), compiledInstructions.get(0).getAccountKeyIndexes());
    }

}
