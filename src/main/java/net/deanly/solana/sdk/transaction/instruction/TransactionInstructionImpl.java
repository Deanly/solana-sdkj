package net.deanly.solana.sdk.transaction.instruction;

import lombok.*;
import net.deanly.solana.sdk.crypto.PublicKey;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@ToString
@Builder(builderClassName = "Builder")
@EqualsAndHashCode
public class TransactionInstructionImpl implements TransactionInstruction {
    private final PublicKey programId;
    private final List<AccountMeta> keys;
    private final byte[] data;

    public TransactionInstructionImpl(PublicKey programId, List<AccountMeta> keys, byte[] data) {
        this.programId = Objects.requireNonNull(programId, "Program ID cannot be null");
        this.keys = Collections.unmodifiableList(Objects.requireNonNull(keys, "Keys cannot be null"));
        this.data = Arrays.copyOf(Objects.requireNonNull(data, "Data cannot be null"), data.length);
    }
}
