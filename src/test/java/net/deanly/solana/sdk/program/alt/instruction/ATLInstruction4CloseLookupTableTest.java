package net.deanly.solana.sdk.program.alt.instruction;

import net.deanly.solana.sdk.transaction.instruction.AccountMeta;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ATLInstruction4CloseLookupTableTest {

    @Test
    void testCloseLookupTableInstruction() {
        // 테스트 데이터
        PublicKey lookupTable = new PublicKey("LookupTable111111111111111111111111111111");
        PublicKey authority = new PublicKey("Authority111111111111111111111111111111");
        PublicKey recipient = new PublicKey("Recipient111111111111111111111111111111");

        // CloseLookupTable 인스턴스 생성
        ATLInstruction4CloseLookupTable instruction = new ATLInstruction4CloseLookupTable();

        // Keys 설정
        instruction.setKeys(lookupTable, authority, recipient);
        assertNotNull(instruction.getKeys());

        // Keys 검증
        List<AccountMeta> keys = instruction.getKeys();
        assertEquals(3, keys.size());

        // Lookup Table 검증
        AccountMeta lookupTableMeta = keys.get(0);
        assertEquals(lookupTable.toBase58(), lookupTableMeta.getPublicKey().toBase58());
        assertTrue(lookupTableMeta.isWritable());
        assertFalse(lookupTableMeta.isSigner());

        // Authority 검증
        AccountMeta authorityMeta = keys.get(1);
        assertEquals(authority.toBase58(), authorityMeta.getPublicKey().toBase58());
        assertFalse(authorityMeta.isWritable());
        assertTrue(authorityMeta.isSigner());

        // Recipient 검증
        AccountMeta recipientMeta = keys.get(2);
        assertEquals(recipient.toBase58(), recipientMeta.getPublicKey().toBase58());
        assertTrue(recipientMeta.isWritable());
        assertFalse(recipientMeta.isSigner());

        // Data 직렬화 및 역직렬화 검증
        byte[] encodedData = instruction.getData();
        assertNotNull(encodedData);

        ATLInstruction4CloseLookupTable decodedInstruction = StructLayout.decode(encodedData, ATLInstruction4CloseLookupTable.class);
        assertNotNull(decodedInstruction);

        // 디코딩된 Keys도 확인
        assertEquals(instruction.getInstruction(), decodedInstruction.getInstruction());
    }
}