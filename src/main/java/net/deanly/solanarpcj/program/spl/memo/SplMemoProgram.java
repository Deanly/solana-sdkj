package net.deanly.solanarpcj.program.spl.memo;

import lombok.Getter;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.layout.Struct;
import net.deanly.solanarpcj.program.spl.memo.instruction.SplMemoInstructionWrite;

public class SplMemoProgram {
    public static final PublicKey PROGRAM_ID = new PublicKey("MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr");

    @Getter
    public static class Base extends Struct {
        private final PublicKey programId = PROGRAM_ID;
    }

    public static SplMemoInstructionWrite write(PublicKey signer, String memo) {
        SplMemoInstructionWrite instruction = new SplMemoInstructionWrite();
        instruction.setKeys(signer);
        instruction.setMemo(memo);
        return instruction;
    }
}
