package org.p2p.solanaj.rpc.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.p2p.solanaj.core.PublicKey;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@RequiredArgsConstructor
public class AddressLookupTableAccount {
    private static final int LOOKUP_TABLE_META_SIZE = 56;
    private static final long U64_MAX = 0xFFFFFFFFFFFFFFFFL;

    private final PublicKey key;
    private final AddressLookupTableState state;

    public boolean isActive() {
        return this.state.deactivationSlot == U64_MAX;
    }

    public static AddressLookupTableAccount deserialize(PublicKey key, byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        int serializedAddressLength = buffer.capacity() - LOOKUP_TABLE_META_SIZE;
        if (serializedAddressLength < 0 || serializedAddressLength % 32 != 0) {
            throw new IllegalArgumentException("lookup table is invalid");
        }

        // 1. typeIndex 읽기 (4 bytes, unsigned)
        int typeIndex = Byte.toUnsignedInt(buffer.get(4));

        // 2. deactivationSlot 읽기 (8 bytes, unsigned)
        long deactivationSlot = Byte.toUnsignedLong(buffer.get(8));

        // 3. lastExtendedSlot 읽기 (8 bytes, unsigned)
        long lastExtendedSlot = Byte.toUnsignedLong(buffer.get(8));

        // 4. lastExtendedStartIndex 읽기 (1 byte, unsigned)
        int lastExtendedStartIndex = Byte.toUnsignedInt(buffer.get());

        // 5. authority 존재 여부 읽기 (1 byte, unsigned)
        boolean authorityExists = Byte.toUnsignedInt(buffer.get()) != 0;

        // 6. authority 처리 (있을 경우 32 bytes 읽기)
        PublicKey authority = null;
        if (authorityExists) {
            byte[] authorityBytes = new byte[32];
            buffer.get(authorityBytes);
            authority = new PublicKey(authorityBytes);
        }

        // 7. remaining data를 addresses로 처리 (32 bytes per address)
        List<PublicKey> addresses = new ArrayList<>();
        while (buffer.remaining() >= 32) {
            byte[] addressBytes = new byte[32];
            buffer.get(addressBytes);
            addresses.add(new PublicKey(addressBytes));
        }

        var state = new AddressLookupTableState(typeIndex, deactivationSlot, lastExtendedSlot, lastExtendedStartIndex, authority, addresses);

        // 8. 객체 생성 후 반환
        return new AddressLookupTableAccount(key, state);
    }


    @Getter
    @ToString
    @RequiredArgsConstructor
    public static class AddressLookupTableState {
        private final int typeIndex;
        private final long deactivationSlot;
        private final long lastExtendedSlot;
        private final int lastExtendedStartIndex;
        private final PublicKey authority;
        private final List<PublicKey> addresses;
    }
}