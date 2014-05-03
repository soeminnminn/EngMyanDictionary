package com.s16.engmyan.fragment;

import com.s16.engmyan.R;
import com.s16.widget.ProgressWheelView;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ProgressWheelFragment extends DialogFragment {
	
	private Context mContext;
	private ProgressWheelView mProgressWheel;
	
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
		
		mProgressWheel = (ProgressWheelView)view.findViewById(R.id.progressBar);
		mProgressWheel.setText(getContext().getString(R.string.install_message));
        
		if (Build.VERSION.SDK_INT < 11) {
			getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_full_holo_dark);
		}
		getDialog().setCancelable(false);
		getDialog().setTitle(R.string.install_text);
		return view;
	}
	
	@Override
    public void onStart() {
        super.onStart();
        mProgressWheel.spin();
    }
	
	@Override
	public void onStop() {
		mProgressWheel.stopSpinning();
		super.onStop();
	}
}
