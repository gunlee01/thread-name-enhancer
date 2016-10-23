package gunlee.proto.tne;

import java.lang.instrument.Instrumentation;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 4.
 */
public class AgentMain {
    private static Instrumentation instrumentation;
    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public static void premain(String args, Instrumentation inst) throws Exception {
        System.out.println("[TNE] start premain");
        try {
            instrumentation = inst;
            instrumentation.addTransformer(new AgentTransformer());
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }
}
