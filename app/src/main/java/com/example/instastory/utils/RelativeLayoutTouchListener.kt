package com.example.instastory.utils;

import android.app.Activity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.instastory.screen.BottomSheet


class RelativeLayoutTouchListener(
    mainActivity: FragmentActivity
) :
    OnTouchListener {
    private val activity: Activity
    private var downX = 0f
    private var downY = 0f
    private var upX = 0f
    private var upY = 0f

    private lateinit var onSwipeUpListener: OnSwipeUp

    fun onRightToLeftSwipe() {
        Log.i(logTag, "RightToLeftSwipe!")
        Toast.makeText(activity, "RightToLeftSwipe", Toast.LENGTH_SHORT).show()
        // activity.doSomething();
    }

    fun onLeftToRightSwipe() {
        Log.i(logTag, "LeftToRightSwipe!")
        Toast.makeText(activity, "LeftToRightSwipe", Toast.LENGTH_SHORT).show()
        // activity.doSomething();
    }

    fun onTopToBottomSwipe() {
        Log.i(logTag, "onTopToBottomSwipe!")
        Toast.makeText(activity, "onTopToBottomSwipe", Toast.LENGTH_SHORT).show()
        // activity.doSomething();
    }

    fun onBottomToTopSwipe() {
        Log.i(logTag, "onBottomToTopSwipe!")
        //Toast.makeText(activity, "onBottomToTopSwipe", Toast.LENGTH_SHORT).show()
        // activity.doSomething();
        onSwipeUpListener.onSwipeUp()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                upX = event.x
                upY = event.y
                val deltaX = downX - upX
                val deltaY = downY - upY

                // swipe horizontal?
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    // left or right
                    if (deltaX < 0) {
                        onLeftToRightSwipe()
                        return true
                    }
                    if (deltaX > 0) {
                        onRightToLeftSwipe()
                        return true
                    }
                } else {
                    Log.i(
                        logTag,
                        "Swipe was only " + Math.abs(deltaX) + " long horizontally, need at least " + MIN_DISTANCE
                    )
                    // return false; // We don't consume the event
                }

                // swipe vertical?
                if (Math.abs(deltaY) > MIN_DISTANCE) {
                    // top or down
                    if (deltaY < 0) {
                        onTopToBottomSwipe()
                        return true
                    }
                    if (deltaY > 0) {
                        onBottomToTopSwipe()
                        return true
                    }
                } else {
                    Log.i(
                        logTag,
                        "Swipe was only " + Math.abs(deltaX) + " long vertically, need at least " + MIN_DISTANCE
                    )
                    // return false; // We don't consume the event
                }
                return false // no swipe horizontally and no swipe vertically
            } // case MotionEvent.ACTION_UP:
        }
        return false
    }

    companion object {
        const val logTag = "ActivitySwipeDetector"
        const val MIN_DISTANCE =
            100 // TODO change this runtime based on screen resolution. for 1920x1080 is to small the 100 distance
    }

    // private MainActivity mMainActivity;
    init {
        activity = mainActivity
    }

    interface OnSwipeUp{
        fun onSwipeUp()
    }

    fun setOnSwipeUpListener(listener:OnSwipeUp) {
        onSwipeUpListener = listener
    }

}