package net.deanly.solana.sdk.transaction;

import lombok.Getter;
import lombok.NonNull;
import net.deanly.solana.sdk.crypto.KeyPair;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.crypto.Ed25519Signer;
import net.deanly.solana.sdk.rpc.client.config.Network;
import net.deanly.solana.sdk.rpc.response.ResValueInstruction;
import net.deanly.solana.sdk.rpc.response.ResValueTransaction;
import net.deanly.solana.sdk.transaction.message.meta.MessageAddressTableLookup;
import net.deanly.solana.sdk.types.Blockhash;
import net.deanly.solana.sdk.types.Encoding;
import net.deanly.solana.sdk.types.Signature;
import net.deanly.solana.sdk.types.StateData;
import net.deanly.solana.sdk.types.codec.Base58;
import net.deanly.solana.sdk.layout.field.SignatureField;
import net.deanly.solana.sdk.layout.field.ShortVecField;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.solana.sdk.transaction.message.MessageV0;
import net.deanly.solana.sdk.transaction.message.VersionedMessage;
import net.deanly.solana.sdk.transaction.message.Message;
import net.deanly.solana.sdk.program.spl.alt.state.AddressLookupTableAccount;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.annotation.StructSequenceField;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a Solana transaction.
 * This class allows for building, signing, and serializing transactions.
 */
@Getter
public class Transaction {

    @StructSequenceField(order = 1, elementType = SignatureField.class, lengthType = ShortVecField.class)
    private final List<Signature> signatures;

    @StructObjectField(order = 2)
    private VersionedMessage message;

    private final List<TransactionInstruction> instructionsForCompile;
    private final List<AddressLookupTableAccount> addressTableLookupsForCompile;
    private Blockhash recentBlockhashForCompile;
    private PublicKey feePayerForCompile;
    private final Network network;

    /**
     * Constructs a new Transaction instance.
     */
    public Transaction() {
        this.instructionsForCompile = new ArrayList<>();
        this.signatures = new ArrayList<>();
        this.addressTableLookupsForCompile = new ArrayList<>();
        this.network = Network.MAINNET;
    }

    /**
     * Constructs a new Transaction instance with the specified network.
     *
     * @param network The network in which the transaction will operate.
     *                Must not be null.
     */
    public Transaction(Network network) {
        this.network = Objects.requireNonNull(network, "Network cannot be null");
        this.instructionsForCompile = new ArrayList<>();
        this.signatures = new ArrayList<>();
        this.addressTableLookupsForCompile = new ArrayList<>();
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
        instructionsForCompile.add(instruction);
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
        addressTableLookupsForCompile.add(addressTableLookup);
        return this;
    }

    /**
     * Sets the fee payer for the transaction.
     *
     * @param feePayerForCompile The public key of the account responsible for paying the transaction fee. Must not be null.
     */
    public void setFeePayerForCompile(@NonNull PublicKey feePayerForCompile) {
        this.feePayerForCompile = feePayerForCompile;
    }

