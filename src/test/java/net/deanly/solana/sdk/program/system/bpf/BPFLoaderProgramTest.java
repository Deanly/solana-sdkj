package net.deanly.solana.sdk.program.system.bpf;

import net.deanly.solana.sdk.crypto.KeyPair;
import net.deanly.solana.sdk.program.core.bpf.BPFLoaderProgram;
import net.deanly.solana.sdk.program.core.system.SystemProgram;
import net.deanly.solana.sdk.types.Blockhash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.*;

import net.deanly.solana.sdk.transaction.Transaction;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.solana.sdk.rpc.client.Network;
import net.deanly.solana.sdk.rpc.client.RpcClient;
import net.deanly.solana.sdk.rpc.client.exception.RpcException;

import java.util.List;

/**
 * Test class for BPFLoader program instructions.
 */
public class BPFLoaderProgramTest {

    private RpcClient client;
    private KeyPair payer;
    private KeyPair bufferKeyPair;
    private KeyPair programKeyPair;
    private KeyPair programDataKeyPair;

    @BeforeEach
    public void setUp() {
        client = new RpcClient(Network.DEVNET);
        payer = new KeyPair();
        bufferKeyPair = new KeyPair();
        programKeyPair = new KeyPair();
        programDataKeyPair = new KeyPair();
    }

    @Test
    public void testInitializeBuffer() {
        TransactionInstruction instruction = BPFLoaderProgram.initializeBuffer(
                bufferKeyPair.getPublicKey(),
                payer.getPublicKey()
        );

        assertEquals(BPFLoaderProgram.PROGRAM_ID, instruction.getProgramId(Network.DEVNET));
        assertEquals(2, instruction.getKeys().size());
        assertEquals(1, instruction.getData().length);
        assertEquals(0, instruction.getData()[0]);
    }

    @Test
    public void testWrite() {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        TransactionInstruction instruction = BPFLoaderProgram.write(
                bufferKeyPair.getPublicKey(),
                payer.getPublicKey(),
                10,
                data
        );

        assertEquals(BPFLoaderProgram.PROGRAM_ID, instruction.getProgramId(Network.DEVNET));
        assertEquals(2, instruction.getKeys().size());
        assertEquals(10, instruction.getData().length);
        assertEquals(1, instruction.getData()[0]);
    }

    @Test
    public void testDeployWithMaxDataLen() {
        TransactionInstruction instruction = BPFLoaderProgram.deployWithMaxDataLen(
                payer.getPublicKey(),
                programDataKeyPair.getPublicKey(),
                programKeyPair.getPublicKey(),
                bufferKeyPair.getPublicKey(),
                payer.getPublicKey(),
                1000
        );

        assertEquals(BPFLoaderProgram.PROGRAM_ID, instruction.getProgramId(Network.DEVNET));
        assertEquals(8, instruction.getKeys().size());
        assertEquals(9, instruction.getData().length);
        assertEquals(2, instruction.getData()[0]);
    }

    @Test
    public void testUpgrade() {
        TransactionInstruction instruction = BPFLoaderProgram.upgrade(
                programDataKeyPair.getPublicKey(),
                programKeyPair.getPublicKey(),
                bufferKeyPair.getPublicKey(),
                payer.getPublicKey(),
                payer.getPublicKey()
        );

        assertEquals(BPFLoaderProgram.PROGRAM_ID, instruction.getProgramId(Network.DEVNET));
        assertEquals(7, instruction.getKeys().size());
        assertEquals(1, instruction.getData().length);
        assertEquals(3, instruction.getData()[0]);
    }

    @Test
    public void testSetAuthority() {
        TransactionInstruction instruction = BPFLoaderProgram.setAuthority(
                programDataKeyPair.getPublicKey(),
                payer.getPublicKey(),
                new KeyPair().getPublicKey()
        );

        assertEquals(BPFLoaderProgram.PROGRAM_ID, instruction.getProgramId(Network.DEVNET));
        assertEquals(3, instruction.getKeys().size());
        assertEquals(1, instruction.getData().length);
        assertEquals(4, instruction.getData()[0]);
    }

    // ... existing code ...

    /**
     * Integration test for initializing a buffer.
     * Note: This test is ignored by default as it requires a connection to the Solana network.
     */
    @Test
    @Disabled
    public void initializeBufferIntegrationTest() throws RpcException {
        KeyPair KeyPair = new KeyPair(); // Replace with your actual account setup
        Transaction transaction = new Transaction();

        // Initialize buffer
        transaction.addInstruction(
                SystemProgram.createAccount(
                        KeyPair.getPublicKey(),
                        bufferKeyPair.getPublicKey(),
                        3290880,
                        165L,
                        BPFLoaderProgram.PROGRAM_ID
                )
        );

        transaction.addInstruction(
                BPFLoaderProgram.initializeBuffer(
                        bufferKeyPair.getPublicKey(),
                        KeyPair.getPublicKey()
                )
        );

        String hash = client.getLegacyApi().getRecentBlockhash();
        transaction.setRecentBlockhashForCompile(Blockhash.of(hash));

        String txId = client.getLegacyApi().sendTransaction(transaction, List.of(KeyPair, bufferKeyPair), hash);
        assertNotNull(txId);
        System.out.println("Transaction ID: " + txId);
    }
}
