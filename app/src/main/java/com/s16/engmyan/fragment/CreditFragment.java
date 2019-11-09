package com.s16.engmyan.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;

import com.s16.app.ActionBarUtils;
import com.s16.engmyan.Constants;
import com.s16.engmyan.R;

public class CreditFragment extends DialogFragment {

	@SuppressLint("InlinedApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme_Translucent);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dialog;
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_credit, container, false);
		
		WebView webView = (WebView)rootView.findViewById(R.id.webViewCredit);
		webView.getSettings().setAllowFileAccess(true);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(Constants.URL_CREDIT);
		
		View actionClose = rootView.findViewById(R.id.closeButton);
		ActionBarUtils.getInstance(getContext()).makeActionButton(actionClose);
		actionClose.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		return rootView;
	}
}
