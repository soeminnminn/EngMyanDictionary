package com.s16.engmyan.fragment;

import java.util.Locale;

import com.s16.engmyan.Constants;
import com.s16.engmyan.ExpansionManager;
import com.s16.engmyan.R;
import com.s16.engmyan.Utility;
import com.s16.engmyan.data.DataProvider;
import com.s16.widget.AnimatingRelativeLayout;
import com.s16.widget.TouchImageView;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
	
	private String mWord;
	private String mTitle;
	private String mDefinition;
	private String mFileName;
	private boolean mHasSound;
	private boolean mHasPicture;
	
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
		if (outState != null) {
			outState.putString(Constants.DETAIL_WORD_KEY, mWord);
			outState.putString(Constants.DETAIL_TITLE_KEY, mTitle);
			outState.putString(Constants.DETAIL_DEFINITION_KEY, mDefinition);
			outState.putString(Constants.DETAIL_FILENAME_KEY, mFileName);
			outState.putBoolean(Constants.DETAIL_PICTURE_KEY, mHasPicture);
			outState.putBoolean(Constants.DETAIL_SOUND_KEY, mHasSound);
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
			mWord = savedInstanceState.getString(Constants.DETAIL_WORD_KEY, "");
			mTitle = savedInstanceState.getString(Constants.DETAIL_TITLE_KEY, "");
			mDefinition = savedInstanceState.getString(Constants.DETAIL_DEFINITION_KEY, "");
			mFileName = savedInstanceState.getString(Constants.DETAIL_FILENAME_KEY, "");
			mHasPicture = savedInstanceState.getBoolean(Constants.DETAIL_PICTURE_KEY, false);
			mHasSound = savedInstanceState.getBoolean(Constants.DETAIL_SOUND_KEY, false);
		}
	}
	
	protected void setDefinition() {
		if (mWebView == null) return;
		
		if (TextUtils.isEmpty(mDefinition)) {
			mWebView.loadUrl(Constants.URL_NOT_FOUND);
			return;
		}
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean usedUnicodeFix = sharedPreferences.getBoolean(Constants.PREFS_USED_UNICODE_FIX, true);
		
		String html = "<html>";
		html += "<head>";
		html += "<meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=yes, width=device-width\" />";
		html += "<meta content=\"" + Constants.MIME_TYPE + "; charset=" + Constants.ENCODING + "\" http-equiv=\"content-type\">";
		html += "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">";
		html += "<title>" + mTitle + "</title>";
		html += "</head>";
		html += "<body>";
		if (usedUnicodeFix) {
			html += Utility.ZawGyiDrawFix(mDefinition);
		} else {
			html += mDefinition;
		}
		html += "</html>";
	
		mWebView.loadDataWithBaseURL(Constants.URL_NOT_FOUND, html
				, Constants.MIME_TYPE, Constants.ENCODING, Constants.URL_DEFAULT);
	}
	
	protected void setImageBitmap() {
		Bitmap bitmap = null;
		if (mHasPicture && !TextUtils.isEmpty(mFileName)) {
				
			String picturePath = Constants.PICTURE_FOLDER + mFileName + ".png";
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
	
	public void setData(DataProvider dataProvider, long id) {
		if ((dataProvider == null) || (!dataProvider.isOpen())) return;
		if (id < 0) return;
		
		Cursor cursor = dataProvider.queryDefinition(id);
		if (!DataProvider.isNull(cursor, DataProvider.COLUMN_DEFINITION)) {
			
			if (!DataProvider.isNull(cursor, DataProvider.COLUMN_WORD)) {
				int wordCol = cursor.getColumnIndex(DataProvider.COLUMN_WORD);
				mWord = cursor.getString(wordCol);
			}
			
			if (!DataProvider.isNull(cursor, DataProvider.COLUMN_TITLE)) {
				int titleCol = cursor.getColumnIndex(DataProvider.COLUMN_TITLE);
				mTitle = cursor.getString(titleCol);
			}
			
			if (!DataProvider.isNull(cursor, DataProvider.COLUMN_DEFINITION)) {
				int definitionCol = cursor.getColumnIndex(DataProvider.COLUMN_DEFINITION);
				mDefinition = cursor.getString(definitionCol);
			}
			
			if (!DataProvider.isNull(cursor, DataProvider.COLUMN_FILENAME)) {
				int fileNameCol = cursor.getColumnIndex(DataProvider.COLUMN_FILENAME);
				mFileName = cursor.getString(fileNameCol);
			}
			
			int soundCol = cursor.getColumnIndex(DataProvider.COLUMN_SOUND);
			mHasSound = cursor.getShort(soundCol) == 1;
			
			int pictureCol = cursor.getColumnIndex(DataProvider.COLUMN_PICTURE);
			mHasPicture = cursor.getShort(pictureCol) == 1;
			setImageBitmap();
		} 
		
		if (mWebView != null) {
			mWebView.getSettings().setSupportZoom(true);
			mWebView.getSettings().setBuiltInZoomControls(true);
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
		return mWord;
	}
	
	public boolean getHasPicture() {
		return mHasPicture;
	}
	
	public boolean getHasSound() {
		if (ExpansionManager.isExpansionExists(getContext())) {
			return mHasSound;
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
		if (ExpansionManager.isExpansionExists(getContext())) {
			
			if (mHasSound) {
				char p = mFileName.charAt(0);
				String soundPath = Constants.SOUND_FOLDER + p + "/" + mFileName + ".wav";
				ExpansionManager.playSoundExpansion(getContext(), soundPath);
				return;
			}
			
		} else if ((mTextToSpeech != null) && mTextToSpeechEnabled) {
			
			if (!TextUtils.isEmpty(mWord)){
				try {
					mTextToSpeech.setLanguage(Locale.US);
					
					String text = mWord.charAt(0) == '-' ? mWord.substring(1) : mWord;
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
