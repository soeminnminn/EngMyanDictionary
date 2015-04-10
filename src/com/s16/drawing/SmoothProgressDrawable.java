package com.s16.drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

public class SmoothProgressDrawable extends Drawable implements Animatable {
	
	private static final long FRAME_DURATION = 1000 / 60;
	private final static float OFFSET_PER_FRAME = 0.01f;
	private Interpolator mInterpolator;
	private Rect mBounds;
	private Paint mPaint;
	private int[] mColors;
	private int mColorsIndex;
	private boolean mRunning;
	private float mCurrentOffset;
	private int mSeparatorLength;
	private int mSectionsCount;
	private float mSpeed;
	private boolean mReversed;
	private boolean mNewTurn;
	private boolean mMirrorMode;

	private SmoothProgressDrawable(Interpolator interpolator, int sectionsCount, int separatorLength, int[] colors, 
			int width, float speed, boolean reversed, boolean mirrorMode) {
		mRunning = false;
		mInterpolator = interpolator;
		mSectionsCount = sectionsCount;
		mSeparatorLength = separatorLength;
		mSpeed = speed;
		mReversed = reversed;
		mColors = colors;
		mColorsIndex = 0;
		mMirrorMode = mirrorMode;

		mPaint = new Paint();
		mPaint.setStrokeWidth(width);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setDither(false);
		mPaint.setAntiAlias(false);
	}

	@Override
	public void draw(Canvas canvas) {
		mBounds = getBounds();
		canvas.clipRect(mBounds);
		int boundsWidth = mBounds.width();
		if (mReversed) {
			canvas.translate(boundsWidth, 0); canvas.scale(-1, 1);
		}
		drawStrokes(canvas);
	}

	private void drawStrokes(Canvas canvas) {
		float prevValue = 0f;
		int boundsWidth = mBounds.width();
		if (mMirrorMode) boundsWidth /= 2;
		int width = boundsWidth + mSeparatorLength + mSectionsCount;
		int centerY = mBounds.centerY();
		float xSectionWidth = 1f / mSectionsCount;
		if (mNewTurn) {
			mColorsIndex = decrementColor(mColorsIndex);
			mNewTurn = false;
		}
		float prev;
		float end;
		float spaceLength;
		float xOffset;
		float ratioSectionWidth;
		float sectionWidth;
		float drawLength;
		int currentIndexColor = mColorsIndex;

		for (int i = 0; i <= mSectionsCount; ++i) {
			xOffset = xSectionWidth * i + mCurrentOffset;
			prev = Math.max(0f, xOffset - xSectionWidth);
			ratioSectionWidth = Math.abs( mInterpolator.getInterpolation(prev) - mInterpolator.getInterpolation(Math.min(xOffset, 1f)));
			sectionWidth = (int)(width * ratioSectionWidth);
			if (sectionWidth + prev < width)
					spaceLength = Math.min(sectionWidth, mSeparatorLength);
			else
					spaceLength = 0f;
			drawLength = sectionWidth > spaceLength ? sectionWidth - spaceLength : 0;
			end = prevValue + drawLength;
			if (end > prevValue) {
				drawLine(canvas, boundsWidth, Math.min(boundsWidth, prevValue), centerY, Math.min(boundsWidth, end), centerY, currentIndexColor);
			}
			prevValue = end + spaceLength;
			currentIndexColor = incrementColor(currentIndexColor);
		}
	}

	private void drawLine(Canvas canvas, int canvasWidth, float startX, float startY, float stopX, float stopY, int currentIndexColor) {
		mPaint.setColor(mColors[currentIndexColor]);
		if (!mMirrorMode) {
			canvas.drawLine(startX, startY, stopX, stopY, mPaint);
		} else {
			if (mReversed) {
				canvas.drawLine(canvasWidth + startX, startY, canvasWidth + stopX, stopY, mPaint);
				canvas.drawLine(canvasWidth - (int)startX, startY, canvasWidth - (int)stopX, stopY, mPaint);
			} else {
				canvas.drawLine(startX, startY, stopX, stopY, mPaint);
				canvas.drawLine(canvasWidth * 2 - (int)startX, startY, canvasWidth * 2 - (int)stopX, stopY, mPaint);
			}
		}
		canvas.save();
	}

