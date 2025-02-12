package net.deanly.solana.sdk.program.alt.state;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.spl.alt.state.AddressLookupTableAccount;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.type.guava.UnsignedLong;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AddressLookupTableAccountTest {

    @Test
    void testIsActive() {
        // Arrange
        PublicKey tableKey = new PublicKey("11111111111111111111111111111111");
        AddressLookupTableAccount mockAccount = createMockAddressLookupTableAccount(tableKey);

        // Act
        boolean isActive = mockAccount.isActive();

        // Assert
        assertTrue(isActive, "The lookup table should be active");
    }

    public static AddressLookupTableAccount createMockAddressLookupTableAccount(PublicKey tableKey) {
        // Mock parameters for AddressLookupTableState
        int typeIndex = 1;
        UnsignedLong deactivationSlot = UnsignedLong.valueOf("FFFFFFFFFFFFFFFF", 16); // Active state
        long lastExtendedSlot = 1234567L; // Example last slot
        int lastExtendedStartIndex = 0; // Default index
        PublicKey authority = new PublicKey("SecondPubey22222222222222222222222222222222"); // Authority

        // List of addresses
        List<PublicKey> addresses = List.of(
                new PublicKey("11111111111111111111111111111111"),
                new PublicKey("ThirdPubkey33333333333333333333333333333333")
        );

        // Create mock AddressLookupTableState
        AddressLookupTableAccount.State mockState =
                new AddressLookupTableAccount.State(
                        typeIndex, deactivationSlot, lastExtendedSlot, lastExtendedStartIndex, authority, addresses
                );

        // Return the AddressLookupTableAccount with the generated state
        return new AddressLookupTableAccount(tableKey, mockState);
    }

    @Test
    void testAddressLookupTableDeserialization() {
        // Arrange
        PublicKey tableKey = new PublicKey("SecondPubey22222222222222222222222222222222");
        byte[] mockData = generateMockData();

        // Act
        AddressLookupTableAccount deserializedAccount = AddressLookupTableAccount.deserialize(tableKey, mockData);

        StructLayout.debug(mockData);
        StructLayout.debug(deserializedAccount.getState());

        // Assert
        assertNotNull(deserializedAccount);
        assertEquals(tableKey, deserializedAccount.getKey());
        assertTrue(deserializedAccount.isActive());
        assertNotNull(deserializedAccount.getState());
        assertEquals(2, deserializedAccount.getState().getAddresses().size());
        assertEquals("11111111111111111111111111111111", deserializedAccount.getState().getAddresses().get(0).toBase58());
        assertEquals("ThirdPubkey33333333333333333333333333333333", deserializedAccount.getState().getAddresses().get(1).toBase58());
    }

    private byte[] generateMockData() {
        // 메타데이터(56바이트) + Address (32 * 2 = 64바이트) = 총 데이터 길이
        byte[] data = new byte[56 + (32 * 2)];
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        // 1. typeIndex (4바이트)
        buffer.putInt(0, 1); // typeIndex = 1

        // 2. deactivationSlot (8바이트)
        buffer.putLong(4, 0xFFFFFFFFFFFFFFFFL); // Maximum long value (deactivationSlot)

        // 3. lastExtendedSlot (8바이트)
        buffer.putLong(12, 1234567L); // Mock value for lastExtendedSlot

        // 4. lastExtendedStartIndex (1바이트)
        buffer.put(20, (byte) 0); // Start index set to 0

        // 5. Option byte (1바이트, 사용하지 않음)
        buffer.put(21, (byte) 0); // Option byte (not used)

        // 7. Authority PublicKey (32바이트)
        PublicKey authority = new PublicKey("SecondPubey22222222222222222222222222222222");
        buffer.position(22); // Authority 시작 위치
        buffer.put(authority.toByteArray());

        // 8. Address 리스트 (32바이트 단위, 2개 주소)
        PublicKey address1 = new PublicKey("11111111111111111111111111111111");
        PublicKey address2 = new PublicKey("ThirdPubkey33333333333333333333333333333333");

        buffer.position(56); // Address 시작 위치
        buffer.put(address1.toByteArray());
        buffer.put(address2.toByteArray());

        return data;
    }

}
