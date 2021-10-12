package com.ming.androidhotfixunitytest;

import android.content.Intent;

public class Util {
    private static Util mInstance;
    public static synchronized Util Instance() {
        if (mInstance == null) {
            mInstance = new Util();
        }
        return mInstance;
    }
}
