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
            long recentSlot,
            int bumpSeed
    ) {
        ATLInstruction0CreateLookupTable instruction = new ATLInstruction0CreateLookupTable();
        instruction.setRecentSlot(recentSlot);
        instruction.setBumpSeed(bumpSeed);
        instruction.setKeys(authority, payer);
        return instruction;
    }

    /**
     * Static method to freeze an Address Lookup Table.
     */
    public static ATLInstruction1FreezeLookupTable freezeLookupTable(
            @NonNull PublicKey lookupTable,
            @NonNull PublicKey authority
    ) {
        ATLInstruction1FreezeLookupTable instruction = new ATLInstruction1FreezeLookupTable();
        instruction.setKeys(lookupTable, authority);
        return instruction;
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
        ATLInstruction2ExtendLookupTable instruction = new ATLInstruction2ExtendLookupTable();
        instruction.setKeys(lookupTable, authority, payer, addresses);
        return instruction;
    }

    /**
     * Static method to deactivate an Address Lookup Table.
     */
    public static ATLInstruction3DeactivateLookupTable deactivateLookupTable(
            @NonNull PublicKey lookupTable,
            @NonNull PublicKey authority
    ) {
        ATLInstruction3DeactivateLookupTable instruction = new ATLInstruction3DeactivateLookupTable();
        instruction.setKeys(lookupTable, authority);
        return instruction;
    }

    /**
     * Static method to close an Address Lookup Table.
     */
    public static ATLInstruction4CloseLookupTable closeLookupTable(
            @NonNull PublicKey lookupTable,
            @NonNull PublicKey authority,
            @NonNull PublicKey recipient
    ) {
        ATLInstruction4CloseLookupTable instruction = new ATLInstruction4CloseLookupTable();
        instruction.setKeys(lookupTable, authority, recipient);
        return instruction;
    }
}
