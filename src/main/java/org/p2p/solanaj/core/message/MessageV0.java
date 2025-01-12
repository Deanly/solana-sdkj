package org.p2p.solanaj.core.message;

import lombok.Getter;
import lombok.Setter;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.rpc.types.AddressLookupTableAccount;
import org.p2p.solanaj.utils.ShortvecEncoding;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a versioned Solana message with Address Lookup Table (ALT) support.
 */
@Getter
@Setter
public class MessageV0 extends Message implements VersionedMessage {
    private static final int VERSION = 0;
    private static final int VERSION_PREFIX = 1 << 7;
    private static final int VERSION_PREFIX_MASK = 0x7F;
    private static final int PACKET_DATA_SIZE = 1280 - 40 - 8;

    private final List<MessageAddressTableLookup> addressTableLookups;

    public MessageV0(MessageHeader header, List<PublicKey> staticAccountKeys, String recentBlockhash,
                     List<MessageCompiledInstruction> compiledInstructions, List<MessageAddressTableLookup> addressTableLookups) {
        super(header, staticAccountKeys, recentBlockhash, compiledInstructions);
        this.addressTableLookups = addressTableLookups;
    }

    @Override
    public Version getVersion() {
        return Version.V0;
    }

    public static MessageV0 compile(PublicKey payerKey, List<TransactionInstruction> instructions,
                                    String recentBlockhash, List<AddressLookupTableAccount> addressLookupTableAccounts) {
        Objects.requireNonNull(payerKey, "Payer key is required");
        Objects.requireNonNull(recentBlockhash, "Recent blockhash is required");
        if (instructions.isEmpty()) {
            throw new IllegalArgumentException("Instructions cannot be empty");
        }
        if (addressLookupTableAccounts.isEmpty()) {
            throw new IllegalArgumentException("ATL cannot be empty");
        }

        CompiledKeys compiledKeys = CompiledKeys.compile(instructions, payerKey);

        List<MessageAddressTableLookup> addressTableLookups = new ArrayList<>();
        LoadedAddresses accountKeysFromLookups = resolveAddressTableLookups(addressTableLookups, addressLookupTableAccounts);

        CompiledKeys.MessageComponents messageComponents = compiledKeys.getMessageComponents();
        MessageHeader header = messageComponents.getHeader();
        List<PublicKey> staticKeys = messageComponents.getStaticAccountKeys();
        MessageAccountKeys accountKeys = new MessageAccountKeys(
                staticKeys,
                new LoadedAddresses(accountKeysFromLookups.getWritable(), accountKeysFromLookups.getReadonly()));

        List<MessageCompiledInstruction> compiledInstructions = accountKeys.compileInstructions(instructions);
        return new MessageV0(header, staticKeys, recentBlockhash, compiledInstructions, addressTableLookups);
    }

    private static LoadedAddresses resolveAddressTableLookups(
            List<MessageAddressTableLookup> addressTableLookups,
            List<AddressLookupTableAccount> addressLookupTableAccounts) {
        List<PublicKey> writableKeys = new ArrayList<>();
        List<PublicKey> readonlyKeys = new ArrayList<>();

        for (MessageAddressTableLookup tableLookup : addressTableLookups) {
            AddressLookupTableAccount tableAccount = addressLookupTableAccounts.stream()
                    .filter(account -> account.getKey().equals(tableLookup.getAccountKey()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format(
                            "Address lookup table not found for key %s. Available keys: %s",
                            tableLookup.getAccountKey().toBase58(),
                            addressLookupTableAccounts.stream()
                                    .map(account -> account.getKey().toBase58())
                                    .collect(Collectors.joining(", ")))));

            for (int index : tableLookup.getWritableIndexes()) {
                if (index < tableAccount.getState().getAddresses().size()) {
                    writableKeys.add(tableAccount.getState().getAddresses().get(index));
                } else {
                    throw new IllegalArgumentException(
                            "Failed to find address for index " + index + " in address lookup table " +
                                    tableLookup.getAccountKey().toBase58());
                }
            }

            for (int index : tableLookup.getReadonlyIndexes()) {
                if (index < tableAccount.getState().getAddresses().size()) {
                    readonlyKeys.add(tableAccount.getState().getAddresses().get(index));
                } else {
                    throw new IllegalArgumentException(
                            "Failed to find address for index " + index + " in address lookup table " +
                                    tableLookup.getAccountKey().toBase58());
                }
            }
        }

        return new LoadedAddresses(writableKeys, readonlyKeys);
    }

