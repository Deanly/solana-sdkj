package net.deanly.solana.sdk.program.spl.token.instruction;

import lombok.*;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.spl.token.SplTokenProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt64LEField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * TokenInstruction14MintToChecked represents the MintToChecked instruction for index 14
 * in the Token Program. This instruction mints new tokens to a specified account
 * while validating the number of decimals for the mint.
 *
 * Accounts expected:
 *   0. `[writable]` The token account to receive the minted tokens.
 *   1. `[writable]` The mint to produce tokens from.
 *   2. `[signer]` The mint's authority (single authority).
 *   Multisignature authority:
 *     2. `[]` The multisignature authority.
 *     3+ `[signer]` M signer accounts.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenInstruction14MintToChecked extends SplTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 14; // Discriminator for MintToChecked (index 14).

    @StructField(order = 2, type = UInt64LEField.class)
    private long amount; // Amount of tokens to mint.

    @StructField(order = 3, type = UInt8Field.class)
    private int decimals; // Number of decimals for token validation.

    private List<AccountMeta> keys = new ArrayList<>(); // List of accounts required for this instruction.

    /**
     * Sets the list of account metadata (keys) for the MintToChecked instruction.
     *
     * @param mint          The mint account (writable).
     * @param token         The token account to receive minted tokens (writable).
     * @param mintAuthority The mint authority account (signer).
     * @param multiSigners  Optional: List of multisignature signer accounts.
     */
    public void setKeys(PublicKey mint, PublicKey token, PublicKey mintAuthority, List<PublicKey> multiSigners) {
        // Validate inputs
        if (mint == null || token == null || mintAuthority == null) {
            throw new IllegalArgumentException("Mint, token, and mintAuthority must not be null.");
        }

        this.keys = new ArrayList<>();

        // Add required accounts
        this.keys.add(new AccountMeta(mint, false, true)); // Mint: writable, not signer
        this.keys.add(new AccountMeta(token, false, true)); // Token: writable, not signer
        this.keys.add(new AccountMeta(mintAuthority, multiSigners == null || multiSigners.isEmpty(), false)); // MintAuthority: readonly, signer if single

        // Add multisigners (if provided)
        if (multiSigners != null && !multiSigners.isEmpty()) {
            for (PublicKey signer : multiSigners) {
                this.keys.add(new AccountMeta(signer, true, false)); // Multisigners: readonly, signer
            }
        }
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
        // Encodes the discriminator, amount, and decimals.
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        // Decode fields (discriminator, amount, decimals). Keys must be set explicitly.
        TokenInstruction14MintToChecked decoded = StructLayout.decode(data, TokenInstruction14MintToChecked.class);
        this.amount = decoded.amount;
        this.decimals = decoded.decimals;
    }


    /**
     * Static factory method to create and configure a MintToChecked instruction.
     *
     * @param mint          The mint account (writable).
     * @param token         The token account to receive minted tokens (writable).
     * @param mintAuthority The mint authority account (signer).
     * @param amount        The amount of tokens to mint.
     * @param decimals      The decimals for the mint.
     * @param multiSigners  Optional: List of multisignature signer accounts.
     * @return A configured TokenInstruction14MintToChecked instance.
     */
    public static TokenInstruction14MintToChecked create(
            PublicKey mint,
            PublicKey token,
            PublicKey mintAuthority,
            long amount,
            int decimals,
            List<PublicKey> multiSigners
    ) {
        // Validate inputs
        if (mint == null || token == null || mintAuthority == null) {
            throw new IllegalArgumentException("Mint, token, and mintAuthority must not be null.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (decimals < 0) {
            throw new IllegalArgumentException("Decimals must be a non-negative value.");
        }

        // Create and configure the instruction
        TokenInstruction14MintToChecked instruction = new TokenInstruction14MintToChecked();
        instruction.setAmount(amount);
        instruction.setDecimals(decimals);
        instruction.setKeys(mint, token, mintAuthority, multiSigners);
        return instruction;
    }
}