package net.deanly.solanarpcj.transaction;

import lombok.extern.slf4j.Slf4j;
import net.deanly.solanarpcj.crypto.KeyPair;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.program.spl.memo.SplMemoProgram;
import net.deanly.solanarpcj.program.spl.memo.instruction.SplMemoInstructionWrite;
import net.deanly.solanarpcj.program.system.account.SystemProgram;

import net.deanly.structlayout.StructLayout;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;
import java.util.List;

import net.deanly.solanarpcj.crypto.Base58;

@Slf4j
public class TransactionTest {

    private final static KeyPair signer = new KeyPair(Base58
            .decode("4Z7cXSyeFR8wNGMVXUE1TwtKn5D5Vu7FzEv69dokLv7KrQk7h6pu4LF8ZRR9yQBhc7uSM6RTTZtU1fmaxiNrxXrs"));

    @Test
    public void signAndSerialize() {
        PublicKey fromPublicKey = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
        PublicKey toPublickKey = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
        int lamports = 3000;

        Transaction transaction = new Transaction();
        transaction.addInstruction(SystemProgram.transfer(fromPublicKey, toPublickKey, lamports));
        transaction.setRecentBlockHash("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn");
        transaction.sign(signer);
        byte[] serializedTransaction = transaction.serialize();

        log.info("Serialized: " + Base64.getEncoder().encodeToString(serializedTransaction));

        assertEquals(
                "ASdDdWBaKXVRA+6flVFiZokic9gK0+r1JWgwGg/GJAkLSreYrGF4rbTCXNJvyut6K6hupJtm72GztLbWNmRF1Q4BAAEDBhrZ0FOHFUhTft4+JhhJo9+3/QL6vHWyI8jkatuFPQzrerzQ2HXrwm2hsYGjM5s+8qMWlbt6vbxngnO8rc3lqgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAy+KIwZmU8DLmYglP3bPzrlpDaKkGu6VIJJwTOYQmRfUBAgIAAQwCAAAAuAsAAAAAAAA=",
                Base64.getEncoder().encodeToString(serializedTransaction));
    }

    @Test
    public void transactionBuilderTest() {
        final String memo = "Test memo";
        SplMemoInstructionWrite instruction = SplMemoProgram.write(
                memo,
                List.of(signer.getPublicKey())
        );
        instruction.setProgramId(SplMemoProgram.PROGRAM_ID_OLD);
        final Transaction transaction = new TransactionBuilder()
                .addInstruction(instruction)
                .setRecentBlockHash("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn")
                .setSigners(List.of(signer))
                .build();

        StructLayout.debug(transaction);

        String expectedBase64 = "AV6w4Af9PSHhNsTSal4vlPF7Su9QXgCVyfDChHImJITLcS5BlNotKFeMoGw87VwjS3eNA2JCL+MEoReynCNbWAoBAAECBhrZ0FOHFUhTft4+JhhJo9+3/QL6vHWyI8jkatuFPQwFSlNQ+F3IgtYUpVZyeIopbd8eq6vQpgZ4iEky9O72oMviiMGZlPAy5mIJT92z865aQ2ipBrulSCScEzmEJkX1AQEBAAlUZXN0IG1lbW8=";
        byte[] expectedBytes = Base64.getDecoder().decode(expectedBase64);
//        Transaction decodedTransaction = StructLayout.decode(expectedBytes, Transaction.class);

//        StructLayout.debug(decodedTransaction);
        StructLayout.debug(expectedBytes);

        assertEquals(
                expectedBase64,
                Base64.getEncoder().encodeToString(transaction.serialize())
        );
    }

}
