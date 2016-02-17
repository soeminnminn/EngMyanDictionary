package com.s16.engmyan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.s16.engmyan.data.DictionaryItem;

public class DefinitionViewHelper {

	public interface DefinitionViewClient {
		void onPageStarted(DefinitionViewHelper helper, String url, DictionaryItem definition);
		void onPageFinished(DefinitionViewHelper helper, String url, DictionaryItem definition);
		void onLoadResource(DefinitionViewHelper helper, String url, DictionaryItem definition);
		boolean onAnchorClick(DefinitionViewHelper helper, String url, DictionaryItem definition);
	}
	
	private static class DefinitionData {
		private String url;
		private DictionaryItem data;
		public CharSequence definition;
		public CharSequence synonym;
		
		public boolean isEmpty() {
			return TextUtils.isEmpty(definition);
		}
	}
	
	static class HistoryItem implements Serializable, Parcelable {

		private static final long serialVersionUID = 1L;
		
		private String mUrl;
		private DictionaryItem mDefinition;
		
		public static final Parcelable.Creator<HistoryItem> CREATOR
		        = new Parcelable.Creator<HistoryItem>() {
		    public HistoryItem createFromParcel(Parcel in) {
		        return new HistoryItem(in);
		    }
		
		    public HistoryItem[] newArray(int size) {
		        return new HistoryItem[size];
		    }
		};
		
		private HistoryItem(Parcel in) {
			if (in != null) {
				mUrl = in.readString();
				mDefinition = in.readParcelable(DictionaryItem.class.getClassLoader());
			}
		}
		
		public HistoryItem(String url, DictionaryItem definition) {
			mUrl = url;
			mDefinition = definition;
		}
		
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(mUrl);
			dest.writeParcelable(mDefinition, PARCELABLE_WRITE_RETURN_VALUE);
		}
		
		public String getUrl() {
			return mUrl;
		}
		
