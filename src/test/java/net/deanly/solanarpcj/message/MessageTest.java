package net.deanly.solanarpcj.message;

import net.deanly.structlayout.StructLayout;
import org.bitcoinj.core.Base58;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import net.deanly.solanarpcj.core.Account;
import net.deanly.solanarpcj.core.PublicKey;
import net.deanly.solanarpcj.layout.ShortVecField;
import net.deanly.solanarpcj.message.meta.MessageCompiledInstruction;
import net.deanly.solanarpcj.message.meta.MessageHeader;
import net.deanly.solanarpcj.programs.SystemProgram;
import net.deanly.solanarpcj.utils.ShortvecEncoding;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

public class MessageTest {

    @Test
    public void serializeMessage() {
        PublicKey fromPublicKey = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
        PublicKey toPublickKey = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
        int lamports = 3000;

        Account signer = new Account(Base58
                .decode("4Z7cXSyeFR8wNGMVXUE1TwtKn5D5Vu7FzEv69dokLv7KrQk7h6pu4LF8ZRR9yQBhc7uSM6RTTZtU1fmaxiNrxXrs"));


        Message message =  Message.compile(
                signer.getPublicKey(),
                List.of(SystemProgram.transfer(fromPublicKey, toPublickKey, lamports)),
                "Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn"
        );

        assertArrayEquals(new int[] { 1, 0, 1, 3, 6, 26, 217, 208, 83, 135, 21, 72, 83, 126, 222, 62, 38, 24, 73, 163,
                223, 183, 253, 2, 250, 188, 117, 178, 35, 200, 228, 106, 219, 133, 61, 12, 235, 122, 188, 208, 216, 117,
                235, 194, 109, 161, 177, 129, 163, 51, 155, 62, 242, 163, 22, 149, 187, 122, 189, 188, 103, 130, 115,
                188, 173, 205, 229, 170, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 203, 226, 136, 193, 153, 148, 240, 50, 230, 98, 9, 79, 221, 179, 243, 174, 90, 67,
                104, 169, 6, 187, 165, 72, 36, 156, 19, 57, 132, 38, 69, 245, 1, 2, 2, 0, 1, 12, 2, 0, 0, 0, 184, 11, 0,
                0, 0, 0, 0, 0 }, toUnsignedByteArray(message.serialize()));
    }

    @Test
    public void testVLEField() {
        int length = 1241; // 또는 instructions.size()
        byte[] vleEncoded = new ShortVecField().encode(length); // StructLayout
        byte[] shortVecEncoded = ShortvecEncoding.encodeLength(length); // 기존 Java Shortvec Encoding

        System.out.println("VLEField Encoded: " + Arrays.toString(vleEncoded));
        System.out.println("Shortvec Encoded: " + Arrays.toString(shortVecEncoded));

        assertEquals(vleEncoded.length, shortVecEncoded.length);
    }

    public byte[] intArrayToByteArray(int[] intArray) {
        // 각 int는 4바이트를 차지하므로 배열 크기는 intArray.length * 4
        byte[] byteArray = new byte[intArray.length * 4];
        ByteBuffer buffer = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN);

        // int 값을 하나씩 byte로 변환하여 ByteBuffer에 추가
        for (int value : intArray) {
            buffer.putInt(value);
        }

