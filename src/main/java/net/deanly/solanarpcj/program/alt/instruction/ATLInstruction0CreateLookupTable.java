package net.deanly.solanarpcj.program.alt.instruction;

import lombok.*;
import net.deanly.solanarpcj.program.alt.AddressLookupTableProgram;
import net.deanly.solanarpcj.program.pda.ProgramDerivedAddress;
import net.deanly.solanarpcj.program.system.account.SystemProgram;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt32LEField;
import net.deanly.structlayout.type.basic.UInt64LEField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solanarpcj.crypto.PublicKey;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ATLInstruction0CreateLookupTable extends AddressLookupTableProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 0; // Index (0) for CreateLookupTable

    @Setter
    @StructField(order = 2, type = UInt64LEField.class)
    private long recentSlot; // Slot number for lookup table creation

    @Setter(AccessLevel.PRIVATE)
    @StructField(order = 3, type = UInt8Field.class)
    private int bumpSeed; // Bump seed used for address derivation

    @Setter
    private List<AccountMeta> keys; // Accounts involved in the transaction

    /**
     * Sets the keys required for the CreateLookupTable instruction by calculating the PDA.
     *
     * @param authority The authority account (signer)
     * @param payer     The payer account (signer and writable)
     * @param recentSlot The recent slot for PDA derivation
     */
    public void setKeys(PublicKey authority, PublicKey payer, long recentSlot) {
        // Input validation
        if (authority == null || payer == null) {
            throw new IllegalArgumentException("Authority and Payer cannot be null.");
        }
        if (recentSlot < 0) {
            throw new IllegalArgumentException("recentSlot cannot be negative.");
        }

        // Derive the PDA (lookupTableAddress) using the authority and recentSlot
        ProgramDerivedAddress derivedAddress = PublicKey.findProgramAddress(
                List.of(
                        authority.toByteArray(),
                        ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(recentSlot).array()
                ),
                this.getProgramId()
        );

        // Set fields for the instruction
        this.setRecentSlot(recentSlot);
        this.setBumpSeed(derivedAddress.getNonce()); // Bump seed for the derived address

        // Define the keys for the transaction
        this.keys = List.of(
                new AccountMeta(derivedAddress.getAddress(), false, true), // LookupTable (Writable, Not Signer)
                new AccountMeta(authority, true, false),                  // Authority (Not Writable, Signer)
                new AccountMeta(payer, true, true),                       // Payer (Writable, Signer)
                new AccountMeta(SystemProgram.PROGRAM_ID, false, false)   // System Program (Not Writable, Not Signer)
        );
    }

    public byte[] getData() {
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        ATLInstruction0CreateLookupTable instruction = StructLayout.decode(data, ATLInstruction0CreateLookupTable.class);
        this.recentSlot = instruction.getRecentSlot();
        this.bumpSeed = instruction.getBumpSeed();
    }


    /**
     * Static factory for creating a new instruction.
     *
     * @param authority   The public key of the authority (Non-Writable, Signer).
     * @param payer       The public key of the payer (Writable, Signer).
     * @param recentSlot  The recent slot for the derivation path (UInt64).
     * @return An instance of ATLInstructionCreateLookupTable initialized with the provided parameters.
     */
    public static ATLInstruction0CreateLookupTable create(
            @NonNull PublicKey authority,
            @NonNull PublicKey payer,
            long recentSlot
    ) {
        // Input validation
        if (authority == null || payer == null) {
            throw new IllegalArgumentException("Authority and Payer cannot be null.");
        }
        if (recentSlot < 0) {
            throw new IllegalArgumentException("recentSlot cannot be negative.");
        }

        // Create and initialize the instruction
        ATLInstruction0CreateLookupTable instruction = new ATLInstruction0CreateLookupTable();
        instruction.setKeys(authority, payer, recentSlot);
        return instruction;
    }
}