		public DictionaryItem getDefinition() {
			return mDefinition;
		}
		
	}
	
	class BackForwardList implements Serializable {
		private static final long serialVersionUID = 1L;
		private int mCurrentIndex;
		private List<HistoryItem> mArray;
		
		public BackForwardList() {
			mCurrentIndex = -1;
			mArray = new ArrayList<HistoryItem>();
		}
		
		BackForwardList(int currentIndex, Object[] itemsArray) {
			this();
			mCurrentIndex = currentIndex;
			if (itemsArray != null) {
				for(Object item : itemsArray) {
					mArray.add((HistoryItem)item);
				}
			}
		}
		
		public int getCurrentIndex() {
			return mCurrentIndex;
		}
		
		public HistoryItem getItemAtIndex(int index) {
			if (index < 0 || index >= getSize()) {
	            return null;
	        }
	        return mArray.get(index);
		}
		
		public HistoryItem getItemAtUrl(String url) {
			if (TextUtils.isEmpty(url)) return null;
			if (getSize() > 0) {
				HistoryItem foundItem = null;
				for(int i = 0; i < getSize(); i++) {
					HistoryItem item = mArray.get(i);
					if (TextUtils.isEmpty(item.getUrl())) continue;
					if (item.getUrl().equals(url)) {
						foundItem = item;
						break;
					}
				}
				return foundItem;
			}
			return null;
		}
		
		public HistoryItem getCurrentItem() {
			return getItemAtIndex(mCurrentIndex);
		}
		
		public int getSize() {
			return mArray.size();
		}
		
		public boolean contains(String url) {
			HistoryItem item = getItemAtUrl(url);
			return item != null;
		}
		
		public void addHistoryItem(HistoryItem item) {
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
		
		public HistoryItem goBack() {
			if (!canGoBack()) return null;
			mCurrentIndex--;
			return getCurrentItem();
		}
		
		public HistoryItem goForward() {
			if (!canGoForward()) return null;
			mCurrentIndex++;
			return getCurrentItem();
		}
		
		public HistoryItem goBackOrForward(int steps) {
			if (!canGoBackOrForward(steps)) return null;
			mCurrentIndex += steps;
			return getCurrentItem();
		}
	}
	
	static class SavedState implements Parcelable {
		int mCurrentIndex;
		Object[] mArray;

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        SavedState() {
        }

        private SavedState(Parcel source) {
            mCurrentIndex = source.readInt();
            mArray = source.readArray(HistoryItem.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mCurrentIndex);
            dest.writeArray(mArray);
        }

		@Override
		public int describeContents() {
			return 0;
		}
    }
	
	static class LocalLinkMovementMethod extends LinkMovementMethod {
		
		private final DefinitionViewHelper mViewHelper;
		private final DictionaryItem mItemData;
		private final DefinitionViewClient mClient;
		
		public LocalLinkMovementMethod(DefinitionViewHelper viewHelper, DefinitionViewClient client, DictionaryItem itemData) {
			mViewHelper = viewHelper;
			mClient = client;
			mItemData = itemData;
		}
		
		@Override
		public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
			int action = event.getAction();
		    if (action == MotionEvent.ACTION_UP) {
		    	int x = (int) event.getX();
		        int y = (int) event.getY();
		        x -= widget.getTotalPaddingLeft();
		        y -= widget.getTotalPaddingTop();
		        x += widget.getScrollX();
		        y += widget.getScrollY();

		        Layout layout = widget.getLayout();
		        int line = layout.getLineForVertical(y);
		        int off = layout.getOffsetForHorizontal(line, x);

		        URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
		        if (link.length != 0 && mClient != null) {
		        	Log.i("LocalLinkMovementMethod", link[0].getURL());
		        	mClient.onAnchorClick(mViewHelper, link[0].getURL(), mItemData);
		        	return true;
		        }
		    }
			return false;
		}
	}
	
	private final Context mContext;
	private final TextView mTextDefinition;
	private final ViewGroup mViewSynonym;
	private BackForwardList mBackForwardList;
	private DefinitionViewClient mDefinitionViewClient;
	
	public DefinitionViewHelper(TextView definition, ViewGroup synonym) {
		mContext = definition.getContext();
		mTextDefinition = definition;
		mViewSynonym = synonym;
		mBackForwardList = new BackForwardList();
		setLoadingState();
	}
	
	protected Context getContext() {
		return mContext;
	}
	
	protected void setLoadingState() {
		mTextDefinition.setText(R.string.loading_text);
		mViewSynonym.setVisibility(View.GONE);
	}
	
	protected boolean isUsedUnicodeFix() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		return sharedPreferences.getBoolean(Constants.PREFS_USED_UNICODE_FIX, false);
	}
	
	protected boolean isUsedWordClickable() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		return sharedPreferences.getBoolean(Constants.PREFS_USED_WORD_CLICKABLE, true);
	}
	
	protected boolean isShowSynonym() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		return sharedPreferences.getBoolean(Constants.PREFS_SHOW_SYNONYM, true);
	}
	
	protected void loadHistoryItem(HistoryItem historyItem) {
		loadDataAsync(historyItem.getUrl(), historyItem.getDefinition());
	}
	
	protected void loadDataAsync(final String url, final DictionaryItem definitionData) {
		
		new AsyncTask<DictionaryItem, Void, DefinitionData>() {

			@Override
			protected DefinitionData doInBackground(DictionaryItem... params) {
				return buildData(url, params[0]);
			}
			
			@Override
			protected void onPreExecute() {
				if (mDefinitionViewClient != null) {
					mDefinitionViewClient.onPageStarted(DefinitionViewHelper.this, url, definitionData);
				}
			};
			
			@Override
			protected void onPostExecute(DefinitionData result) {
				if (result != null && !result.isEmpty()) {
					loadData(result);
					if (mDefinitionViewClient != null) {
						mDefinitionViewClient.onPageFinished(DefinitionViewHelper.this, result.url, result.data);
					}
				}
			}
						
		}.execute(definitionData);
	}
	
	protected DefinitionData buildData(String url, final DictionaryItem itemData) {
		DefinitionData data = new DefinitionData();
		data.url = url;
		data.data = itemData;
		
		CharSequence definition = itemData.definition;
		if (isUsedUnicodeFix()) {
			definition = Utility.ZawGyiDrawFix(itemData.definition);
		} 
		
		if (isUsedWordClickable() && (!TextUtils.isEmpty(itemData.keywords))) {
			Pattern pattern = Pattern.compile("[^,]+");
			Matcher m = pattern.matcher(itemData.keywords);
			while(m.find()) {
				definition = Utility.RegexReplace(definition, "([^A-Za-z\\/\\?=])(" + m.group() + ")([^A-Za-z\\/\\?=])", 
						"$1<a href=\"#?w=" + m.group() + "\">$2</a>$3", Pattern.CASE_INSENSITIVE);
			}
			
			definition = definition.toString().replaceAll("\\#\\?", Constants.URL_DEFINITION + "?");
		}
		data.definition = Html.fromHtml(definition.toString());
		
		if (isShowSynonym() && (!TextUtils.isEmpty(itemData.synonym))) {
			data.synonym = Html.fromHtml(itemData.synonym);
		} else {
			data.synonym = null;
		}
		
		return data;
	}
	
	protected void loadData(DefinitionData definition) {
		if (definition != null && !definition.isEmpty()) {
			mTextDefinition.setTypeface(Constants.getZawgyiTypeface(getContext()));
			mTextDefinition.setText(definition.definition);
			mTextDefinition.setLinksClickable(true);
			mTextDefinition.setMovementMethod(new LocalLinkMovementMethod(this, mDefinitionViewClient, definition.data));
			
			if (!TextUtils.isEmpty(definition.synonym)) {
				TextView textSynonym = (TextView)mViewSynonym.findViewById(R.id.textViewSynonym);
				textSynonym.setText(definition.synonym);
				textSynonym.setLinksClickable(true);
				textSynonym.setMovementMethod(new LocalLinkMovementMethod(this, mDefinitionViewClient, definition.data));
				mViewSynonym.setVisibility(View.VISIBLE);
			} else {
				mViewSynonym.setVisibility(View.GONE);
			}
		}
	}
	
	public void setDefinition(DictionaryItem definitionData) {
		if (definitionData == null) return;
		
		setLoadingState();
		
		final String newUrl = Constants.URL_DEFINITION + "?id=" + definitionData.id;
		loadDataAsync(newUrl, definitionData);
		
		HistoryItem historyItem = new HistoryItem(newUrl, definitionData);
		mBackForwardList.addHistoryItem(historyItem);
	}
	
	public void setDefinitionViewClient(DefinitionViewClient client) {
		mDefinitionViewClient = client;
	}
	
	public void reload() {
		HistoryItem item = mBackForwardList.getCurrentItem();
		loadHistoryItem(item);
	}
	
	public boolean canGoBack() {
		return mBackForwardList.canGoBack();
	}
	
	public void goBack() {
		HistoryItem item = mBackForwardList.goBack();
		loadHistoryItem(item);
	}
	
	public boolean canGoForward() {
		return mBackForwardList.canGoForward();
	}
	
	public void goForward() {
		HistoryItem item = mBackForwardList.goForward();
		loadHistoryItem(item);
	}
	
	public boolean canGoBackOrForward(int steps) {
		return mBackForwardList.canGoBackOrForward(steps);
	}
	
	public void goBackOrForward(int steps) {
		HistoryItem item = mBackForwardList.goBackOrForward(steps);
		loadHistoryItem(item);
	}
	
	public void clearHistory() {
		mBackForwardList.close();
	}
	
	public Parcelable saveState() {
		SavedState savedState = new SavedState();
		savedState.mCurrentIndex = mBackForwardList.getCurrentIndex();
		savedState.mArray = mBackForwardList.mArray.toArray();
		return savedState;
	}
	
	public void restoreState(Parcelable state) {
		SavedState savedState = (SavedState)state;
		if (savedState != null && savedState.mArray != null) {
			mBackForwardList = new BackForwardList(savedState.mCurrentIndex, savedState.mArray); 
		}
	}
}
