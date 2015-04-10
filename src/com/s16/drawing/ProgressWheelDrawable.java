package com.s16.drawing;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Paint.Style;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.TextUtils;

public class ProgressWheelDrawable extends Drawable implements Animatable {

	private static final long FRAME_DURATION = 1000 / 60;
	
	//Sizes (with defaults)
	private int mFullRadius = 100;
	private int mCircleRadius = 80;
	private int mBarLength = 60;
	private int mBarWidth = 20;
	private int mRimWidth = 20;
	private int mTextSize = 20;
	
	//Colors (with defaults)
	private int mBarColor = 0xAA000000;
	private int mCircleColor = 0x00000000;
	private int mRimColor = 0xAADDDDDD;
	private int mTextColor = 0xFF000000;
	
	//Paints
	private Paint mBarPaint = new Paint();
	private Paint mCirclePaint = new Paint();
	private Paint mRimPaint = new Paint();
	private Paint mTextPaint = new Paint();
	
	//Rectangles
	private RectF mCircleBounds = new RectF();
	
	//Animation
	//The amount of pixels to move the bar by on each draw
	private int mSpinSpeed = 3;
	private int mProgress = 0;
	private final Runnable mUpdater = new Runnable() {

		@Override
		public void run() {
			if(mRunning) {
				mProgress+=mSpinSpeed;
				if(mProgress>360) {
					mProgress = 0;
				}
				scheduleSelf(mUpdater, SystemClock.uptimeMillis() + FRAME_DURATION);
			}
			
			invalidateSelf();
		}
	};
	private boolean mRunning;
	
	//Other
	private CharSequence mText = "";
	private String[] mSplitText = {};
	
	public ProgressWheelDrawable() {
		setupPaints();
	}
	
	/**
	 * Set the properties of the paints we're using to 
	 * draw the progress wheel
	 */
	private void setupPaints() {
		mBarPaint.setColor(mBarColor);
        mBarPaint.setAntiAlias(true);
        mBarPaint.setStyle(Style.STROKE);
        mBarPaint.setStrokeWidth(mBarWidth);
        
        mRimPaint.setColor(mRimColor);
        mRimPaint.setAntiAlias(true);
        mRimPaint.setStyle(Style.STROKE);
        mRimPaint.setStrokeWidth(mRimWidth);
        
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Style.FILL);
        
