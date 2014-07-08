package com.s16.engmyan.fragment;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.s16.engmyan.Constants;
import com.s16.engmyan.ExpansionManager;
import com.s16.engmyan.R;
import com.s16.engmyan.Utility;
import com.s16.engmyan.data.DictionaryItem;
import com.s16.widget.AnimatingRelativeLayout;
import com.s16.widget.LocalWebView;
import com.s16.widget.TouchImageView;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class DetailViewFragment extends Fragment 
		implements AnimatingRelativeLayout.AnimationCompleteListener {

	protected static String TAG = DetailViewFragment.class.getSimpleName();
	
	public interface DetailDataChangeListener {
		void onNavigationChanged(boolean navBackEnabled, boolean navForwardEnabled);
		DictionaryItem onLoadDetailData(long id, String word);
		void onLoadFinished();
	}
	
	private static boolean isDataLoading = false;
	private Context mContext;
	
	private TextToSpeech mTextToSpeech;
	private boolean mTextToSpeechEnabled;
	
	private RelativeLayout mLayoutWebView;
	private AnimatingRelativeLayout mLayoutImageView;
	private TouchImageView mImageView;
	private ImageView mImageCaution; 
	private LocalWebView mWebView;
	private View mProgressFrame; 
	private DictionaryItem mData;
	private DetailDataChangeListener mDetailDataChangeListener;
	
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
		
		mProgressFrame = view.findViewById(R.id.frameProgressIndeterminate);
		final ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.progressBar);
		progressBar.setIndeterminate(true);
		mProgressFrame.setVisibility(View.GONE);
		
		mWebView = (LocalWebView)view.findViewById(R.id.webViewDetail);
		mWebView.setLocalWebViewClient(new LocalWebView.LocalWebViewClient() {
			
			@Override
			public void onPageStarted(LocalWebView view, String url, Bitmap favicon) {
				isDataLoading = true;
				showProgress();
			}
			
			@Override
			public void onPageFinished(LocalWebView view, String url) {
	    		if (mDetailDataChangeListener != null) {
	    			mDetailDataChangeListener.onLoadFinished();
	    			mDetailDataChangeListener.onNavigationChanged(view.canGoBack(), view.canGoForward());
	    		}
				hideProgress();
				isDataLoading = false;
			}
			
			@Override
			public void onLoadResource(LocalWebView view, String url) {
			}
			
			@Override
			public boolean onAnchorClick(LocalWebView view, String url) {
				return setDefinition(view, url);
			}
		});
		
		mTextToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {

			@Override
			public void onInit(int status) {
				mTextToSpeechEnabled = (status == TextToSpeech.SUCCESS);
			} 
		});
		
		setImageBitmap();
		if (mData != null) {
			setDefinition(mWebView, mData);
		}
	}
	
	protected void showProgress() {
		if (mProgressFrame != null) {
			mProgressFrame.setVisibility(View.VISIBLE);
		}
	}
	
	protected void hideProgress() {
		if (mProgressFrame != null) {
			mProgressFrame.setVisibility(View.GONE);
		}
	}
	
	protected void setSaveInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mData = savedInstanceState.getParcelable(Constants.DETAIL_DATA_KEY);
		}
	}
	
	protected boolean getUsedUnicodeFix() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		return sharedPreferences.getBoolean(Constants.PREFS_USED_UNICODE_FIX, true);
	}
	
	protected boolean getUsedWordClickable() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		return sharedPreferences.getBoolean(Constants.PREFS_USED_WORD_CLICKABLE, true);
	}
	
	protected boolean getShowSynonym() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		return sharedPreferences.getBoolean(Constants.PREFS_SHOW_SYNONYM, true);
	}
	
	protected String getDefinitionHtml(DictionaryItem itemData) {
		if (itemData == null) return Constants.EMPTY_STRING;
		
		String html = "<html>";
		html += "<head>";
		html += "<meta content=\"" + Constants.MIME_TYPE + "; charset=" + Constants.ENCODING + "\" http-equiv=\"content-type\">";
		html += "<meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=yes, width=device-width\" />";
		html += "<meta name=\"Keywords\" content=\"\">";
		html += "<meta name=\"Options\" content=\"{'addfont':false, 'drawfix':false, 'applykeywords':false}\">";
		html += "<link rel=\"stylesheet\" type=\"text/css\" href=\"css/style.css\">";
		html += "<script type=\"text/javascript\" src=\"js/script.js\"></script>";
		if (TextUtils.isEmpty(itemData.title)) {
			html += "<title>Untitled</title>";
		} else {
			html += "<title>" + itemData.title + "</title>";
		}
		html += "</head>";
		html += "<body>";
		//html += "<a href=\"" + Constants.URL_DEFINITION + "?id=1\">Test</a>";
		
		CharSequence definition = itemData.definition;
		if (getUsedUnicodeFix()) {
			definition = Utility.ZawGyiDrawFix(itemData.definition);
		} 
		
		if (getUsedWordClickable() && (!TextUtils.isEmpty(itemData.keywords))) {
			if (itemData.keywords.toLowerCase().contains("word")) {
				definition = Utility.RegexReplace(definition, "([^A-Za-z\\/\\?=])(word)([^A-Za-z\\/\\?=])", 
						"$1<a href=\"" + Constants.URL_DEFINITION + "?word=word\">$2</a>$3", Pattern.CASE_INSENSITIVE);
			}
			Pattern pattern = Pattern.compile("[^,]+");
			Matcher m = pattern.matcher(itemData.keywords);
			while(m.find()) {
				if (m.group().equalsIgnoreCase("word")) continue;
				definition = Utility.RegexReplace(definition, "([^A-Za-z\\/\\?=])(" + m.group() + ")([^A-Za-z\\/\\?=])", 
						"$1<a href=\"" + Constants.URL_DEFINITION + "?word=" + m.group() + "\">$2</a>$3", Pattern.CASE_INSENSITIVE);
			}
		}
		html += definition;
		
		if (getShowSynonym() && (!TextUtils.isEmpty(itemData.synonym))) {
			html += "<hr />";
			html += "<h3>Synonym</h3>";
			html += "<p class=\"synonym\">";
			html += itemData.synonym;
			html += "</p>";
		}
		
		html += "</html>";
		return html;
	}
	
	protected DictionaryItem loadDictionaryItem(String url) {
		if (TextUtils.isEmpty(url)) return null;
		
		if (url.startsWith(Constants.URL_DEFINITION)) {
			Uri uri = Uri.parse(url);
			
			long id = -1;
			String queryParam = uri.getQueryParameter("id");
			if (!TextUtils.isEmpty(queryParam)) {
				id = Long.valueOf(queryParam);
			}
			queryParam = uri.getQueryParameter("word");
			
			if (mDetailDataChangeListener != null) {
				return mDetailDataChangeListener.onLoadDetailData(id, queryParam);
			}
		}
		return null;
	}
	
	protected boolean setDefinition(LocalWebView webView, String url) {
		if (webView == null) return false;
		DictionaryItem itemData = loadDictionaryItem(url);
		if (itemData != null) {
			mData = itemData;
			setDefinition(webView, itemData);
			return true;
		} else if (mData != null) {
			setDefinition(webView, mData);
			return true;
		}
		
		return false;
	}
	
	protected void setDefinition(LocalWebView webView, DictionaryItem itemData) {
		if (webView == null) return;
		
		if ((itemData == null) || (TextUtils.isEmpty(itemData.definition))) {
			webView.loadUrl(Constants.URL_NOT_FOUND);
			return;
		}
		
		final String newUrl = Constants.URL_DEFINITION + "?id=" + itemData.id;
		//String html = getDefinitionHtml(itemData);
		//webView.loadDataWithBaseURL(newUrl, html, Constants.MIME_TYPE, Constants.ENCODING, newUrl);
		
		final LocalWebView pWebView = webView;
		new AsyncTask<DictionaryItem, Void, String>() {

			@Override
			protected String doInBackground(DictionaryItem... params) {
				return getDefinitionHtml(params[0]);
			}
			
			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				
				pWebView.loadDataWithBaseURL(newUrl, result
						, Constants.MIME_TYPE, Constants.ENCODING, newUrl);
			}
		}.execute(itemData);
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
	
	public void setDetailDataChangeListener(DetailDataChangeListener listener) {
		mDetailDataChangeListener = listener;
	}
	
	public void setData(DictionaryItem itemData) {
		if (isDataLoading) return;
		if ((itemData == null) || (itemData.id < 0)) return;
		
		isDataLoading = true;
		mData = itemData;
		showProgress();
		setImageBitmap();
		setDefinition(mWebView, itemData);
	}
	
	public long getDetailId() {
		if (mData != null) return mData.id;
		return -1;
	}
	
	public boolean getCanGoBack() {
		if (mWebView == null) return false;
		return mWebView.canGoBack();
	}
	
	public boolean getCanGoForward() {
		if (mWebView == null) return false;
		return mWebView.canGoForward();
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
	
	public boolean performNavBack() {
		if (mWebView == null) return false;
		if (isDataLoading) return false;
		if (mWebView.canGoBack()) {
			mWebView.goBack();
			
			if (mDetailDataChangeListener != null) {
    			mDetailDataChangeListener.onNavigationChanged(mWebView.canGoBack(), mWebView.canGoForward());
    		}
			return true;
		}
		return false;
	}
	
	public boolean performNavForward() {
		if (mWebView == null) return false;
		if (isDataLoading) return false;
		if (mWebView.canGoForward()) {
			mWebView.goForward();
			
			if (mDetailDataChangeListener != null) {
    			mDetailDataChangeListener.onNavigationChanged(mWebView.canGoBack(), mWebView.canGoForward());
    		}
			return true;
		}
		return false;
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
