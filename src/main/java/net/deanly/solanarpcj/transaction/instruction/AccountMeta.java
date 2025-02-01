package net.deanly.solanarpcj.transaction.instruction;

import lombok.*;
import net.deanly.solanarpcj.crypto.PublicKey;

/*lw*
 * A class representing metadata associated with a Solana account.
 * <p>
 * This metadata includes information about the account's public key,
 * whether the account is writable, and whether the account is required to
 * sign a transaction. It plays a critical role in defining the behavior
 * and access control for accounts during transactions in the Solana blockchain.
 * </p>
 *
 * <p>
 * The {@link AccountMeta} class is immutable and ensures that all fields are
 * initialized at creation time. It is often used in conjunction with
 * transaction instructions to define how accounts interact during Solana
 * blockchain operations.
 * </p>
 */
@Data
@ToString
@EqualsAndHashCode
public class AccountMeta {
    /**
     * Represents the public key associated with the account metadata.
     * <p>
     * The {@code publicKey} is a unique identifier for a Solana account,
     * ensuring access to its associated data and functionality.
     * It uses the Ed25519 curve for cryptographic purposes and maintains a
     * constant size of 32 bytes. This field is immutable and must conform to
     * the expected format and length requirements for a valid public key.
     */
    private final PublicKey publicKey;

    /**
     * Indicates whether the associated account can be modified by a transaction.
     * <p>
     * If {@code isWritable} is {@code true}, the account can be written to during
     * the execution of a transaction. This flag determines the mutability of the
     * account state and is critical for ensuring proper transaction behavior and
     * network rules compliance in the Solana blockchain.
     */
    private final boolean isWritable;

    /**
     * Indicates whether the associated account has signing authority for a transaction.
     * <p>
     * If {@code isSigner} is {@code true}, this flag determines that the account's
     * corresponding private key must be used to sign the transaction. This is a
     * critical parameter for ensuring the integrity and authenticity of transactions
     * in the Solana blockchain.
     */
    private final boolean isSigner;

    public AccountMeta(PublicKey publicKey, boolean isSigner, boolean isWritable) {
        this.publicKey = publicKey;
        this.isWritable = isWritable;
        this.isSigner = isSigner;
    }

    public static AccountMeta roleReadOnlySigner(PublicKey publicKey) {
        return new AccountMeta(publicKey, true, false);
    }

    public static AccountMeta roleReadOnlyNoSigner(PublicKey publicKey) {
        return new AccountMeta(publicKey, false, false);
    }

    public static AccountMeta roleWritableSigner(PublicKey publicKey) {
        return new AccountMeta(publicKey, true, true);
    }

    public static AccountMeta roleWritableNoSigner(PublicKey publicKey) {
        return new AccountMeta(publicKey, false, true);
    }
}