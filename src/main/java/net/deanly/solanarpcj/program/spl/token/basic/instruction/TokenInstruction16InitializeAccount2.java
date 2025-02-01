package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import lombok.*;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.system.Sysvar;
import net.deanly.solanarpcj.program.spl.token.basic.SplTokenProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solanarpcj.layout.field.PublicKeyField;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * TokenInstruction16InitializeAccount2 represents the InitializeAccount2 instruction for index 16
 * in the Token Program. This initializes a token account with owner information passed in the instruction data.
 *
 * Accounts expected:
 *   0. `[writable]` The account to initialize.
 *   1. `[]` The mint this account will be associated with.
 *   2. `[]` Rent sysvar.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction16InitializeAccount2 extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 16; // Discriminator for InitializeAccount2 instruction (index 16).

    @Setter
    @StructField(order = 2, type = PublicKeyField.class)
    private PublicKey owner; // The owner of the initialized account.

    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Sets the accounts (`keys`) for the InitializeAccount2 instruction.
     *
     * @param account The writable account to initialize.
     * @param mint The associated mint for the account.
     * @param rent The Rent sysvar account (uses default SysvarRent if null).
     */
    public void setKeys(PublicKey account, PublicKey mint, PublicKey rent) {
        // Validate inputs
        if (account == null || mint == null) {
            throw new IllegalArgumentException("Account and mint must not be null.");
        }

        // Fallback for rent if null
        if (rent == null) {
            rent = Sysvar.SYSVAR_RENT_ADDRESS; // Use default Rent sysvar
        }

        // Set keys using JavaScript's isWritable and isSigner logic
        this.keys = new ArrayList<>();
        this.keys.add(new AccountMeta(account, false, true));  // Writable account to initialize
        this.keys.add(new AccountMeta(mint, false, false));    // Read-only mint
        this.keys.add(new AccountMeta(rent, false, false));    // Read-only Rent sysvar
    }

    @Override
    public List<AccountMeta> getKeys() {
        if (this.keys == null || this.keys.isEmpty()) {
            throw new IllegalStateException("Account metadata (keys) must be set before building the transaction.");
        }
        return this.keys;
    }

    @Override
    public byte[] getData() {
        // Encodes the discriminator and owner fields.
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        // Decode the structure fields (discriminator and owner only, as keys must be set explicitly).
        TokenInstruction16InitializeAccount2 decoded = StructLayout.decode(data, TokenInstruction16InitializeAccount2.class);
        this.owner = decoded.getOwner();
    }


    /**
     * Static factory method to create and configure an InitializeAccount2 instruction.
     *
     * @param account The writable account to be initialized.
     * @param mint The associated mint for the account.
     * @param owner The owner of the new account.
     * @param rent Optional: The Rent sysvar account (defaults to SysvarRent if null).
     * @return A fully configured `TokenInstruction16InitializeAccount2` object.
     */
    public static TokenInstruction16InitializeAccount2 create(
            PublicKey account,
            PublicKey mint,
            PublicKey owner,
            PublicKey rent
    ) {
        // Validate mandatory inputs
        if (account == null || mint == null || owner == null) {
            throw new IllegalArgumentException("Account, mint, and owner must not be null.");
        }

        // Create and configure the instruction
        TokenInstruction16InitializeAccount2 instruction = new TokenInstruction16InitializeAccount2();
        instruction.setOwner(owner);
        instruction.setKeys(account, mint, rent); // Set account metadata (keys)
        return instruction;
    }
}