        return byteArray;
    }

    int[] toUnsignedByteArray(byte[] in) {
        int[] out = new int[in.length];

        for (int i = 0; i < in.length; i++) {
            out[i] = in[i] & 0xff;
        }

        return out;
    }

    @Test
    public void compile_nullPayerKey_shouldThrowException() {
        Exception exception = assertThrows(
                NullPointerException.class,
                () -> Message.compile(null, List.of(), "RecentBlockhash")
        );

        assertEquals("Payer key is required", exception.getMessage());
    }

    @Test
    public void compile_nullRecentBlockhash_shouldThrowException() {
        PublicKey payerKey = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");

        Exception exception = assertThrows(
                NullPointerException.class,
                () -> Message.compile(payerKey, List.of(), null)
        );

        assertEquals("Recent blockhash is required", exception.getMessage());
    }

    @Test
    public void compile_emptyInstructions_shouldThrowException() {
        PublicKey payerKey = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");

        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> Message.compile(payerKey, List.of(), "RecentBlockhash")
        );

        assertEquals("Instructions cannot be empty", exception.getMessage());
    }

    @Test
    public void messageConstructor_shouldInitializeProperly() {
        MessageHeader header = new MessageHeader(2, 1, 0);
        List<PublicKey> accountKeys = List.of(
                new PublicKey("11111111111111111111111111111111"),
                new PublicKey("SecondPubey22222222222222222222222222222222")
        );
        String recentBlockhash = "Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn";
        MessageCompiledInstruction instruction = new MessageCompiledInstruction(1, List.of(0), new byte[]{});
        List<MessageCompiledInstruction> instructions = List.of(instruction);

        Message message = new Message(header, accountKeys, recentBlockhash, instructions);

        assertEquals(header, message.getHeader(), "Message header mismatch!");
        assertEquals(accountKeys, message.getStaticAccountKeys(), "Account keys mismatch!");
        assertEquals(recentBlockhash, message.getRecentBlockhash().toBase58(), "Recent blockhash mismatch!");
        assertEquals(instructions, message.getInstructions(), "Instructions mismatch!");
    }



    @Test
    public void serializeAndDeserialize_shouldReturnEquivalentMessage() {
        MessageHeader header = new MessageHeader(2, 0, 1);
        List<PublicKey> accountKeys = List.of(
                new PublicKey("11111111111111111111111111111111"),
                new PublicKey("SecondPubey22222222222222222222222222222222")
        );
        String recentBlockhash = "Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn";
        MessageCompiledInstruction instruction = new MessageCompiledInstruction(
                0,
                List.of(0, 1),
                new byte[] {0x01, 0x02, 0x03}
        );
        List<MessageCompiledInstruction> instructions = List.of(instruction);
        Message originalMessage = new Message(header, accountKeys, recentBlockhash, instructions);

        StructLayout.debug(originalMessage);

        // Serialize the message
        byte[] serializedMessage = originalMessage.serialize();
        StructLayout.debug(serializedMessage);

        // Deserialize back to an object
        Message deserializedMessage = Message.deserialize(serializedMessage);

        // Validate
        assertEquals(originalMessage.getHeader().getNumRequiredSignatures(), deserializedMessage.getHeader().getNumRequiredSignatures());
        assertEquals(originalMessage.getRecentBlockhash(), deserializedMessage.getRecentBlockhash());
        assertEquals(originalMessage.getStaticAccountKeys().size(), deserializedMessage.getStaticAccountKeys().size());
        assertEquals(originalMessage.getInstructions().size(), deserializedMessage.getInstructions().size());
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @Test
    public void isAccountSigner_shouldReturnCorrectResult() {
        MessageHeader header = new MessageHeader(2, 0, 0);
        List<PublicKey> accountKeys = List.of(
                new PublicKey("11111111111111111111111111111111"),
                new PublicKey("SecondPubey22222222222222222222222222222222")
        );
        String recentBlockhash = "Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn";
        List<MessageCompiledInstruction> instructions = List.of();

        Message message = new Message(header, accountKeys, recentBlockhash, instructions);

        assertTrue(message.isAccountSigner(0), "Account 0 should be a signer!");
        assertTrue(message.isAccountSigner(1), "Account 1 should be a signer!");
        assertThrows(IndexOutOfBoundsException.class, () -> message.isAccountSigner(2), "Account 2 should not be a signer!");
    }

    @Test
    public void isAccountWritable_shouldReturnCorrectResult() {
        MessageHeader header = new MessageHeader(2, 0, 1);
        List<PublicKey> accountKeys = List.of(
                new PublicKey("11111111111111111111111111111111"),
                new PublicKey("SecondPubey22222222222222222222222222222222"),
                new PublicKey("ThirdPubkey33333333333333333333333333333333")
        );
        String recentBlockhash = "Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn";
        List<MessageCompiledInstruction> instructions = List.of();

        Message message = new Message(header, accountKeys, recentBlockhash, instructions);

        assertTrue(message.isAccountWritable(0), "Account 0 should be writable!");
        assertTrue(message.isAccountWritable(1), "Account 1 should be writable!");
        assertFalse(message.isAccountWritable(2), "Account 2 should not be writable!");
    }
}