        mTextPaint.setColor(mTextColor);
        mTextPaint.setStyle(Style.FILL);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mTextSize);
	}
	
	/**
	 * Set the bounds of the component
	 */
	private void setupBounds() {
		Rect padding = new Rect();
		getPadding(padding);
		mCircleBounds = new RectF(padding.left + mBarWidth,
				padding.top + mBarWidth,
                this.getWidth() - padding.right - mBarWidth,
                this.getHeight() - padding.bottom - mBarWidth);
		
		mFullRadius = (int) ((this.getWidth() - padding.right - mBarWidth)/2);
	    mCircleRadius = (mFullRadius - mBarWidth) + 1;
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

	/**
	 * Increment the progress by 1 (of 360)
	 */
	public void incrementProgress() {
		mRunning = false;
		mProgress++;
		setText(Math.round(((float)mProgress/360)*100) + "%");
		scheduleSelf(mUpdater, SystemClock.uptimeMillis() + FRAME_DURATION);
		invalidateSelf();
	}

	/**
	 * Set the progress to a specific value
	 */
	public void setProgress(int i) {
		mRunning = false;
	    mProgress=i;
	    scheduleSelf(mUpdater, SystemClock.uptimeMillis() + FRAME_DURATION);
		invalidateSelf();
	}
	
	/**
	 * Reset the count (in increment mode)
	 */
	public void resetCount() {
		mProgress = 0;
		setText("0%");
		invalidateSelf();
	}
	
	@Override
	protected void onBoundsChange(Rect bounds) {
		setupBounds();
	}

	@Override
	public void draw(Canvas canvas) {
		
		Rect padding = new Rect();
		getPadding(padding);
		
		int saveCount = canvas.save();
		
		float dx = (getBounds().width() - getWidth()) / 2;
		float dy = (getBounds().height() - getHeight()) / 2;
		canvas.translate(dx, dy);
		
		//Draw the rim
		canvas.drawArc(mCircleBounds, 360, 360, false, mRimPaint);
		//Draw the bar
		if(mRunning) {
			canvas.drawArc(mCircleBounds, mProgress - 90, mBarLength, false,
				mBarPaint);
		} else {
			canvas.drawArc(mCircleBounds, -90, mProgress, false, mBarPaint);
		}
		//Draw the inner circle
		canvas.drawCircle((mCircleBounds.width()/2) + mRimWidth + padding.left, 
				(mCircleBounds.height()/2) + mRimWidth + padding.top, 
				mCircleRadius, 
				mCirclePaint);
		//Draw the text (attempts to center it horizontally and vertically)
		if (mSplitText != null) {
			int offsetNum = 0;
			for(String s : mSplitText) {
				float offset = mTextPaint.measureText(s) / 2;
				canvas.drawText(s, this.getWidth() / 2 - offset, 
					this.getHeight() / 2 + (mTextSize*(offsetNum)) 
					- ((mSplitText.length-1)*(mTextSize/2)), mTextPaint);
				offsetNum++;
			}
		}
		
		canvas.restoreToCount(saveCount);
	}

	//----------------------------------
	//Getters + setters
	//----------------------------------
	@Override
	public void setAlpha(int alpha) {
		mBarPaint.setAlpha(alpha);
		mRimPaint.setAlpha(alpha);
		mCirclePaint.setAlpha(alpha);
		mTextPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
	}

	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}
	
	private float getWidth() {
		return Math.min(getBounds().width(), getBounds().height());
	}
	
	private float getHeight() {
		return Math.min(getBounds().width(), getBounds().height());
	}
	
	public void setText(CharSequence text) {
		mText = text;
		if (!TextUtils.isEmpty(mText)) {
			mSplitText = mText.toString().split("\n");
		}
		invalidateSelf();
	}
	
	public int getCircleRadius() {
		return mCircleRadius;
	}

	public void setCircleRadius(int circleRadius) {
		mCircleRadius = circleRadius;
		invalidateSelf();
	}

	public int getBarLength() {
		return mBarLength;
	}

	public void setBarLength(int barLength) {
		mBarLength = barLength;
		invalidateSelf();
	}

	public int getBarWidth() {
		return mBarWidth;
	}

	public void setBarWidth(int barWidth) {
		mBarWidth = barWidth;
		mBarPaint.setStrokeWidth(barWidth);
		invalidateSelf();
	}

	public int getTextSize() {
		return mTextSize;
	}

	public void setTextSize(int textSize) {
		mTextSize = textSize;
		mTextPaint.setTextSize(mTextSize);
		invalidateSelf();
	}

	public int getTextColor() {
		return mTextColor;
	}

	public void setTextColor(int textColor) {
		mTextColor = textColor;
		mTextPaint.setColor(mTextColor);
		invalidateSelf();
	}
	
	public int getBarColor() {
		return mBarColor;
	}

	public void setBarColor(int barColor) {
		mBarColor = barColor;
		mBarPaint.setColor(mBarColor);
		invalidateSelf();
	}

	public int getCircleColor() {
		return mCircleColor;
	}

	public void setCircleColor(int circleColor) {
		mCircleColor = circleColor;
		mCirclePaint.setColor(mCircleColor);
		invalidateSelf();
	}

	public int getRimColor() {
		return mRimColor;
	}

	public void setRimColor(int rimColor) {
		mRimColor = rimColor;
		mRimPaint.setColor(mRimColor);
		invalidateSelf();
	}
	
	public Shader getRimShader() {
		return mRimPaint.getShader();
	}

	public void setRimShader(Shader shader) {
		mRimPaint.setShader(shader);
		invalidateSelf();
	}
	
	public int getSpinSpeed() {
		return mSpinSpeed;
	}

	public void setSpinSpeed(int spinSpeed) {
		mSpinSpeed = spinSpeed;
		invalidateSelf();
	}
	
	public int getRimWidth() {
		return mRimWidth;
	}

	public void setRimWidth(int rimWidth) {
		mRimWidth = rimWidth;
		mRimPaint.setStrokeWidth(mRimWidth);
		invalidateSelf();
	}
}
