package net.deanly.solanarpcj.transaction.message.compiler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.transaction.message.meta.MessageAddressTableLookup;
import net.deanly.solanarpcj.transaction.message.meta.MessageHeader;
import net.deanly.solanarpcj.program.alt.state.AddressLookupTableAccount;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KeyHolderTest {

    private KeyHolder keyHolder;

    @BeforeEach
    void setUp() {
        keyHolder = new KeyHolder();
    }

    @Test
    void addKey_addsNewKeySuccessfully() {
        PublicKey key = new PublicKey("8NxBtVx5B9qi1xzHeNzzkYQyTXmfp8D6gkc52hUVrtF");

        keyHolder.addKey(key, true, true);

        assertEquals(1, keyHolder.getAllSortedKeys().size());
        assertTrue(keyHolder.getAllSortedKeys().contains(key));
    }

    @Test
    void updateKey_updatesExistingKeySuccessfully() {
        PublicKey key = new PublicKey("FNeZ5mJX5eX9oFyrFFFbWE3a5PKRALikEKwYaZZShAfm");

        keyHolder.addKey(key, false, false);
        keyHolder.updateKey(key, true, true);

        List<PublicKey> allKeys = keyHolder.getAllSortedKeys();
        assertEquals(1, allKeys.size());
    }

    @Test
    void getKeyIndex_returnsCorrectIndex() {
        PublicKey key1 = new PublicKey("G51txqx8mdPTP1PfaXkBW7WKZAtuRSNpCVybULtcHYeP");
        PublicKey key2 = new PublicKey("CkLydwZtjoHXNBagC9kST83SG7diJUfDWQoUcDAT3VXp");

        keyHolder.addKey(key1, true, true);
        keyHolder.addKey(key2, false, true);

        int index1 = keyHolder.getKeyIndex(key1);
        int index2 = keyHolder.getKeyIndex(key2);

        assertEquals(0, index1);
        assertEquals(1, index2);
    }


    @Test
    void generateHeader_generatesCorrectHeader() {
        PublicKey key1 = new PublicKey("EvLdNbr4AWYFNrPhmwbEkvbwPBnCkBjZyhaSUFbvLZUw");
        PublicKey key2 = new PublicKey("3DKaWPSRPqkEZQG2nUv5SYrf85w2LGSejLn6JmLmESD1");

        keyHolder.addKey(key1, true, true); // Writable signer
        keyHolder.addKey(key2, true, false); // Readonly signer

        MessageHeader header = keyHolder.generateHeader();
        assertEquals(2, header.getNumRequiredSignatures());
        assertEquals(1, header.getNumReadonlySignedAccounts());
    }

    @Test
    void getKeyIndex_throwsForUnknownKey() {
        PublicKey unknownKey = new PublicKey("7zZ5P1ocsrLBJ9DfppMm9xyLtKie4bLcfHkPAbZAehEZ");
        assertEquals(-1, keyHolder.getKeyIndex(unknownKey));
    }


    @Test
    void processAddressTables_handlesAddressLookupTableCorrectly() {
        // Test Address Lookup Table
        PublicKey atlKey = new PublicKey("BpZDoac5TvQLf4XJP6DXQMABMNJU6noQBUhBxrKXSqpM");
        PublicKey keyInLookupTable = new PublicKey("DqMHcPoHTZCEXKtzkhpzhjggaxiTokenJrHVXmQ1Pqvi");
        PublicKey keyNotInLookupTable = new PublicKey("7AGaZcLUPFngZ8KeB2kG1jBxhEssNUi6dftNopYqdaDY");

        // AddressLookupTableAccount with a State containing one address
        AddressLookupTableAccount.State atlState = new AddressLookupTableAccount.State(
                1, // typeIndex
                new BigInteger("FFFFFFFFFFFFFFFF", 16), // deactivationSlot
                0, // lastExtendedSlot
                0, // lastExtendedStartIndex
                null, // authority
                List.of(keyInLookupTable) // addresses
        );

        AddressLookupTableAccount atlAccount = new AddressLookupTableAccount(atlKey, atlState);

        // Add keys to KeyHolder
        keyHolder.addKey(keyInLookupTable, false, true); // Key present in ATL
        keyHolder.addKey(keyNotInLookupTable, false, false); // Key not present in ATL

        // Process the Address Lookup Table
        List<MessageAddressTableLookup> lookups = keyHolder.processAddressTables(List.of(atlAccount));

        // Assertions
        assertEquals(1, lookups.size());
        MessageAddressTableLookup lookup = lookups.get(0);

        assertEquals(atlKey, lookup.getAccountKey());
        assertTrue(lookup.getWritableIndexes().contains(0)); // Key at index 0
        assertTrue(lookup.getReadonlyIndexes().isEmpty()); // No readonly keys
    }

    @Test
    void processAddressTables_withEmptyAddressLookupTable() {
        // Empty AddressLookupTableAccount (no addresses)
        PublicKey atlKey = new PublicKey("8ZbeNDwpRSDrLZzjFxh46T6vAZisDjTheoTUGmJnFTVL");
        AddressLookupTableAccount.State emptyState = new AddressLookupTableAccount.State(
                1,new BigInteger("FFFFFFFFFFFFFFFF", 16), 0, 0, null, List.of() // Empty address list
        );
        AddressLookupTableAccount emptyAtl = new AddressLookupTableAccount(atlKey, emptyState);

        // Process empty Address Lookup Table
        List<MessageAddressTableLookup> lookups = keyHolder.processAddressTables(List.of(emptyAtl));

        // Assertions
        assertNotNull(lookups);
        assertTrue(lookups.isEmpty());
    }
}