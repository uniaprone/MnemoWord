package com.kite.mnemoai.utils;

import android.content.Context;

public class UIUtil {
    public static int dp2px(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}

