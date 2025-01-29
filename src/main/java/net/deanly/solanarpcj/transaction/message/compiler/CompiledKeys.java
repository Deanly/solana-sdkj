package net.deanly.solanarpcj.transaction.message.compiler;

import lombok.*;
import net.deanly.solanarpcj.program.alt.state.AddressLookupTableAccount;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.transaction.instruction.TransactionInstruction;
import net.deanly.solanarpcj.transaction.message.meta.LoadedAddresses;
import net.deanly.solanarpcj.transaction.message.meta.MessageAddressTableLookup;
import net.deanly.solanarpcj.transaction.message.meta.MessageHeader;

import java.util.*;
import java.util.stream.Collectors;

@Data
@ToString
public class CompiledKeys {
    PublicKey payer;
    Map<PublicKey, CompiledKeyMeta> staticKeyMetaMap;

    public CompiledKeys(PublicKey payer, Map<PublicKey, CompiledKeyMeta> staticKeyMetaMap) {
        this.payer = payer;
        this.staticKeyMetaMap = staticKeyMetaMap;
    }

    public static CompiledKeys compile(List<TransactionInstruction> instructions, PublicKey payer) {
        Objects.requireNonNull(payer, "Payer is required");

        Map<PublicKey, CompiledKeyMeta> keyMetaMap = new HashMap<>();
        CompiledKeyMeta payerKeyMeta = keyMetaMap.computeIfAbsent(payer, k -> new CompiledKeyMeta());
        payerKeyMeta.setSigner(true);
        payerKeyMeta.setWritable(true);

        for (TransactionInstruction instruction : instructions) {
            keyMetaMap.computeIfAbsent(instruction.getProgramId(), k -> new CompiledKeyMeta()).setInvoked(true);
            for (AccountMeta accountMeta : instruction.getKeys()) {
                CompiledKeyMeta keyMeta = keyMetaMap.computeIfAbsent(accountMeta.getPublicKey(), k -> new CompiledKeyMeta());
                keyMeta.setSigner(keyMeta.isSigner() || accountMeta.isSigner());
                keyMeta.setWritable(keyMeta.isWritable() || accountMeta.isWritable());
            }
        }

        return new CompiledKeys(payer, keyMetaMap);
    }

    public MessageComponents getMessageComponents() {
        List<Map.Entry<PublicKey, CompiledKeyMeta>> mapEntries = new ArrayList<>(staticKeyMetaMap.entrySet());
        if (mapEntries.size() > 256) {
            throw new IllegalArgumentException("Max static account keys length exceeded");
        }

        List<Map.Entry<PublicKey, CompiledKeyMeta>> writableSigners = filterEntries(mapEntries, true, true);
        List<Map.Entry<PublicKey, CompiledKeyMeta>> readonlySigners = filterEntries(mapEntries, true, false);
        List<Map.Entry<PublicKey, CompiledKeyMeta>> writableNonSigners = filterEntries(mapEntries, false, true);
        List<Map.Entry<PublicKey, CompiledKeyMeta>> readonlyNonSigners = filterEntries(mapEntries, false, false);

        MessageHeader header = new MessageHeader(
                writableSigners.size() + readonlySigners.size(),
                readonlySigners.size(),
                readonlyNonSigners.size()
        );

        if (writableSigners.isEmpty()) {
            throw new IllegalArgumentException("Expected at least one writable signer key");
        }

        PublicKey payerAddress = writableSigners.get(0).getKey();
        if (!payerAddress.equals(payer)) {
            throw new IllegalArgumentException("Expected first writable signer key to be the fee payer");
        }

        List<PublicKey> staticAccountKeys = new ArrayList<>();
        staticAccountKeys.addAll(writableSigners.stream().map(Map.Entry::getKey).toList());
        staticAccountKeys.addAll(readonlySigners.stream().map(Map.Entry::getKey).toList());
        staticAccountKeys.addAll(writableNonSigners.stream().map(Map.Entry::getKey).toList());
        staticAccountKeys.addAll(readonlyNonSigners.stream().map(Map.Entry::getKey).toList());

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
        Iterator<Map.Entry<PublicKey, CompiledKeyMeta>> iterator = staticKeyMetaMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<PublicKey, CompiledKeyMeta> entry = iterator.next();
            PublicKey address = entry.getKey();
            CompiledKeyMeta meta = entry.getValue();

            if (filter.filter(meta)) {
                int index = lookupTableEntries.indexOf(address);
                if (index >= 0 && index < 256) {
                    lookupIndexes.add(index);
                    drainedKeys.add(address);
                    iterator.remove();
                }
            }
        }
    }

    private List<Map.Entry<PublicKey, CompiledKeyMeta>> filterEntries(
            List<Map.Entry<PublicKey, CompiledKeyMeta>> entries, boolean isSigner, boolean isWritable) {
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