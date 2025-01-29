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
public class ATLInstruction3DeactivateLookupTable extends AddressLookupTableProgram.Base implements TransactionInstruction {

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
                new AccountMeta(lookupTable, true, false), // Lookup Table (Writable, Not Signer)
                new AccountMeta(authority, false, true) // Authority (Signer, Not Writable)
        );
    }

    public byte[] getData() {
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        ATLInstruction3DeactivateLookupTable instruction = StructLayout.decode(data, ATLInstruction3DeactivateLookupTable.class);
        this.keys = instruction.getKeys();
    }
}