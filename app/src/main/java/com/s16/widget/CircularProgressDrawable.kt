/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.s16.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.Paint.Cap
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.IntDef
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlin.math.floor

/**
 * Drawable that renders the animated indeterminate progress indicator in the Material design style
 * without depending on API level 11.
 *
 *
 * While this may be used to draw an indeterminate spinner using [.start] and [ ][.stop] methods, this may also be used to draw a progress arc using [ ][.setStartEndTrim] method. CircularProgressDrawable also supports adding an arrow
 * at the end of the arc by [.setArrowEnabled] and [.setArrowDimensions] methods.
 *
 *
 * To use one of the pre-defined sizes instead of using your own, [.setStyle] should
 * be called with one of the [.DEFAULT] or [.LARGE] styles as its parameter. Doing it
 * so will update the arrow dimensions, ring size and stroke width to fit the one specified.
 *
 *
 * If no center radius is set via [.setCenterRadius] or [.setStyle]
 * methods, CircularProgressDrawable will fill the bounds set via [.setBounds].
 */
class CircularProgressDrawable(context: Context) : Drawable(),
    Animatable {
    @IntDef(LARGE, DEFAULT)
    annotation class ProgressDrawableSize

    /** The indicator ring, used to manage animation state.  */
    private val mRing: Ring

    /** Canvas rotation in degrees.  */
    private var rotation = 0f
    private val mResources: Resources = context.resources
    private var mAnimator: Animator? = null
    var mRotationCount = 0f
    var mFinishing = false

    /** Sets all parameters at once in dp.  */
    private fun setSizeParameters(
        centerRadius: Float, strokeWidth: Float, arrowWidth: Float,
        arrowHeight: Float
    ) {
        val ring = mRing
        val metrics = mResources.displayMetrics
        val screenDensity = metrics.density
        ring.strokeWidth = strokeWidth * screenDensity
        ring.centerRadius = centerRadius * screenDensity
        ring.setColorIndex(0)
        ring.setArrowDimensions(arrowWidth * screenDensity, arrowHeight * screenDensity)
    }

    /**
     * Sets the overall size for the progress spinner. This updates the radius
     * and stroke width of the ring, and arrow dimensions.
     *
     * @param size one of [.LARGE] or [.DEFAULT]
     */
    fun setStyle(@ProgressDrawableSize size: Int) {
        if (size == LARGE) {
            setSizeParameters(
                CENTER_RADIUS_LARGE,
                STROKE_WIDTH_LARGE,
                ARROW_WIDTH_LARGE.toFloat(),
                ARROW_HEIGHT_LARGE.toFloat()
            )
        } else {
            setSizeParameters(
                CENTER_RADIUS,
                STROKE_WIDTH,
                ARROW_WIDTH.toFloat(),
                ARROW_HEIGHT.toFloat()
            )
        }
        invalidateSelf()
    }

    /**
     * Returns the stroke width for the progress spinner in pixels.
     *
     * @return stroke width in pixels
     */
    /**
     * Sets the stroke width for the progress spinner in pixels.
     *
     * @param strokeWidth stroke width in pixels
     */
    var strokeWidth: Float
        get() = mRing.strokeWidth
        set(strokeWidth) {
            mRing.strokeWidth = strokeWidth
            invalidateSelf()
        }

    /**
     * Returns the center radius for the progress spinner in pixels.
     *
     * @return center radius in pixels
     */
    /**
     * Sets the center radius for the progress spinner in pixels. If set to 0, this drawable will
     * fill the bounds when drawn.
     *
     * @param centerRadius center radius in pixels
     */
    var centerRadius: Float
        get() = mRing.centerRadius
        set(centerRadius) {
            mRing.centerRadius = centerRadius
            invalidateSelf()
        }

    /**
     * Returns the stroke cap of the progress spinner.
     *
     * @return stroke cap
     */
    /**
     * Sets the stroke cap of the progress spinner. Default stroke cap is [Paint.Cap.SQUARE].
     *
     * @param strokeCap stroke cap
     */
    var strokeCap: Cap
        get() = mRing.strokeCap!!
        set(strokeCap) {
            mRing.strokeCap = strokeCap
            invalidateSelf()
        }

    /**
     * Returns the arrow width in pixels.
     *
     * @return arrow width in pixels
     */
    val arrowWidth: Float
        get() = mRing.arrowWidth

    /**
     * Returns the arrow height in pixels.
     *
     * @return arrow height in pixels
     */
    val arrowHeight: Float
        get() = mRing.arrowHeight

    /**
     * Sets the dimensions of the arrow at the end of the spinner in pixels.
     *
     * @param width width of the baseline of the arrow in pixels
     * @param height distance from tip of the arrow to its baseline in pixels
     */
    fun setArrowDimensions(width: Float, height: Float) {
        mRing.setArrowDimensions(width, height)
        invalidateSelf()
    }

    /**
     * Returns `true` if the arrow at the end of the spinner is shown.
     *
     * @return `true` if the arrow is shown, `false` otherwise.
     */
    /**
     * Sets if the arrow at the end of the spinner should be shown.
     *
     * @param show `true` if the arrow should be drawn, `false` otherwise
     */
    var arrowEnabled: Boolean
        get() = mRing.showArrow
        set(show) {
            mRing.showArrow = show
            invalidateSelf()
        }

    /**
     * Returns the scale of the arrow at the end of the spinner.
     *
     * @return scale of the arrow
     */
    /**
     * Sets the scale of the arrow at the end of the spinner.
     *
     * @param scale scaling that will be applied to the arrow's both width and height when drawing.
     */
    var arrowScale: Float
        get() = mRing.arrowScale
        set(scale) {
            mRing.arrowScale = scale
            invalidateSelf()
        }

    /**
     * Returns the start trim for the progress spinner arc
     *
     * @return start trim from [0..1]
     */
    val startTrim: Float
        get() = mRing.startTrim

    /**
     * Returns the end trim for the progress spinner arc
     *
     * @return end trim from [0..1]
     */
    val endTrim: Float
        get() = mRing.endTrim

    /**
     * Sets the start and end trim for the progress spinner arc. 0 corresponds to the geometric
     * angle of 0 degrees (3 o'clock on a watch) and it increases clockwise, coming to a full circle
     * at 1.
     *
     * @param start starting position of the arc from [0..1]
     * @param end ending position of the arc from [0..1]
     */
    fun setStartEndTrim(start: Float, end: Float) {
        mRing.startTrim = start
        mRing.endTrim = end
        invalidateSelf()
    }

    /**
     * Returns the amount of rotation applied to the progress spinner.
     *
     * @return amount of rotation from [0..1]
     */
    /**
     * Sets the amount of rotation to apply to the progress spinner.
     *
     * @param rotation rotation from [0..1]
     */
    var progressRotation: Float
        get() = mRing.rotation
        set(rotation) {
            mRing.rotation = rotation
            invalidateSelf()
        }

    /**
     * Returns the background color of the circle drawn inside the drawable.
     *
     * @return an ARGB color
     */
    /**
     * Sets the background color of the circle inside the drawable. Calling [ ][.setAlpha] does not affect the visibility background color, so it should be set
     * separately if it needs to be hidden or visible.
     *
     * @param color an ARGB color
     */
    var backgroundColor: Int
        get() = mRing.backgroundColor
        set(color) {
            mRing.backgroundColor = color
            invalidateSelf()
        }

    /**
     * Returns the colors used in the progress animation
     *
     * @return list of ARGB colors
     */
    /**
     * Sets the colors used in the progress animation from a color list. The first color will also
     * be the color to be used if animation is not started yet.
     *
     * @param colors list of ARGB colors to be used in the spinner
     */
    val colorSchemeColors: IntArray
        get() = mRing.colors

    fun setColorSchemeColors(vararg colors: Int) {
        mRing.colors = colors
        mRing.setColorIndex(0)
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        canvas.save()
        canvas.rotate(rotation, bounds.exactCenterX(), bounds.exactCenterY())
        mRing.draw(canvas, bounds)
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {
        mRing.alpha = alpha
        invalidateSelf()
    }

    override fun getAlpha(): Int {
        return mRing.alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mRing.setColorFilter(colorFilter)
        invalidateSelf()
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun isRunning(): Boolean {
        return mAnimator!!.isRunning
    }

    /**
     * Starts the animation for the spinner.
     */
    override fun start() {
        mAnimator!!.cancel()
        mRing.storeOriginals()
        // Already showing some part of the ring
        if (mRing.endTrim != mRing.startTrim) {
            mFinishing = true
            mAnimator!!.duration = ANIMATION_DURATION / 2.toLong()
            mAnimator!!.start()
        } else {
            mRing.setColorIndex(0)
            mRing.resetOriginals()
            mAnimator!!.duration = ANIMATION_DURATION.toLong()
            mAnimator!!.start()
        }
    }

    /**
     * Stops the animation for the spinner.
     */
    override fun stop() {
        mAnimator!!.cancel()
        rotation = 0f
        mRing.showArrow = false
        mRing.setColorIndex(0)
        mRing.resetOriginals()
        invalidateSelf()
    }

    // Adapted from ArgbEvaluator.java
    private fun evaluateColorChange(fraction: Float, startValue: Int, endValue: Int): Int {
        val startA = startValue shr 24 and 0xff
        val startR = startValue shr 16 and 0xff
        val startG = startValue shr 8 and 0xff
        val startB = startValue and 0xff
        val endA = endValue shr 24 and 0xff
        val endR = endValue shr 16 and 0xff
        val endG = endValue shr 8 and 0xff
        val endB = endValue and 0xff
        return startA + (fraction * (endA - startA)).toInt() shl 24 or (startR + (fraction * (endR - startR)).toInt() shl 16
                ) or (startG + (fraction * (endG - startG)).toInt() shl 8
                ) or startB + (fraction * (endB - startB)).toInt()
    }

    /**
     * Update the ring color if this is within the last 25% of the animation.
     * The new ring color will be a translation from the starting ring color to
     * the next color.
     */
    fun  /* synthetic access */updateRingColor(
        interpolatedTime: Float,
        ring: Ring
    ) {
        if (interpolatedTime > COLOR_CHANGE_OFFSET) {
            ring.setColor(
                evaluateColorChange(
                    (interpolatedTime - COLOR_CHANGE_OFFSET)
                            / (1f - COLOR_CHANGE_OFFSET),
                    ring.startingColor,
                    ring.nextColor
                )
            )
        } else {
            ring.setColor(ring.startingColor)
        }
    }

    /**
     * Update the ring start and end trim if the animation is finishing (i.e. it started with
     * already visible progress, so needs to shrink back down before starting the spinner).
     */
    private fun applyFinishTranslation(interpolatedTime: Float, ring: Ring) {
        // shrink back down and complete a full rotation before
        // starting other circles
        // Rotation goes between [0..1].
        updateRingColor(interpolatedTime, ring)
        val targetRotation =
            (floor(ring.startingRotation / MAX_PROGRESS_ARC.toDouble())
                    + 1f).toFloat()
        val startTrim = (ring.startingStartTrim
                + (ring.startingEndTrim - MIN_PROGRESS_ARC - ring.startingStartTrim)
                * interpolatedTime)
        ring.startTrim = startTrim
        ring.endTrim = ring.startingEndTrim
        val rotation = (ring.startingRotation
                + (targetRotation - ring.startingRotation) * interpolatedTime)
        ring.rotation = rotation
    }

    /**
     * Update the ring start and end trim according to current time of the animation.
     */
    fun  /* synthetic access */applyTransformation(
        interpolatedTime: Float,
        ring: Ring,
        lastFrame: Boolean
    ) {
        if (mFinishing) {
            applyFinishTranslation(interpolatedTime, ring)
            // Below condition is to work around a ValueAnimator issue where onAnimationRepeat is
            // called before last frame (1f).
        } else if (interpolatedTime != 1f || lastFrame) {
            val startingRotation = ring.startingRotation
            val startTrim: Float
            val endTrim: Float
            if (interpolatedTime < SHRINK_OFFSET) { // Expansion occurs on first half of animation
                val scaledTime =
                    interpolatedTime / SHRINK_OFFSET
                startTrim = ring.startingStartTrim
                endTrim =
                    startTrim + ((MAX_PROGRESS_ARC - MIN_PROGRESS_ARC)
                            * MATERIAL_INTERPOLATOR.getInterpolation(
                        scaledTime
                    ) + MIN_PROGRESS_ARC)
            } else { // Shrinking occurs on second half of animation
                val scaledTime =
                    (interpolatedTime - SHRINK_OFFSET) / (1f - SHRINK_OFFSET)
                endTrim =
                    ring.startingStartTrim + (MAX_PROGRESS_ARC - MIN_PROGRESS_ARC)
                startTrim =
                    endTrim - ((MAX_PROGRESS_ARC - MIN_PROGRESS_ARC)
                            * (1f - MATERIAL_INTERPOLATOR.getInterpolation(
                        scaledTime
                    ))
                            + MIN_PROGRESS_ARC)
            }
            var rotation =
                startingRotation + RING_ROTATION * interpolatedTime
            val groupRotation =
                GROUP_FULL_ROTATION * (interpolatedTime + mRotationCount)
            ring.startTrim = startTrim
            ring.endTrim = endTrim
            ring.rotation = rotation
            rotation = groupRotation
        }
    }

    private fun setupAnimators() {
        val ring = mRing
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.addUpdateListener { animation ->
            val interpolatedTime = animation.animatedValue as Float
            updateRingColor(interpolatedTime, ring)
            applyTransformation(interpolatedTime, ring, false)
            invalidateSelf()
        }
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.RESTART
        animator.interpolator = LINEAR_INTERPOLATOR
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
                mRotationCount = 0f
            }

            override fun onAnimationEnd(animator: Animator) {
                // do nothing
            }

            override fun onAnimationCancel(animation: Animator) {
                // do nothing
            }

            override fun onAnimationRepeat(animator: Animator) {
                applyTransformation(1f, ring, true)
                ring.storeOriginals()
                ring.goToNextColor()
                if (mFinishing) {
                    // finished closing the last ring from the swipe gesture; go
                    // into progress mode
                    mFinishing = false
                    animator.cancel()
                    animator.duration = ANIMATION_DURATION.toLong()
                    animator.start()
                    ring.showArrow = false
                } else {
                    mRotationCount += 1
                }
            }
        })
        mAnimator = animator
    }

    /**
     * A private class to do all the drawing of CircularProgressDrawable, which includes background,
     * progress spinner and the arrow. This class is to separate drawing from animation.
     */
    class Ring internal constructor() {
        private val mTempBounds = RectF()
        private val mPaint = Paint()
        private val mArrowPaint = Paint()
        private val mCirclePaint = Paint()
        var startTrim = 0f
        var endTrim = 0f
        var rotation = 0f
        var mStrokeWidth = 5f
        private lateinit var mColors: IntArray

        // mColorIndex represents the offset into the available mColors that the
        // progress circle should currently display. As the progress circle is
        // animating, the mColorIndex moves by one to the next available color.
        var mColorIndex = 0
        var startingStartTrim = 0f
        var startingEndTrim = 0f

        /**
         * @return The amount the progress spinner is currently rotated, between [0..1].
         */
        var startingRotation = 0f
        var mShowArrow = false
        var mArrow: Path? = null
        var mArrowScale = 1f

        /**
         * @param centerRadius inner radius in px of the circle the progress spinner arc traces
         */
        var centerRadius = 0f
        var mArrowWidth = 0
        var mArrowHeight = 0
        /**
         * @return current alpha of the progress spinner and arrowhead
         */
        /**
         * @param alpha alpha of the progress spinner and associated arrowhead.
         */
        var alpha = 255
        var mCurrentColor = 0

        /**
         * Sets the dimensions of the arrowhead.
         *
         * @param width width of the hypotenuse of the arrow head
         * @param height height of the arrow point
         */
        fun setArrowDimensions(width: Float, height: Float) {
            mArrowWidth = width.toInt()
            mArrowHeight = height.toInt()
        }

        var strokeCap: Cap?
            get() = mPaint.strokeCap
            set(strokeCap) {
                mPaint.strokeCap = strokeCap
            }

        val arrowWidth: Float
            get() = mArrowWidth.toFloat()

        val arrowHeight: Float
            get() = mArrowHeight.toFloat()

        /**
         * Draw the progress spinner
         */
        fun draw(c: Canvas, bounds: Rect) {
            val arcBounds = mTempBounds
            var arcRadius = centerRadius + mStrokeWidth / 2f
            if (centerRadius <= 0) {
                // If center radius is not set, fill the bounds
                arcRadius =
                    Math.min(bounds.width(), bounds.height()) / 2f - Math.max(
                        mArrowWidth * mArrowScale / 2f, mStrokeWidth / 2f
                    )
            }
            arcBounds[bounds.centerX() - arcRadius, bounds.centerY() - arcRadius, bounds.centerX() + arcRadius] =
                bounds.centerY() + arcRadius
            val startAngle = (startTrim + rotation) * 360
            val endAngle = (endTrim + rotation) * 360
            val sweepAngle = endAngle - startAngle
            mPaint.color = mCurrentColor
            mPaint.alpha = alpha

            // Draw the background first
            val inset = mStrokeWidth / 2f // Calculate inset to draw inside the arc
            arcBounds.inset(inset, inset) // Apply inset
            c.drawCircle(
                arcBounds.centerX(), arcBounds.centerY(), arcBounds.width() / 2f,
                mCirclePaint
            )
            arcBounds.inset(-inset, -inset) // Revert the inset
            c.drawArc(arcBounds, startAngle, sweepAngle, false, mPaint)
            drawTriangle(c, startAngle, sweepAngle, arcBounds)
        }

        fun drawTriangle(
            c: Canvas,
            startAngle: Float,
            sweepAngle: Float,
            bounds: RectF
        ) {
            if (mShowArrow) {
                if (mArrow == null) {
                    mArrow = Path()
                    mArrow!!.fillType = Path.FillType.EVEN_ODD
                } else {
                    mArrow!!.reset()
                }
                val centerRadius =
                    Math.min(bounds.width(), bounds.height()) / 2f
                val inset = mArrowWidth * mArrowScale / 2f
                // Update the path each time. This works around an issue in SKIA
                // where concatenating a rotation matrix to a scale matrix
                // ignored a starting negative rotation. This appears to have
                // been fixed as of API 21.
                mArrow!!.moveTo(0f, 0f)
                mArrow!!.lineTo(mArrowWidth * mArrowScale, 0f)
                mArrow!!.lineTo(
                    mArrowWidth * mArrowScale / 2, mArrowHeight
                            * mArrowScale
                )
                mArrow!!.offset(
                    centerRadius + bounds.centerX() - inset,
                    bounds.centerY() + mStrokeWidth / 2f
                )
                mArrow!!.close()
                // draw a triangle
                mArrowPaint.color = mCurrentColor
                mArrowPaint.alpha = alpha
                c.save()
                c.rotate(
                    startAngle + sweepAngle, bounds.centerX(),
                    bounds.centerY()
                )
                c.drawPath(mArrow!!, mArrowPaint)
                c.restore()
            }
        }// if colors are reset, make sure to reset the color index as well

        /**
         * Sets the colors the progress spinner alternates between.
         *
         * @param colors array of ARGB colors. Must be non-`null`.
         */
        var colors: IntArray
            get() = mColors
            set(colors) {
                mColors = colors
                // if colors are reset, make sure to reset the color index as well
                setColorIndex(0)
            }

        /**
         * Sets the absolute color of the progress spinner. This is should only
         * be used when animating between current and next color when the
         * spinner is rotating.
         *
         * @param color an ARGB color
         */
        fun setColor(color: Int) {
            mCurrentColor = color
        }

        /**
         * Sets the background color of the circle inside the spinner.
         */
        var backgroundColor: Int
            get() = mCirclePaint.color
            set(color) {
                mCirclePaint.color = color
            }

        /**
         * @param index index into the color array of the color to display in
         * the progress spinner.
         */
        fun setColorIndex(index: Int) {
            mColorIndex = index
            mCurrentColor = mColors[mColorIndex]
        }

        /**
         * @return int describing the next color the progress spinner should use when drawing.
         */
        val nextColor: Int
            get() = mColors[nextColorIndex]

        val nextColorIndex: Int
            get() = (mColorIndex + 1) % mColors.size

        /**
         * Proceed to the next available ring color. This will automatically
         * wrap back to the beginning of colors.
         */
        fun goToNextColor() {
            setColorIndex(nextColorIndex)
        }

        fun setColorFilter(filter: ColorFilter?) {
            mPaint.colorFilter = filter
        }

        /**
         * @param strokeWidth set the stroke width of the progress spinner in pixels.
         */
        var strokeWidth: Float
            get() = mStrokeWidth
            set(strokeWidth) {
                mStrokeWidth = strokeWidth
                mPaint.strokeWidth = strokeWidth
            }

        val startingColor: Int
            get() = mColors[mColorIndex]

        /**
         * @param show `true` if should show the arrow head on the progress spinner
         */
        var showArrow: Boolean
            get() = mShowArrow
            set(show) {
                if (mShowArrow != show) {
                    mShowArrow = show
                }
            }

        /**
         * @param scale scale of the arrowhead for the spinner
         */
        var arrowScale: Float
            get() = mArrowScale
            set(scale) {
                if (scale != mArrowScale) {
                    mArrowScale = scale
                }
            }

        /**
         * If the start / end trim are offset to begin with, store them so that animation starts
         * from that offset.
         */
        fun storeOriginals() {
            startingStartTrim = startTrim
            startingEndTrim = endTrim
            startingRotation = rotation
        }

        /**
         * Reset the progress spinner to default rotation, start and end angles.
         */
        fun resetOriginals() {
            startingStartTrim = 0f
            startingEndTrim = 0f
            startingRotation = 0f
            startTrim = 0f
            endTrim = 0f
            rotation = 0f
        }

        init {
            mPaint.strokeCap = Cap.SQUARE
            mPaint.isAntiAlias = true
            mPaint.style = Paint.Style.STROKE
            mArrowPaint.style = Paint.Style.FILL
            mArrowPaint.isAntiAlias = true
            mCirclePaint.color = Color.TRANSPARENT
        }
    }

    companion object {
        private val LINEAR_INTERPOLATOR: Interpolator = LinearInterpolator()
        private val MATERIAL_INTERPOLATOR: Interpolator =
            FastOutSlowInInterpolator()

        /** Maps to ProgressBar.Large style.  */
        const val LARGE = 0
        private const val CENTER_RADIUS_LARGE = 11f
        private const val STROKE_WIDTH_LARGE = 3f
        private const val ARROW_WIDTH_LARGE = 12
        private const val ARROW_HEIGHT_LARGE = 6

        /** Maps to ProgressBar default style.  */
        const val DEFAULT = 1
        private const val CENTER_RADIUS = 7.5f
        private const val STROKE_WIDTH = 2.5f
        private const val ARROW_WIDTH = 10
        private const val ARROW_HEIGHT = 5

        /**
         * This is the default set of colors that's used in spinner. [ ][.setColorSchemeColors] allows modifying colors.
         */
        private val COLORS = intArrayOf(
            Color.BLACK
        )

        /**
         * The value in the linear interpolator for animating the drawable at which
         * the color transition should start
         */
        private const val COLOR_CHANGE_OFFSET = 0.75f
        private const val SHRINK_OFFSET = 0.5f

        /** The duration of a single progress spin in milliseconds.  */
        private const val ANIMATION_DURATION = 1332

        /** Full rotation that's done for the animation duration in degrees.  */
        private const val GROUP_FULL_ROTATION = 1080f / 5f

        /** Maximum length of the progress arc during the animation.  */
        private const val MAX_PROGRESS_ARC = .8f

        /** Minimum length of the progress arc during the animation.  */
        private const val MIN_PROGRESS_ARC = .01f

        /** Rotation applied to ring during the animation, to complete it to a full circle.  */
        private const val RING_ROTATION =
            1f - (MAX_PROGRESS_ARC - MIN_PROGRESS_ARC)
    }

    /**
     * @param context application context
     */
    init {
        mRing = Ring()
        mRing.colors = COLORS
        strokeWidth = STROKE_WIDTH
        setupAnimators()
    }
}