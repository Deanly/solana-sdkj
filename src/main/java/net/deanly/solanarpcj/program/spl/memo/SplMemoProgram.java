package net.deanly.solanarpcj.program.spl.memo;

import lombok.Getter;
import lombok.Setter;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.layout.Struct;
import net.deanly.solanarpcj.program.spl.memo.instruction.SplMemoInstructionWrite;

import java.util.List;

public class SplMemoProgram {
    public static final PublicKey PROGRAM_ID = new PublicKey("MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr");
    public static final PublicKey PROGRAM_ID_OLD = new PublicKey("Memo1UhkJRfHyvLMcVucJwxXeuD728EqVDDwQDxFMNo");

    @Getter
    @Setter
    public static class Base extends Struct {
        private PublicKey programId = PROGRAM_ID;

        public void setProgramId(PublicKey programId) {
            if (programId == null) {
                throw new IllegalArgumentException("programId must not be null");
            }
            if (!programId.toBase58().startsWith("Memo")) {
                throw new IllegalArgumentException("programId must be a valid Memo program");
            }
            this.programId = programId;
        }
    }

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
