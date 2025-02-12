package net.deanly.solana.sdk.program.spl.alt.state;

import lombok.*;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.Struct;
import net.deanly.solana.sdk.layout.field.PublicKeyPadding2Field;
import net.deanly.solana.sdk.layout.field.PublicKeyField;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.type.advanced.NoneField;
import net.deanly.structlayout.type.basic.*;
import net.deanly.structlayout.type.guava.UnsignedLong;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@RequiredArgsConstructor
public class AddressLookupTableAccount {
    private static final UnsignedLong U64_MAX_BIGINT = UInt64LEField.UINT64_MAX;

    private final PublicKey key;
    private final State state;

    public boolean isActive() {
        return this.state.deactivationSlot.equals(U64_MAX_BIGINT);
    }

    public static AddressLookupTableAccount deserialize(PublicKey key, byte[] data) {
        var state = StructLayout.decode(data, State.class);
        return new AddressLookupTableAccount(key, state);
    }

    public static List<PublicKey> resolveAddresses(List<AddressLookupTableAccount> lookupTableAccounts) {
        List<PublicKey> resolvedAddresses = new ArrayList<>();

        for (AddressLookupTableAccount lookupTable : lookupTableAccounts) {
            resolvedAddresses.addAll(lookupTable.getState().getAddresses());
        }

        return resolvedAddresses;
    }


    @Getter
    @ToString
    @NoArgsConstructor
    public static class State extends Struct {
        @StructField(order = 1, type = UInt32LEField.class)
        long typeIndex;

        @StructField(order = 2, type = UInt64LEField.class)
        UnsignedLong deactivationSlot;

        @StructField(order = 3, type = UInt64LEField.class)
        long lastExtendedSlot;

        @StructField(order = 4, type = UInt8Field.class)
        int lastExtendedStartIndex;

        @StructField(order = 5, type = ByteField.class)
        Byte optionByte;

        @StructField(order = 6, type = PublicKeyPadding2Field.class)
        PublicKey authority;

        @StructSequenceField(order = 7, elementType = PublicKeyField.class, lengthType = NoneField.class)
        List<PublicKey> addresses;

        public State(int typeIndex, UnsignedLong deactivationSlot, long lastExtendedSlot, int lastExtendedStartIndex, PublicKey authority, List<PublicKey> addresses) {
            this.typeIndex = typeIndex;
            this.deactivationSlot = deactivationSlot;
            this.lastExtendedSlot = lastExtendedSlot;
            this.lastExtendedStartIndex = lastExtendedStartIndex;
            this.authority = authority;
            this.addresses = addresses;
        }
    }
}