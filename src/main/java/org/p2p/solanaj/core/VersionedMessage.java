package org.p2p.solanaj.core;

import lombok.Getter;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.rpc.types.AddressLookupTableAccount;
import org.p2p.solanaj.utils.ShortvecEncoding;

import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a versioned message for Solana transactions, supporting Address Lookup Tables.
 * This class allows adding instructions and address table lookups, and serializing the message.
 */
public class VersionedMessage {

    @Setter
    @Getter
    private byte version;
    @Setter
    @Getter
    private MessageHeader header;
    @Getter
    private List<PublicKey> accountKeys;
    @Getter
    private String recentBlockhash;
    @Getter
    private List<CompiledInstruction> instructions;
    @Getter
    private List<MessageAddressTableLookup> addressTableLookups;

    // Maps to track unique account keys and their indices
    private Map<PublicKey, Integer> accountKeyIndexMap;

    /**
     * Constructs a new VersionedMessage with default values.
     */
    public VersionedMessage() {
        this.version = 0; // Version 0
        this.header = new MessageHeader();
        this.accountKeys = new ArrayList<>();
        this.instructions = new ArrayList<>();
        this.addressTableLookups = new ArrayList<>();
        this.accountKeyIndexMap = new HashMap<>();
    }

    private int calculateSize() {
        int size = 1; // Version byte
        size += header.getSerializedSize();
        size += ShortvecEncoding.encodeLength(accountKeys.size()).length + (accountKeys.size() * 32);
        size += 32; // Recent blockhash
        size += ShortvecEncoding.encodeLength(instructions.size()).length;
        for (CompiledInstruction instruction : instructions) {
            size += instruction.getLength();
        }
        size += ShortvecEncoding.encodeLength(addressTableLookups.size()).length;
        for (MessageAddressTableLookup lookup : addressTableLookups) {
            size += lookup.getSerializedLength();
        }
        return size;
    }

    /**
     * Adds a TransactionInstruction to the message.
     *
     * @param instruction The TransactionInstruction to add
     */
    public void addInstruction(TransactionInstruction instruction) {
        Objects.requireNonNull(instruction, "Instruction cannot be null");
        CompiledInstruction compiled = compileInstruction(instruction);
        instructions.add(compiled);
    }


    /**
     * Adds an Address Lookup Table to the message.
     *
     * @param lookupTable The AddressTableLookup to add
     */
    public void addAddressTableLookup(AddressTableLookup lookupTable) {
        Objects.requireNonNull(lookupTable, "LookupTable cannot be null");
        MessageAddressTableLookup messageLookup = new MessageAddressTableLookup(
                lookupTable.getAccountKey(),
                lookupTable.getWritableIndexes(),
                lookupTable.getReadonlyIndexes()
        );
        addressTableLookups.add(messageLookup);
    }

    /**
     * Adds an Address Lookup Table account to resolve writable and readonly addresses.
     *
     * @param atlAccount Address Lookup Table Account
     */
    public void addAddressTableLookup(AddressLookupTableAccount atlAccount) {
        Objects.requireNonNull(atlAccount, "Address Lookup Table Account cannot be null");

        MessageAddressTableLookup lookup = new MessageAddressTableLookup(
                atlAccount.getKey(),
                resolveIndexes(atlAccount.getState().getAddresses(), true),
                resolveIndexes(atlAccount.getState().getAddresses(), false)
        );

        addressTableLookups.add(lookup);
    }

    /**
     * Resolves address indexes for writable or readonly addresses from the ATL account.
     *
     * @param addresses Addresses from the Address Lookup Table Account
     * @param writable  If true, resolve writable indexes; otherwise resolve readonly indexes
     * @return List of resolved indexes
     */
    private List<Integer> resolveIndexes(List<PublicKey> addresses, boolean writable) {
        List<Integer> resolvedIndexes = new ArrayList<>();
        for (int i = 0; i < addresses.size(); i++) {
            PublicKey address = addresses.get(i);
            if (writable) {
                resolvedIndexes.add(addAccountKey(address));
            } else {
                resolvedIndexes.add(addAccountKey(address));
            }
        }
        return resolvedIndexes;
    }

    /**
     * Sets the recent blockhash for the transaction.
     *
     * @param recentBlockhash The recent blockhash to set
     */
    public void setRecentBlockhash(String recentBlockhash) {
        this.recentBlockhash = Objects.requireNonNull(recentBlockhash, "RecentBlockhash cannot be null");
    }


