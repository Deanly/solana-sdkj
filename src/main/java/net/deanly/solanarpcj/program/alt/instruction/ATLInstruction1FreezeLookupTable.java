package net.deanly.solanarpcj.program.alt.instruction;

import lombok.*;
import net.deanly.solanarpcj.program.alt.AddressLookupTableProgram;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt32LEField;
import net.deanly.solanarpcj.crypto.PublicKey;

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
                new AccountMeta(authority, false, true) // Authority (Signer, Not Writable)
        );
    }

    public byte[] getData() {
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        ATLInstruction1FreezeLookupTable instruction = StructLayout.decode(data, ATLInstruction1FreezeLookupTable.class);
        this.keys = instruction.getKeys();
    }
}