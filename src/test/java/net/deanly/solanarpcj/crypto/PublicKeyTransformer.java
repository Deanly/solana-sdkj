package net.deanly.solanarpcj.crypto;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CannotCompileException;
import javassist.NotFoundException;

public class PublicKeyTransformer {

    public static void transform() {
        try {
            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClass = classPool.get("net.deanly.solanarpcj.crypto.PublicKey");
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