package com.s16.engmyan.fragment;

import com.s16.data.CursorRecyclerViewAdapter;
import com.s16.engmyan.Constants;
import com.s16.engmyan.R;
import com.s16.engmyan.adapters.SearchListAdapter;
import com.s16.engmyan.data.DictionaryDataProvider;
import com.s16.engmyan.data.SearchQueryHelper;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;

public class MainListFragment extends Fragment 
		implements CursorRecyclerViewAdapter.OnItemClickListener {

	public interface OnListItemClickListener {
		public void onListItemClick(long id, CharSequence searchText);
	}
	
	private RecyclerView mRecyclerView;
	private CharSequence mSearchText;
	private SearchListAdapter mListAdapter;
	private OnListItemClickListener mOnListItemClickListener;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main_list, container, false);
		
		mRecyclerView = (RecyclerView)rootView.findViewById(R.id.listResult);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		
		return rootView;
	}
	
	@Override
	public void onItemClick(View view, Cursor cursor, int position, long id) {
		notifyItemClick(id, mSearchText);
	}
	
	public void setOnListItemClickListener(OnListItemClickListener listener) {
		mOnListItemClickListener = listener;
	}
	
	public void prepareSearch(CharSequence query) {
		if (mRecyclerView == null) return;
		
		Cursor cursor = null;
		if (!TextUtils.isEmpty(query)) {
			cursor = SearchQueryHelper.getInstance(getContext()).query(query);
		} else {
			cursor = SearchQueryHelper.getInstance(getContext()).querySuggestWord();
		}
		
		mListAdapter = new SearchListAdapter(getContext(), cursor, DictionaryDataProvider.COLUMN_WORD, 
				mRecyclerView, Constants.SEARCH_LIST_ITEM_LIMIT);
		mListAdapter.setFilterQueryProvider(new FilterQueryProvider() {
			
			@Override
			public Cursor runQuery(CharSequence constraint) {
				return SearchQueryHelper.getInstance(getContext()).query(constraint);
			}
		});
		mListAdapter.setOnItemClickListener(this);
		mRecyclerView.setAdapter(mListAdapter);
	}
	
	public void performSearch(CharSequence query) {
		if (mRecyclerView == null) return;
		if (mListAdapter == null) return;
		
		mSearchText = query;
		mListAdapter.getFilter().filter(query);
	}
	
	public boolean submitSearch(CharSequence query) {
		if (mRecyclerView == null) return false;
		if (mListAdapter == null) return false;
		
		mSearchText = query;
		if (TextUtils.isEmpty(query)) return false;
		
		boolean retVal = false;
		Cursor cursor = SearchQueryHelper.getInstance(getContext()).exactQuery(query.toString());
		if (cursor != null) {
			if (cursor.getCount() != 1) {
				cursor.close();
				retVal = false;
				
			} else {
				int colIdx = cursor.getColumnIndex(DictionaryDataProvider.COLUMN_ID);
				if (colIdx != -1) {
					long id = cursor.getInt(colIdx);
					notifyItemClick(id, query);
					retVal = true;
				}
			}
			cursor.close();
		}
		
		return retVal;
	}
	
	private void notifyItemClick(long id, CharSequence query) {
		if (mOnListItemClickListener != null) {
			mOnListItemClickListener.onListItemClick(id, query);
		}
	}
}
