package com.s16.widget;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LocalWebView extends WebView {

	protected static final int LOAD_MODE_UNKNOWN = 0;
	protected static final int LOAD_MODE_URL = 1;
	protected static final int LOAD_MODE_POST = 2;
	protected static final int LOAD_MODE_DATA = 3;
	protected static final int LOAD_MODE_BASEURL = 4;
	protected static final int LOAD_MODE_ANCHOR = 5;
	
	public interface LocalWebViewClient {
		boolean onAnchorClick(LocalWebView view, String url);
		void onPageStarted(LocalWebView view, String url, Bitmap favicon);
		void onPageFinished(LocalWebView view, String url);
		void onLoadResource(LocalWebView view, String url);
	}
	
	protected LocalWebViewClient mLocalWebViewClient;
	
	private final WebViewClient mWebViewClient = 
			new WebViewClient() {
		
		@Override
    	public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return onAttackAnchor(url);
    	}
		
		@Override
    	public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if (mLocalWebViewClient != null) {
				mLocalWebViewClient.onPageStarted((LocalWebView)view, url, favicon);
			}
    	}
		
    	@Override
    	public void onPageFinished(WebView view, String url) {
    		if (mLocalWebViewClient != null) {
				mLocalWebViewClient.onPageFinished((LocalWebView)view, url);
			}
    	}
    	
    	@Override
    	public void onLoadResource(WebView view, String url) {
    		if (mLocalWebViewClient != null) {
				mLocalWebViewClient.onLoadResource((LocalWebView)view, url);
			}
        }
	};
	
	class LocalHistoryItem implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		public String Url;
		public String BaseUrl;
		public String MimeType;
		public String Encoding;
		public String DocumentData;
		public byte[] PostData;
		public int LoadMode;
		
		public LocalHistoryItem(String url) {
			Url = url;
		}
		
		public LocalHistoryItem(String url, byte[] postData) {
			Url = url;
			PostData = postData;
		}
		
		public LocalHistoryItem(String data, String mimeType, String encoding) {
			Url = data;
			DocumentData = data;
			MimeType = mimeType;
			Encoding = encoding;
		}
		
		public LocalHistoryItem(String baseUrl, String data,
	        String mimeType, String encoding, String historyUrl) {
			BaseUrl = baseUrl;
			DocumentData = data;
			MimeType = mimeType;
			Encoding = encoding;
			Url = historyUrl;
		}
	}
	
	class LocalBackForwardList implements Serializable {
		
		private static final long serialVersionUID = 1L;
		private int mCurrentIndex;
		private ArrayList<LocalHistoryItem> mArray;
		
		public LocalBackForwardList() {
			mCurrentIndex = -1;
			mArray = new ArrayList<LocalHistoryItem>();
		}
		
		LocalBackForwardList(int currentIndex, Object[] itemsArray) {
			this();
			mCurrentIndex = currentIndex;
			if (itemsArray != null) {
				for(Object item : itemsArray) {
					mArray.add((LocalHistoryItem)item);
				}
			}
		}
		
		public int getCurrentIndex() {
			return mCurrentIndex;
		}
		
		public LocalHistoryItem getItemAtIndex(int index) {
			if (index < 0 || index >= getSize()) {
	            return null;
	        }
	        return mArray.get(index);
		}
		
		public LocalHistoryItem getItemAtUrl(String url) {
			if (TextUtils.isEmpty(url)) return null;
			if (getSize() > 0) {
				LocalHistoryItem foundItem = null;
				for(int i = 0; i < getSize(); i++) {
					LocalHistoryItem item = mArray.get(i);
					if (TextUtils.isEmpty(item.Url)) continue;
					if (item.Url.equals(url)) {
						foundItem = item;
						break;
					}
				}
				return foundItem;
			}
			return null;
		}
		
		public LocalHistoryItem getCurrentItem() {
			return getItemAtIndex(mCurrentIndex);
		}
		
		public int getSize() {
			return mArray.size();
		}
		
		public boolean contains(String url) {
			LocalHistoryItem item = getItemAtUrl(url);
			return item != null;
		}
		
		public void addHistoryItem(LocalHistoryItem item) {
			//if (contains(item.Url)) return;
			++mCurrentIndex;
			final int size = mArray.size();
	        final int newPos = mCurrentIndex;
	        if (newPos != size) {
	            for (int i = size - 1; i >= newPos; i--) {
	                mArray.remove(i);
	            }
	        }
	        // Add the item to the list.
	        mArray.add(item);
		}
		
		public void removeHistoryItem(int index) {
			if (index < 0 || index >= getSize()) return;
			mArray.remove(index);
			mCurrentIndex--;
		}
		
		public void close() {
			mArray.clear();
	        mCurrentIndex = -1;
		}
		
		public boolean canGoBack() {
			return getSize() > 1 && mCurrentIndex > 0;
		}
		
		public boolean canGoForward() {
			return getSize() > 1 && mCurrentIndex < (getSize() - 1);
		}
		
		public boolean canGoBackOrForward(int steps) {
			if (getSize() > 0) {
				int index = mCurrentIndex + steps;
				return index > -1 && index < getSize();
			}
			return false;
		}
		
		public LocalHistoryItem goBack() {
			if (!canGoBack()) return null;
			mCurrentIndex--;
			return getCurrentItem();
		}
		
		public LocalHistoryItem goForward() {
			if (!canGoForward()) return null;
			mCurrentIndex++;
			return getCurrentItem();
		}
		
		public LocalHistoryItem goBackOrForward(int steps) {
			if (!canGoBackOrForward(steps)) return null;
			mCurrentIndex += steps;
			return getCurrentItem();
		}
	}
	
	static class SavedState extends BaseSavedState {
		int mCurrentIndex;
		Object[] mArray;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel source) {
            super(source);
            mCurrentIndex = source.readInt();
            mArray = source.readArray(LocalHistoryItem.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mCurrentIndex);
            dest.writeArray(mArray);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
	
	protected LocalBackForwardList mLocalBackForwardList;
	
	public LocalWebView(Context context) {
		super(context);
		initialize(context);
	}

	public LocalWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}
	
	public LocalWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}
	
	private void initialize(Context context) {
		if (isInEditMode()) {
			return;
		}
		
		mLocalBackForwardList = new LocalBackForwardList();
		setWebViewClient(mWebViewClient);
		setWebViewSettings();
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	protected void setWebViewSettings() {
		WebSettings webViewSettings = super.getSettings(); 
		webViewSettings.setAllowFileAccess(true);
		webViewSettings.setJavaScriptEnabled(true);
		webViewSettings.setSupportZoom(true);
		webViewSettings.setBuiltInZoomControls(true);
		
		if (Build.VERSION.SDK_INT >= 11) {
			webViewSettings.setDisplayZoomControls(false);
			webViewSettings.setAllowContentAccess(true);
		}
	}
	
	protected void LoadHistoryItem(LocalHistoryItem item) {
		if (item != null) {
			switch(item.LoadMode) {
			case LOAD_MODE_URL:
				super.loadUrl(item.Url);
				break;
			case LOAD_MODE_POST:
				super.postUrl(item.Url, item.PostData);
				break;
			case LOAD_MODE_DATA:
				super.loadData(item.DocumentData, item.MimeType, item.Encoding);
				break;
			case LOAD_MODE_BASEURL:
				super.loadDataWithBaseURL(item.BaseUrl, item.DocumentData, item.MimeType, item.MimeType, item.Url);
				break;
			default:
				break;
			}
		}
	}
	
	protected boolean onAttackAnchor(String url) {
		LocalHistoryItem item;
		if (mLocalWebViewClient != null) {
			if (mLocalWebViewClient.onAnchorClick(this, url)) {
				return true;	
			}
			item = mLocalBackForwardList.getCurrentItem();
			if (item != null) {
				LoadHistoryItem(item);
				return true;
			}
		}
		
		item = mLocalBackForwardList.getItemAtUrl(url);
		if (item != null) {
			LoadHistoryItem(item);
			return true;
		}
		return false;
	}
	
	public void setLocalWebViewClient(LocalWebViewClient client) {
		mLocalWebViewClient = client;
	}
	
	@Override
    public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        if (mLocalBackForwardList != null) {
        	ss.mCurrentIndex = mLocalBackForwardList.getCurrentIndex();
        	ss.mArray = mLocalBackForwardList.mArray.toArray();
        }
        return ss;
	}
	
	@Override
    public void onRestoreInstanceState(Parcelable state) {
		SavedState ss = (SavedState)state;
        super.onRestoreInstanceState(ss.getSuperState());
        if (ss.mArray != null) {
        	mLocalBackForwardList = new LocalBackForwardList(ss.mCurrentIndex, ss.mArray);
        	reload();
        }
	}
	
	@Override
	public void reload() {
		//super.reload();
		LocalHistoryItem item = mLocalBackForwardList.getCurrentItem();
		LoadHistoryItem(item);
	}
	
	@Override
	public boolean canGoBack() {
		return mLocalBackForwardList.canGoBack();
	}
	
	@Override
	public void goBack() {
		//super.goBack();
		LocalHistoryItem item = mLocalBackForwardList.goBack();
		LoadHistoryItem(item);
	}
	
	@Override
	public boolean canGoForward() {
		return mLocalBackForwardList.canGoForward();
	}
	
	@Override
	public void goForward() {
		//super.goForward();
		LocalHistoryItem item = mLocalBackForwardList.goForward();
		LoadHistoryItem(item);
	}
	
	@Override
	public boolean canGoBackOrForward(int steps) {
		return mLocalBackForwardList.canGoBackOrForward(steps);
	}
	
	@Override
	public void goBackOrForward(int steps) {
		//super.goBackOrForward(steps);
		LocalHistoryItem item = mLocalBackForwardList.goBackOrForward(steps);
		LoadHistoryItem(item);
	}
	
	@Override
	public void clearHistory() {
		super.clearHistory();
		mLocalBackForwardList.close();
	}
	
	@Deprecated
	@Override
	public WebBackForwardList copyBackForwardList() {
		return super.copyBackForwardList();
	}
	
	@Override
	public WebBackForwardList saveState(Bundle outState) {
		return super.saveState(outState);
	}
	
	@Override
	public WebBackForwardList restoreState(Bundle inState) {
		return super.restoreState(inState);
	}
	
	@Override
	public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
		super.loadUrl(url, additionalHttpHeaders);
		
		LocalHistoryItem item = new LocalHistoryItem(url);
		item.LoadMode = LOAD_MODE_URL;
		mLocalBackForwardList.addHistoryItem(item);
	}
	
	@Override
	public void loadUrl(String url) {
		super.loadUrl(url);
		LocalHistoryItem item = new LocalHistoryItem(url);
		item.LoadMode = LOAD_MODE_URL;
		mLocalBackForwardList.addHistoryItem(item);
	}
	
	@Override
	public void postUrl(String url, byte[] postData) {
		super.postUrl(url, postData);
		LocalHistoryItem item = new LocalHistoryItem(url, postData);
		item.LoadMode = LOAD_MODE_POST;
		mLocalBackForwardList.addHistoryItem(item);
	}

	@Override
	public void loadData(String data, String mimeType, String encoding) {
		super.loadData(data, mimeType, encoding);
		LocalHistoryItem item = new LocalHistoryItem(data, mimeType, encoding);
		item.LoadMode = LOAD_MODE_DATA;
		mLocalBackForwardList.addHistoryItem(item);
	}
	
	@Override
	public void loadDataWithBaseURL(String baseUrl, String data,
            String mimeType, String encoding, String historyUrl) {
		super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
		LocalHistoryItem item = new LocalHistoryItem(baseUrl, data, mimeType, encoding, historyUrl);
		item.LoadMode = LOAD_MODE_BASEURL;
		mLocalBackForwardList.addHistoryItem(item);
	}
}
