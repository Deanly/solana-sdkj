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
public class ATLInstruction1FreezeLookupTable extends AddressLookupTableProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 1; // Index (1) for FreezeLookupTable

    private List<AccountMeta> keys; // Accounts involved in the transaction

    /**
     * Sets the accounts required for the FreezeLookupTable instruction.
     *
     * @param lookupTable The public key of the lookup table to be frozen
     * @param authority The public key of the authority (signer)
     */
    public void setKeys(PublicKey lookupTable, PublicKey authority) {
        this.keys = List.of(
                new AccountMeta(lookupTable, true, true), // Lookup Table (Writable)
                new AccountMeta(authority, true, false) // Authority (Signer, Not Writable)
        );
    }

    public byte[] getData() {
        return StructLayout.encode(this);
    }

    /**
     * Creates a new instance of {@code ATLInstruction1FreezeLookupTable} configured
     * for the FreezeLookupTable operation with the specified lookup table and authority.
     *
     * @param lookupTable The public key of the lookup table to be frozen (cannot be null).
     * @param authority The public key of the authority responsible for the FreezeLookupTable operation (cannot be null).
     * @return A new instance of {@code ATLInstruction1FreezeLookupTable} with the specified keys set.
     * @throws IllegalArgumentException If either {@code lookupTable} or {@code authority} is null.
     */
    public static ATLInstruction1FreezeLookupTable create(
            @NonNull PublicKey lookupTable,
            @NonNull PublicKey authority) {
        // Input validation
        if (authority == null || authority == null) {
            throw new IllegalArgumentException("LookupTable and authority cannot be null.");
        }
        ATLInstruction1FreezeLookupTable instruction = new ATLInstruction1FreezeLookupTable();
        instruction.setKeys(lookupTable, authority);
        return instruction;
    }
}