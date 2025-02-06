package net.deanly.solana.sdk.transaction;

import lombok.Getter;
import lombok.NonNull;
import net.deanly.solana.sdk.crypto.KeyPair;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.crypto.Ed25519Signer;
import net.deanly.solana.sdk.types.Blockhash;
import net.deanly.solana.sdk.types.codec.Base58;
import net.deanly.solana.sdk.layout.field.Base58Bytes64Field;
import net.deanly.solana.sdk.layout.field.ShortVecField;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.solana.sdk.transaction.message.MessageV0;
import net.deanly.solana.sdk.transaction.message.VersionedMessage;
import net.deanly.solana.sdk.transaction.message.Message;
import net.deanly.solana.sdk.program.alt.state.AddressLookupTableAccount;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.annotation.StructSequenceField;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Solana transaction.
 * This class allows for building, signing, and serializing transactions.
 */
@Getter
public class Transaction {

    public static final int SIGNATURE_LENGTH = 64;

    @StructSequenceField(order = 1, elementType = Base58Bytes64Field.class, lengthType = ShortVecField.class)
    private final List<String> signatures;

    @StructObjectField(order = 2)
    private VersionedMessage message;

    private final List<TransactionInstruction> instructions;
    private final List<AddressLookupTableAccount> addressTableLookups;
    private Blockhash recentBlockhash;
    private PublicKey feePayer;

    /**
     * Constructs a new Transaction instance.
     */
    public Transaction() {
        this.instructions = new ArrayList<>();
        this.signatures = new ArrayList<>();
        this.addressTableLookups = new ArrayList<>();
    }

    /**
     * Adds an instruction to the transaction.
     *
     * @param instruction The instruction to add
     * @return This Transaction instance for method chaining
     * @throws NullPointerException if the instruction is null
     */
    public Transaction addInstruction(TransactionInstruction instruction) {
        Objects.requireNonNull(instruction, "Instruction cannot be null"); // Add input validation
        instructions.add(instruction);
        return this;
    }


    /**
     * Adds an address lookup table (ALT) account to the transaction.
     * <p>
     * Address lookup tables allow a transaction to reference additional
     * accounts without exceeding the transaction size limit. These accounts
     * are used to resolve public keys during transaction execution, enabling
     * more efficient account management in complex transactions.
     * </p>
     *
     * @param addressTableLookup The address lookup table account to add.
     *                           Must not be null.
     * @return This Transaction instance for method chaining.
     * @throws NullPointerException if the provided address lookup table account is null.
     */
    public Transaction addAddressTableLookups(AddressLookupTableAccount addressTableLookup) {
        Objects.requireNonNull(addressTableLookup, "ATL cannot be null"); // Add input validation
        addressTableLookups.add(addressTableLookup);
        return this;
    }

    /**
     * Sets the fee payer for the transaction.
     *
     * @param feePayer The public key of the account responsible for paying the transaction fee. Must not be null.
     */
    public void setFeePayer(@NonNull PublicKey feePayer) {
        this.feePayer = feePayer;
    }

    /**
     * Sets the recent blockhash for the transaction.
     *
     * @param recentBlockhash The recent blockhash to set
     * @throws NullPointerException if the recentBlockhash is null
     */
    public void setRecentBlockhash(Blockhash recentBlockhash) {
        this.recentBlockhash = Objects.requireNonNull(recentBlockhash, "Recent blockhash cannot be null");
    }

    /**
     * Signs the transaction with a single signer.
     *
     * @param signer The account to sign the transaction
     * @throws NullPointerException if the signer is null
     */
    public void sign(KeyPair signer) {
        sign(List.of(Objects.requireNonNull(signer, "Signer cannot be null"))); // Add input validation
    }

    /**
     * Signs the transaction with multiple signers.
     *
     * @param signers The list of accounts to sign the transaction
     * @throws IllegalArgumentException if no signers are provided
     */
    public void sign(List<KeyPair> signers) {
        if (signers == null || signers.isEmpty()) {
            throw new IllegalArgumentException("No signers provided");
        }
        if (feePayer == null) {
            feePayer = signers.get(0).getPublicKey();
        } else if (!feePayer.equals(signers.get(0).getPublicKey())) {
            message = null;
            feePayer = signers.get(0).getPublicKey();
        }
        if (!isCompiled()) {
            compile();
        }

        byte[] serializedMessage = message.serialize();

        signatures.clear();
        for (KeyPair signer : signers) {
            byte[] signature = Ed25519Signer.sign(serializedMessage, signer.toByteArray());
            signatures.add(Base58.encode(signature));
        }
    }

