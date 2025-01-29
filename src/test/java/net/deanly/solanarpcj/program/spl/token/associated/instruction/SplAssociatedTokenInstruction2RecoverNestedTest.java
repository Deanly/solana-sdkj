package net.deanly.solanarpcj.program.spl.token.associated.instruction;

import net.deanly.solanarpcj.transaction.instruction.AccountMeta;
import net.deanly.solanarpcj.crypto.PublicKey;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SplAssociatedTokenInstruction2RecoverNestedTest {

    @Test
    public void instructionTest() {
        PublicKey nestedAssociatedAccount = new PublicKey("NestedAccountPublicKey");
        PublicKey nestedTokenMint = new PublicKey("NestedTokenMintKey");
        PublicKey destinationAccount = new PublicKey("DestinationAccountKey");
        PublicKey ownerAssociatedAccount = new PublicKey("OwnerAccountKey");
        PublicKey ownerTokenMint = new PublicKey("OwnerMintKey");
        PublicKey walletKey = new PublicKey("WalletKey");
        PublicKey tokenProgramKey = new PublicKey("TokenProgramKey");

        // Create instruction
        SplAssociatedTokenInstruction2RecoverNested instruction =
                SplAssociatedTokenInstruction2RecoverNested.create(
                        nestedAssociatedAccount,
                        nestedTokenMint,
                        destinationAccount,
                        ownerAssociatedAccount,
                        ownerTokenMint,
                        walletKey
                );

        // Validate set keys
        List<AccountMeta> keys = instruction.getKeys();
        assert keys.size() == 7;
        assert keys.get(0).getPublicKey().toString().equals("NestedAccountPublicKey");
        assert keys.get(6).getPublicKey().toString().equals("TokenProgramKey");

        // Validate data encoding
        byte[] encodedData = instruction.getData();
        assert encodedData.length == 1; // Discriminator only
        assert encodedData[0] == 2; // Discriminator value matches
    }
}
