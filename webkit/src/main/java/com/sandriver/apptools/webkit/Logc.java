package com.sandriver.apptools.webkit;


import android.content.Context;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

public class Logc {

    /**
     * 控制台日志前缀
     */
    public static String customTagPrefix = "sandriver";

    /**
     * 控制日志是否在logcat中打印
     */
    public static boolean DEBUG = true;

    /**
     * 默认日志保存级别
     */
    public static LogLevel LEVEL_SAVE = LogLevel.LEVEL_VERBOSE;

    public enum LogLevel {
        LEVEL_OFF(0),//日志输出关闭
        LEVEL_VERBOSE(1),//日志输出级别V
        LEVEL_INFO(2),// 日志输出级别I
        LEVEL_DEBUG(3),//日志输出级别D
        LEVEL_WARN(4),//日志输出级别W
        LEVEL_ERROR(5),//日志输出级别E
        LEVEL_WTF(6);//日志输出级别WTF

        private int level;

        LogLevel(int code) {
            this.level = code;
        }

        public int getLevel() {
            return this.level;
        }

    }
    /**
     * 设置日志保存级别
     *
     * @param logLevel
     */
    public static void setLogLevel(LogLevel logLevel) {
        LEVEL_SAVE = logLevel;
    }

    public static void setToDebug(boolean b) {
        DEBUG = b;
    }

    private static int mTag = 99;

    /**
     * 设置日志保存tag
     *
     * @param tag
     */
    public static void setTag(int tag) {
        mTag = tag;
    }

    public static int getTag() {
        return mTag;
    }

    private static StringBuilder traceLogStringBuilder = new StringBuilder();
    private static String CENTER_BREAK = ":";
    private static String LINE_BREAK = "\n";

    public static boolean allowD = DEBUG && true;
    public static boolean allowE = DEBUG && true;
    public static boolean allowI = DEBUG && true;
    public static boolean allowV = DEBUG && true;
    public static boolean allowW = DEBUG && true;

    //自定义日志
    public static CustomLogger customLogger;

    public interface CustomLogger {

        void init(Context context);

        void onTerminate();

        void appenderFlush(boolean isSync);

        void logV(String tag, String filename, String funcname, int line, int pid, long tid,
                  long maintid, String log);

        void logI(String tag, String filename, String funcname, int line, int pid, long tid,
                  long maintid, String log);

        void logD(String tag, String filename, String funcname, int line, int pid, long tid,
                  long maintid, String log);

        void logW(String tag, String filename, String funcname, int line, int pid, long tid,
                  long maintid, String log);

        void logE(String tag, String filename, String funcname, int line, int pid, long tid,
                  long maintid, String log);

        void logF(String tag, String filename, String funcname, int line, int pid, long tid,
                  long maintid, String log);

    }

    public static void init(Context context, CustomLogger logimpl) {
        customLogger = logimpl;
        customLogger.init(context);
    }

    public static void onTerminate() {
        if (customLogger != null) {
            customLogger.onTerminate();
        }
    }

    public static void flush() {
        if (customLogger != null) {
            customLogger.appenderFlush(false);
        }
    }

    private static String generateTag(StackTraceElement caller) {
        String tag = "(%s:%d).%s";
        String callerClazzName = (caller == null ? "" : caller.getFileName());
        tag = String.format(Locale.getDefault(), tag, callerClazzName, (caller == null ? 0 :
                caller.getLineNumber()), (caller == null ? "" : caller.getMethodName()));
        tag = TextUtils.isEmpty(customTagPrefix) ? tag : customTagPrefix + ":" + tag;
        return tag;
    }

    private static StackTraceElement getCallerStackTraceElement() {
        Thread cThread = Thread.currentThread();
        StackTraceElement[] stackTrace = cThread.getStackTrace();
        if (stackTrace.length >= 6) {
            return stackTrace[5];
        }
        return null;
    }

    private static StackTraceElement getCallerStackTraceElement2() {
        Thread cThread = Thread.currentThread();
        StackTraceElement[] stackTrace = cThread.getStackTrace();
        if (stackTrace.length >= 5) {
            return stackTrace[4];
        }
        return null;
    }

    public static void v(String content) {
        logV("", content, null);
    }