	private int incrementColor(int colorIndex) {
		++colorIndex;
		if (colorIndex >= mColors.length) colorIndex = 0;
		return colorIndex;
	}

	private int decrementColor(int colorIndex) {
		--colorIndex;
		if (colorIndex < 0) colorIndex = mColors.length - 1;
		return colorIndex;
	}

	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSPARENT;
	}

	@Override
	public void start() {
		if (isRunning()) return;
		scheduleSelf(mUpdater, SystemClock.uptimeMillis() + FRAME_DURATION);
		invalidateSelf();
	}

	@Override
	public void stop() {
		if (!isRunning()) return;
		mRunning = false;
		unscheduleSelf(mUpdater);
	}

	@Override
	public void scheduleSelf(Runnable what, long when) {
		mRunning = true;
		super.scheduleSelf(what, when);
	}

	@Override
	public boolean isRunning() {
		return mRunning;
	}

	private final Runnable mUpdater = new Runnable() {

		@Override
		public void run() {
			mCurrentOffset += (OFFSET_PER_FRAME * mSpeed);
			if (mCurrentOffset >= (1f / mSectionsCount)) {
				mNewTurn = true;
				mCurrentOffset = 0f;
			}
			scheduleSelf(mUpdater, SystemClock.uptimeMillis() + FRAME_DURATION);
			invalidateSelf();
		}
	};

	/** * Builder for SmoothProgressDrawable! You must use it! */
	public static class Builder {
		private Interpolator mInterpolator;
		private int mSectionsCount = 4;
		private int[] mColors;
		private float mSpeed = 1.0f;
		private boolean mReversed;
		private boolean mMirrorMode;
		private int mStrokeSeparatorLength;
		private int mStrokeWidth;

		public Builder(Context context) {
			initValues(context);
		}

		public SmoothProgressDrawable build() {
			SmoothProgressDrawable ret = new SmoothProgressDrawable(mInterpolator, mSectionsCount, mStrokeSeparatorLength, mColors, 
					mStrokeWidth, mSpeed, mReversed, mMirrorMode);
			return ret;
		}

		private void initValues(Context context) {
			DisplayMetrics dm = context.getResources().getDisplayMetrics();
			mInterpolator = new AccelerateInterpolator();
			//mColors = new int[]{ 0xff33b5e5 };
			mColors = new int[]{ 0xff3e802f, 0xfff4b400, 0xff427fed, 0xffb23424 };
			mStrokeSeparatorLength = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, dm); // 4dip
			mStrokeWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, dm); // 4dip
		}

		public Builder interpolator(Interpolator interpolator) {
			if (interpolator == null)
				throw new IllegalArgumentException("Interpolator can\'t be null");
			mInterpolator = interpolator;
			return this;
		}

		public Builder sectionsCount(int sectionsCount) {
			if (sectionsCount <= 0)
				throw new IllegalArgumentException("SectionsCount must be > 0");
			mSectionsCount = sectionsCount;
			return this;
		}

		public Builder separatorLength(int separatorLength) {
			if (separatorLength < 0)
				throw new IllegalArgumentException("SeparatorLength must be >= 0");
			mStrokeSeparatorLength = separatorLength;
			return this;
		}

		public Builder color(int color) {
			mColors = new int[]{color};
			return this;
		}

		public Builder colors(int[] colors) {
			if (colors == null || colors.length == 0)
				throw new IllegalArgumentException("Your color array must not be empty");
			mColors = colors;
			return this;
		}

		public Builder width(int width) {
			if (width < 0)
				throw new IllegalArgumentException("The width must be >= 0");
			mStrokeWidth = width;
			return this;
		}

		public Builder speed(float speed) {
			if (speed < 0)
				throw new IllegalArgumentException("Speed must be >= 0");
			mSpeed = speed;
			return this;
		}

		public Builder reversed(boolean reversed) {
			mReversed = reversed;
			return this;
		}

		public Builder mirrorMode(boolean mirrorMode) {
			mMirrorMode = mirrorMode;
			return this;
		}
	}
}