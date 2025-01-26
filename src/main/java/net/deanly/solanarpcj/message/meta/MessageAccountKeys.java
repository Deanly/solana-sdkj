package net.deanly.solanarpcj.message.meta;

import lombok.ToString;
import lombok.Value;
import net.deanly.solanarpcj.core.PublicKey;
import net.deanly.solanarpcj.core.TransactionInstruction;

import java.util.*;
import java.util.stream.Collectors;

@Value
@ToString
public class MessageAccountKeys {

    List<PublicKey> staticAccountKeys;
    LoadedAddresses accountKeysFromLookups;

    public MessageAccountKeys(List<PublicKey> staticAccountKeys, LoadedAddresses accountKeysFromLookups) {
        this.staticAccountKeys = Objects.requireNonNull(staticAccountKeys, "Static account keys cannot be null");
        this.accountKeysFromLookups = accountKeysFromLookups;
    }

    public MessageAccountKeys(List<PublicKey> staticAccountKeys) {
        this.staticAccountKeys = Objects.requireNonNull(staticAccountKeys, "Static account keys cannot be null");
        this.accountKeysFromLookups = new LoadedAddresses(Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Returns key segments in order: static keys, writable lookup keys, readonly lookup keys.
     */
    public List<List<PublicKey>> keySegments() {
        List<List<PublicKey>> keySegments = new ArrayList<>();
        keySegments.add(staticAccountKeys);
        if (accountKeysFromLookups != null) {
            keySegments.add(accountKeysFromLookups.getWritable());
            keySegments.add(accountKeysFromLookups.getReadonly());
        }
        return keySegments;
    }

    /**
     * Gets a PublicKey at a specific index across all key segments.
     *
     * @param index the index of the PublicKey.
     * @return the PublicKey if found, or null if the index is out of bounds.
     */
    public PublicKey get(int index) {
        for (List<PublicKey> keySegment : keySegments()) {
            if (index < keySegment.size()) {
                return keySegment.get(index);
            } else {
                index -= keySegment.size();
            }
        }
        return null;
    }

    /**
     * Returns the total number of keys across all segments.
     *
     * @return the total key count.
     */
    public int getLength() {
        return keySegments().stream().mapToInt(List::size).sum();
    }
    /**
     * Compiles instructions by resolving key indices and creating compiled instructions.
     *
     * @param instructions the transaction instructions to compile.
     * @return a list of compiled instructions.
     */
    public List<MessageCompiledInstruction> compileInstructions(List<TransactionInstruction> instructions) {
        final int U8_MAX = 255;
        if (getLength() > U8_MAX + 1) {
            throw new IllegalArgumentException("Account index overflow encountered during compilation");
        }

        // 1. Create keyIndexMap
        Map<String, Integer> keyIndexMap = new HashMap<>();
        List<PublicKey> allKeys = keySegments().stream()
                .flatMap(List::stream)
                .toList();
        for (int i = 0; i < allKeys.size(); i++) {
            keyIndexMap.put(allKeys.get(i).toBase58(), i);
        }

        // 2. Compile instructions
        return instructions.stream().map(instruction -> {
            int programIdIndex = findKeyIndex(keyIndexMap, instruction.getProgramId());
            List<Integer> accountKeyIndexes = instruction.getKeys().stream()
                    .map(meta -> findKeyIndex(keyIndexMap, meta.getPublicKey()))
                    .collect(Collectors.toList());
            return new MessageCompiledInstruction(programIdIndex, accountKeyIndexes, instruction.getData());
        }).collect(Collectors.toList());
    }

    /**
     * Finds the key index for a given public key.
     *
     * @param keyIndexMap The map of keys to indices.
     * @param key The public key to find.
     * @return The index of the key.
     * @throws IllegalArgumentException If the key is not found in the map.
     */
    private int findKeyIndex(Map<String, Integer> keyIndexMap, PublicKey key) {
        Integer index = keyIndexMap.get(key.toBase58());
        if (index == null) {
            throw new IllegalArgumentException(
                    "Encountered an unknown instruction account key during compilation: " + key.toBase58());
        }
        return index;
    }

}