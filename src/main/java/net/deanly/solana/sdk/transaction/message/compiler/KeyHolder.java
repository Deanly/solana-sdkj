package net.deanly.solana.sdk.transaction.message.compiler;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.rpc.client.Network;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.solana.sdk.transaction.message.meta.MessageAddressTableLookup;
import net.deanly.solana.sdk.transaction.message.meta.MessageHeader;
import net.deanly.solana.sdk.program.spl.alt.state.AddressLookupTableAccount;

import java.util.*;
import java.util.stream.Collectors;

public class KeyHolder {
    private final Map<PublicKey, KeyMeta> keyMetaMap = new LinkedHashMap<>();

    public static KeyHolder compileKeys(Network network, List<TransactionInstruction> instructions, PublicKey payer) {
        KeyHolder keyHolder = new KeyHolder();
        keyHolder.addKey(payer, true, true); // Payer is always a signer and writable

        for (TransactionInstruction instruction : instructions) {
            keyHolder.addKey(instruction.getProgramId(network), false, false);
            for (var accountMeta : instruction.getKeys()) {
                keyHolder.updateKey(accountMeta.getPublicKey(), accountMeta.isSigner(), accountMeta.isWritable());
            }
        }
        return keyHolder;
    }

    public void addKey(PublicKey key, boolean isSigner, boolean isWritable) {
        keyMetaMap.putIfAbsent(key, new KeyMeta(isSigner, isWritable));
    }

    public void updateKey(PublicKey key, boolean isSigner, boolean isWritable) {
        keyMetaMap.computeIfPresent(key, (k, meta) -> {
            meta.setSigner(meta.isSigner() || isSigner);
            meta.setWritable(meta.isWritable() || isWritable);
            return meta;
        });
    }

    public int getKeyIndex(PublicKey key) {
        List<PublicKey> allKeys = getAllSortedKeys();
        return allKeys.indexOf(key);
    }

    public List<PublicKey> getAllSortedKeys() {
        return keyMetaMap.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getValue().priority()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public MessageHeader generateHeader() {
        long writableSigners = keyMetaMap.values().stream().filter(KeyMeta::isWritableSigner).count();
        long readonlySigners = keyMetaMap.values().stream().filter(KeyMeta::isReadonlySigner).count();
        long readonlyNonSigners = keyMetaMap.values().stream().filter(KeyMeta::isReadonlyNonSigner).count();

        return new MessageHeader((int) writableSigners, (int) readonlySigners, (int) readonlyNonSigners);
    }

    public List<MessageAddressTableLookup> processAddressTables(List<AddressLookupTableAccount> addressLookupTableAccounts) {
        List<MessageAddressTableLookup> lookups = new ArrayList<>();
        for (AddressLookupTableAccount atl : addressLookupTableAccounts) {
            MessageAddressTableLookup lookup = extractFromATL(atl);
            if (lookup != null) {
                lookups.add(lookup);
            }
        }
        return lookups;
    }

    private MessageAddressTableLookup extractFromATL(AddressLookupTableAccount atl) {
        List<Integer> writableIndexes = new ArrayList<>();
        List<Integer> readonlyIndexes = new ArrayList<>();
        List<PublicKey> atlKeys = atl.getState().getAddresses();

        for (int i = 0; i < atlKeys.size(); i++) {
            if (keyMetaMap.containsKey(atlKeys.get(i))) {
                KeyMeta meta = keyMetaMap.get(atlKeys.get(i));
                if (meta.isWritable()) {
                    writableIndexes.add(i);
                } else {
                    readonlyIndexes.add(i);
                }
                keyMetaMap.remove(atlKeys.get(i)); // Remove from static keys
            }
        }

        if (writableIndexes.isEmpty() && readonlyIndexes.isEmpty()) {
            return null;
        }
        return new MessageAddressTableLookup(atl.getKey(), writableIndexes, readonlyIndexes);
    }

    @Data
    @AllArgsConstructor
    private static class KeyMeta {
        private boolean isSigner;
        private boolean isWritable;

        public boolean isWritableSigner() {
            return isSigner;
        }

        public boolean isReadonlySigner() {
            return isSigner && !isWritable;
        }

        public boolean isReadonlyNonSigner() {
            return !isSigner && !isWritable;
        }

        public int priority() {
            if (isSigner && isWritable) return 0;
            if (isSigner) return 1;
            if (isWritable) return 2;
            return 3;
        }
    }
}