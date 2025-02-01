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
import net.deanly.structlayout.type.basic.Int64LEField;
import net.deanly.structlayout.type.basic.UInt32LEField;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;

import java.util.List;

/**
 * Represents a System program AllocateWithSeed instruction in the Solana blockchain.
 * This instruction allocates space for a derived account on-chain using a base public key and a seed string.
 *
 * <pre>
 * Fields:
 * - {@code instruction}: Constant index (9) representing the AllocateWithSeed instruction.
 * - {@code keys}: A list of accounts involved in the transaction (includes the derived account).
 * - {@code base}: Base public key used for deriving the account.
 * - {@code seed}: Seed string used to derive the account.
 * - {@code space}: Amount of space (in bytes) to allocate for the account.
 * - {@code programId}: The program ID to which the allocated account is assigned.
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
public class SystemInstruction9AllocateWithSeed extends SystemProgram.Base implements TransactionInstruction {

    @StructField(order = 1, type = UInt32LEField.class)
    private final int instruction = 9; // Instruction index (9) for AllocateWithSeed

    private List<AccountMeta> keys; // Accounts involved (mainly derived account)

    @StructField(order = 2, type = PublicKeyField.class)
    private PublicKey base; // Base public key for seed-derived account

    @StructField(order = 3, type = RustStringField.class)
    private String seed; // Seed string for deriving the account

    @StructField(order = 4, type = Int64LEField.class)
    private long space; // Space allocation for the account (in bytes)

    @StructField(order = 5, type = PublicKeyField.class)
    private PublicKey programId; // The program ID to assign the derived account to

    @Override
    public byte[] getData() {
        return StructLayout.encode(this);
    }

    public void setData(byte[] data) {
        SystemInstruction9AllocateWithSeed instruction = StructLayout.decode(data, SystemInstruction9AllocateWithSeed.class);
        this.keys = instruction.getKeys();
        this.base = instruction.getBase();
        this.seed = instruction.getSeed();
        this.space = instruction.getSpace();
        this.programId = instruction.getProgramId();
    }

    /**
     * Configures the accounts involved in this instruction based on the base public key.
     *
     * @param accountPubkey Public key of the account to allocate.
     * @param basePubkey    Base public key used to derive the new address.
     */
    public void setKeys(PublicKey accountPubkey, PublicKey basePubkey) {
        if (accountPubkey == null || basePubkey == null) {
            throw new IllegalArgumentException("accountPubkey and basePubkey must not be null.");
        }

        // Set the keys (order and flags based on Typescript implementation)
        this.keys = List.of(
                new AccountMeta(accountPubkey, false, true), // Account to allocate (writable, not signer)
                new AccountMeta(basePubkey, true, false)    // Base public key (signer, not writable)
        );
    }

    /**
     * Factory method to create an instance of this instruction.
     *
     * @param accountPubkey Public key of the account to allocate.
     * @param basePubkey    Base public key used to derive the address.
     * @param seed          Seed string used to derive the address.
     * @param space         Amount of space to allocate.
     * @param programId     Public key of the program to assign.
     * @return Configured instance of {@code SystemInstruction9AllocateWithSeed}.
     */
    public static SystemInstruction9AllocateWithSeed create(
            PublicKey accountPubkey,
            PublicKey basePubkey,
            String seed,
            long space,
            PublicKey programId
    ) {
        if (accountPubkey == null || basePubkey == null || seed == null || space <= 0 || programId == null) {
            throw new IllegalArgumentException("All parameters must be non-null, and space must be greater than 0.");
        }

        SystemInstruction9AllocateWithSeed instruction = new SystemInstruction9AllocateWithSeed();
        instruction.setBase(basePubkey);
        instruction.setSeed(seed);
        instruction.setSpace(space);
        instruction.setProgramId(programId);
        instruction.setKeys(accountPubkey, basePubkey);
        return instruction;
    }
}