package net.deanly.solana.sdk.transaction.message;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructSequenceObjectField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.solana.sdk.layout.field.ShortVecField;
import net.deanly.solana.sdk.transaction.message.compiler.MessageCompiler;
import net.deanly.solana.sdk.transaction.message.meta.LoadedAddresses;
import net.deanly.solana.sdk.transaction.message.meta.MessageAddressTableLookup;
import net.deanly.solana.sdk.transaction.message.meta.MessageCompiledInstruction;
import net.deanly.solana.sdk.transaction.message.meta.MessageHeader;
import net.deanly.solana.sdk.program.alt.state.AddressLookupTableAccount;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * Represents a versioned Solana message with Address Lookup Table (ALT) support.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MessageV0 extends Message implements VersionedMessage {
    private static final short VERSION_PREFIX = 1 << 7;
    private static final int VERSION_PREFIX_MASK = 0x7F;
    private static final int PACKET_DATA_SIZE = 1280 - 40 - 8;

    @StructField(order = 0, type = UInt8Field.class)
    private final short versionPrefix = VERSION_PREFIX;

    @StructSequenceObjectField(order = 5, lengthType = ShortVecField.class)
    private List<MessageAddressTableLookup> addressTableLookups;


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
        return MessageCompiler.compileV0(payerKey, instructions, recentBlockhash, addressLookupTableAccounts);
    }

    public static LoadedAddresses resolveAddressTableLookups(
            List<MessageAddressTableLookup> addressTableLookups,
            List<AddressLookupTableAccount> addressLookupTableAccounts) {

        // Writable and Readonly keys to be collected
        List<PublicKey> writableKeys = new ArrayList<>();
        List<PublicKey> readonlyKeys = new ArrayList<>();

        // Iterate through each lookup in the address table lookups
        for (MessageAddressTableLookup tableLookup : addressTableLookups) {
            // Search for matching AddressLookupTableAccount by accountKey
            AddressLookupTableAccount matchingAccount = addressLookupTableAccounts.stream()
                    .filter(account -> account.getKey().equals(tableLookup.getAccountKey()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format(
                            "Address lookup table not found for account key: %s",
                            tableLookup.getAccountKey().toBase58()
                    )));

            // Extract writable keys by their indexes
            for (Integer writableIndex : tableLookup.getWritableIndexes()) {
                if (writableIndex >= 0 && writableIndex < matchingAccount.getState().getAddresses().size()) {
                    writableKeys.add(matchingAccount.getState().getAddresses().get(writableIndex));
                } else {
                    throw new IllegalArgumentException(String.format(
                            "Writable address index out of bounds. Index: %d, Total Addresses: %d",
                            writableIndex, matchingAccount.getState().getAddresses().size()
                    ));
                }
            }

            // Extract readonly keys by their indexes
            for (Integer readonlyIndex : tableLookup.getReadonlyIndexes()) {
                if (readonlyIndex >= 0 && readonlyIndex < matchingAccount.getState().getAddresses().size()) {
                    readonlyKeys.add(matchingAccount.getState().getAddresses().get(readonlyIndex));
                } else {
                    throw new IllegalArgumentException(String.format(
                            "Readonly address index out of bounds. Index: %d, Total Addresses: %d",
                            readonlyIndex, matchingAccount.getState().getAddresses().size()
                    ));
                }
            }
        }

        // Combine writable and readonly keys into a LoadedAddresses object
        return new LoadedAddresses(writableKeys, readonlyKeys);
    }

    @Override
    public byte[] serialize() {
        byte[] serializedData = StructLayout.encode(this);

        if (serializedData.length > PACKET_DATA_SIZE) {
            throw new IllegalStateException(String.format(
                    "Serialized message exceeds allowed packet size. Size: %d, Limit: %d",
                    serializedData.length, PACKET_DATA_SIZE
            ));
        }

        return serializedData;
    }

    public static MessageV0 deserialize(ByteBuffer buffer) {
        return StructLayout.decode(buffer.array(), MessageV0.class);
    }

    public static MessageV0 deserialize(byte[] serializedMessage) {
        ByteBuffer buffer = ByteBuffer.wrap(serializedMessage).order(ByteOrder.LITTLE_ENDIAN);
        return deserialize(buffer);
    }

    public List<MessageAddressTableLookup> getAddressTableLookup(
            List<AddressLookupTableAccount> addressLookupTableAccounts) {
        // Ensure required lookup table accounts are provided
        if (addressLookupTableAccounts == null || addressLookupTableAccounts.isEmpty()) {
            throw new IllegalArgumentException(
                    "Address lookup table accounts must be provided to retrieve address table lookups"
            );
        }

        // Initialize result collection to store discovered MessageAddressTableLookups
        List<MessageAddressTableLookup> resolvedAddressTableLookups = new ArrayList<>();

        // Iterate through each MessageAddressTableLookup in the current message
        for (MessageAddressTableLookup tableLookup : addressTableLookups) {
            // Find the AddressLookupTableAccount matching the key
            AddressLookupTableAccount tableAccount = addressLookupTableAccounts.stream()
                    .filter(account -> account.getKey().equals(tableLookup.getAccountKey()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format(
                                    "Failed to find address lookup table account for key: %s",
                                    tableLookup.getAccountKey().toBase58()
                            )
                    ));

            // Validate that writable and readonly indexes are within bounds
            for (Integer index : tableLookup.getWritableIndexes()) {
                if (index < 0 || index >= tableAccount.getState().getAddresses().size()) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Writable index %d is out of bounds for address lookup table %s",
                                    index, tableLookup.getAccountKey().toBase58()
                            )
                    );
                }
            }
            for (Integer index : tableLookup.getReadonlyIndexes()) {
                if (index < 0 || index >= tableAccount.getState().getAddresses().size()) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Readonly index %d is out of bounds for address lookup table %s",
                                    index, tableLookup.getAccountKey().toBase58()
                            )
                    );
                }
            }

            // Add the validated table lookup to the result
            resolvedAddressTableLookups.add(tableLookup);
        }

        // Return the resolved address table lookups
        return resolvedAddressTableLookups;
    }

    /**
     * Determines if the account at the given index is writable.
     *
     * @param index The index of the account to check.
     * @return True if the account is writable, false otherwise.
     */
    @Override
    public boolean isAccountWritable(int index) {
        int numSignedAccounts = header.getNumRequiredSignatures();
        int numStaticAccountKeys = this.staticAccountKeys.size();

        if (index >= numStaticAccountKeys) {
            // Checking lookup account keys
            int lookupAccountKeysIndex = index - numStaticAccountKeys;
            int numWritableLookupAccountKeys = addressTableLookups.stream()
                    .mapToInt(lookup -> lookup.getWritableIndexes().size())
                    .sum();
            return lookupAccountKeysIndex < numWritableLookupAccountKeys;
        } else if (index >= header.getNumRequiredSignatures()) {
            // Checking unsigned static accounts
            int unsignedAccountIndex = index - numSignedAccounts;
            int numUnsignedAccounts = numStaticAccountKeys - numSignedAccounts;
            int numWritableUnsignedAccounts = numUnsignedAccounts - header.getNumReadonlyUnsignedAccounts();
            return unsignedAccountIndex < numWritableUnsignedAccounts;
        } else {
            // Checking signed static accounts
            int numWritableSignedAccounts = numSignedAccounts - header.getNumReadonlySignedAccounts();
            return index < numWritableSignedAccounts;
        }
    }

    public static boolean validateSerialization(MessageV0 original) {
        byte[] serialized = original.serialize();
        MessageV0 deserialized = MessageV0.deserialize(serialized);
        return original.equals(deserialized);
    }

}