    /**
     * Sets the recent blockhash for the transaction.
     *
     * @param recentBlockhashForCompile The recent blockhash to set
     * @throws NullPointerException if the recentBlockhash is null
     */
    public void setRecentBlockhashForCompile(Blockhash recentBlockhashForCompile) {
        this.recentBlockhashForCompile = Objects.requireNonNull(recentBlockhashForCompile, "Recent blockhash cannot be null");
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
        if (feePayerForCompile == null) {
            feePayerForCompile = signers.get(0).getPublicKey();
        } else if (!feePayerForCompile.equals(signers.get(0).getPublicKey())) {
            message = null;
            feePayerForCompile = signers.get(0).getPublicKey();
        }
        if (!isCompiled()) {
            compile();
        }

        byte[] serializedMessage = message.serialize();

        signatures.clear();
        for (KeyPair signer : signers) {
            byte[] signature = Ed25519Signer.sign(serializedMessage, signer.toByteArray());
            signatures.add(Signature.of(Base58.encode(signature)));
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
        compile(feePayerForCompile, recentBlockhashForCompile, instructionsForCompile, addressTableLookupsForCompile);
    }

    /**
     * Compiles the transaction into a `VersionedMessage` using the provided fee payer
     * and the stored recent blockhash, instructions, and address lookup tables.
     *
     * @param feePayer The public key of the account responsible for paying the transaction fee.
     */
    public void compile(PublicKey feePayer) {
        compile(feePayer, recentBlockhashForCompile, instructionsForCompile, addressTableLookupsForCompile);
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
        compile(feePayer, recentBlockhash, instructions, addressTableLookupsForCompile);
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
            this.message = MessageV0.compile(this.network, feePayer, instructions, recentBlockhash, addressTableLookups);
        } else {
            this.message = Message.compile(this.network, feePayer, instructions, recentBlockhash);
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
    }

    /**
     * Deserializes a transaction from a byte array.
     */
    public static Transaction deserialize(byte[] serializedTransaction) {
        return StructLayout.decode(serializedTransaction, Transaction.class);
    }
    /**
     * Converts this `Transaction` into a `ResValueTransaction`.
     *
     * @return ResValueTransaction representation of this Transaction.
     */
    public ResValueTransaction toResValueTransaction() {
        // 1. Static Account Keys
        List<PublicKey> staticAccountKeys = this.message.getStaticAccountKeys();

        // 2. Check if the message is MessageV0
        List<PublicKey> allAccountKeys = new ArrayList<>(staticAccountKeys);
        List<ResValueTransaction.Message.AddressTableLookup> resAddressTableLookups = new ArrayList<>();

        if (this.message instanceof MessageV0 messageV0) {

            // Load ALT Keys
            for (MessageAddressTableLookup atl : messageV0.getAddressTableLookups()) {
                AddressLookupTableAccount matchingAccount = this.addressTableLookupsForCompile.stream()
                        .filter(account -> account.getKey().equals(atl.getAccountKey()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Address lookup table not found for account key: " + atl.getAccountKey().toBase58()
                        ));

                // Add Writable ALT Keys
                for (Integer writableIndex : atl.getWritableIndexes()) {
                    if (writableIndex >= 0 && writableIndex < matchingAccount.getState().getAddresses().size()) {
                        allAccountKeys.add(matchingAccount.getState().getAddresses().get(writableIndex));
                    } else {
                        throw new IllegalArgumentException("Writable index out of bounds for ATL: " + atl.getAccountKey().toBase58());
                    }
                }

                // Add Readonly ALT Keys
                for (Integer readonlyIndex : atl.getReadonlyIndexes()) {
                    if (readonlyIndex >= 0 && readonlyIndex < matchingAccount.getState().getAddresses().size()) {
                        allAccountKeys.add(matchingAccount.getState().getAddresses().get(readonlyIndex));
                    } else {
                        throw new IllegalArgumentException("Readonly index out of bounds for ATL: " + atl.getAccountKey().toBase58());
                    }
                }

                // Add ATL details to the result
                resAddressTableLookups.add(
                        ResValueTransaction.Message.AddressTableLookup.builder()
                                .accountKey(atl.getAccountKey())
                                .writableIndexes(atl.getWritableIndexes())
                                .readonlyIndexes(atl.getReadonlyIndexes())
                                .build()
                );
            }
        }

        // 3. Instructions (Resolve Account Indexes)
        List<ResValueInstruction> instructions = this.message.getInstructions().stream()
                .map(instruction -> {
                    List<Integer> accountIndexes = instruction.getAccountKeyIndexes();
                    int programIdIndex = instruction.getProgramIdIndex();
                    return ResValueInstruction.builder()
                            .accounts(accountIndexes)
                            .data(new StateData(Encoding.BASE58, Base58.encode(instruction.getData()), true))
                            .programIdIndex(programIdIndex)
                            .build();
                })
                .collect(Collectors.toList());

        // 4. Header
        ResValueTransaction.Message.Header header = ResValueTransaction.Message.Header.builder()
                .numReadonlySignedAccounts(this.message.getHeader().getNumReadonlySignedAccounts())
                .numReadonlyUnsignedAccounts(this.message.getHeader().getNumReadonlyUnsignedAccounts())
                .numRequiredSignatures(this.message.getHeader().getNumRequiredSignatures())
                .build();

        // 5. Message
        ResValueTransaction.Message resMessage = ResValueTransaction.Message.builder()
                .accountKeys(allAccountKeys) // Combined Static and ALT keys
                .recentBlockhash(this.message.getRecentBlockhash())
                .instructions(instructions) // Resolved instructions
                .addressTableLookups(resAddressTableLookups) // ATL information if MessageV0
                .header(header) // Header information
                .build();

        // 6. Final ResValueTransaction
        return ResValueTransaction.builder()
                .message(resMessage)
                .signatures(this.signatures) // Transaction signatures
                .build();
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "version=" + message.getVersion().name() +
                ", recentBlockhash='" + recentBlockhashForCompile + '\'' +
                ", feePayer=" + feePayerForCompile +
                ", isCompiled=" + isCompiled() +
                ", isSigned=" + isSigned() +
                ", signatures.count=" + signatures.size() +
                '}';
    }
}