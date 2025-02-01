package net.deanly.solana.sdk.transaction.message.compiler;

import net.deanly.solana.sdk.transaction.instruction.TransactionInstructionImpl;
import org.junit.jupiter.api.Test;
import net.deanly.solana.sdk.program.alt.state.AddressLookupTableAccount;
import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.solana.sdk.transaction.message.meta.LoadedAddresses;
import net.deanly.solana.sdk.transaction.message.meta.MessageAddressTableLookup;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CompiledKeysTest {

    @Test
    void testCompileMethodWithValidData() {
        PublicKey payer = new PublicKey("11111111111111111111111111111111");
        PublicKey programId = new PublicKey("SecondPubey22222222222222222222222222222222");
        PublicKey accountKey = new PublicKey("ThirdPubkey33333333333333333333333333333333");

        AccountMeta accountMeta = new AccountMeta(accountKey, true, false);
        TransactionInstruction instruction = new TransactionInstructionImpl(
                programId, Collections.singletonList(accountMeta), new byte[]{}
        );

        List<TransactionInstruction> instructions = Collections.singletonList(instruction);

        // Compile keys
        CompiledKeys compiledKeys = CompiledKeys.compile(instructions, payer);

        // Assertions
        assertEquals(payer, compiledKeys.getPayer());
        assertNotNull(compiledKeys.getStaticKeyMetaMap());
        assertTrue(compiledKeys.getStaticKeyMetaMap().containsKey(payer));
        assertTrue(compiledKeys.getStaticKeyMetaMap().containsKey(programId));
        assertTrue(compiledKeys.getStaticKeyMetaMap().containsKey(accountKey));

        CompiledKeys.CompiledKeyMeta payerMeta = compiledKeys.getStaticKeyMetaMap().get(payer);
        assertTrue(payerMeta.isSigner());
        assertTrue(payerMeta.isWritable());

        CompiledKeys.CompiledKeyMeta programMeta = compiledKeys.getStaticKeyMetaMap().get(programId);
        assertFalse(programMeta.isSigner());
        assertFalse(programMeta.isWritable());
        assertTrue(programMeta.isInvoked());
    }

    @Test
    void testGetMessageComponents() {
        PublicKey payer = new PublicKey("11111111111111111111111111111111");
        PublicKey account1 = new PublicKey("SecondPubey22222222222222222222222222222222");
        PublicKey account2 = new PublicKey("ThirdPubkey33333333333333333333333333333333");

        // Create key meta map
        CompiledKeys.CompiledKeyMeta payerMeta = new CompiledKeys.CompiledKeyMeta(true, true, false);
        CompiledKeys.CompiledKeyMeta account1Meta = new CompiledKeys.CompiledKeyMeta(false, true, false);
        CompiledKeys.CompiledKeyMeta account2Meta = new CompiledKeys.CompiledKeyMeta(true, false, false);

        // Construct CompiledKeys instance
        CompiledKeys compiledKeys = new CompiledKeys(
                payer,
                Map.of(
                        payer, payerMeta,
                        account1, account1Meta,
                        account2, account2Meta
                )
        );

        // Get message components
        CompiledKeys.MessageComponents components = compiledKeys.getMessageComponents();

        // Assertions
        assertNotNull(components);
        assertEquals(2, components.getHeader().getNumRequiredSignatures());
        assertEquals(1, components.getHeader().getNumReadonlySignedAccounts());
        assertEquals(0, components.getHeader().getNumReadonlyUnsignedAccounts());

        assertEquals(3, components.getStaticAccountKeys().size());
        assertEquals(payer, components.getStaticAccountKeys().get(0)); // Payer should always be first
    }

    @Test
    void testCompileThrowsIfPayerIsNull() {
        PublicKey programId = new PublicKey("SecondPubey22222222222222222222222222222222");
        AccountMeta accountMeta = new AccountMeta(programId, true, false);
        TransactionInstruction instruction = new TransactionInstructionImpl(
                programId, Collections.singletonList(accountMeta), new byte[]{}
        );

        List<TransactionInstruction> instructions = Collections.singletonList(instruction);

        // Test for NullPointerException
        Exception exception = assertThrows(NullPointerException.class, () -> {
            CompiledKeys.compile(instructions, null);
        });

        assertEquals("Payer is required", exception.getMessage());
    }

    @Test
    void testGetMessageComponentsThrowsIfNoWritableSigner() {
        PublicKey payer = new PublicKey("11111111111111111111111111111111");

        // Construct with an invalid key (no writable signer)
        CompiledKeys.CompiledKeyMeta readOnlyKeyMeta = new CompiledKeys.CompiledKeyMeta(false, false, false);
        CompiledKeys compiledKeys = new CompiledKeys(
                payer,
                Map.of(payer, readOnlyKeyMeta)
        );

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            compiledKeys.getMessageComponents();
        });

        assertEquals("Expected at least one writable signer key", exception.getMessage());
    }

    @Test
    void testExtractTableLookupRemovesKeysFromStaticKeyMetaMap() {
        // ALT에 지정될 계정 키들
        PublicKey altKey = new PublicKey("11111111111111111111111111111111");
        PublicKey writableKey = new PublicKey("SecondPubey22222222222222222222222222222222");
        PublicKey readonlyKey = new PublicKey("ThirdPubkey33333333333333333333333333333333");

        Map<PublicKey, CompiledKeys.CompiledKeyMeta> staticKeyMetaMap = new HashMap<>();
        staticKeyMetaMap.put(writableKey, new CompiledKeys.CompiledKeyMeta(false, true, false));
        staticKeyMetaMap.put(readonlyKey, new CompiledKeys.CompiledKeyMeta(false, false, false));

        CompiledKeys compiledKeys = new CompiledKeys(altKey, staticKeyMetaMap);

        AddressLookupTableAccount.State atlState = new AddressLookupTableAccount.State(
                1, // typeIndex
                new BigInteger("FFFFFFFFFFFFFFFF", 16), // deactivationSlot
                0, // lastExtendedSlot
                0, // lastExtendedStartIndex
                null, // 권한(authority): 현재는 없으므로 null
                List.of(writableKey, readonlyKey) // ALT에 포함될 주소들
        );

        AddressLookupTableAccount lookupTableAccount = new AddressLookupTableAccount(altKey, atlState);

        // extractTableLookup 호출
        var result = compiledKeys.extractTableLookup(lookupTableAccount);

        // 1. 결과 Optional이 비어있지 않은지 확인
        assertTrue(result.isPresent(), "TableLookupResult should be present");
        CompiledKeys.TableLookupResult tableLookupResult = result.get();

        // 2. LoadedAddresses 로드된 키 확인
        LoadedAddresses keysFromLookups = tableLookupResult.getKeysFromLookups();
        assertTrue(keysFromLookups.getWritable().contains(writableKey), "Writable key should be loaded from ALT");
        assertTrue(keysFromLookups.getReadonly().contains(readonlyKey), "Readonly key should be loaded from ALT");

        // 3. staticKeyMetaMap에서 ALT 키가 제거되었는지 확인
        assertFalse(compiledKeys.getStaticKeyMetaMap().containsKey(writableKey), "Writable key should be removed from staticKeyMetaMap");
        assertFalse(compiledKeys.getStaticKeyMetaMap().containsKey(readonlyKey), "Readonly key should be removed from staticKeyMetaMap");

        // 4. ALT의 tableLookup이 예상대로 설정되었는지 확인
        MessageAddressTableLookup tableLookup = tableLookupResult.getTableLookup();
        assertEquals(altKey, tableLookup.getAccountKey(), "ALT account key should match");
        assertTrue(tableLookup.getWritableIndexes().contains(0), "Writable key index should match ALT entry");
        assertTrue(tableLookup.getReadonlyIndexes().contains(1), "Readonly key index should match ALT entry");
    }
}