package com.konradrej.rcpc.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

/**
 * Represents a {@link View} modelling a touchpad.
 *
 * @author Konrad Rej
 * @author www.konradrej.com
 * @version 1.0
 */
public class ScrollBarView extends View implements
        View.OnTouchListener,
        GestureDetector.OnGestureListener {

    private OnTouchListener wrappedOnTouchListener = null;
    private OnScrollBarEventListener onScrollBarEventListener = null;
    private GestureDetectorCompat gestureDetector;
    private boolean ignoreFirstScrollEvent = true;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context view context
     */
    public ScrollBarView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor that is called when inflating a view from XML.
     *
     * @param context view context
     * @param attrs   attributes to use
     */
    public ScrollBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Perform inflation from XML and apply a class-specific base style from a theme attribute.
     *
     * @param context      view context
     * @param attrs        attributes to use
     * @param defStyleAttr base style to apply
     */
    public ScrollBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gestureDetector = new GestureDetectorCompat(getContext(), this);
        super.setOnTouchListener(this);
    }

    /**
     * Set onTouchListener.
     *
     * @param listener listener to set
     */
    @Override
    public void setOnTouchListener(OnTouchListener listener) {
        wrappedOnTouchListener = listener;
    }

    /**
     * Set OnScrollBarEventListener.
     *
     * @param listener listener to set
     */
    public void setOnScrollBarEventListener(OnScrollBarEventListener listener) {
        onScrollBarEventListener = listener;
    }

    /**
     * Calls onTouchListener and gestureDetector.
     *
     * @param v     event source view
     * @param event the motion event
     * @return true if event was consumed, false if not
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean consumed = false;

        if (wrappedOnTouchListener != null)
            consumed = wrappedOnTouchListener.onTouch(v, event);

        consumed |= gestureDetector.onTouchEvent(event);

        return consumed;
    }

    /**
     * Calls scrollBarEventListener callback on scroll.
     *
     * @param e1        first motion event
     * @param e2        second motion event
     * @param distanceX x distance between events
     * @param distanceY y distance between events
     * @return true
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (ignoreFirstScrollEvent) {
            ignoreFirstScrollEvent = false;
        } else {
            if (onScrollBarEventListener != null) {
                onScrollBarEventListener.onScroll(distanceX, distanceY);
            }
        }

        return true;
    }

    /**
     * TODO
     *
     * @param e ignored
     * @return true
     */
    @Override
    public boolean onDown(MotionEvent e) {
        ignoreFirstScrollEvent = true;
        return true;
    }

    /**
     * Not in use, implemented cause of interface requirement.
     *
     * @param e ignored
     */
    @Override
    public void onShowPress(MotionEvent e) {
    }

    /**
     * Not in use, implemented cause of interface requirement.
     *
     * @param e ignored
     * @return true
     */
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    /**
     * Not in use, implemented cause of interface requirement.
     *
     * @param e ignored
     */
    @Override
    public void onLongPress(MotionEvent e) {
    }

    /**
     * Not in use, implemented cause of interface requirement.
     *
     * @param e1        ignored
     * @param e2        ignored
     * @param velocityX ignored
     * @param velocityY ignored
     * @return true
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

    /**
     * Callback interface for scrollbar events.
     */
    public interface OnScrollBarEventListener {
        void onScroll(float distanceX, float distanceY);
    }
}
