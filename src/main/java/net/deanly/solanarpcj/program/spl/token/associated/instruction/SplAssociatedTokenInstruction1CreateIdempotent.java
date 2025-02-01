package net.deanly.solanarpcj.program.spl.token.associated.instruction;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.deanly.solanarpcj.program.spl.token.basic.SplTokenProgram;
import net.deanly.solanarpcj.program.system.account.SystemProgram;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.spl.token.associated.SplAssociatedTokenProgram;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt8Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an idempotent instruction for creating an associated token account
 * in the Solana blockchain via the Associated Token Program.
 *
 * <p>
 * This instruction ensures that creating an associated token account is idempotent,
 * meaning if the account already exists, the instruction will not fail but confirm
 * the existing account. The associated token account is tied to a specific wallet
 * address and token mint, functioning as a unique storage for token balances.
 * </p>
 *
 * <p>
 * The {@code AssociatedTokenInstruction01CreateIdempotent} class implements the
 * serialization, deserialization, and account metadata setup for creating the
 * associated token account. It uses the discriminator field to uniquely identify
 * the instruction type and ensures proper ordering of accounts in the Solana
 * transaction.
 * </p>
 */
@Getter
@NoArgsConstructor
public class SplAssociatedTokenInstruction1CreateIdempotent extends SplAssociatedTokenProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt8Field.class)
    private final int discriminator = 1; // Fixed identifier for this instruction.

    @Setter
    private List<AccountMeta> keys = new ArrayList<>(); // Key meta information.

    /**
     * Set account metadata and order used in the accounts field for Solana.
     *
     * @param payer Funding account (writer & signer).
     * @param owner Wallet address for the new associated token account (readonly).
     * @param mint The token mint (readonly).
     */
    public void setKeys(
            PublicKey payer,
            PublicKey owner,
            PublicKey mint
    ) {
        this.keys.clear();
        PublicKey ata = findAssociatedTokenAddress(owner, mint);
        this.keys.add(new AccountMeta(payer, true, true)); // payer: writable & signer
        this.keys.add(new AccountMeta(ata, false, true)); // ata: writable
        this.keys.add(new AccountMeta(owner, false, false)); // owner: readonly
        this.keys.add(new AccountMeta(mint, false, false)); // mint: readonly
        this.keys.add(new AccountMeta(SystemProgram.PROGRAM_ID, false, false)); // systemProgram: readonly
        this.keys.add(new AccountMeta(SplTokenProgram.PROGRAM_ID, false, false)); // tokenProgram: readonly
    }

    /**
     * Retrieve account metadata for the instruction.
     */
    public List<AccountMeta> getKeys() {
        return keys != null ? Collections.unmodifiableList(keys) : Collections.emptyList();
    }

    /**
     * Serialize the current struct as a byte array.
     */
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    /**
     * Factory method to create an instance of this instruction class.
     *
     * @param payer Funding public key.
     * @param owner Owner public key.
     * @param mint Token mint public key.
     * @return Instance of AssociatedTokenInstruction01CreateIdempotent.
     */
    public static SplAssociatedTokenInstruction1CreateIdempotent create(
            PublicKey payer,
            PublicKey owner,
            PublicKey mint
    ) {
        SplAssociatedTokenInstruction1CreateIdempotent instruction = new SplAssociatedTokenInstruction1CreateIdempotent();
        instruction.setKeys(payer, owner, mint);
        return instruction;
    }
}