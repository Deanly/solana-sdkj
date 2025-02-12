package net.deanly.solana.sdk.program.core.bpf;

import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.Program;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class SharedMemoryProgram extends Program {

    public static final PublicKey PROGRAM_ID = new PublicKey("ABbdZW8gJcXEB9XkRZLwDDuGmom3hBwWEsG2y49bHv45");

    public static TransactionInstruction initializeBuffer(final PublicKey accountToWrite,
                                                          byte[] data,
                                                          int offset) {
        final List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(accountToWrite, true, false));

        ByteBuffer result = ByteBuffer.allocate(8 + data.length);
        result.order(ByteOrder.LITTLE_ENDIAN);
        result.putInt(0, offset);
        result.put(8, data);

        return createTransactionInstruction(
                PROGRAM_ID,
                keys,
                result.array()
        );
    }

}
