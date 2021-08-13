package com.konradrej.rcpc.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

public class TouchPadView extends View implements
        View.OnTouchListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private OnTouchListener wrappedOnTouchListener = null;
    private OnTouchPadEventListener onTouchPadEventListener = null;
    private GestureDetectorCompat gestureDetector;
    private int touchAmount = 0;

    public TouchPadView(Context context) {
        super(context);
        init();
    }

    public TouchPadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchPadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TouchPadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

    public void setOnTouchPadEventListener(OnTouchPadEventListener listener) {
        onTouchPadEventListener = listener;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (onTouchPadEventListener != null)
            onTouchPadEventListener.onLeftClick();

        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (onTouchPadEventListener != null)
            onTouchPadEventListener.onRightClick();

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (onTouchPadEventListener != null) {
            if (touchAmount == 1) {
                onTouchPadEventListener.onMove(distanceX, distanceY);
            } else if (touchAmount == 2) {
                onTouchPadEventListener.onScroll(distanceX, distanceY);
            }
        }

        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        touchAmount = event.getPointerCount();
        boolean consumed = false;

        if (wrappedOnTouchListener != null)
            consumed |= wrappedOnTouchListener.onTouch(v, event);

        consumed |= gestureDetector.onTouchEvent(event);

        return consumed;
    }

    public interface OnTouchPadEventListener {
        void onMove(float distanceX, float distanceY);

        void onScroll(float distanceX, float distanceY);

        void onLeftClick();

        void onRightClick();
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
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
}
