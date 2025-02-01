package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import lombok.*;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.layout.field.AuthorityTypeField;
import net.deanly.solanarpcj.layout.field.PublicKeyBorshOptionField;
import net.deanly.solanarpcj.program.spl.token.basic.SplTokenProgram;
import net.deanly.solanarpcj.program.spl.token.basic.type.AuthorityType;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TokenInstruction06SetAuthority represents the SetAuthority instruction for index 6
 * in the Token Program. It sets a new authority on a mint or an account.
 *
 * Accounts expected:
 *   Single authority:
 *     0. `[writable]` The account or mint to change authority.
 *     1. `[signer]` The current authority of the account or mint.
 *
 *   Multisignature:
 *     0. `[writable]` The account or mint to change authority.
 *     1. `[]` The current multisignature authority.
 *     2+ `[signer]` M signer accounts.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction06SetAuthority extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 6; // Discriminator for SetAuthority instruction (index 6).

    @Setter
    @StructField(order = 2, type = AuthorityTypeField.class)
    private AuthorityType authorityType; // The type of the authority being updated.

    @Setter
    @StructField(order = 3, type = PublicKeyBorshOptionField.class)
    private PublicKey newAuthority; // The new authority to be set, null for withdrawing authority.

    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Configures `keys` for the instruction based on input accounts and multi-signers.
     *
     * @param account Owned account or mint (writable).
     * @param owner Current authority (signer, read-only).
     * @param multiSigners Optional: Additional signer accounts (read-only).
     */
    public void setKeys(PublicKey account, PublicKey owner, List<PublicKey> multiSigners) {
        // Input validation
        if (account == null || owner == null) {
            throw new IllegalArgumentException("Account and owner cannot be null.");
        }

        // Initialize keys
        keys = new ArrayList<>();
        // Add owned account: writable, not signer
        keys.add(new AccountMeta(account, false, true));
        // Add owner: read-only, signer
        keys.add(new AccountMeta(owner, true, false));
        // Add multi-signers: read-only, signers
        if (multiSigners != null && !multiSigners.isEmpty()) {
            for (PublicKey signer : multiSigners) {
                keys.add(new AccountMeta(signer, true, false));
            }
        }
    }

    /**
     * Provides account metadata for this instruction.
     *
     * @return List of accounts used in this transaction.
     */
    @Override
    public List<AccountMeta> getKeys() {
        return keys == null ? Collections.emptyList() : keys;
    }

    /**
     * Encodes the instruction data for SetAuthority.
     *
     * @return Encoded byte array for this instruction.
     */
    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    /**
     * Decodes and initializes the instruction data from a byte array.
     *
     * @param data Byte array to decode.
     */
    public void setData(byte[] data) {
        TokenInstruction06SetAuthority decoded = StructLayout.decode(data, TokenInstruction06SetAuthority.class);
        this.authorityType = decoded.authorityType;
        this.newAuthority = decoded.newAuthority;
    }

    /**
     * Static factory method to create a configured `TokenInstruction06SetAuthority` instance.
     *
     * @param account Owned account or mint to be updated.
     * @param owner Current authority (signer).
     * @param authorityType Type of authority to be updated.
     * @param newAuthority Optional: New authority to be set.
     * @param multiSigners Optional: Multi-signature signer keys.
     * @return Configured `TokenInstruction06SetAuthority` instance.
     */
    public static TokenInstruction06SetAuthority create(
            PublicKey account,
            PublicKey owner,
            AuthorityType authorityType,
            PublicKey newAuthority,
            List<PublicKey> multiSigners
    ) {
        // Validate required parameters
        if (account == null || owner == null || authorityType == null) {
            throw new IllegalArgumentException("Account, owner, and authorityType cannot be null.");
        }

        // Initialize instruction
        TokenInstruction06SetAuthority instruction = new TokenInstruction06SetAuthority();
        instruction.authorityType = authorityType;
        instruction.newAuthority = newAuthority;

        // Set account keys
        instruction.setKeys(account, owner, multiSigners);

        return instruction;
    }

}