package com.xhh.pdfui;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * UI工具类
 * 作者：齐行超
 * 日期：2019.08.07
 */
public class UIUtils {
    //屏幕密度（做成全局变量，目的：只获取一次）
    private static float density;

    /**
     * dp转px
     * @param context 上下文
     * @param dip dp
     * @return px
     */
    public static int dip2px(Context context, float dip) {
        return (int) (dip * getDensity(context) + 0.5f);
    }

    /**
     * 获得屏幕密度（只通过WindowManager获取一次）
     * @param context 上下文
     * @return 屏幕密度
     */
    private static float getDensity(Context context) {
        try {
            if (density == 0) {
                WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics dm = new DisplayMetrics();
                windowManager.getDefaultDisplay().getMetrics(dm);
                density = dm.density;
            }
            return density;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 初始化窗体样式(沉浸式)
     * @param window Activity的window对象
     */
    public static void initWindowStyle(Window window, ActionBar actionBar){
        if(window == null) {
            return;
        }
        //安卓系统5.0之后才支持沉浸式效果，所以需判断是否大于等于21
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = window.getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            window.setNavigationBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        //隐藏ActionBar
        if(actionBar != null){
            actionBar.hide();
        }
    }
}
