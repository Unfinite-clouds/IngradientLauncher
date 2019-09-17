package com.example.launchertest;

import android.app.WallpaperManager;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.HorizontalScrollView;
import androidx.annotation.RequiresApi;

public class CustomHScrollView extends HorizontalScrollView {
    private int maxScrollX;
    private int maxOverscroll;
    public CustomHScrollView(Context context) {
        super(context);
    }

    public CustomHScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomHScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomHScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        maxScrollX = getChildAt(0).getWidth() - getWidth();
        maxOverscroll = 12;
        Log.d("D", "CustomHScrollLayout");
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Log.d("D", ""+maxScrollX+" "+getScrollX() + " wall: " +(getScrollX()+maxOverscroll)/(maxScrollX+maxOverscroll*2f));

        WallpaperManager.getInstance(getContext()).setWallpaperOffsets(this.getWindowToken(), (getScrollX()+maxOverscroll)/(maxScrollX+maxOverscroll*2f), 0f);
    }

}
