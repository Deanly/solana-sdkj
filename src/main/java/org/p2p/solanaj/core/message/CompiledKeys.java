package org.p2p.solanaj.core.message;

import lombok.*;
import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.rpc.types.AddressLookupTableAccount;

import java.util.*;
import java.util.stream.Collectors;

@Value
@ToString
public class CompiledKeys {
    PublicKey payer;
    Map<String, CompiledKeyMeta> keyMetaMap;

    public CompiledKeys(PublicKey payer, Map<String, CompiledKeyMeta> keyMetaMap) {
        this.payer = payer;
        this.keyMetaMap = keyMetaMap;
    }

    public static CompiledKeys compile(List<TransactionInstruction> instructions, PublicKey payer) {
        Objects.requireNonNull(payer, "Payer is required");

        Map<String, CompiledKeyMeta> keyMetaMap = new HashMap<>();
        CompiledKeyMeta payerKeyMeta = keyMetaMap.computeIfAbsent(payer.toBase58(), k -> new CompiledKeyMeta());
        payerKeyMeta.setSigner(true);
        payerKeyMeta.setWritable(true);

        for (TransactionInstruction instruction : instructions) {
            keyMetaMap.computeIfAbsent(instruction.getProgramId().toBase58(), k -> new CompiledKeyMeta()).setInvoked(true);
            for (AccountMeta accountMeta : instruction.getKeys()) {
                CompiledKeyMeta keyMeta = keyMetaMap.computeIfAbsent(accountMeta.getPublicKey().toBase58(), k -> new CompiledKeyMeta());
                keyMeta.setSigner(keyMeta.isSigner() || accountMeta.isSigner());
                keyMeta.setWritable(keyMeta.isWritable() || accountMeta.isWritable());
            }
        }

        return new CompiledKeys(payer, keyMetaMap);
    }

    public MessageComponents getMessageComponents() {
        List<Map.Entry<String, CompiledKeyMeta>> mapEntries = new ArrayList<>(keyMetaMap.entrySet());
        if (mapEntries.size() > 256) {
            throw new IllegalArgumentException("Max static account keys length exceeded");
        }

        List<Map.Entry<String, CompiledKeyMeta>> writableSigners = filterEntries(mapEntries, true, true);
        List<Map.Entry<String, CompiledKeyMeta>> readonlySigners = filterEntries(mapEntries, true, false);
        List<Map.Entry<String, CompiledKeyMeta>> writableNonSigners = filterEntries(mapEntries, false, true);
        List<Map.Entry<String, CompiledKeyMeta>> readonlyNonSigners = filterEntries(mapEntries, false, false);

        MessageHeader header = new MessageHeader(
                writableSigners.size() + readonlySigners.size(),
                readonlySigners.size(),
                readonlyNonSigners.size()
        );

        if (writableSigners.isEmpty()) {
            throw new IllegalArgumentException("Expected at least one writable signer key");
        }

        String payerAddress = writableSigners.get(0).getKey();
        if (!payerAddress.equals(payer.toBase58())) {
            throw new IllegalArgumentException("Expected first writable signer key to be the fee payer");
        }

        List<PublicKey> staticAccountKeys = new ArrayList<>();
        staticAccountKeys.addAll(writableSigners.stream().map(e -> new PublicKey(e.getKey())).collect(Collectors.toList()));
        staticAccountKeys.addAll(readonlySigners.stream().map(e -> new PublicKey(e.getKey())).collect(Collectors.toList()));
        staticAccountKeys.addAll(writableNonSigners.stream().map(e -> new PublicKey(e.getKey())).collect(Collectors.toList()));
        staticAccountKeys.addAll(readonlyNonSigners.stream().map(e -> new PublicKey(e.getKey())).collect(Collectors.toList()));

        return new MessageComponents(header, staticAccountKeys);
    }

    public Optional<TableLookupResult> extractTableLookup(AddressLookupTableAccount lookupTable) {
        List<PublicKey> lookupTableEntries = lookupTable.getState().getAddresses();

        List<Integer> writableIndexes = new ArrayList<>();
        List<PublicKey> drainedWritableKeys = new ArrayList<>();
        drainKeysFoundInLookupTable(lookupTableEntries, writableIndexes, drainedWritableKeys,
                meta -> !meta.isSigner() && !meta.isInvoked() && meta.isWritable());

        List<Integer> readonlyIndexes = new ArrayList<>();
        List<PublicKey> drainedReadonlyKeys = new ArrayList<>();
        drainKeysFoundInLookupTable(lookupTableEntries, readonlyIndexes, drainedReadonlyKeys,
                meta -> !meta.isSigner() && !meta.isInvoked() && !meta.isWritable());

        if (writableIndexes.isEmpty() && readonlyIndexes.isEmpty()) {
            return Optional.empty();
        }

        MessageAddressTableLookup tableLookup = new MessageAddressTableLookup(
                lookupTable.getKey(), writableIndexes, readonlyIndexes
        );
        LoadedAddresses keysFromLookups = new LoadedAddresses(drainedWritableKeys, drainedReadonlyKeys);
        return Optional.of(new TableLookupResult(tableLookup, keysFromLookups));
    }

    private void drainKeysFoundInLookupTable(List<PublicKey> lookupTableEntries,
                                             List<Integer> lookupIndexes,
                                             List<PublicKey> drainedKeys,
                                             KeyMetaFilter filter) {
        Iterator<Map.Entry<String, CompiledKeyMeta>> iterator = keyMetaMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CompiledKeyMeta> entry = iterator.next();
            String address = entry.getKey();
            CompiledKeyMeta meta = entry.getValue();

            if (filter.filter(meta)) {
                PublicKey key = new PublicKey(address);
                int index = lookupTableEntries.indexOf(key);
                if (index >= 0 && index < 256) {
                    lookupIndexes.add(index);
                    drainedKeys.add(key);
                    iterator.remove();
                }
            }
        }
    }

    private List<Map.Entry<String, CompiledKeyMeta>> filterEntries(
            List<Map.Entry<String, CompiledKeyMeta>> entries, boolean isSigner, boolean isWritable) {
        return entries.stream()
                .filter(entry -> entry.getValue().isSigner() == isSigner && entry.getValue().isWritable() == isWritable)
                .collect(Collectors.toList());
    }

    @FunctionalInterface
    private interface KeyMetaFilter {
        boolean filter(CompiledKeyMeta keyMeta);
    }

    @Data
    @RequiredArgsConstructor
    public static class MessageComponents {
        private final MessageHeader header;
        private final List<PublicKey> staticAccountKeys;

    }

    @Data
    @RequiredArgsConstructor
    public static class TableLookupResult {
        private final MessageAddressTableLookup tableLookup;
        private final LoadedAddresses keysFromLookups;

        public MessageAddressTableLookup getTableLookup() {
            return tableLookup;
        }

        public LoadedAddresses getKeysFromLookups() {
            return keysFromLookups;
        }
    }

    @Data
    @AllArgsConstructor
    public static class CompiledKeyMeta {
        boolean isSigner;
        boolean isWritable;
        boolean isInvoked;

        public CompiledKeyMeta() {
            this.isSigner = false;
            this.isWritable = false;
            this.isInvoked = false;
        }
    }
}