package com.tinashe.hymnal.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.OverScroller
import androidx.core.widget.NestedScrollView

class ZoomableNestedScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    private var scaleFactor = 1f
    private val minScale = 0.5f
    private val maxScale = 3.0f
    
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var isScaling = false
    
    private val scroller = OverScroller(context, AccelerateDecelerateInterpolator())
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    init {
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        scaleGestureDetector?.onTouchEvent(ev)
        
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = ev.x
                lastTouchY = ev.y
                if (!scroller.isFinished) {
                    scroller.abortAnimation()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isScaling) {
                    val deltaX = ev.x - lastTouchX
                    val deltaY = ev.y - lastTouchY
                    lastTouchX = ev.x
                    lastTouchY = ev.y
                    
                    // Allow normal scrolling when not scaling
                    return super.onTouchEvent(ev)
                }
            }
        }
        
        return super.onTouchEvent(ev) || isScaling
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        scaleGestureDetector?.onTouchEvent(ev)
        return if (isScaling) {
            true
        } else {
            super.onInterceptTouchEvent(ev)
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            isScaling = true
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = detector.scaleFactor
            val newScale = scaleFactor * scale
            
            if (newScale in minScale..maxScale) {
                scaleFactor = newScale
                applyScale()
            }
            
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            isScaling = false
            // Snap to min/max if out of bounds
            if (scaleFactor < minScale) {
                animateScale(minScale)
            } else if (scaleFactor > maxScale) {
                animateScale(maxScale)
            }
        }
    }

    private fun applyScale() {
        val child = getChildAt(0) ?: return
        child.scaleX = scaleFactor
        child.scaleY = scaleFactor
        child.pivotX = width / 2f
        child.pivotY = height / 2f
    }

    private fun animateScale(targetScale: Float) {
        val startScale = scaleFactor
        val animator = android.animation.ValueAnimator.ofFloat(startScale, targetScale).apply {
            duration = 200
            addUpdateListener { animation ->
                scaleFactor = animation.animatedValue as Float
                applyScale()
            }
        }
        animator.start()
    }

    fun resetZoom() {
        animateScale(1f)
    }

    fun getCurrentScale(): Float = scaleFactor
}

