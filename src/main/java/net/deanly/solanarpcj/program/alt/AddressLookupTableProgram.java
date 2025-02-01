package net.deanly.solanarpcj.program.alt;

import lombok.Getter;
import lombok.NonNull;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.layout.Struct;
import net.deanly.solanarpcj.program.alt.instruction.*;

import java.util.List;

public abstract class AddressLookupTableProgram {
    public static final PublicKey PROGRAM_ID = new PublicKey("AddressLookupTab1e1111111111111111111111111");

    @Getter
    public static class Base extends Struct {
        private final PublicKey programId = PROGRAM_ID;
    }


    /**
     * Static method to create a new Address Lookup Table.
     */
    public static ATLInstruction0CreateLookupTable createLookupTable(
            @NonNull PublicKey authority,
            @NonNull PublicKey payer,
            long recentSlot
    ) {
        return ATLInstruction0CreateLookupTable.create(authority, payer, recentSlot);
    }

    /**
     * Static method to freeze an Address Lookup Table.
     */
    public static ATLInstruction1FreezeLookupTable freezeLookupTable(
            @NonNull PublicKey lookupTable,
            @NonNull PublicKey authority
    ) {
        return ATLInstruction1FreezeLookupTable.create(lookupTable, authority);
    }

    /**
     * Static method to extend an Address Lookup Table with additional addresses.
     */
    public static ATLInstruction2ExtendLookupTable extendLookupTable(
            @NonNull PublicKey lookupTable,
            @NonNull PublicKey authority,
            PublicKey payer,
            @NonNull List<PublicKey> addresses
    ) {
        return ATLInstruction2ExtendLookupTable.create(lookupTable, authority, payer, addresses);
    }

    /**
     * Static method to deactivate an Address Lookup Table.
     */
    public static ATLInstruction3DeactivateLookupTable deactivateLookupTable(
            @NonNull PublicKey lookupTable,
            @NonNull PublicKey authority
    ) {
        return ATLInstruction3DeactivateLookupTable.create(lookupTable, authority);
    }

    /**
     * Static method to close an Address Lookup Table.
     */
    public static ATLInstruction4CloseLookupTable closeLookupTable(
            @NonNull PublicKey lookupTable,
            @NonNull PublicKey authority,
            @NonNull PublicKey recipient
    ) {
        return ATLInstruction4CloseLookupTable.create(lookupTable, authority, recipient);
    }
}
