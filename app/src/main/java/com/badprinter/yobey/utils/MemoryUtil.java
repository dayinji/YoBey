package com.badprinter.yobey.utils;

import android.app.ActivityManager;
import android.content.Context;

import com.badprinter.yobey.commom.AppContext;

/**
 * Created by root on 15-9-7.
 */
public class MemoryUtil {
    static public String getMemoryInfo()
    {
        Context context = AppContext.getInstance();
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.getMemoryInfo(outInfo);
        long totalMem = outInfo.totalMem/1024;
        long avaliMem = outInfo.availMem/1024;
        long userMem = totalMem - avaliMem;
        StringBuilder builder = new StringBuilder();
        builder.append("\tAll: " + Long.toString(totalMem) +
        "\tAvali: " + Long.toString(avaliMem) + "\tUsed: " + Long.toString(userMem));
        return builder.toString();
    }
}
