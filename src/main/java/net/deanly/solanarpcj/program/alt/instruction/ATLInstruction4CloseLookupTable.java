package net.deanly.solanarpcj.program.alt.instruction;

import lombok.*;
import net.deanly.solanarpcj.program.alt.AddressLookupTableProgram;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt32LEField;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ATLInstruction4CloseLookupTable extends AddressLookupTableProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 4; // Index (4) for CloseLookupTable

    private List<AccountMeta> keys; // Accounts involved in the instruction

    /**
     * Sets the accounts required for the CloseLookupTable instruction.
     *
     * @param lookupTable The public key of the lookup table to close
     * @param authority The authority account (signer) for the close operation
     * @param recipient The recipient account (writable) that will receive lamports
     */
    public void setKeys(PublicKey lookupTable, PublicKey authority, PublicKey recipient) {
        this.keys = List.of(
                new AccountMeta(lookupTable, true, false),  // Lookup Table (Writable, Not Signer)
                new AccountMeta(authority, false, true),   // Authority (Signer, Not Writable)
                new AccountMeta(recipient, true, false)    // Recipient (Writable, Not Signer)
        );
    }

    /**
     * Encodes the instruction data.
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
        ATLInstruction4CloseLookupTable instruction = StructLayout.decode(data, ATLInstruction4CloseLookupTable.class);
        this.keys = instruction.getKeys();
    }

    /**
     * Gets the keys/accounts involved in this instruction.
     *
     * @return A list of AccountMeta objects representing the keys
     */
    @Override
    public List<AccountMeta> getKeys() {
        return this.keys;
    }
}