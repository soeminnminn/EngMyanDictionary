package com.s16.engmyan.fragment;

import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.s16.engmyan.Constants;
import com.s16.engmyan.DefinitionViewHelper;
import com.s16.engmyan.ExpansionManager;
import com.s16.engmyan.R;
import com.s16.engmyan.data.DictionaryItem;
import com.s16.widget.AnimatingRelativeLayout;
import com.s16.widget.TouchImageView;

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
	
	private RelativeLayout mLayoutDefinitionView;
	private AnimatingRelativeLayout mLayoutImageView;
	private TouchImageView mImageView;
	private ImageView mImageCaution; 
	private DefinitionViewHelper mDefinitionView;
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
    public void onStop() {
		super.onStop();
		clear();
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		clear();
	}
	
	private void initialize(View view) {
		mLayoutDefinitionView = (RelativeLayout)view.findViewById(R.id.layoutDefinitionView);
		
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
		
		mDefinitionView = new DefinitionViewHelper((TextView)(mLayoutDefinitionView.findViewById(R.id.textViewDetails)), (ViewGroup)(mLayoutDefinitionView.findViewById(R.id.layoutSynonymView)));
		mDefinitionView.setDefinitionViewClient(new DefinitionViewHelper.DefinitionViewClient() {
			
			@Override
			public void onPageStarted(DefinitionViewHelper helper, String url, DictionaryItem definition) {
				isDataLoading = true;
				showProgress();
			}
			
			@Override
			public void onPageFinished(DefinitionViewHelper helper, String url, DictionaryItem definition) {
				onPageLoaded(url, helper.canGoBack(), helper.canGoForward());
				hideProgress();
				isDataLoading = false;
			}
			
			@Override
			public void onLoadResource(DefinitionViewHelper helper, String url, DictionaryItem definition) {
			}
			
			@Override
			public boolean onAnchorClick(DefinitionViewHelper helper, String url, DictionaryItem definition) {
				return setDefinition(helper, url);
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
			mDefinitionView.setDefinition(mData);
		}
	}
	
	private void clear() {
		if (mTextToSpeech != null) {
			try {
				mTextToSpeech.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				mTextToSpeech.shutdown();
				mTextToSpeech = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
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
	
	protected void onPageLoaded(String url, boolean navBackEnabled, boolean navForwardEnabled) {
		DictionaryItem item = loadDictionaryItem(url);
		if (item != null && (mData == null || mData.id != item.id)) {
			mData = item;
		}
		
		if (mDetailDataChangeListener != null) {
			mDetailDataChangeListener.onLoadFinished();
			mDetailDataChangeListener.onNavigationChanged(navBackEnabled, navForwardEnabled);
		}
	}
	
	protected void setSaveInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mData = savedInstanceState.getParcelable(Constants.DETAIL_DATA_KEY);
		}
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
			queryParam = uri.getQueryParameter("w");
			
			if (mDetailDataChangeListener != null) {
				return mDetailDataChangeListener.onLoadDetailData(id, queryParam);
			}
		}
		return null;
	}
	
	protected boolean setDefinition(DefinitionViewHelper helper, String url) {
		if (helper == null) return false;
		DictionaryItem itemData = loadDictionaryItem(url);
		if (itemData != null) {
			mData = itemData;
			helper.setDefinition(itemData);
			return true;
		} else if (mData != null) {
			helper.setDefinition(mData);
			return true;
		}
		
		return false;
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
		mDefinitionView.setDefinition(itemData);
	}
	
	public long getDetailId() {
		if (mData != null) return mData.id;
		return -1;
	}
	
	public boolean getCanGoBack() {
		if (mDefinitionView == null) return false;
		return mDefinitionView.canGoBack();
	}
	
	public boolean getCanGoForward() {
		if (mDefinitionView == null) return false;
		return mDefinitionView.canGoForward();
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
		if (mDefinitionView == null) return false;
		if (isDataLoading) return false;
		if (mDefinitionView.canGoBack()) {
			mDefinitionView.goBack();
			
			if (mDetailDataChangeListener != null) {
    			mDetailDataChangeListener.onNavigationChanged(mDefinitionView.canGoBack(), mDefinitionView.canGoForward());
    		}
			return true;
		}
		return false;
	}
	
	public boolean performNavForward() {
		if (mDefinitionView == null) return false;
		if (isDataLoading) return false;
		if (mDefinitionView.canGoForward()) {
			mDefinitionView.goForward();
			
			if (mDetailDataChangeListener != null) {
    			mDetailDataChangeListener.onNavigationChanged(mDefinitionView.canGoBack(), mDefinitionView.canGoForward());
    		}
			return true;
		}
		return false;
	}
	
	public void toggleImageView() {
		if (mLayoutDefinitionView == null) return;
		if (mLayoutImageView == null) return;
		
		if (mLayoutImageView.isVisible()) {
			mLayoutImageView.hide();
		} else {
			mLayoutImageView.show();
		}
	}
	
	@SuppressWarnings("deprecation")
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
		if (mLayoutDefinitionView == null) return;
		if (mLayoutImageView == null) return;
		
		if (mLayoutImageView.isVisible()) {
			mLayoutDefinitionView.setVisibility(View.GONE);
		} else {
			mLayoutDefinitionView.setVisibility(View.VISIBLE);
		}
	}
}
