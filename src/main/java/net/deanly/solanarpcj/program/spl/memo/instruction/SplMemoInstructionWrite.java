package net.deanly.solanarpcj.program.spl.memo.instruction;

import lombok.*;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.layout.field.UTF8StringField;
import net.deanly.solanarpcj.program.spl.memo.SplMemoProgram;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.solanarpcj.crypto.PublicKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SplMemoInstructionWrite extends SplMemoProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UTF8StringField.class)
    private String memo; // UTF-8 memo string

    private List<AccountMeta> keys = Collections.emptyList();

    public void setMemo(String memo) {
        if (memo == null || memo.isEmpty()) {
            throw new IllegalArgumentException("Memo must not be empty or null.");
        }
        this.memo = memo;
    }

    /**
     * Sets the keys for the MemoInstructionAdd.
     *
     * @param signers The PublicKey of the signers.
     * @throws IllegalArgumentException if the signer is null
     */
    public void setKeys(List<PublicKey> signers) {
        this.keys = signers.stream().map(AccountMeta::roleReadOnlySigner).toList(); // Signer: writable=false, signer=true
    }

    /**
     * Encodes the instruction data as a UTF-8 byte array.
     *
     * @return Encoded instruction data as a byte array
     */
    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    /**
     * Decodes the instruction data from a byte array.
     *
     * @param data The encoded instruction data
     */
    public void setData(byte[] data) {
        SplMemoInstructionWrite decoded = StructLayout.decode(data, SplMemoInstructionWrite.class);
        this.memo = decoded.getMemo();
    }

    /**
     * Returns the keys associated with this instruction.
     *
     * @return A list of {@link AccountMeta} representing the keys.
     */
    @Override
    public List<AccountMeta> getKeys() {
        return this.keys;
    }
}