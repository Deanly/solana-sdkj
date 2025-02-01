package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import lombok.*;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.spl.token.basic.SplTokenProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the InitializeMultisig2 instruction (index 19) for the Token Program.
 * This initializes a multisignature token account without requiring the Rent sysvar to be provided.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction19InitializeMultisig2 extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 19; // Discriminator for InitializeMultisig2 instruction.

    @Setter
    @StructField(order = 2, type = UInt8Field.class)
    private int m; // Number of required signers.

    /// Accounts expected by this instruction:
    ///
    ///   0. `[writable]` The multisignature account to initialize.
    ///   1. ..`1+N` `[]` The signer accounts, must equal to N where `1 <= N <=
    ///      11`.
    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // Account metadata for the instruction.

    /**
     * Sets the account metadata (keys) for the InitializeMultisig2 instruction.
     *
     * @param multisig The multisignature account to initialize (`isWritable = true`, `isSigner = false`).
     * @param signers  The list of signers for the multisignature account.
     */
    public void setKeys(PublicKey multisig, List<PublicKey> signers) {
        if (multisig == null || signers == null || signers.isEmpty()) {
            throw new IllegalArgumentException("Multisig account and signers must not be null or empty.");
        }
        if (signers.size() > 11) {
            throw new IllegalArgumentException("Number of signers must be less than or equal to 11.");
        }

        // Initialize keys for the multisig and signers
        keys = new ArrayList<>();
        keys.add(new AccountMeta(multisig, false, true)); // Multisig account; writable, not a signer.
        for (PublicKey signer : signers) {
            keys.add(new AccountMeta(signer, true, false)); // Signers; not writable, must be signers.
        }

        if (this.m < 1) {
            this.m = signers.size(); // Set the number of required signers.
        }
    }

    @Override
    public List<AccountMeta> getKeys() {
        if (keys == null) {
            return Collections.emptyList();
        }
        return this.keys;
    }

    @Override
    public byte[] getData() {
        // Serialize fields using the layout (discriminator, m, and signers).
        return StructLayout.encode(this);
    }


    /**
     * Static factory method to create a TokenInstruction19InitializeMultisig2 instance.
     *
     * @param multisig The multisignature account to initialize (`isWritable = true`, `isSigner = false`).
     * @param m        The number of signatures required for the multisig.
     * @param signers  The list of signers for the multisignature account.
     * @return A configured `TokenInstruction19InitializeMultisig2` instance.
     */
    public static TokenInstruction19InitializeMultisig2 create(PublicKey multisig, int m, List<PublicKey> signers) {
        if (multisig == null) {
            throw new IllegalArgumentException("Multisig account must not be null.");
        }
        if (m <= 0) {
            throw new IllegalArgumentException("Number of required signatures (m) must be greater than zero.");
        }
        if (signers == null || signers.isEmpty()) {
            throw new IllegalArgumentException("Signers must not be null or empty.");
        }
        if (signers.size() < m) {
            throw new IllegalArgumentException("Number of signers must be greater than or equal to m.");
        }

        // Create and configure the instruction
        TokenInstruction19InitializeMultisig2 instruction = new TokenInstruction19InitializeMultisig2();
        instruction.setM(m);
        instruction.setKeys(multisig, signers);
        return instruction;
    }

}