    public static void v(Throwable tr) {
        logV("", "", tr);
    }

    public static void v(String content, Throwable tr) {
        logV("", content, tr);
    }

    public static void v(String uTag, String content) {
        logV(uTag, content, null);
    }

    public static void v(String uTag, String content, Throwable tr) {
        logV(uTag, content, tr);
    }

    /**
     * verbose日志，可传入自定义tag,
     *
     * @param uTag    自定义的tag
     * @param content 内容
     * @param tr      异常
     */
    private static void logV(String uTag, String content, Throwable tr) {
        if (!DEBUG || LEVEL_SAVE.level > LogLevel.LEVEL_VERBOSE.level) {
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        uTag = generateTag(caller) + (TextUtils.isEmpty(uTag) ? "" : ("-" + uTag));
        content = getThrowable(tr, content);


        if (customLogger != null) {
            String filename = "";
            String funName = "";
            int line = 0;
            if (caller != null) {
                filename = caller.getFileName();
                funName = caller.getMethodName();
                line = caller.getLineNumber();
            }
            customLogger.logV(uTag, filename, funName, line, Process.myPid(),
                    Thread.currentThread().getId(), Looper.getMainLooper().getThread().getId(),
                    content);
        }

        Log.v(uTag, content);
    }

    public static void i(String content) {
        logI("", content, null);
    }

    public static void i(String content, Throwable tr) {
        logI("", content, tr);
    }

    public static void i(String uTag, String content) {
        logI(uTag, content, null);
    }


    public static void i(String uTag, String content, Throwable tr) {
        logI(uTag, content, tr);
    }

    /**
     * info日志，可传入自定义tag,
     *
     * @param uTag    自定义的tag
     * @param content 内容
     * @param tr      异常
     */
    private static void logI(String uTag, String content, Throwable tr) {
        if (!DEBUG || LEVEL_SAVE.level > LogLevel.LEVEL_INFO.level) {
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        uTag = generateTag(caller) + (TextUtils.isEmpty(uTag) ? "" : ("-" + uTag));
        content = getThrowable(tr, content);


        if (customLogger != null) {
            String filename = "";
            String funName = "";
            int line = 0;
            if (caller != null) {
                filename = caller.getFileName();
                funName = caller.getMethodName();
                line = caller.getLineNumber();
            }
            customLogger.logI(uTag, filename, funName, line, Process.myPid(),
                    Thread.currentThread().getId(), Looper.getMainLooper().getThread().getId(),
                    content);
        }

        Log.i(uTag, content);
    }


    public static void d(String content) {
        logD("", content, null);
    }

    public static void d(String content, Throwable tr) {
        logD("", content, tr);
    }

    public static void d(String uTag, String content) {
        logD(uTag, content, null);
    }

    public static void d(String uTag, String content, Throwable tr) {
        logD(uTag, content, tr);
    }

    /**
     * debug日志，可传入自定义tag,
     *
     * @param uTag    自定义的tag
     * @param content 内容
     * @param tr      异常
     */
    private static void logD(String uTag, String content, Throwable tr) {
        if (!DEBUG || LEVEL_SAVE.level > LogLevel.LEVEL_DEBUG.level) {
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        uTag = generateTag(caller) + (TextUtils.isEmpty(uTag) ? "" : ("-" + uTag));

        content = getThrowable(tr, content);


        if (customLogger != null) {
            String filename = "";
            String funName = "";
            int line = 0;
            if (caller != null) {
                filename = caller.getFileName();
                funName = caller.getMethodName();
                line = caller.getLineNumber();
            }
            customLogger.logD(uTag, filename, funName, line, Process.myPid(),
                    Thread.currentThread().getId(), Looper.getMainLooper().getThread().getId(),
                    content);
        }

        Log.d(uTag, content);
    }


    public static void w(String content) {
        logW("", content, null);
    }

    public static void w(String content, Throwable tr) {
        logW("", content, tr);
    }

    public static void w(String uTag, String content) {
        logW(uTag, content, null);
    }

    public static void w(String uTag, String content, Throwable tr) {
        logW(uTag, content, tr);
    }

    /**
     * warn日志，可传入自定义tag,
     *
     * @param uTag    自定义的tag
     * @param content 内容
     * @param tr      异常
     */
    private static void logW(String uTag, String content, Throwable tr) {
        if (!DEBUG || LEVEL_SAVE.level > LogLevel.LEVEL_WARN.level) {
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        uTag = generateTag(caller) + (TextUtils.isEmpty(uTag) ? "" : ("-" + uTag));
        content = getThrowable(tr, content);


        if (customLogger != null) {
            String filename = "";
            String funName = "";
            int line = 0;
            if (caller != null) {
                filename = caller.getFileName();
                funName = caller.getMethodName();
                line = caller.getLineNumber();
            }
            customLogger.logW(uTag, filename, funName, line, Process.myPid(),
                    Thread.currentThread().getId(), Looper.getMainLooper().getThread().getId(),
                    content);
        }

        Log.w(uTag, content);
    }


    public static void e(String content) {
        logE("", content, null);
    }

    /**
     * 保留 兼容之前有些库调用此方法
     */
    @Deprecated
    public static void e(String content, boolean isSave) {
        logE("", content, null);
    }

    public static void e(String content, Throwable tr) {
        logE("", content, tr);
    }

    public static void e(String uTag, String content) {
        logE(uTag, content, null);
    }

    public static void e(String uTag, String content, Throwable tr) {
        logE(uTag, content, tr);
    }

    /**
     * 错误异常日志，可传入自定义tag
     *
     * @param uTag    自定义的tag
     * @param content 内容
     * @param tr      异常
     */
    private static void logE(String uTag, String content, Throwable tr) {
        if (!DEBUG || LEVEL_SAVE.level > LogLevel.LEVEL_ERROR.level) {
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        uTag = generateTag(caller) + (TextUtils.isEmpty(uTag) ? "" : ("-" + uTag));
        content = getThrowable(tr, content);


        if (customLogger != null) {
            String filename = "";
            String funName = "";
            int line = 0;
            if (caller != null) {
                filename = caller.getFileName();
                funName = caller.getMethodName();
                line = caller.getLineNumber();
            }
            customLogger.logE(uTag, filename, funName, line, Process.myPid(),
                    Thread.currentThread().getId(), Looper.getMainLooper().getThread().getId(),
                    content);
        }
        Log.e(uTag, content);
    }

    /**
     * 保留 兼容之前有些库调用此方法
     */
    @Deprecated
    public static void e(String uTag, String content, boolean isSave) {
        logE(uTag, content, null);
    }


    public static void wtf(String content) {
        logWtf("", content, null);
    }

    public static void wtf(String content, Throwable tr) {
        logWtf("", content, tr);
    }

    public static void wtf(String uTag, String content) {
        logWtf(uTag, content, null);
    }

    public static void wtf(String uTag, String content, Throwable tr) {
        logWtf(uTag, content, tr);
    }

    /**
     * what a failure日志，可传入自定义tag,
     *
     * @param uTag    自定义的tag
     * @param content 内容
     * @param tr      异常
     */
    private static void logWtf(String uTag, String content, Throwable tr) {
        if (!DEBUG || LEVEL_SAVE.level > LogLevel.LEVEL_WTF.level) {
            return;
        }
        StackTraceElement caller = getCallerStackTraceElement();
        uTag = generateTag(caller) + (TextUtils.isEmpty(uTag) ? "" : ("-" + uTag));
        content = getThrowable(tr, content);


        if (customLogger != null) {
            String filename = "";
            String funName = "";
            int line = 0;
            if (caller != null) {
                filename = caller.getFileName();
                funName = caller.getMethodName();
                line = caller.getLineNumber();
            }
            customLogger.logF(uTag, filename, funName, line, Process.myPid(),
                    Thread.currentThread().getId(), Looper.getMainLooper().getThread().getId(),
                    content);
        }

        Log.wtf(uTag, content);
    }

    public static String getThrowable(Throwable throwable, String msg) {
        StringBuffer sb = new StringBuffer();
        if (!TextUtils.isEmpty(msg)) {
            sb.append(msg);
        }
        if (throwable != null) {
            sb.append(LINE_BREAK);
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            throwable.printStackTrace(printWriter);
            sb.append(stringWriter.toString());
        }
        return sb.toString();
    }
}

