package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import lombok.*;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.spl.token.basic.SplTokenProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt64LEField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TokenInstruction07MintTo represents the MintTo instruction for index 7
 * in the Token Program. It mints new tokens to an account.
 *
 * <pre>
 * Accounts expected:
 *   * Single authority:
 *     0. `[writable]` The mint.
 *     1. `[writable]` The account to mint tokens to.
 *     2. `[signer]` The mint's minting authority.
 *
 *   * Multisignature authority:
 *     0. `[writable]` The mint.
 *     1. `[writable]` The account to mint tokens to.
 *     2. `[]` The mint's multisignature mint-tokens authority.
 *     3. ..`3+M` `[signer]` M signer accounts.
 * </pre>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction07MintTo extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 7; // Discriminator for MintTo instruction (index 7).

    @Setter
    @StructField(order = 2, type = UInt64LEField.class)
    private long amount; // The amount of tokens to mint.

    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.


    /**
     * Configures account metadata (keys) for the MintTo instruction based on input parameters.
     * This method dynamically handles both single authority and multisignature authority.
     *
     * @param mint          The mint account's public key (writable).
     * @param destination   The account to mint tokens to (writable).
     * @param mintAuthority The mint authority or multisignature authority's public key.
     * @param multiSigners  Optional: List of signers for multisignature authority.
     */
    public void setKeys(PublicKey mint, PublicKey destination, PublicKey mintAuthority, List<PublicKey> multiSigners) {
        // Validate inputs
        if (mint == null || destination == null || mintAuthority == null) {
            throw new IllegalArgumentException("Mint, destination, and mint authority cannot be null.");
        }

        // Initialize keys
        keys = new ArrayList<>();
        keys.add(new AccountMeta(mint, false, true));           // Mint: writable, not signer
        keys.add(new AccountMeta(destination, false, true));    // Destination: writable, not signer
        keys.add(new AccountMeta(mintAuthority, true, false));  // Mint authority: signer, read-only

        // Add multiSigners if provided
        if (multiSigners != null && !multiSigners.isEmpty()) {
            for (PublicKey signer : multiSigners) {
                keys.add(new AccountMeta(signer, true, false)); // Signers: signer, read-only
            }
        }
    }
    /**
     * Returns the account metadata for this instruction.
     *
     * @return List of accounts used in this transaction.
     */
    @Override
    public List<AccountMeta> getKeys() {
        return keys == null ? Collections.emptyList() : keys;
    }

    /**
     * Encodes the instruction data for MintTo.
     *
     * @return Encoded byte array for this instruction.
     */
    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    /**
     * Decodes the instruction data from a byte array.
     *
     * @param data Byte array to decode.
     */
    public void setData(byte[] data) {
        TokenInstruction07MintTo decoded = StructLayout.decode(data, TokenInstruction07MintTo.class);
        this.amount = decoded.getAmount();
    }


    /**
     * Static factory method to create a configured `TokenInstruction07MintTo` instance.
     *
     * @param mint          The mint account's public key (writable).
     * @param destination   The destination account's public key (writable).
     * @param mintAuthority The mint authority's public key (signer).
     * @param amount        The amount of tokens to mint.
     * @param multiSigners  Optional: List of multisignature signer public keys.
     * @return Configured `TokenInstruction07MintTo` instance.
     */
    public static TokenInstruction07MintTo create(
            PublicKey mint,
            PublicKey destination,
            PublicKey mintAuthority,
            long amount,
            List<PublicKey> multiSigners
    ) {
        // Validate inputs
        if (mint == null || destination == null || mintAuthority == null || amount <= 0) {
            throw new IllegalArgumentException("Invalid parameters: mint, destination, mintAuthority cannot be null, and amount must be positive.");
        }

        // Create instruction instance
        TokenInstruction07MintTo instruction = new TokenInstruction07MintTo();
        instruction.setAmount(amount);
        instruction.setKeys(mint, destination, mintAuthority, multiSigners);

        return instruction;
    }
}