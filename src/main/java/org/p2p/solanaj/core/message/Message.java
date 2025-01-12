package org.p2p.solanaj.core.message;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.*;
import org.p2p.solanaj.utils.ShortvecEncoding;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

@Getter
@ToString
public class Message implements VersionedMessage {
    private static final int RECENT_BLOCK_HASH_LENGTH = 32;

    protected final MessageHeader header;
    protected final List<PublicKey> accountKeys;
    @Setter
    protected String recentBlockhash;
    protected final List<MessageCompiledInstruction> instructions;
    private final Map<Integer, PublicKey> indexToProgramIds = new HashMap<>();

    public Message(MessageHeader messageHeader, List<PublicKey> accountKeys, String recentBlockhash, List<MessageCompiledInstruction> instructions) {
        this.header = messageHeader;
        this.accountKeys = accountKeys;
        this.recentBlockhash = recentBlockhash;
        this.instructions = instructions;
        for (MessageCompiledInstruction instruction : instructions) {
            indexToProgramIds.put(instruction.getProgramIdIndex(), accountKeys.get(instruction.getProgramIdIndex()));
        }
    }

    @Override
    public Version getVersion() {
        return Version.LEGACY;
    }

    @Override
    public List<PublicKey> getSigners() {
        List<PublicKey> signers = new ArrayList<>();
        for (int i = 0; i < header.getNumRequiredSignatures(); i++) {
            signers.add(accountKeys.get(i));
        }
        return signers;
    }

    /**
     * Compile the message using the payer key, instructions, and recent blockhash.
     */
    public static Message compile(PublicKey payerKey, List<TransactionInstruction> instructions, String recentBlockhash) {
        Objects.requireNonNull(payerKey, "Payer key is required");
        Objects.requireNonNull(recentBlockhash, "Recent blockhash is required");
        if (instructions.isEmpty()) {
            throw new IllegalArgumentException("Instructions cannot be empty");
        }

        CompiledKeys compiledKeys = CompiledKeys.compile(instructions, payerKey);
        CompiledKeys.MessageComponents components = compiledKeys.getMessageComponents();
        final List<PublicKey> accountKeys = components.getStaticAccountKeys();

        Map<PublicKey, Integer> keyIndexMap = new HashMap<>();
        for (int i = 0; i < accountKeys.size(); i++) {
            keyIndexMap.put(accountKeys.get(i), i);
        }

        List<MessageCompiledInstruction> compiledInstructions = instructions.stream()
                .map(instr -> new MessageCompiledInstruction(
                        keyIndexMap.get(instr.getProgramId()),
                        instr.getKeys().stream()
                                .map(meta -> keyIndexMap.get(meta.getPublicKey()))
                                .toList(),
                        instr.getData()
                ))
                .toList();

        return new Message(
                components.getHeader(),
                accountKeys,
                recentBlockhash,
                compiledInstructions
        );
    }

    /**
     * Serialize the message to a byte array.
     */
    @Override
    public byte[] serialize() {
        if (recentBlockhash == null) {
            throw new IllegalArgumentException("Recent blockhash is required");
        }

        byte[] accountAddressesLength = ShortvecEncoding.encodeLength(accountKeys.size());
        byte[] instructionsLength = ShortvecEncoding.encodeLength(instructions.size());

        int compiledInstructionsLength = instructions.stream()
                .mapToInt(MessageCompiledInstruction::getSerializedLength)
                .sum();

        int bufferSize = MessageHeader.HEADER_LENGTH + RECENT_BLOCK_HASH_LENGTH
                + accountAddressesLength.length + (accountKeys.size() * PublicKey.PUBLIC_KEY_LENGTH)
                + instructionsLength.length + compiledInstructionsLength;

        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.put(header.serialize());
        buffer.put(accountAddressesLength);
        for (PublicKey key : accountKeys) {
            buffer.put(key.toByteArray());
        }
        buffer.put(Base58.decode(recentBlockhash));
        buffer.put(instructionsLength);
        for (MessageCompiledInstruction instruction : instructions) {
            buffer.put(instruction.serialize());
        }

        return buffer.array();
    }

    public static Message deserialize(ByteBuffer buffer) {
        // Skip the prefix (for version compatibility)
        buffer.get(); // Skip 1 byte for the prefix

        // Deserialize the message header
        MessageHeader header = MessageHeader.deserialize(buffer);

        // Deserialize account keys
        int accountKeysLength = ShortvecEncoding.decodeLength(buffer);
        List<PublicKey> accountKeys = new ArrayList<>();
        for (int i = 0; i < accountKeysLength; i++) {
            byte[] keyBytes = new byte[PublicKey.PUBLIC_KEY_LENGTH];
            buffer.get(keyBytes);
            accountKeys.add(new PublicKey(keyBytes));
        }

        // Deserialize recent blockhash
        byte[] recentBlockhashBytes = new byte[PublicKey.PUBLIC_KEY_LENGTH];
        buffer.get(recentBlockhashBytes);
        String recentBlockhash = Base58.encode(recentBlockhashBytes);

        // Deserialize instructions
        int instructionsLength = ShortvecEncoding.decodeLength(buffer);
        List<MessageCompiledInstruction> instructions = new ArrayList<>();
        for (int i = 0; i < instructionsLength; i++) {
            instructions.add(MessageCompiledInstruction.deserialize(buffer));
        }

        // Create and return the Message object
        return new Message(header, accountKeys, recentBlockhash, instructions);
    }

    public static Message deserialize(byte[] serializedMessage) {
        ByteBuffer buffer = ByteBuffer.wrap(serializedMessage).order(ByteOrder.LITTLE_ENDIAN);
        return deserialize(buffer);
    }

    public MessageAccountKeys getMessageAccountKeys() {
        return new MessageAccountKeys(accountKeys);
    }

    public List<MessageAddressTableLookup> getAddressTableLookups() {
        return Collections.emptyList();
    }

    public boolean isAccountSigner(int accountIndex) {
        return accountIndex < header.getNumRequiredSignatures();
    }

    public boolean isAccountWritable(int accountIndex) {
        int numSignedAccounts = header.getNumRequiredSignatures();
        if (accountIndex >= header.getNumRequiredSignatures()) {
            int unsignedAccountIndex = accountIndex - numSignedAccounts;
            int numUnsignedAccounts = accountKeys.size() - numSignedAccounts;
            int numWritableUnsignedAccounts = numUnsignedAccounts - header.getNumReadonlyUnsignedAccounts();
            return unsignedAccountIndex < numWritableUnsignedAccounts;
        } else {
            int numWritableSignedAccounts = numSignedAccounts - header.getNumReadonlySignedAccounts();
            return accountIndex < numWritableSignedAccounts;
        }
    }

    public boolean isProgramId(int index) {
        return indexToProgramIds.containsKey(index);
    }

    public List<PublicKey> programIds() {
        return indexToProgramIds.values().stream().toList();
    }

    public List<PublicKey> nonProgramIds() {
        return indexToProgramIds.entrySet().stream()
                .filter(entry -> !isProgramId(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();
    }

}
