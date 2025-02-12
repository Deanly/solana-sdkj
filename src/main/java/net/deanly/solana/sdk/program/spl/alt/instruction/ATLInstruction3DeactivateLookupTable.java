package net.deanly.solana.sdk.program.spl.alt.instruction;

import lombok.*;
import net.deanly.solana.sdk.program.spl.alt.AddressLookupTableProgram;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt32LEField;
import net.deanly.solana.sdk.crypto.PublicKey;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ATLInstruction3DeactivateLookupTable extends AtlInstructionBase implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 3; // Index (3) for DeactivateLookupTable

    private List<AccountMeta> keys; // Accounts involved in the transaction

    /**
     * Sets the accounts required for the DeactivateLookupTable instruction.
     *
     * @param lookupTable The public key of the lookup table to deactivate
     * @param authority The public key of the authority (signer)
     */
    public void setKeys(PublicKey lookupTable, PublicKey authority) {
        this.keys = List.of(
                new AccountMeta(lookupTable, false, true), // Lookup Table (Writable, Not Signer)
                new AccountMeta(authority, true, false) // Authority (Signer, Not Writable)
        );
    }

    public byte[] getData() {
        return StructLayout.encode(this);
    }

    /**
     * Creates a new instance of {@code ATLInstruction3DeactivateLookupTable} and sets the required keys
     * for the DeactivateLookupTable instruction.
     *
     * @param lookupTable The public key of the lookup table to deactivate.
     * @param authority The public key of the authority (signer) responsible for the lookup table.
     * @return A new instance of {@code ATLInstruction3DeactivateLookupTable} initialized with the provided keys.
     */
    public static ATLInstruction3DeactivateLookupTable create(
            @NonNull PublicKey lookupTable,
            @NonNull PublicKey authority) {
        ATLInstruction3DeactivateLookupTable instruction = new ATLInstruction3DeactivateLookupTable();
        instruction.setKeys(lookupTable, authority);
        return instruction;
    }
}