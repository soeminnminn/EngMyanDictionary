package com.s16.engmyan.fragment;

import com.s16.drawing.ProgressWheelDrawable;
import com.s16.engmyan.R;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ProgressWheelFragment extends DialogFragment {
	
	private Context mContext;
	private ImageView mProgressWheel;
	private ProgressWheelDrawable mDrawable;
	
	public ProgressWheelFragment() {
		super();
	}
	
	public ProgressWheelFragment(Context context) {
		super();
		mContext = context;
	}
	
	protected Context getContext() {
		return mContext;
	}
	
	private void init(Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		int barLength = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, dm);
		int barWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, dm);
		int rimWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, dm);
		int spinSpeed = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, dm);
		int textSize = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, dm);
		
		CharSequence text = getResources().getString(R.string.install_message);
		int textColor = getResources().getColor(R.color.black);
		int rimColor = getResources().getColor(R.color.title_background);
		int circleColor = getResources().getColor(R.color.background);
		
		mDrawable = new ProgressWheelDrawable();
		mDrawable.setText(text);
		mDrawable.setTextSize(textSize);
		mDrawable.setTextColor(textColor);
		mDrawable.setRimColor(rimColor);
		mDrawable.setCircleColor(circleColor);
		mDrawable.setBarColor(0x80000000);
		mDrawable.setBarLength(barLength);
		mDrawable.setBarWidth(barWidth);
		mDrawable.setRimWidth(rimWidth);
		mDrawable.setSpinSpeed(spinSpeed);
	}
	
	@Override
    public void onCreate(Bundle icicle) {
        setStyle(STYLE_NORMAL, R.style.DialogTheme);
        super.onCreate(icicle);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.progress_wheel_fragment, container, false);
		if (mContext == null) {
			mContext = inflater.getContext();
		}
		
		init(inflater.getContext());
		mProgressWheel = (ImageView)view.findViewById(R.id.progressBar);
		mProgressWheel.setImageDrawable(mDrawable);
        
		if (Build.VERSION.SDK_INT < 11) {
			getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_full_holo_dark);
		}
		setCancelable(false);
		getDialog().setTitle(R.string.install_text);
		return view;
	}
	
	@Override
    public void onStart() {
        super.onStart();
        mDrawable.start();
    }
	
	@Override
	public void onStop() {
		mDrawable.stop();
		super.onStop();
	}
}
