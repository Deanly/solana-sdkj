package net.deanly.solanarpcj.program.alt.instruction;

import lombok.*;
import net.deanly.solanarpcj.program.alt.AddressLookupTableProgram;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt32LEField;
import net.deanly.structlayout.type.basic.UInt64LEField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.system.Sysvar;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ATLInstruction0CreateLookupTable extends AddressLookupTableProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 0; // Index (0) for CreateLookupTable

    @StructField(order = 2, type = UInt64LEField.class)
    private long recentSlot; // Slot number for lookup table creation

    @StructField(order = 3, type = UInt8Field.class)
    private int bumpSeed; // Bump seed used for address derivation

    private List<AccountMeta> keys; // Accounts involved in the transaction

    /**
     * Sets the accounts required for the CreateLookupTable instruction.
     *
     * @param authority The authority account (signer)
     * @param payer The payer account (signer and writable)
     */
    public void setKeys(PublicKey authority, PublicKey payer) {
        // Derive the address for the lookup table
        PublicKey derivedAddress = PublicKey.findProgramAddress(
                List.of(authority.toByteArray(), ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(recentSlot).array()),
                this.getProgramId()
        ).getAddress();

        // Define the keys for the transaction
        this.keys = List.of(
                new AccountMeta(derivedAddress, true, false), // Derived Address (Writable, Not Signer)
                new AccountMeta(authority, false, true), // Authority (Signer, Not Writable)
                new AccountMeta(payer, true, true), // Payer (Signer and Writable)
                new AccountMeta(Sysvar.SYSVAR_CLOCK_ADDRESS, false, false) // Sysvar.Clock (Readonly)
        );
    }

    public byte[] getData() {
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        ATLInstruction0CreateLookupTable instruction = StructLayout.decode(data, ATLInstruction0CreateLookupTable.class);
        this.recentSlot = instruction.getRecentSlot();
        this.bumpSeed = instruction.getBumpSeed();
        this.keys = instruction.getKeys();
    }
}