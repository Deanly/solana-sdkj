package net.deanly.solana.sdk.program.system.account.instruction;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.field.PublicKeyField;
import net.deanly.solana.sdk.layout.field.RustStringField;
import net.deanly.solana.sdk.program.system.account.SystemProgram;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt32LEField;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a System program AssignWithSeed instruction in the Solana blockchain.
 * This instruction assigns a derived account to a program using a base public key and a seed string.
 *
 * <pre>
 * Fields:
 * - {@code instruction}: Constant index (10) representing the AssignWithSeed instruction.
 * - {@code keys}: A list of accounts involved in the transaction (includes the derived account).
 * - {@code base}: Base public key used for deriving the account.
 * - {@code seed}: Seed string used to derive the account.
 * - {@code programId}: The program ID to which the derived account is assigned.
 *
 * Methods:
 * - {@code getData()}: Encodes the instruction fields into a byte array.
 * - {@code setData(byte[] data)}: Decodes the given byte array to populate this instruction's attributes.
 * </pre>
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemInstruction10AssignWithSeed extends SystemProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 10; // Instruction index (10) for AssignWithSeed

    @StructField(order = 2, type = PublicKeyField.class)
    private PublicKey base; // Base public key for seed-derived account

    @StructField(order = 3, type = RustStringField.class)
    private String seed; // Seed string for deriving the account

    @StructField(order = 4, type = PublicKeyField.class)
    private PublicKey ownerProgramId; // The program ID to assign the derived account to

    private List<AccountMeta> keys = new ArrayList<>(); // Accounts involved (mainly derived account)

    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        SystemInstruction10AssignWithSeed instruction = StructLayout.decode(data, SystemInstruction10AssignWithSeed.class);
        this.keys = instruction.getKeys();
        this.base = instruction.getBase();
        this.seed = instruction.getSeed();
        this.ownerProgramId = instruction.getProgramId();
    }

    /**
     * Configures the account metas for this instruction.
     *
     * @param accountPubkey Public key of the account to be assigned.
     * @param basePubkey    Base public key used for deriving the address.
     */
    public void setKeys(PublicKey accountPubkey, PublicKey basePubkey) {
        if (accountPubkey == null || basePubkey == null) {
            throw new IllegalArgumentException("accountPubkey and basePubkey must not be null.");
        }

        this.keys = List.of(
                new AccountMeta(accountPubkey, false, true), // Target account to be assigned (writable, not signer)
                new AccountMeta(basePubkey, true, false)    // Base pubkey (signer, not writable)
        );
    }

    /**
     * Creates a new instance of this instruction with full parameter setting.
     *
     * @param accountPubkey Public key of the account to be assigned.
     * @param basePubkey    Base public key used for deriving the address.
     * @param seed          The seed to derive the new address.
     * @param ownerProgramId     Public key of the program to assign as the new owner.
     * @return A fully configured instance of {@code SystemInstruction10AssignWithSeed}.
     */
    public static SystemInstruction10AssignWithSeed create(
            PublicKey accountPubkey,
            PublicKey basePubkey,
            String seed,
            PublicKey ownerProgramId
    ) {
        if (accountPubkey == null || basePubkey == null || seed == null || ownerProgramId == null) {
            throw new IllegalArgumentException("All parameters must be non-null.");
        }

        SystemInstruction10AssignWithSeed instruction = new SystemInstruction10AssignWithSeed();
        instruction.setBase(basePubkey);
        instruction.setSeed(seed);
        instruction.setOwnerProgramId(ownerProgramId);
        instruction.setKeys(accountPubkey, basePubkey);
        return instruction;
    }
}