package com.s16.engmyan.fragment;

import com.s16.engmyan.Constants;
import com.s16.engmyan.R;
import com.s16.engmyan.Utility;
import com.s16.engmyan.data.DictionaryDataProvider;
import com.s16.engmyan.data.SearchListAdapter;
import com.s16.widget.SearchBarView;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;

public class SearchListFragment extends Fragment
		implements OnItemClickListener {
	
	public interface OnSearchListItemClickListener {
		public void onSearchListItemClick(long id, CharSequence searchText);
	}
	
	protected static String TAG = SearchListFragment.class.getSimpleName();
	
	private Context mContext;
	private DictionaryDataProvider mDataProvider;
	private SearchBarView mTextSearch;
	private ListView mResultList;
	private CharSequence mSearchText;
	private OnSearchListItemClickListener mOnSearchListItemClickListener;
	
	public SearchListFragment() {
		super();
	}
	
	public SearchListFragment(Context context, DictionaryDataProvider dataProvider) {
		super();
		mContext = context;
		mDataProvider = dataProvider;
	}
	
	protected Context getContext() {
		return mContext;
	}
	
	public DictionaryDataProvider getDataProvider() {
		return mDataProvider;
	}
	
	public void setDataProvider(DictionaryDataProvider dataProvider) {
		mDataProvider = dataProvider;
	}
	
	public void setSearchText(String text) {
		mSearchText = text;
		if (mTextSearch != null) {
			mTextSearch.setText(text);
		}
	}
	
	public String getSearchText() {
		if (mSearchText != null) {
			return mSearchText.toString();
		}
		if (mTextSearch != null) {
			return mTextSearch.getText().toString(); 
		}
		return "";
	}
	
	public TextView getSearchTextView() {
		if (mTextSearch != null) {		
			return mTextSearch.getTextView();
		}
		return null;
	}
	
	public View getSearchView() {
		return mTextSearch;
	}
	
	public void setSelection() {
		if (mTextSearch != null) {
			CharSequence constraint = mTextSearch.getText();
			int index = TextUtils.isEmpty(constraint) ? 0 : constraint.length();
			mTextSearch.setSelection(index);
			mTextSearch.requestFocus();
		}
	}
	
	public void setOnSearchListItemClickListener(OnSearchListItemClickListener listener) {
		mOnSearchListItemClickListener = listener;
	}
	
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.search_list_fragment, container, false);
		if (mContext == null) {
			mContext = inflater.getContext();
		}
		
		initialize(view);
		return view;
	}
	
	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mTextSearch != null) {
			CharSequence constraint = mTextSearch.getText();
			onItemSelected(id, constraint);
		}
	}

	private void initialize(View view) {
		
		mTextSearch = (SearchBarView)view.findViewById(R.id.searchBarView);
		mTextSearch.setHint(R.string.search_hint);
		mTextSearch.setSaveEnabled(true);
		mTextSearch.setOnQueryTextListener(new SearchBarView.OnQueryTextListener() {
			
			@Override
			public void onQueryTextChanged(CharSequence query, int count) {
				performSearch(query);
			}
			
			@Override
			public boolean onQuerySubmit(CharSequence query) {
				return submitSearch(query);
			}
		});
		mTextSearch.requestFocus();
		
		mResultList = (ListView)view.findViewById(R.id.listViewResult);
		mResultList.setSmoothScrollbarEnabled(true);
		mResultList.setFocusable(false);
		mResultList.setSaveEnabled(true);
	}
	
	public void clearText() {
		if (mTextSearch != null) {
			mTextSearch.clearText();
		}
	}
	
	public void setEnabled(boolean enabled) {
		View view = getView();
		if (view != null) {
			if (mTextSearch != null) mTextSearch.setEnabled(enabled);
			if (mResultList != null) mResultList.setEnabled(enabled);
			view.setEnabled(enabled);
		}
	}
	
	public boolean isEnabled() {
		View view = getView();
		if (view != null) {
			return view.isEnabled();
		}
		return true;
	}
	
	public void prepareSearch() {
		if ((mDataProvider != null) && (mDataProvider.isOpen())) {
			Cursor cursor = null;
			CharSequence searchText = mTextSearch.getText();
			if(!TextUtils.isEmpty(searchText)) {
				cursor = mDataProvider.query(searchText.toString());
			} else {
				cursor = mDataProvider.querySuggestWord();
			}
			
			SearchListAdapter listAdapter = new SearchListAdapter(getContext(), cursor, 
					DictionaryDataProvider.COLUMN_ID, DictionaryDataProvider.COLUMN_WORD, Constants.SEARCH_LIST_ITEM_LIMIT);
			listAdapter.setFilterQueryProvider(new FilterQueryProvider() {
		         public Cursor runQuery(CharSequence constraint) {
		             return mDataProvider.query(constraint.toString());
		         }
			});
			mResultList.setAdapter(listAdapter);
			mResultList.setOnScrollListener(listAdapter);
			mResultList.setOnItemClickListener(this);
		}
	}
	
	protected void onItemSelected(long id, CharSequence query) {
		if (mOnSearchListItemClickListener != null) {
			mOnSearchListItemClickListener.onSearchListItemClick(id, query);
		}
	}
	
	public boolean performSearch(CharSequence query) {
		if (mResultList == null) return false;
		if (mDataProvider == null) return false;
		if (TextUtils.isEmpty(query)) return false;
		
		SearchListAdapter listAdapter = (SearchListAdapter)mResultList.getAdapter();
		if (listAdapter != null) {
			listAdapter.getFilter().filter(query);
			mSearchText = query;
			return true;
		}
		
		return false;
	}
	
	public boolean submitSearch(CharSequence query) {
		if (mResultList == null) return false;
		if (mDataProvider == null) return false;
		if (TextUtils.isEmpty(query)) return false;
		
		Cursor cursor = mDataProvider.exactQuery(query.toString());
		if (cursor != null) {
			if (cursor.getCount() != 1) {
				cursor.close();
				return false;	
			}
			
			if (!Utility.isNull(cursor, DictionaryDataProvider.COLUMN_ID)) {
				int colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_ID);
				long id = cursor.getInt(colIdx);
				onItemSelected(id, query);
				cursor.close();
				return true;
			}
			cursor.close();
		}
		
		return false;
	}
}
