package com.konradrej.rcpc.client.View;

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
 * @version 1.1
 * @since 1.0
 */
public class TouchPadView extends View implements
        View.OnTouchListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private OnTouchListener wrappedOnTouchListener = null;
    private OnTouchPadEventListener onTouchPadEventListener = null;
    private GestureDetectorCompat gestureDetector;
    private boolean ignoreFirstScrollEvent = true;
    private boolean isDoubleClickHold = false;
    private float lastDragX;
    private float lastDragY;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context view context
     * @since 1.0
     */
    public TouchPadView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor that is called when inflating a view from XML.
     *
     * @param context view context
     * @param attrs   attributes to use
     * @since 1.0
     */
    public TouchPadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Perform inflation from XML and apply a class-specific base style from a theme attribute.
     *
     * @param context      view context
     * @param attrs        attributes to use
     * @param defStyleAttr base style to apply
     * @since 1.0
     */
    public TouchPadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
     * @since 1.0
     */
    @Override
    public void setOnTouchListener(OnTouchListener listener) {
        wrappedOnTouchListener = listener;
    }

    /**
     * Set onTouchPadEventListener.
     *
     * @param listener listener to set
     * @since 1.0
     */
    public void setOnTouchPadEventListener(OnTouchPadEventListener listener) {
        onTouchPadEventListener = listener;
    }

    /**
     * Calls touchPadEventListener callback on singleTapConfirmed.
     *
     * @param e ignored
     * @return true
     * @since 1.0
     */
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (onTouchPadEventListener != null)
            onTouchPadEventListener.onLeftClick();

        return true;
    }

    /**
     * Not in use, implemented cause of interface requirement.
     *
     * @param e ignored
     * @return true
     * @since 1.0
     */
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return true;
    }

    /**
     * Calls onTouchListener and gestureDetector. Also handles drag and click
     * move event, see {@link #onDoubleTapEvent(MotionEvent)} for why.
     *
     * @param v     event source view
     * @param event the motion event
     * @return true if event was consumed, false if not
     * @since 1.0
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean consumed = false;

        if (wrappedOnTouchListener != null)
            consumed = wrappedOnTouchListener.onTouch(v, event);

        consumed |= gestureDetector.onTouchEvent(event);

        if (isDoubleClickHold && event.getAction() == MotionEvent.ACTION_MOVE) {
            sendClickDragMove(event);
        }

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
     * @since 1.0
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (ignoreFirstScrollEvent) {
            ignoreFirstScrollEvent = false;
        } else {
            int touchAmount = e2.getPointerCount();

            if (onTouchPadEventListener != null) {
                // If single finger move, if two scroll, else ignore
                if (touchAmount == 1) {
                    onTouchPadEventListener.onMove(distanceX, distanceY);
                } else if (touchAmount == 2) {
                    onTouchPadEventListener.onScroll(distanceX, distanceY);
                }
            }
        }

        return true;
    }

    /**
     * Handles click and drag start and stop. Click and drag move is handled
     * in onTouch to avoid batching and therefore laggy behaviour.
     *
     * @param e the motion event
     * @return true
     * @since 1.0
     */
    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        if (onTouchPadEventListener != null) {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                isDoubleClickHold = true;
                onTouchPadEventListener.onClickDragStart();

                lastDragX = e.getX();
                lastDragY = e.getY();
            } else if (e.getAction() == MotionEvent.ACTION_UP) {
                isDoubleClickHold = false;

                sendClickDragMove(e);

                onTouchPadEventListener.onClickDragEnd();
            }
        }

        return true;
    }

    /**
     * Sets ignore first click to true.
     *
     * @param e ignored
     * @return true
     * @since 1.0
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
     * @since 1.0
     */
    @Override
    public void onShowPress(MotionEvent e) {
    }

    /**
     * Not in use, implemented cause of interface requirement.
     *
     * @param e ignored
     * @return true
     * @since 1.0
     */
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    /**
     * Not in use, implemented cause of interface requirement.
     *
     * @param e ignored
     * @since 1.0
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
     * @since 1.0
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

    private void sendClickDragMove(MotionEvent e) {
        float distanceX = lastDragX - e.getX();
        float distanceY = lastDragY - e.getY();

        lastDragX = e.getX();
        lastDragY = e.getY();

        onTouchPadEventListener.onClickDragMove(distanceX, distanceY);
    }

    /**
     * Callback interface for touchpad events.
     *
     * @since 1.0
     */
    public interface OnTouchPadEventListener {
        void onMove(float distanceX, float distanceY);

        void onScroll(float distanceX, float distanceY);

        void onLeftClick();

        void onRightClick();

        void onClickDragStart();

        void onClickDragMove(float distanceX, float distanceY);

        void onClickDragEnd();
    }
}
