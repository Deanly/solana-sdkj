package net.deanly.solanarpcj.crypto;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class PublicKeyTestBase {

    @BeforeAll
    public static void initializeTransformer() {
        PublicKeyTransformer.transform();
    }
}
