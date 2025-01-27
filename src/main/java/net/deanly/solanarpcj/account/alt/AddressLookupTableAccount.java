package net.deanly.solanarpcj.account.alt;

import lombok.*;
import net.deanly.solanarpcj.account.PublicKey;
import net.deanly.solanarpcj.layout.Struct;
import net.deanly.solanarpcj.layout.field.AuthorityField;
import net.deanly.solanarpcj.layout.field.PublicKeyField;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.annotation.StructSequenceObjectField;
import net.deanly.structlayout.type.basic.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@RequiredArgsConstructor
public class AddressLookupTableAccount {
    private static final int TYPE_INDEX = 1;
    private static final int PUBLIC_KEY_SIZE = 32;
    private static final int LOOKUP_TABLE_META_SIZE = 56;
    private static final long U64_MAX = 0xFFFFFFFFFFFFFFFFL;

    private final PublicKey key;
    private final State state;

    public boolean isActive() {
        return this.state.deactivationSlot == U64_MAX;
    }

    public static AddressLookupTableAccount deserialize(PublicKey key, byte[] data) {
//        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
//
//        // 1. typeIndex 읽기 (4 bytes)
//        int typeIndex = buffer.getInt();
//        if (typeIndex != TYPE_INDEX) {
//            throw new IllegalArgumentException("invalid account data; account type mismatch " + typeIndex + " != 1");
//        }
//
//        // 2. deactivationSlot 읽기 (8 bytes)
//        long deactivationSlot = buffer.getLong();
//
//        // 3. lastExtendedSlot 읽기 (8 bytes)
//        long lastExtendedSlot = buffer.getLong();
//
//        // 4. lastExtendedStartIndex 읽기 (1 byte)
//        int lastExtendedStartIndex = Byte.toUnsignedInt(buffer.get());
//
//        // 5. Read option byte 버리기 (1 byte, not used directly here)
//        buffer.get();
//
//        // 6. authority 존재 여부 읽기 (1 byte)
//        boolean authorityExists = buffer.get() != 0;
//
//        // 7. authority 처리 (있을 경우 32 bytes 읽기)
//        PublicKey authority = null;
//        if (authorityExists) {
//            buffer.position(buffer.position() - 1);
//            byte[] authorityBytes = new byte[PUBLIC_KEY_SIZE];
//            buffer.get(authorityBytes);
//            authority = new PublicKey(authorityBytes);
//        }
//
//        // 8. remaining data를 addresses로 처리 (32 bytes per address)
//        buffer.position(LOOKUP_TABLE_META_SIZE);
//        int serializedAddressLength = buffer.capacity() - LOOKUP_TABLE_META_SIZE;
//        if (serializedAddressLength < 0 || serializedAddressLength % 32 != 0) {
//            throw new IllegalArgumentException("lookup table is invalid");
//        }
//        List<PublicKey> addresses = new ArrayList<>();
//        while (buffer.remaining() >= PUBLIC_KEY_SIZE) {
//            byte[] addressBytes = new byte[PUBLIC_KEY_SIZE];
//            buffer.get(addressBytes);
//            addresses.add(new PublicKey(addressBytes));
//        }
//
//        // 9. 객체 생성 후 반환
//        var state = new State(typeIndex, deactivationSlot, lastExtendedSlot, lastExtendedStartIndex, authority, addresses);

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
        long deactivationSlot;

        @StructField(order = 3, type = UInt64LEField.class)
        long lastExtendedSlot;

        @StructField(order = 4, type = UInt8Field.class)
        int lastExtendedStartIndex;

        @StructField(order = 5, type = ByteField.class)
        Byte optionByte;

        @StructField(order = 6, type = AuthorityField.class)
        PublicKey authority;

        @StructSequenceField(order = 7, elementType = PublicKeyField.class, lengthType = NoneField.class)
        List<PublicKey> addresses;

        public State(int typeIndex, long deactivationSlot, long lastExtendedSlot, int lastExtendedStartIndex, PublicKey authority, List<PublicKey> addresses) {
            this.typeIndex = typeIndex;
            this.deactivationSlot = deactivationSlot;
            this.lastExtendedSlot = lastExtendedSlot;
            this.lastExtendedStartIndex = lastExtendedStartIndex;
            this.authority = authority;
            this.addresses = addresses;
        }
    }
}