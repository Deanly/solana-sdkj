package net.deanly.solana.sdk.transaction.message.compiler;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.solana.sdk.transaction.message.Message;
import net.deanly.solana.sdk.transaction.message.MessageV0;
import net.deanly.solana.sdk.program.alt.state.AddressLookupTableAccount;
import net.deanly.solana.sdk.transaction.message.meta.LoadedAddresses;
import net.deanly.solana.sdk.transaction.message.meta.MessageAddressTableLookup;
import net.deanly.solana.sdk.transaction.message.meta.MessageCompiledInstruction;
import net.deanly.solana.sdk.transaction.message.meta.MessageHeader;
import net.deanly.solana.sdk.types.Blockhash;

import java.util.*;

public class MessageCompiler {

    /**
     * Compile the message using the payer key, instructions, and recent blockhash.
     */
    public static Message compileLegacy(PublicKey payerKey, List<TransactionInstruction> instructions, Blockhash recentBlockhash) {
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

    public static MessageV0 compileV0(PublicKey payerKey, List<TransactionInstruction> instructions,
                                      Blockhash recentBlockhash, List<AddressLookupTableAccount> addressLookupTableAccounts) {
        Objects.requireNonNull(payerKey, "Payer key is required");
        Objects.requireNonNull(recentBlockhash, "Recent blockhash is required");
        if (instructions == null || instructions.isEmpty()) {
            throw new IllegalArgumentException("Instructions cannot be empty");
        }

        // Compile static and dynamic account keys
        CompiledKeys compiledKeys = CompiledKeys.compile(instructions, payerKey);

        List<MessageAddressTableLookup> addressTableLookups = new ArrayList<>();
        LoadedAddresses accountKeysFromLookups = new LoadedAddresses(new ArrayList<>(), new ArrayList<>());

        if (addressLookupTableAccounts != null && !addressLookupTableAccounts.isEmpty()) {
            for (AddressLookupTableAccount lookupTable : addressLookupTableAccounts) {
                compiledKeys.extractTableLookup(lookupTable).ifPresent(extractionResult -> {
                    MessageAddressTableLookup addressTableLookup = extractionResult.getTableLookup();
                    accountKeysFromLookups.getWritable().addAll(extractionResult.getKeysFromLookups().getWritable());
                    accountKeysFromLookups.getReadonly().addAll(extractionResult.getKeysFromLookups().getReadonly());
                    addressTableLookups.add(addressTableLookup);
                });
            }
        }

        // Get compiled message header and static keys
        CompiledKeys.MessageComponents components = compiledKeys.getMessageComponents();
        List<PublicKey> staticAccountKeys = components.getStaticAccountKeys();
        MessageHeader header = components.getHeader();

        // Combine static keys and dynamic keys into account keys
        MessageAccountKeys accountKeys = new MessageAccountKeys(
                staticAccountKeys,
                accountKeysFromLookups
        );

        // Compile instructions using account keys
        List<MessageCompiledInstruction> compiledInstructions = accountKeys.compileInstructions(instructions);

        // Construct the final MessageV0 object
        return new MessageV0(header, staticAccountKeys, recentBlockhash, compiledInstructions, addressTableLookups);
    }

}