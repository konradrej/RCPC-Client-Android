package com.konradrej.rcpc.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

public class ScrollBarView extends View implements
        View.OnTouchListener,
        GestureDetector.OnGestureListener {

    private OnTouchListener wrappedOnTouchListener = null;
    private OnScrollBarEventListener onScrollBarEventListener = null;
    private GestureDetectorCompat gestureDetector;

    public ScrollBarView(Context context) {
        super(context);
        init();
    }

    public ScrollBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScrollBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ScrollBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void init() {
        gestureDetector = new GestureDetectorCompat(getContext(), this);
        super.setOnTouchListener(this);
    }

    @Override
    public void setOnTouchListener(OnTouchListener listener) {
        wrappedOnTouchListener = listener;
    }

    public void setOnScrollBarEventListener(OnScrollBarEventListener listener) {
        onScrollBarEventListener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean consumed = false;

        if (wrappedOnTouchListener != null)
            consumed |= wrappedOnTouchListener.onTouch(v, event);

        consumed |= gestureDetector.onTouchEvent(event);

        return consumed;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (onScrollBarEventListener != null) {
            onScrollBarEventListener.onScroll(distanceX, distanceY);
        }

        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

    public interface OnScrollBarEventListener {
        void onScroll(float distanceX, float distanceY);
    }
}
