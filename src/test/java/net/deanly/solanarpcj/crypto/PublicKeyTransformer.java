package net.deanly.solanarpcj.crypto;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.atomic.AtomicBoolean;

public final class PublicKeyTransformer {

    private static final AtomicBoolean isTransformed = new AtomicBoolean(false);

    public static synchronized void transform() {
        System.out.println("Transforming PublicKey...");
        if (!isTransformed.compareAndSet(false, true)) {
            System.out.println("PublicKey already transformed. Skipping...");
            return;
        }
        System.out.println("Transforming PublicKey...2");

        try {
            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClass = classPool.get("net.deanly.solanarpcj.crypto.PublicKey");
            if (ctClass.isFrozen()) {
                System.out.println("PublicKey class is frozen. Skipping transform.");
                return;
            }
            CtConstructor constructor = ctClass.getDeclaredConstructor(new CtClass[]{classPool.get("java.lang.String")});

            constructor.insertBefore(
                    "{" +
                            "   $1 = net.deanly.solanarpcj.crypto.PublicKeyGenerator.createDummyPublicKey($1);" +
                            "}"
            );

            ctClass.toClass();

        } catch (NotFoundException | CannotCompileException e) {
            throw new RuntimeException("Failed to transform PublicKey class", e);
        }
    }

}