    /**
     * Checks if the transaction has been signed.
     *
     * @return true if the transaction has at least one signature; false otherwise.
     */
    public boolean isSigned() {
        return !signatures.isEmpty();
    }

    /**
     * Compiles the transaction into a `VersionedMessage` using the stored fee payer,
     * recent blockhash, instructions, and address lookup tables.
     * This method determines whether to compile as a legacy `Message` or `MessageV0`
     * based on the presence of address lookup tables.
     */
    public void compile() {
        compile(feePayer, recentBlockhash, instructions, addressTableLookups);
    }

    /**
     * Compiles the transaction into a `VersionedMessage` using the provided fee payer
     * and the stored recent blockhash, instructions, and address lookup tables.
     *
     * @param feePayer The public key of the account responsible for paying the transaction fee.
     */
    public void compile(PublicKey feePayer) {
        compile(feePayer, recentBlockhash, instructions, addressTableLookups);
    }

    /**
     * Compiles the transaction into a `VersionedMessage` using the provided fee payer,
     * recent blockhash, and instructions, along with the stored address lookup tables.
     *
     * @param feePayer       The public key of the account responsible for paying the transaction fee.
     * @param recentBlockhash The recent blockhash for the transaction.
     * @param instructions   The list of transaction instructions.
     */
    public void compile(PublicKey feePayer, Blockhash recentBlockhash, List<TransactionInstruction> instructions) {
        compile(feePayer, recentBlockhash, instructions, addressTableLookups);
    }

    /**
     * Compiles the transaction into a `VersionedMessage` using the provided fee payer,
     * recent blockhash, instructions, and address lookup tables.
     *
     * This method determines whether to compile as a legacy `Message` or `MessageV0`
     * based on the presence of address lookup tables.
     *
     * @param feePayer       The public key of the account responsible for paying the transaction fee.
     * @param recentBlockhash The recent blockhash for the transaction.
     * @param instructions   The list of transaction instructions.
     * @param addressTableLookups The list of address lookup table accounts to use for the transaction.
     */
    public void compile(PublicKey feePayer, Blockhash recentBlockhash, List<TransactionInstruction> instructions, List<AddressLookupTableAccount> addressTableLookups) {
        if (!addressTableLookups.isEmpty()) {
            message = MessageV0.compile(feePayer, instructions, recentBlockhash, addressTableLookups);
        } else {
            message = Message.compile(feePayer, instructions, recentBlockhash);
        }
    }

    /**
     * Checks if the transaction has been compiled into a `VersionedMessage`.
     *
     * @return true if the transaction has been compiled; false otherwise.
     */
    public boolean isCompiled() {
        return message != null;
    }

    /**
     * Serializes the transaction into a byte array.
     *
     * @return The serialized transaction as a byte array
     */
    public byte[] serialize() {
        if (message == null) {
            compile();
        }

        return StructLayout.encode(this);

//        if (serializedMessage == null) {
//            serializedMessage = message.serialize();
//        }
//
//        int signatureCount = signatures.size();
//        byte[] signatureCountEncoded = ShortvecEncoding.encodeLength(signatureCount);
//
//        int totalSize = signatureCountEncoded.length + signatureCount * SIGNATURE_LENGTH + serializedMessage.length;
//        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
//
//        buffer.put(signatureCountEncoded);
//        for (String signature : signatures) {
//            buffer.put(Base58.decode(signature));
//        }
//        buffer.put(serializedMessage);
//
//        return buffer.array();
    }

    /**
     * Deserializes a transaction from a byte array.
     */
    public static Transaction deserialize(byte[] serializedTransaction) {
//        ByteBuffer buffer = ByteBuffer.wrap(serializedTransaction);
//
//        int signatureCount = ShortvecEncoding.decodeLength(buffer);
//        List<String> signatures = new ArrayList<>();
//        for (int i = 0; i < signatureCount; i++) {
//            byte[] signatureBytes = new byte[SIGNATURE_LENGTH];
//            buffer.get(signatureBytes);
//            signatures.add(Base58.encode(signatureBytes));
//        }
//
//        VersionedMessage message = VersionedMessage.deserialize(buffer.array());
//
//        Transaction transaction = new Transaction();
//        transaction.message = message;
//        transaction.signatures.addAll(signatures);
//        return transaction;
        return StructLayout.decode(serializedTransaction, Transaction.class);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "version=" + message.getVersion().name() +
                ", recentBlockhash='" + recentBlockhash + '\'' +
                ", feePayer=" + feePayer +
                ", isCompiled=" + isCompiled() +
                ", isSigned=" + isSigned() +
                ", signatures.count=" + signatures.size() +
                '}';
    }
}