    @Override
    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(PACKET_DATA_SIZE).order(ByteOrder.LITTLE_ENDIAN);

        // Add version prefix
        buffer.put((byte) VERSION_PREFIX);

        // Serialize header and keys
        buffer.put(getHeader().serialize());
        buffer.put(ShortvecEncoding.encodeLength(getAccountKeys().size()));
        for (PublicKey key : getAccountKeys()) {
            buffer.put(key.toByteArray());
        }

        // Add recent blockhash
        buffer.put(Base58.decode(getRecentBlockhash()));

        // Serialize instructions
        buffer.put(ShortvecEncoding.encodeLength(getInstructions().size()));
        for (MessageCompiledInstruction instruction : getInstructions()) {
            buffer.put(instruction.serialize());
        }

        // Serialize address table lookups
        buffer.put(ShortvecEncoding.encodeLength(addressTableLookups.size()));
        for (MessageAddressTableLookup lookup : addressTableLookups) {
            buffer.put(lookup.serialize());
        }

        if (buffer.position() > PACKET_DATA_SIZE) {
            throw new IllegalStateException("Serialized message exceeds allowed packet size.");
        }

        return Arrays.copyOf(buffer.array(), buffer.position());
    }

    public static MessageV0 deserialize(ByteBuffer buffer) {

        // Step 1: Check version
        int prefix = Byte.toUnsignedInt(buffer.get());
        int version = prefix & VERSION_PREFIX_MASK;
        if (version != VERSION) {
            throw new IllegalArgumentException(String.format(
                    "Unexpected versioned message. Prefix: %d, Expected version: %d, Found version: %d",
                    prefix, VERSION, version
            ));
        }

        // Step 2: Deserialize header
        MessageHeader header = MessageHeader.deserialize(buffer);

        // Step 3: Deserialize static account keys
        int staticAccountKeysLength = ShortvecEncoding.decodeLength(buffer);
        List<PublicKey> staticAccountKeys = new ArrayList<>();
        for (int i = 0; i < staticAccountKeysLength; i++) {
            byte[] keyBytes = new byte[PublicKey.PUBLIC_KEY_LENGTH];
            buffer.get(keyBytes);
            staticAccountKeys.add(new PublicKey(keyBytes));
        }

        // Step 4: Deserialize recent blockhash
        byte[] recentBlockhashBytes = new byte[PublicKey.PUBLIC_KEY_LENGTH];
        buffer.get(recentBlockhashBytes);
        String recentBlockhash = Base58.encode(recentBlockhashBytes);

        // Step 5: Deserialize instructions
        int instructionCount = ShortvecEncoding.decodeLength(buffer);
        List<MessageCompiledInstruction> compiledInstructions = new ArrayList<>();
        for (int i = 0; i < instructionCount; i++) {
            compiledInstructions.add(MessageCompiledInstruction.deserialize(buffer));
        }

        // Step 6: Deserialize address table lookups
        int addressTableLookupsCount = ShortvecEncoding.decodeLength(buffer);
        List<MessageAddressTableLookup> addressTableLookups = new ArrayList<>();
        for (int i = 0; i < addressTableLookupsCount; i++) {
            addressTableLookups.add(MessageAddressTableLookup.deserialize(buffer));
        }

        // Step 7: Ensure no extra data remains
        if (buffer.remaining() > 0) {
            throw new IllegalStateException(String.format(
                    "Extra bytes found in serialized message data. Remaining bytes: %d",
                    buffer.remaining()
            ));
        }

        // Step 8: Return the deserialized MessageV0
        return new MessageV0(header, staticAccountKeys, recentBlockhash, compiledInstructions, addressTableLookups);
    }

    public static MessageV0 deserialize(byte[] serializedMessage) {
        ByteBuffer buffer = ByteBuffer.wrap(serializedMessage).order(ByteOrder.LITTLE_ENDIAN);
        return deserialize(buffer);
    }

    public static boolean validateSerialization(MessageV0 original) {
        byte[] serialized = original.serialize();
        MessageV0 deserialized = MessageV0.deserialize(serialized);
        return original.equals(deserialized);
    }

}