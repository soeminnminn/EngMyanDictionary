package com.s16.engmyan.fragment;

import com.s16.engmyan.Constants;
import com.s16.engmyan.R;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

public class CreditFragment extends DialogFragment {

	private Context mContext;
	
	public CreditFragment() {
		super();
	}
	
	public CreditFragment(Context context) {
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
		
		View view = inflater.inflate(R.layout.credit_fragment, container, false);
		if (mContext == null) {
			mContext = inflater.getContext();
		}
		
		if (Build.VERSION.SDK_INT < 11) {
			getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_full_holo_dark);
		}
		getDialog().setTitle(R.string.prefs_credit);
		
		WebView webView = (WebView)view.findViewById(R.id.webViewCredit);
		webView.loadUrl(Constants.URL_CREDIT);
		
		Button closeButton = (Button)view.findViewById(android.R.id.closeButton); 
		closeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getDialog().dismiss();
			}
		});
		
		return view;
	}
}
