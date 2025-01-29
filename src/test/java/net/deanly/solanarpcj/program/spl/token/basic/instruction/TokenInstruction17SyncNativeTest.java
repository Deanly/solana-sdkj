package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.spl.token.basic.instruction.TokenInstruction17SyncNative;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TokenInstruction17SyncNativeTest {

    @Test
    public void testSyncNative() {
        // Step 1: Prepare necessary data
        PublicKey nativeAccount = new PublicKey("NativePublicKey");

        // Step 2: Create instruction
        TokenInstruction17SyncNative instruction = new TokenInstruction17SyncNative();
        instruction.setKeys(nativeAccount); // Set the native account to sync

        // Step 3: Verify metadata (keys)
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(1, instruction.getKeys().size());

        // Native token account
        Assertions.assertEquals(nativeAccount, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertFalse(instruction.getKeys().get(0).isSigner());

        // Step 4: Verify encoded data
        byte[] encodedData = instruction.getData();
        Assertions.assertNotNull(encodedData);
        Assertions.assertTrue(encodedData.length > 0);

        // Step 5: Decode data to verify it
        TokenInstruction17SyncNative decodedInstruction = new TokenInstruction17SyncNative();
        Assertions.assertEquals(17, decodedInstruction.getDiscriminator());
    }
}