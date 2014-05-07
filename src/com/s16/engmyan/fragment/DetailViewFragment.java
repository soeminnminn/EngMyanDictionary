package com.s16.engmyan.fragment;

import java.util.Locale;

import com.s16.engmyan.Constants;
import com.s16.engmyan.ExpansionManager;
import com.s16.engmyan.R;
import com.s16.engmyan.Utility;
import com.s16.engmyan.data.DictionaryItem;
import com.s16.widget.AnimatingRelativeLayout;
import com.s16.widget.TouchImageView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class DetailViewFragment extends Fragment 
		implements AnimatingRelativeLayout.AnimationCompleteListener {

	protected static String TAG = DetailViewFragment.class.getSimpleName();
	private Context mContext;
	
	private TextToSpeech mTextToSpeech;
	private boolean mTextToSpeechEnabled;
	
	private RelativeLayout mLayoutWebView;
	private AnimatingRelativeLayout mLayoutImageView;
	private TouchImageView mImageView;
	private ImageView mImageCaution; 
	private WebView mWebView;
	private DictionaryItem mData;
	
	public DetailViewFragment() {
		super();
	}
	
	public DetailViewFragment(Context context) {
		super();
		mContext = context;
	}
	
	protected Context getContext() {
		return mContext;
	}
	
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.detailview_fragment, container, false);
		if (mContext == null) {
			mContext = inflater.getContext();
		}
		
		initialize(view);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onSaveInstanceState(final Bundle outState) {
		if ((outState != null) && (mData != null)) {
			outState.putParcelable(Constants.DETAIL_DATA_KEY, mData);
		}
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onDestroy() {
		if (mTextToSpeech != null) {
			mTextToSpeech.stop();
			mTextToSpeech.shutdown();
		}
		super.onDestroy();
	}
	
	private void initialize(View view) {
		mLayoutWebView = (RelativeLayout)view.findViewById(R.id.layoutWebView);
		
		mLayoutImageView = (AnimatingRelativeLayout)view.findViewById(R.id.layoutImageView);
		mLayoutImageView.setAnimationCompleteListener(this);
		mLayoutImageView.hide(false);
		
		mImageView = (TouchImageView)view.findViewById(R.id.imagePreView);
		mImageView.setMaxZoom(6);
		mImageCaution = (ImageView)view.findViewById(R.id.imageCaution);
		
		mWebView = (WebView)view.findViewById(R.id.webViewDetail);
		mWebView.loadUrl(Constants.URL_NOT_FOUND);
		
		mTextToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {

			@Override
			public void onInit(int status) {
				mTextToSpeechEnabled = (status == TextToSpeech.SUCCESS);
			} 
		});
		
		setImageBitmap();
		setDefinition();
	}
	
	protected void setSaveInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mData = savedInstanceState.getParcelable(Constants.DETAIL_DATA_KEY);
		}
	}
	
	protected void setDefinition() {
		if (mWebView == null) return;
		
		if ((mData == null) || (TextUtils.isEmpty(mData.definition))) {
			mWebView.loadUrl(Constants.URL_NOT_FOUND);
			return;
		}
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean usedUnicodeFix = sharedPreferences.getBoolean(Constants.PREFS_USED_UNICODE_FIX, true);
		
		String html = "<html>";
		html += "<head>";
		html += "<meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=yes, width=device-width\" />";
		html += "<meta content=\"" + Constants.MIME_TYPE + "; charset=" + Constants.ENCODING + "\" http-equiv=\"content-type\">";
		html += "<link rel=\"stylesheet\" type=\"text/css\" href=\"css/style.css\">";
		if (TextUtils.isEmpty(mData.title)) {
			html += "<title>Untitled</title>";
		} else {
			html += "<title>" + mData.title + "</title>";
		}
		html += "</head>";
		html += "<body>";
		if (usedUnicodeFix) {
			html += Utility.ZawGyiDrawFix(mData.definition);
		} else {
			html += mData.definition;
		}
		html += "</html>";
	
		mWebView.loadDataWithBaseURL(Constants.URL_NOT_FOUND, html
				, Constants.MIME_TYPE, Constants.ENCODING, Constants.URL_DEFAULT);
	}
	
	protected void setImageBitmap() {
		if (mData == null) return;
		Bitmap bitmap = null;
		if (mData.picture && !TextUtils.isEmpty(mData.filename)) {
				
			String picturePath = Constants.PICTURE_FOLDER + mData.filename + ".png";
			bitmap = ExpansionManager.getBitmapAssert(getContext(), picturePath);
		}
		
		if (mImageView != null) {
			if (bitmap != null) {
				if (mImageCaution != null) mImageCaution.setVisibility(View.GONE);
				mImageView.setVisibility(View.VISIBLE);
				mImageView.setImageBitmap(bitmap);
				mImageView.invalidate();
			} else {
				mImageView.setVisibility(View.GONE);
				if (mImageCaution != null) mImageCaution.setVisibility(View.VISIBLE);
			}
		}	
		
		if ((mLayoutImageView != null) && (mLayoutImageView.isVisible())) {
			toggleImageView();
		}
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	public void setData(DictionaryItem data) {
		if ((data == null) || (data.id < 0)) return;
		mData = data;
		
		setImageBitmap();
		if (mWebView != null) {
			WebSettings webViewSettings = mWebView.getSettings(); 
			webViewSettings.setAllowFileAccess(true);
			webViewSettings.setJavaScriptEnabled(true);
			webViewSettings.setSupportZoom(true);
			webViewSettings.setBuiltInZoomControls(true);
			
			if (Build.VERSION.SDK_INT >= 11) {
				webViewSettings.setDisplayZoomControls(false);
				webViewSettings.setAllowContentAccess(true);
			}
			
			setDefinition();
		}
	}
	
	public boolean getImageVisible() {
		if (mLayoutImageView != null) {
			return (mLayoutImageView.getVisibility() == View.VISIBLE);
		}
		return false;
	}
	
	public String getTitle() {
		if (mData != null) return mData.word;
		return Constants.EMPTY_STRING;
	}
	
	public boolean getHasPicture() {
		if (mData != null) return mData.picture;
		return false;
	}
	
	public boolean getHasSound() {
		if (ExpansionManager.isExpansionExists(getContext())) {
			if (mData != null) return mData.sound;
		}
		return true;
	}
	
	public void toggleImageView() {
		if (mLayoutWebView == null) return;
		if (mLayoutImageView == null) return;
		
		if (mLayoutImageView.isVisible()) {
			mLayoutImageView.hide();
		} else {
			mLayoutImageView.show();
		}
	}
	
	public void doSpeak() {
		if (mData == null) return;
		if (ExpansionManager.isExpansionExists(getContext())) {
			
			if ((mData.sound) && (!TextUtils.isEmpty(mData.filename))) {
				char p = mData.filename.charAt(0);
				String soundPath = Constants.SOUND_FOLDER + p + "/" + mData.filename + ".wav";
				ExpansionManager.playSoundExpansion(getContext(), soundPath);
				return;
			}
			
		} else if ((mTextToSpeech != null) && mTextToSpeechEnabled) {
			
			if (!TextUtils.isEmpty(mData.word)){
				try {
					mTextToSpeech.setLanguage(Locale.US);
					
					String text = mData.word.charAt(0) == '-' ? mData.word.substring(1) : mData.word;
					if (mTextToSpeech.speak(text, 0, null) == TextToSpeech.SUCCESS) {
						return;
					}
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		Toast.makeText(getContext(), getString(R.string.no_sound_message), Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onAnimationComplete() {
		if (mLayoutWebView == null) return;
		if (mLayoutImageView == null) return;
		
		if (mLayoutImageView.isVisible()) {
			mLayoutWebView.setVisibility(View.GONE);
		} else {
			mLayoutWebView.setVisibility(View.VISIBLE);
		}
	}
}
