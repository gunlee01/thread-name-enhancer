package gunlee.proto.tne.deco;

import gunlee.proto.tne.deco.context.ServletTraceContext;
import gunlee.proto.tne.deco.context.ServletTraceContextManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 4.
 */
public class CommonDeco {
    private static Class[] arg_c = {};
    private static Object[] arg_o = {};

    private static java.lang.reflect.Method getRequestURI;

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

    public static String getRequestURI(Object req) {
        try {
            if (getRequestURI == null) {
                getRequestURI = req.getClass().getMethod("getRequestURI", arg_c);
                getRequestURI.setAccessible(true);
            }
            return (String) getRequestURI.invoke(req, arg_o);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
