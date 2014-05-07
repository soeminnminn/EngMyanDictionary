package com.s16.widget;

import com.s16.engmyan.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;

public class AnimatingRelativeLayout extends RelativeLayout 
		implements AnimationListener {
	
	private Context mContext;
	private Animation mShowAnimation;
	private Animation mHideAnimation;
	private AnimationCompleteListener mAnimationCompleteListener;
	
	public interface AnimationCompleteListener {
		public void onAnimationComplete();
	}

    public AnimatingRelativeLayout(Context context) {
        super(context);
        this.mContext = context;
        initAnimations();

    }

    public AnimatingRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initAnimations();
    }

    public AnimatingRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        initAnimations();
    }

    private void initAnimations() {
    	if (isInEditMode()) return;
        mShowAnimation = (Animation)AnimationUtils.loadAnimation(mContext, R.anim.expand_from_top);
        mShowAnimation.setAnimationListener(this);
        
        mHideAnimation = (Animation)AnimationUtils.loadAnimation(mContext, R.anim.collapse_to_top);
        mHideAnimation.setAnimationListener(this);
    }

    public void show() {
        if (isVisible()) return;
        show(true);
    }

    public void show(boolean withAnimation) {
    	if (isVisible()) return;
        if (withAnimation) this.startAnimation(mShowAnimation);
        this.setVisibility(View.VISIBLE);
    }

    public void hide() {
        if (!isVisible()) return;
        hide(true);
    }

    public void hide(boolean withAnimation) {
    	if (!isVisible()) return;
        if (withAnimation) this.startAnimation(mHideAnimation);
        this.setVisibility(View.GONE);
    }

    public boolean isVisible() {
        return (this.getVisibility() == View.VISIBLE);
    }

    public void overrideDefaultShowAnimation(Animation value) {
        mShowAnimation = value;
        mShowAnimation.setAnimationListener(this);
    }

    public void overrideDefaultHideAnimation(Animation value) {
        mHideAnimation = value;
        mHideAnimation.setAnimationListener(this);
    }
    
    public void setAnimationCompleteListener(AnimationCompleteListener value) {
    	mAnimationCompleteListener = value;
    }

	@Override
	public void onAnimationStart(Animation animation) {
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (mAnimationCompleteListener != null) {
			mAnimationCompleteListener.onAnimationComplete();
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}
}
