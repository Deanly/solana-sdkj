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
                new AccountMeta(lookupTable, false, true),  // Lookup Table (Writable, Not Signer)
                new AccountMeta(authority, true, false),   // Authority (Signer, Not Writable)
                new AccountMeta(recipient, false, true)    // Recipient (Writable, Not Signer)
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

    /**
     * Creates a new instance of ATLInstruction4CloseLookupTable with the specified accounts.
     *
     * @param lookupTable The public key of the lookup table to close. This must be writable and is not a signer.
     * @param authority The public key of the authority account responsible for the close operation. This must be a signer and is not writable.
     * @param recipient The public key of the recipient account that will receive lamports. This must be writable and is not a signer.
     * @return A configured ATLInstruction4CloseLookupTable object representing the CloseLookupTable instruction.
     */
    public static ATLInstruction4CloseLookupTable create(
            @NonNull PublicKey lookupTable,
            @NonNull PublicKey authority,
            @NonNull PublicKey recipient
    ) {
        ATLInstruction4CloseLookupTable instruction = new ATLInstruction4CloseLookupTable();
        instruction.setKeys(lookupTable, authority, recipient);
        return instruction;
    }
}