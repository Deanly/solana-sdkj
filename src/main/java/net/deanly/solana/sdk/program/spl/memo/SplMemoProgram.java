package net.deanly.solana.sdk.program.spl.memo;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.spl.memo.instruction.SplMemoInstructionWrite;

import java.util.List;

public class SplMemoProgram {
    public static final PublicKey PROGRAM_ID = new PublicKey("MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr");
    public static final PublicKey PROGRAM_ID_OLD = new PublicKey("Memo1UhkJRfHyvLMcVucJwxXeuD728EqVDDwQDxFMNo");

    public static SplMemoInstructionWrite write(
            String memo,
            List<PublicKey> signer) {
        if (memo == null || memo.isEmpty()) {
            throw new IllegalArgumentException("memo must not be null or empty");
        }
        if (signer == null) {
            signer = List.of();
        }
        SplMemoInstructionWrite instruction = new SplMemoInstructionWrite();
        instruction.setKeys(signer);
        instruction.setMemo(memo);
        return instruction;
    }
}
