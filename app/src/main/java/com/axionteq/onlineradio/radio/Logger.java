/*
 * This is the source code of DMAudioStreaming for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry(dibakar.ece@gmail.com), 2017.
 */
package com.axionteq.onlineradio.radio;


import android.util.Log;

import com.nostra13.universalimageloader.BuildConfig;


public class Logger {
    private static final String LOG_PREFIX = "com.axionteq";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 23;

    private static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }
        return LOG_PREFIX + str;
    }

    static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }


    public static void v(String tag, Object... messages) {
        if (BuildConfig.DEBUG) {
            log(tag, Log.VERBOSE, null, messages);
        }
    }

    static void d(String tag, Object... messages) {
        if (BuildConfig.DEBUG) {
            log(tag, Log.DEBUG, null, messages);
        }
    }

    public static void i(String tag, Object... messages) {
        log(tag, Log.INFO, null, messages);
    }

    public static void w(String tag, Object... messages) {
        log(tag, Log.WARN, null, messages);
    }

    public static void w(String tag, Throwable t, Object... messages) {
        log(tag, Log.WARN, t, messages);
    }

    static void e(String tag, Object... messages) {
        log(tag, Log.ERROR, null, messages);
    }

    static void e(String tag, Throwable t, Object... messages) {
        log(tag, Log.ERROR, t, messages);
    }

    private static void log(String tag, int level, Throwable t, Object... messages) {
        if (Log.isLoggable(tag, level)) {
            String message;
            if (t == null && messages != null && messages.length == 1) {
                message = messages[0].toString();
            } else {
                StringBuilder sb = new StringBuilder();
                if (messages != null) for (Object m : messages) {
                    sb.append(m);
                }
                if (t != null) {
                    sb.append("\n").append( Log.getStackTraceString(t));
                }
                message = sb.toString();
            }
            Log.println(level, tag, message);
        }
    }
}
