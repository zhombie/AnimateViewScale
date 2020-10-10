/*
 * Copyright 2017 TheKhaeng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    GitHub: https://github.com/TheKhaeng/pushdown-anim-click
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kz.q19.animateviewscale

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import java.lang.ref.WeakReference

data class ScalableView(
    var weakView: WeakReference<View>? = null,

    val pushDuration: Long = DEFAULT_PUSH_DURATION,
    val releaseDuration: Long = DEFAULT_RELEASE_DURATION,

    val pushInterpolator: Interpolator = DEFAULT_INTERPOLATOR,
    val releaseInterpolator: Interpolator = DEFAULT_INTERPOLATOR,

    val pushScaleMode: ScaleMode = DEFAULT_SCALE_MODE,

    val pushScale: Float = when (pushScaleMode) {
        ScaleMode.FLOAT_RANGE -> DEFAULT_PUSH_SCALE_FLOAT_RANGE
        ScaleMode.DP -> DEFAULT_PUSH_SCALE_DP
    }
) {

    enum class ScaleMode {
        FLOAT_RANGE,
        DP
    }

    companion object {
        private val DEFAULT_SCALE_MODE: ScaleMode = ScaleMode.FLOAT_RANGE

        private const val DEFAULT_PUSH_SCALE_FLOAT_RANGE: Float = 0.9F
        private const val DEFAULT_PUSH_SCALE_DP: Float = 10F

        private const val DEFAULT_PUSH_DURATION: Long = 50L
        private const val DEFAULT_RELEASE_DURATION: Long = 125L

        private val DEFAULT_INTERPOLATOR: Interpolator = AccelerateDecelerateInterpolator()

        inline fun View.setScalableViewAnimationListener(block: Builder.() -> Unit): ScalableView =
            Builder(this).apply(block).build()

        inline fun View.setScalableViewAnimationListener(
            params: Builder.() -> Unit,
            onTouchListener: View.OnTouchListener? = null,
            onClickListener: View.OnClickListener? = null,
            onLongClickListener: View.OnLongClickListener? = null
        ): ScalableView =
            Builder(this).apply(params).build()
                .setOnTouchEvent(onTouchListener)
                .setOnClickListener(onClickListener)
                .setOnLongClickListener(onLongClickListener)
    }

    private constructor(
        view: View,
        builder: Builder
    ) : this(
        weakView = WeakReference(view),
        pushDuration = builder.pushDuration,
        releaseDuration = builder.releaseDuration,
        pushInterpolator = builder.pushInterpolator,
        releaseInterpolator = builder.releaseInterpolator,
        pushScaleMode = builder.pushScaleMode,
        pushScale = builder.pushScale
    )

    class Builder(var view: View) {
        var pushDuration: Long = DEFAULT_PUSH_DURATION
        var releaseDuration: Long = DEFAULT_RELEASE_DURATION

        var pushInterpolator: Interpolator = DEFAULT_INTERPOLATOR
        var releaseInterpolator: Interpolator = DEFAULT_INTERPOLATOR

        var pushScaleMode: ScaleMode = DEFAULT_SCALE_MODE

        var pushScale: Float = when (pushScaleMode) {
            ScaleMode.FLOAT_RANGE -> DEFAULT_PUSH_SCALE_FLOAT_RANGE
            ScaleMode.DP -> DEFAULT_PUSH_SCALE_DP
        }

        fun build(): ScalableView {
            if (pushScaleMode == ScaleMode.FLOAT_RANGE && (pushScale < 0.0F || pushScale > 1.0F)) {
                throw IllegalStateException("The value of pushScale should be between 0.0 & 1.0.")
            }
            return ScalableView(view, this)
        }
    }

    private var defaultScaleX: Float = 0F

    private var scaleAnimatorSet: AnimatorSet? = null

    init {
        weakView?.get()?.isClickable = false
        defaultScaleX = weakView?.get()?.scaleX ?: 0F
    }

    fun setScaleAnimation(): ScalableView {
        setOnTouchEvent(null)
        return this
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setOnTouchEvent(listener: View.OnTouchListener?): ScalableView {
        if (weakView?.get() != null) {
            if (listener == null) {
                weakView?.get()?.setOnTouchListener(object : View.OnTouchListener {
                    var isOutSide = false
                    var rect: Rect? = null

                    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                        if (view == null) return false

                        if (view.isClickable) {
                            when (event?.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    isOutSide = false
                                    rect = Rect(
                                        view.left,
                                        view.top,
                                        view.right,
                                        view.bottom
                                    )
                                    makeDecisionOnScaleAnimation(
                                        view,
                                        pushScaleMode,
                                        pushScale,
                                        pushDuration,
                                        pushInterpolator
                                    )
                                }
                                MotionEvent.ACTION_MOVE -> {
                                    if (!isOutSide &&
                                        rect?.contains(
                                            view.left + event.x.toInt(),
                                            view.top + event.y.toInt()
                                        ) == false
                                    ) {
                                        isOutSide = true
                                        makeDecisionOnScaleAnimation(
                                            view,
                                            pushScaleMode,
                                            defaultScaleX,
                                            releaseDuration,
                                            releaseInterpolator
                                        )
                                    }
                                }
                                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                                    makeDecisionOnScaleAnimation(
                                        view,
                                        pushScaleMode,
                                        defaultScaleX,
                                        releaseDuration,
                                        releaseInterpolator
                                    )
                                }
                            }
                        }
                        return false
                    }
                })
            } else {
                weakView?.get()?.setOnTouchListener { _, motionEvent ->
                    listener.onTouch(weakView?.get(), motionEvent)
                }
            }
        }
        return this
    }

    fun setOnClickListener(onClickListener: View.OnClickListener?): ScalableView {
        weakView?.get()?.setOnClickListener(onClickListener)
        return this
    }

    fun setOnLongClickListener(onLongClickListener: View.OnLongClickListener?): ScalableView {
        weakView?.get()?.setOnLongClickListener(onLongClickListener)
        return this
    }

    fun release() {
        scaleAnimatorSet = null

        defaultScaleX = 0F

        weakView?.get()?.setOnTouchListener(null)
        weakView?.get()?.setOnClickListener(null)
        weakView?.get()?.setOnLongClickListener(null)
        weakView?.clear()
        weakView = null
    }

    private fun makeDecisionOnScaleAnimation(
        view: View,
        scaleMode: ScaleMode,
        pushScale: Float,
        duration: Long,
        interpolator: TimeInterpolator
    ) {
        var tempScale = pushScale
        if (scaleMode == ScaleMode.DP) {
            tempScale = getScaleFromStaticSize(view, pushScale)
        }
        animateScale(view, tempScale, duration, interpolator)
    }

    private fun animateScale(
        view: View,
        scale: Float,
        duration: Long,
        interpolator: TimeInterpolator
    ) {
        view.animate().cancel()

        scaleAnimatorSet?.cancel()

        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", scale)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", scale)

        scaleX.interpolator = interpolator
        scaleX.duration = duration

        scaleY.interpolator = interpolator
        scaleY.duration = duration

        scaleAnimatorSet = AnimatorSet()
        scaleAnimatorSet
            ?.play(scaleX)
            ?.with(scaleY)

        scaleX.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
            }
        })

        scaleX.addUpdateListener {
            val viewParent = view.parent
            if (viewParent is View) {
                viewParent.invalidate()
            }
        }

        scaleAnimatorSet?.start()
    }

    private fun getScaleFromStaticSize(view: View, sizeStaticDp: Float): Float {
        val width = view.measuredWidth
        val height = view.measuredHeight
        if (sizeStaticDp <= 0) return defaultScaleX
        val sizePx: Float = dpToPx(view.context, sizeStaticDp)
        return if (width > height) {
            if (sizePx > width) return 1.0F
            val pushWidth: Float = width - sizePx * 2
            pushWidth / width
        } else {
            if (sizePx > height) return 1.0F
            val pushHeight: Float = height - sizePx * 2
            pushHeight / height
        }
    }

    private fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }

}