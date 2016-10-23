package gunlee.proto.tne;

import gunlee.proto.tne.asm.ScouterClassWriter;
import gunlee.proto.tne.asm.probe.ServletServiceProbe;
import scouter.org.objectweb.asm.ClassReader;
import scouter.org.objectweb.asm.ClassVisitor;
import scouter.org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 4.
 */
public class AgentTransformer implements ClassFileTransformer {
    public static ThreadLocal<ClassLoader> hookingCtx = new ThreadLocal<ClassLoader>();

    public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            if(!"javax/servlet/http/HttpServlet".equals(className)) {
                return null;
            }

            hookingCtx.set(loader);

            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ScouterClassWriter(ClassWriter.COMPUTE_MAXS);
            ClassVisitor cv = new ServletServiceProbe().transform(cw, className);

            cr.accept(cv, ClassReader.EXPAND_FRAMES);

            System.out.println(className + "\t[" + loader + "]");

            return cw.toByteArray();

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            hookingCtx.set(null);
        }
        return null;
    }
}
