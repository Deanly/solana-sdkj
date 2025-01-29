package net.deanly.solanarpcj.program.alt.instruction;

import lombok.*;
import net.deanly.solanarpcj.program.alt.AddressLookupTableProgram;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.layout.field.PublicKeyField;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.type.basic.UInt32LEField;
import net.deanly.structlayout.type.basic.UInt64LEField;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ATLInstruction2ExtendLookupTable extends AddressLookupTableProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 2; // Index (2) for ExtendLookupTable

    @StructSequenceField(
            order = 2,
            elementType = PublicKeyField.class,
            lengthType = UInt64LEField.class
    )
    private List<PublicKey> addresses; // List of PublicKeys to add to the LookupTable

    private List<AccountMeta> keys; // Accounts involved in the transaction

    /**
     * Sets the accounts required for the ExtendLookupTable instruction.
     *
     * @param lookupTable The public key of the lookup table to extend (Writable)
     * @param authority   The public key of the authority (Signatory)
     * @param payer       The public key of the payer (Optional, Writable and Signatory)
     * @param addresses   The list of public keys to add to the lookup table
     */
    public void setKeys(PublicKey lookupTable, PublicKey authority, PublicKey payer, List<PublicKey> addresses) {
        this.addresses = new ArrayList<>(addresses); // Add addresses to the instruction

        this.keys = new ArrayList<>();
        this.keys.add(new AccountMeta(lookupTable, true, false)); // Lookup Table (Writable, Non-Signatory)
        this.keys.add(new AccountMeta(authority, false, true));  // Authority (Signatory, Non-Writable)
        if (payer != null) {
            this.keys.add(new AccountMeta(payer, true, true));   // Payer (Optional)
        }
    }

    public byte[] getData() {
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        ATLInstruction2ExtendLookupTable instruction =
                StructLayout.decode(data, ATLInstruction2ExtendLookupTable.class);
        this.addresses = instruction.getAddresses();
        this.keys = instruction.getKeys();
    }
}