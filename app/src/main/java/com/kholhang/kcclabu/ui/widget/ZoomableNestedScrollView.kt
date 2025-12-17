package com.kholhang.kcclabu.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.OverScroller
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.core.widget.NestedScrollView
import androidx.preference.PreferenceManager
import com.kholhang.kcclabu.extensions.prefs.HymnalPrefs
import com.kholhang.kcclabu.extensions.prefs.HymnalPrefsImpl

class ZoomableNestedScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    private var fontScaleFactor = 1f
    private val minFontScale = 0.5f  // 50% of base size
    private val maxFontScale = 1.6f  // 160% of base size
    
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var isScaling = false
    
    private val scroller = OverScroller(context, AccelerateDecelerateInterpolator())
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    
    private var baseFontSize: Float = 22f
    private var currentFontSize: Float = 22f
    
    private val prefs: HymnalPrefs by lazy {
        HymnalPrefsImpl(PreferenceManager.getDefaultSharedPreferences(context))
    }

    init {
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        // Initialize with current font size from preferences
        baseFontSize = prefs.getFontSize()
        currentFontSize = baseFontSize
        fontScaleFactor = 1f
    }
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Sync with current font size from preferences when view is attached
        post {
            baseFontSize = prefs.getFontSize()
            currentFontSize = baseFontSize
            fontScaleFactor = 1f
            // Apply current font size to all text views
            applyFontSize()
        }
    }
    
    private fun isPinchZoomEnabled(): Boolean {
        return prefs.isPinchZoomEnabled()
    }
    
    /**
     * Update base font size when preferences change
     */
    fun updateBaseFontSize() {
        val newBaseSize = prefs.getFontSize()
        if (newBaseSize != baseFontSize) {
            // Adjust current size proportionally
            val ratio = currentFontSize / baseFontSize
            baseFontSize = newBaseSize
            currentFontSize = baseFontSize * ratio
            fontScaleFactor = currentFontSize / baseFontSize
            applyFontSize()
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // Only process zoom gestures if enabled
        if (isPinchZoomEnabled()) {
            scaleGestureDetector?.onTouchEvent(ev)
        }
        
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = ev.x
                lastTouchY = ev.y
                if (!scroller.isFinished) {
                    scroller.abortAnimation()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isScaling || !isPinchZoomEnabled()) {
                    val deltaX = ev.x - lastTouchX
                    val deltaY = ev.y - lastTouchY
                    lastTouchX = ev.x
                    lastTouchY = ev.y
                    
                    // Allow normal scrolling when not scaling or zoom disabled
                    return super.onTouchEvent(ev)
                }
            }
        }
        
        return super.onTouchEvent(ev) || (isScaling && isPinchZoomEnabled())
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Only intercept if zoom is enabled
        if (isPinchZoomEnabled()) {
            scaleGestureDetector?.onTouchEvent(ev)
        }
        return if (isScaling && isPinchZoomEnabled()) {
            true
        } else {
            super.onInterceptTouchEvent(ev)
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            isScaling = true
            // Update base font size from preferences at start of gesture
            baseFontSize = prefs.getFontSize()
            currentFontSize = baseFontSize * fontScaleFactor
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = detector.scaleFactor
            val newFontScale = fontScaleFactor * scale
            
            // Clamp to min/max bounds
            val clampedScale = newFontScale.coerceIn(minFontScale, maxFontScale)
            fontScaleFactor = clampedScale
            currentFontSize = baseFontSize * fontScaleFactor
            
            applyFontSize()
            
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            isScaling = false
            
            // Snap to min/max if out of bounds
            if (fontScaleFactor < minFontScale) {
                animateFontSize(baseFontSize * minFontScale)
            } else if (fontScaleFactor > maxFontScale) {
                animateFontSize(baseFontSize * maxFontScale)
            } else {
                // Save the new font size to preferences
                saveFontSizeToPreferences()
            }
        }
    }

    /**
     * Find all TextView and AppCompatCheckedTextView in the view hierarchy and update their font sizes
     */
    private fun applyFontSize() {
        val rootView = getChildAt(0) ?: return
        updateTextViewFontSize(rootView, currentFontSize)
    }
    
    /**
     * Recursively find and update all TextViews in the view hierarchy
     */
    private fun updateTextViewFontSize(view: View, fontSize: Float) {
        when (view) {
            is TextView -> {
                view.textSize = fontSize
            }
            is AppCompatCheckedTextView -> {
                view.textSize = fontSize
            }
            is ViewGroup -> {
                for (i in 0 until view.childCount) {
                    updateTextViewFontSize(view.getChildAt(i), fontSize)
                }
            }
        }
    }
    
    /**
     * Animate font size change smoothly
     */
    private fun animateFontSize(targetSize: Float) {
        val startSize = currentFontSize
        val animator = android.animation.ValueAnimator.ofFloat(startSize, targetSize).apply {
            duration = 200
            addUpdateListener { animation ->
                currentFontSize = animation.animatedValue as Float
                fontScaleFactor = currentFontSize / baseFontSize
                applyFontSize()
            }
            addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {}
                override fun onAnimationCancel(animation: android.animation.Animator) {}
                override fun onAnimationRepeat(animation: android.animation.Animator) {}
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    saveFontSizeToPreferences()
                }
            })
        }
        animator.start()
    }
    
    /**
     * Save the current font size to preferences
     */
    private fun saveFontSizeToPreferences() {
        // Clamp to valid range (12f to 36f as per settings)
        val clampedSize = currentFontSize.coerceIn(12f, 36f)
        prefs.setFontSize(clampedSize)
        // Update base size to match saved value
        baseFontSize = clampedSize
        currentFontSize = clampedSize
        fontScaleFactor = 1f
    }

    /**
     * Reset font size to base size from preferences
     */
    fun resetFontSize() {
        baseFontSize = prefs.getFontSize()
        animateFontSize(baseFontSize)
    }

    /**
     * Get current font size multiplier
     */
    fun getCurrentFontScale(): Float = fontScaleFactor
}

