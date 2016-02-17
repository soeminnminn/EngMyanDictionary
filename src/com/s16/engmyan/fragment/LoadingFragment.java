package com.s16.engmyan.fragment;

import com.s16.engmyan.R;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

public class LoadingFragment extends DialogFragment {

	private CharSequence mMessage;
	private int mMessageId;
	private TextView mTextMessage;
	
	@SuppressLint("InlinedApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTranslucentTheme);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	  Dialog dialog = super.onCreateDialog(savedInstanceState);

	  // request a window without the title
	  dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
	  return dialog;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_loading, container, false);
		
		mTextMessage = (TextView)rootView.findViewById(R.id.textLoading);
		if (mMessage != null) {
			mTextMessage.setText(mMessage);
		} else if (mMessageId != 0) {
			mTextMessage.setText(mMessageId);
		}
		
		return rootView;
	}
	
	public void setMessage(int resId) {
		mMessageId = resId;
		if (mTextMessage != null) {
			mTextMessage.setText(resId);
		}
	}
	
	public void setMessage(CharSequence message) {
		mMessage = message;
		if (mTextMessage != null) {
			mTextMessage.setText(message);
		}
	}
}
