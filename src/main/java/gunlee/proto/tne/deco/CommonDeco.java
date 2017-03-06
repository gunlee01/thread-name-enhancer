package gunlee.proto.tne.deco;

import gunlee.proto.tne.deco.context.ServletTraceContext;
import gunlee.proto.tne.deco.context.ServletTraceContextManager;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 4.
 */
public class CommonDeco {
    private static Class[] arg_c = {};
    private static Object[] arg_o = {};

    private static Map<Class<?>, Method> getRequestURIMethodSet = new HashMap<Class<?>, Method>();

    public static void beforeHttpService(Object req, Object res) {
        ServletTraceContext ctx = ServletTraceContextManager.getContext();
        if (ctx != null) {
            return;
        }
        ctx = ServletTraceContextManager.start();

        long startTimestamp = System.currentTimeMillis();
        ctx.setStartTime(startTimestamp);
        ctx.setServiceName(getRequestURI(req));

        String orgThreadName = Thread.currentThread().getName();
        ctx.setOrgThreadName(orgThreadName);

        Date startDate = new Date(ctx.getStartTime());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String startDateString = formatter.format(startDate);

        Thread.currentThread().setName(orgThreadName +
                                       " [uri]" + ctx.getServiceName() +
                                       " [start at] " + startDateString + " [" + ctx.getStartTime() + "]");

        //String pid = ManagementFactory.getRuntimeMXBean().getName();
    }

    public static void afterHttpService(Throwable thr) {
        ServletTraceContext ctx = ServletTraceContextManager.end();
        if(ctx == null) {
            return;
        }

        Thread.currentThread().setName(ctx.getOrgThreadName());

        if(thr != null) {
        } else {
        }
    }

    public static Object lock = new Object();
    public static String getRequestURI(Object req) {
        try {
            Method m = getRequestURIMethodSet.get(req.getClass());
            if(m == null) {
                synchronized(lock) {
                    m = getRequestURIMethodSet.get(req.getClass());
                    if (m == null) {
                        m = req.getClass().getMethod("getRequestURI", arg_c);
                        m.setAccessible(true);
                        getRequestURIMethodSet.put(req.getClass(), m);
                    }
                }
            }
            return (String) m.invoke(req, arg_o);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
