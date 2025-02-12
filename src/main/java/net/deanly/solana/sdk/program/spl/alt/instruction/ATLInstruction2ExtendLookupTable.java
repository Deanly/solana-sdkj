package net.deanly.solana.sdk.program.spl.alt.instruction;

import lombok.*;
import net.deanly.solana.sdk.program.spl.alt.AddressLookupTableProgram;
import net.deanly.solana.sdk.program.core.system.SystemProgram;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.field.PublicKeyField;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
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
     * Sets the keys for the ExtendLookupTable instruction.
     *
     * @param lookupTable The public key of the lookup table to extend (Writable).
     * @param authority   The public key of the authority (Signatory).
     * @param payer       Optional. The public key of the payer (Writable and Signatory).
     */
    public void setKeys(PublicKey lookupTable, PublicKey authority, PublicKey payer) {
        // Validation: Ensure major keys are not null
        if (lookupTable == null) {
            throw new IllegalArgumentException("LookupTable public key cannot be null.");
        }
        if (authority == null) {
            throw new IllegalArgumentException("Authority public key cannot be null.");
        }

        this.keys = new ArrayList<>();
        this.keys.add(new AccountMeta(lookupTable, false, true)); // Lookup Table (Writable, Non-Signatory)
        this.keys.add(new AccountMeta(authority, true, false));  // Authority (Non-Writable, Signatory)
        if (payer != null) {
            this.keys.add(new AccountMeta(payer, true, true));   // Payer (Writable, Signatory if provided)
            this.keys.add(new AccountMeta(SystemProgram.PROGRAM_ID, false, false));
        }
    }

    /**
     * Sets the addresses for the instruction.
     *
     * @param addresses The list of public keys to add to the lookup table.
     */
    public void setAddresses(List<PublicKey> addresses) {
        // Validation: Ensure addresses are not null or empty
        if (addresses == null || addresses.isEmpty()) {
            throw new IllegalArgumentException("Addresses must be a non-empty list.");
        }

        // Optional: Maximum allowed addresses (example: 256)
        if (addresses.size() > 256) {
            throw new IllegalArgumentException("Addresses list exceeds maximum allowed size of 256.");
        }

        this.addresses = new ArrayList<>(addresses);
    }

    public byte[] getData() {
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        ATLInstruction2ExtendLookupTable instruction =
                StructLayout.decode(data, ATLInstruction2ExtendLookupTable.class);
        this.addresses = instruction.getAddresses();
    }

    /**
     * Static factory for creating a new instruction.
     *
     * @param lookupTable The public key of the lookup table to extend (Writable).
     * @param authority   The public key of the authority (Signatory).
     * @param payer       Optional. The public key of the payer (Writable and Signatory).
     * @param addresses   The list of public keys to add to the lookup table.
     * @return An instance of the instruction.
     */
    public static ATLInstruction2ExtendLookupTable create(
            @NonNull PublicKey lookupTable,
            @NonNull PublicKey authority,
            PublicKey payer,
            @NonNull List<PublicKey> addresses
    ) {
        ATLInstruction2ExtendLookupTable instance = new ATLInstruction2ExtendLookupTable();
        instance.setKeys(lookupTable, authority, payer);      // Set keys
        instance.setAddresses(addresses);                    // Set addresses
        return instance;
    }
}