    /**
     * Serializes the versioned message into a byte array, including Address Lookup Tables.
     *
     * @return The serialized message as a byte array
     */
    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(calculateSize());
        buffer.put(header.serialize());
        buffer.put(ShortvecEncoding.encodeLength(accountKeys.size()));
        for (PublicKey key : accountKeys) {
            buffer.put(key.toByteArray());
        }
        buffer.put(Base58.decode(recentBlockhash));
        buffer.put(ShortvecEncoding.encodeLength(instructions.size()));
        for (CompiledInstruction instruction : instructions) {
            buffer.put(instruction.serialize());
        }
        buffer.put(ShortvecEncoding.encodeLength(addressTableLookups.size()));
        for (MessageAddressTableLookup lookup : addressTableLookups) {
            buffer.put(lookup.serialize());
        }
        return buffer.array();
    }

    /**
     * Serializes the V0 message according to Solana's specification.
     *
     * @return Serialized V0 message as a byte array
     */
    public byte[] serializeV0Message() {
        byte[] headerBytes = header.serialize();
        byte[] accountKeysBytes = serializeAccountKeys();
        byte[] recentBlockhashBytes = recentBlockhash.getBytes();
        byte[] instructionsBytes = serializeInstructions();
        byte[] addressTableLookupsBytes = serializeAddressTableLookups();

        ByteBuffer buffer = ByteBuffer.allocate(
                headerBytes.length +
                        accountKeysBytes.length +
                        recentBlockhashBytes.length +
                        instructionsBytes.length +
                        addressTableLookupsBytes.length
        );

        buffer.put(headerBytes);
        buffer.put(accountKeysBytes);
        buffer.put(recentBlockhashBytes);
        buffer.put(instructionsBytes);
        buffer.put(addressTableLookupsBytes);

        return buffer.array();
    }


    /**
     * Serializes the account keys with their respective lengths.
     *
     * @return Serialized account keys as a byte array
     */
    private byte[] serializeAccountKeys() {
        byte[] accountKeysLength = ShortvecEncoding.encodeLength(accountKeys.size());
        ByteBuffer buffer = ByteBuffer.allocate(accountKeysLength.length + accountKeys.size() * 32);
        buffer.put(accountKeysLength);
        for (PublicKey key : accountKeys) {
            buffer.put(key.toByteArray());
        }
        return buffer.array();
    }

    /**
     * Serializes the instructions with their respective lengths.
     *
     * @return Serialized instructions as a byte array
     */
    private byte[] serializeInstructions() {
        byte[] instructionsLength = ShortvecEncoding.encodeLength(instructions.size());
        int totalInstructionBytes = instructions.stream().mapToInt(CompiledInstruction::getLength).sum();
        ByteBuffer buffer = ByteBuffer.allocate(instructionsLength.length + totalInstructionBytes);
        buffer.put(instructionsLength);
        for (CompiledInstruction instruction : instructions) {
            buffer.put(instruction.serialize());
        }
        return buffer.array();
    }

    /**
     * Serializes the address table lookups with their respective lengths.
     *
     * @return Serialized address table lookups as a byte array
     */
    private byte[] serializeAddressTableLookups() {
        byte[] lookupsLength = ShortvecEncoding.encodeLength(addressTableLookups.size());
        int totalLookupBytes = addressTableLookups.stream().mapToInt(MessageAddressTableLookup::getSerializedLength).sum();
        ByteBuffer buffer = ByteBuffer.allocate(lookupsLength.length + totalLookupBytes);
        buffer.put(lookupsLength);
        for (MessageAddressTableLookup lookup : addressTableLookups) {
            buffer.put(lookup.serialize());
        }
        return buffer.array();
    }

    /**
     * Compiles a TransactionInstruction into a CompiledInstruction by mapping
     * program ID and account keys to their respective indices.
     *
     * @param instruction The TransactionInstruction to compile
     * @return The compiled instruction
     */
    private CompiledInstruction compileInstruction(TransactionInstruction instruction) {
        // Ensure program ID is in accountKeys
        int programIdIndex = addAccountKey(instruction.getProgramId());

        // Map account keys to indices
        List<Integer> accountIndices = new ArrayList<>();
        for (AccountMeta meta : instruction.getKeys()) {
            accountIndices.add(addAccountKey(meta.getPublicKey()));
        }

        // Encode account indices as u8
        byte[] accountIndicesBytes = new byte[accountIndices.size()];
        for (int i = 0; i < accountIndices.size(); i++) {
            int index = accountIndices.get(i);
            if (index > 255) {
                throw new IllegalStateException("Account index exceeds u8 limit");
            }
            accountIndicesBytes[i] = (byte) index;
        }

        // Compile instruction data
        byte[] data = instruction.getData();

        return new CompiledInstruction(
                (byte) programIdIndex,
                ShortvecEncoding.encodeLength(accountIndices.size()),
                accountIndicesBytes,
                ShortvecEncoding.encodeLength(data.length),
                data
        );
    }


    /**
     * Adds an account key to the accountKeys list if not already present.
     *
     * @param key The PublicKey to add
     * @return The index of the key in the accountKeys list
     */
    private int addAccountKey(PublicKey key) {
        if (accountKeyIndexMap.containsKey(key)) {
            return accountKeyIndexMap.get(key);
        } else {
            accountKeys.add(key);
            int index = accountKeys.size() - 1;
            accountKeyIndexMap.put(key, index);
            return index;
        }
    }

    /**
     * Resolves account keys using provided Address Lookup Table accounts.
     *
     * @param atlAccounts List of Address Lookup Table accounts
     * @return Account keys resolved from the ATL accounts
     */
    public List<PublicKey> resolveAccountKeys(List<AddressLookupTableAccount> atlAccounts) {
        List<PublicKey> resolvedKeys = new ArrayList<>();
        for (MessageAddressTableLookup lookup : addressTableLookups) {
            AddressLookupTableAccount atlAccount = atlAccounts.stream()
                    .filter(account -> account.getKey().equals(lookup.getAccountKey()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Address Lookup Table account not found for key: " + lookup.getAccountKey().toBase58()));

            resolvedKeys.addAll(resolveKeysFromLookup(atlAccount, lookup));
        }
        return resolvedKeys;
    }

    /**
     * Resolves keys from a specific MessageAddressTableLookup and ATL account.
     *
     * @param atlAccount Address Lookup Table Account
     * @param lookup     MessageAddressTableLookup
     * @return List of resolved keys
     */
    private List<PublicKey> resolveKeysFromLookup(AddressLookupTableAccount atlAccount, MessageAddressTableLookup lookup) {
        List<PublicKey> keys = new ArrayList<>();
        List<PublicKey> addresses = atlAccount.getState().getAddresses();

        for (Byte index : lookup.getWritableIndexes()) {
            if (index < 0 || index >= addresses.size()) {
                throw new IllegalArgumentException("Invalid writable index: " + index);
            }
            keys.add(addresses.get(index));
        }

        for (Byte index : lookup.getReadonlyIndexes()) {
            if (index < 0 || index >= addresses.size()) {
                throw new IllegalArgumentException("Invalid readonly index: " + index);
            }
            keys.add(addresses.get(index));
        }

        return keys;
    }

    /**
     * Ensures that signers are at the beginning of the accountKeys list in the correct order.
     *
     * @param signers The list of signers to ensure are at the beginning of accountKeys.
     */
    public void reorderSignersInAccountKeys(List<Account> signers) {
        // 임시 리스트에 모든 서명자 키를 저장
        List<PublicKey> signersKeys = new ArrayList<>();
        for (Account signer : signers) {
            signersKeys.add(signer.getPublicKey());
        }

        // 서명자만 필터링해서 맨 앞에 추가
        List<PublicKey> reorderedAccountKeys = new ArrayList<>(signersKeys);

        // 서명자가 아닌 기존 accountKeys 추가
        for (PublicKey key : accountKeys) {
            if (!signersKeys.contains(key)) {
                reorderedAccountKeys.add(key);
            }
        }

        // 재정렬된 accountKeys로 교체
        accountKeys.clear();
        accountKeys.addAll(reorderedAccountKeys);

        // accountKeyIndexMap 업데이트
        for (int i = 0; i < accountKeys.size(); i++) {
            accountKeyIndexMap.put(accountKeys.get(i), i);
        }
    }

    /**
     * Inner class representing the message header.
     */
    @Setter
    public static class MessageHeader {
        private byte numRequiredSignatures;
        private byte numReadonlySignedAccounts;
        private byte numReadonlyUnsignedAccounts;

        public byte[] serialize() {
            ByteBuffer buffer = ByteBuffer.allocate(3);
            buffer.put(numRequiredSignatures);
            buffer.put(numReadonlySignedAccounts);
            buffer.put(numReadonlyUnsignedAccounts);
            return buffer.array();
        }

        public int getSerializedSize() {
            return 3;
        }

        // Getters and setters...
    }

    /**
     * Inner class representing a compiled instruction.
     */
    public static class CompiledInstruction {
        private byte programIdIndex;
        private byte[] accountsCount;
        private byte[] accounts;
        private byte[] dataLength;
        private byte[] data;

        /**
         * Constructs a CompiledInstruction from components.
         *
         * @param programIdIndex The index of the program ID in accountKeys
         * @param accountsCount  The encoded length of accounts
         * @param accounts       The account indices as bytes
         * @param dataLength     The encoded length of data
         * @param data           The instruction data
         */
        public CompiledInstruction(byte programIdIndex, byte[] accountsCount, byte[] accounts, byte[] dataLength, byte[] data) {
            this.programIdIndex = programIdIndex;
            this.accountsCount = accountsCount;
            this.accounts = accounts;
            this.dataLength = dataLength;
            this.data = data;
        }

        /**
         * Serializes the compiled instruction into a byte array.
         *
         * @return Byte array representing the compiled instruction
         */
        public byte[] serialize() {
            ByteBuffer buffer = ByteBuffer.allocate(
                    1 + // programIdIndex
                            accountsCount.length +
                            accounts.length +
                            dataLength.length +
                            data.length
            );
            buffer.put(programIdIndex);
            buffer.put(accountsCount);
            buffer.put(accounts);
            buffer.put(dataLength);
            buffer.put(data);
            return buffer.array();
        }


        /**
         * Gets the length of the compiled instruction.
         *
         * @return Length in bytes
         */
        public int getLength() {
            return 1 + accountsCount.length + accounts.length + dataLength.length + data.length;
        }
    }


    /**
     * Inner class representing a MessageAddressTableLookup.
     */
    public static class MessageAddressTableLookup {
        private final PublicKey accountKey;
        private final List<Byte> writableIndexes;
        private final List<Byte> readonlyIndexes;

        /**
         * Constructs a MessageAddressTableLookup with the given parameters.
         *
         * @param accountKey      The public key of the address lookup table
         * @param writableIndexes The list of writable address indexes
         * @param readonlyIndexes The list of readonly address indexes
         */
        public MessageAddressTableLookup(PublicKey accountKey, List<Integer> writableIndexes, List<Integer> readonlyIndexes) {
            this.accountKey = Objects.requireNonNull(accountKey, "AccountKey cannot be null");
            this.writableIndexes = new ArrayList<>();
            for (Integer index : writableIndexes) {
                if (index < 0 || index > 255) {
                    throw new IllegalArgumentException("Writable index must be between 0 and 255");
                }
                this.writableIndexes.add(index.byteValue());
            }
            this.readonlyIndexes = new ArrayList<>();
            for (Integer index : readonlyIndexes) {
                if (index < 0 || index > 255) {
                    throw new IllegalArgumentException("Readonly index must be between 0 and 255");
                }
                this.readonlyIndexes.add(index.byteValue());
            }
        }

        /**
         * Serializes the MessageAddressTableLookup into a byte array.
         *
         * @return Byte array representing the serialized lookup table
         */
        public byte[] serialize() {
            byte[] accountKeyBytes = accountKey.toByteArray();
            byte[] writableLength = ShortvecEncoding.encodeLength(writableIndexes.size());
            byte[] readonlyLength = ShortvecEncoding.encodeLength(readonlyIndexes.size());

            ByteBuffer buffer = ByteBuffer.allocate(getSerializedLength());
            buffer.put(accountKeyBytes);
            buffer.put(writableLength);
            for (Byte index : writableIndexes) {
                buffer.put(index);
            }
            buffer.put(readonlyLength);
            for (Byte index : readonlyIndexes) {
                buffer.put(index);
            }
            return buffer.array();
        }

        /**
         * Gets the serialized length of the MessageAddressTableLookup.
         *
         * @return Length in bytes
         */
        public int getSerializedLength() {
            return 32 + // accountKey
                    ShortvecEncoding.encodeLength(writableIndexes.size()).length +
                    writableIndexes.size() +
                    ShortvecEncoding.encodeLength(readonlyIndexes.size()).length +
                    readonlyIndexes.size();
        }

        // Getters

        /**
         * Gets the account key of the lookup table.
         *
         * @return The PublicKey of the lookup table
         */
        public PublicKey getAccountKey() {
            return accountKey;
        }

        /**
         * Gets the writable indexes.
         *
         * @return List of writable indexes as bytes
         */
        public List<Byte> getWritableIndexes() {
            return writableIndexes;
        }


        /**
         * Gets the readonly indexes.
         *
         * @return List of readonly indexes as bytes
         */
        public List<Byte> getReadonlyIndexes() {
            return readonlyIndexes;
        }
    }
}