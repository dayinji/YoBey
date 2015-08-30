package com.badprinter.yobey.commom;

import android.app.Application;

/**
 * Created by root on 15-8-30.
 */
public class AppContext extends Application {
    public static final int NETTYPE_WIFI = 0x01;
    public static final int NETTYPE_CMWAP = 0x02;
    public static final int NETTYPE_CMNET = 0x03;

    public static final int PAGE_SIZE = 20;//默认分页大小
    private static final int CACHE_TIME = 10*60000;//缓存失效时间
    private static AppContext instance;
    public static AppContext getInstance() {
        return instance;
    }
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        instance = this;
    }
}
