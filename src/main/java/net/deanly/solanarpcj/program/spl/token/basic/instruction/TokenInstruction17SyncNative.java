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
 * TokenInstruction17SyncNative represents the SyncNative instruction for index 17
 * in the Token Program. This synchronizes a wrapped native SOL account's amount
 * field with its underlying lamports balance.
 *
 * Accounts expected:
 *   0. `[writable]` The native token account to sync with its underlying lamports.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction17SyncNative extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 17; // Discriminator for SyncNative instruction (index 17).

    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Sets the account metadata (keys) for the SyncNative instruction.
     *
     * @param nativeAccount The writable native token account to synchronize.
     */
    public void setKeys(PublicKey nativeAccount) {
        // Validate input
        if (nativeAccount == null) {
            throw new IllegalArgumentException("Native account must not be null.");
        }

        // Set keys with correct isWritable and isSigner flags
        this.keys = Collections.singletonList(
                new AccountMeta(nativeAccount, true, false)  // Writable native account; not a signer
        );
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
        // Encodes only the discriminator, as no other data fields are required for this instruction.
        return StructLayout.encode(this);
    }

    /**
     * Static factory method to create and configure a SyncNative instruction.
     *
     * @param nativeAccount The writable native token account to synchronize.
     * @return A fully configured `TokenInstruction17SyncNative` object.
     */
    public static TokenInstruction17SyncNative create(PublicKey nativeAccount) {
        // Validate required parameters
        if (nativeAccount == null) {
            throw new IllegalArgumentException("Native account cannot be null.");
        }

        // Create and configure the SyncNative instruction
        TokenInstruction17SyncNative instruction = new TokenInstruction17SyncNative();
        instruction.setKeys(nativeAccount); // Set account metadata (keys)
        return instruction;
    }
}