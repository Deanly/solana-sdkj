package net.deanly.solanarpcj.program.spl.token.basic.instruction;

import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.spl.token.basic.instruction.TokenInstruction06SetAuthority;
import net.deanly.solanarpcj.program.spl.token.basic.type.AuthorityType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TokenInstruction06SetAuthorityTest {

    @Test
    public void testSingleAuthoritySetInstruction() {
        // Prepare test data
        PublicKey account = new PublicKey("AccountPublicKey");
        PublicKey currentAuthority = new PublicKey("AuthorityPublicKey");
        PublicKey newAuthority = new PublicKey("NewAuthorityPublicKey");
        AuthorityType authorityType = AuthorityType.FREEZE_ACCOUNT;

        // Create instruction
        TokenInstruction06SetAuthority instruction = new TokenInstruction06SetAuthority();
        instruction.setAuthorityType(authorityType);
        instruction.setNewAuthority(newAuthority);
        instruction.setKeys(account, currentAuthority, null);

        // Verify authorityType
        Assertions.assertEquals(authorityType, instruction.getAuthorityType());

        // Verify newAuthority
        Assertions.assertEquals(newAuthority, instruction.getNewAuthority());

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(2, instruction.getKeys().size());
        Assertions.assertEquals(account, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(currentAuthority, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(1).isSigner());

        // Verify encoded data
        byte[] encoded = instruction.getData();
        Assertions.assertNotNull(encoded);
        Assertions.assertTrue(encoded.length > 0);

        // Decode and verify
        TokenInstruction06SetAuthority decoded = new TokenInstruction06SetAuthority();
        decoded.setData(encoded);
        Assertions.assertEquals(authorityType, decoded.getAuthorityType());
        Assertions.assertEquals(newAuthority, decoded.getNewAuthority());
    }

    @Test
    public void testMultisigAuthoritySetInstruction() {
        // Prepare test data
        PublicKey account = new PublicKey("AccountPublicKey");
        PublicKey multisigAuthority = new PublicKey("MultisigAuthorityPublicKey");
        PublicKey signer1 = new PublicKey("Signer1PublicKey");
        PublicKey signer2 = new PublicKey("Signer2PublicKey");
        PublicKey newAuthority = new PublicKey("NewAuthorityPublicKey");
        AuthorityType authorityType = AuthorityType.ACCOUNT_OWNER;

        // Create instruction
        TokenInstruction06SetAuthority instruction = new TokenInstruction06SetAuthority();
        instruction.setAuthorityType(authorityType);
        instruction.setNewAuthority(newAuthority);
        instruction.setKeys(account, multisigAuthority, Arrays.asList(signer1, signer2));

        // Verify authorityType
        Assertions.assertEquals(authorityType, instruction.getAuthorityType());

        // Verify newAuthority
        Assertions.assertEquals(newAuthority, instruction.getNewAuthority());

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(4, instruction.getKeys().size());
        Assertions.assertEquals(account, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(multisigAuthority, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertFalse(instruction.getKeys().get(1).isWritable());
        Assertions.assertEquals(signer1, instruction.getKeys().get(2).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(2).isSigner());
        Assertions.assertEquals(signer2, instruction.getKeys().get(3).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(3).isSigner());

        // Verify encoded data
        byte[] encoded = instruction.getData();
        Assertions.assertNotNull(encoded);
        Assertions.assertTrue(encoded.length > 0);

        // Decode and verify
        TokenInstruction06SetAuthority decoded = new TokenInstruction06SetAuthority();
        decoded.setData(encoded);
        Assertions.assertEquals(authorityType, decoded.getAuthorityType());
        Assertions.assertEquals(newAuthority, decoded.getNewAuthority());
    }

    @Test
    public void testWithdrawAuthority() {
        // Prepare test data
        PublicKey account = new PublicKey("AccountPublicKey");
        PublicKey currentAuthority = new PublicKey("AuthorityPublicKey");
        AuthorityType authorityType = AuthorityType.CLOSE_ACCOUNT;

        // Create instruction (without newAuthority, withdrawal case)
        TokenInstruction06SetAuthority instruction = new TokenInstruction06SetAuthority();
        instruction.setAuthorityType(authorityType);
        instruction.setNewAuthority(null); // Withdraw authority
        instruction.setKeys(account, currentAuthority, null);

        // Verify authorityType
        Assertions.assertEquals(authorityType, instruction.getAuthorityType());

        // Verify newAuthority is null
        Assertions.assertNull(instruction.getNewAuthority());

        // Verify keys
        Assertions.assertNotNull(instruction.getKeys());
        Assertions.assertEquals(2, instruction.getKeys().size());
        Assertions.assertEquals(account, instruction.getKeys().get(0).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(0).isWritable());
        Assertions.assertEquals(currentAuthority, instruction.getKeys().get(1).getPublicKey());
        Assertions.assertTrue(instruction.getKeys().get(1).isSigner());

        // Verify encoded data
        byte[] encoded = instruction.getData();
        Assertions.assertNotNull(encoded);
        Assertions.assertTrue(encoded.length > 0);

        // Decode and verify
        TokenInstruction06SetAuthority decoded = new TokenInstruction06SetAuthority();
        decoded.setData(encoded);
        Assertions.assertEquals(authorityType, decoded.getAuthorityType());
        Assertions.assertNull(decoded.getNewAuthority());
    }
}