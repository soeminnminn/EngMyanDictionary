package com.s16.engmyan.data;

import com.s16.data.CursorAdapter;
import com.s16.engmyan.R;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class SearchListAdapter extends CursorAdapter implements OnScrollListener {
	
	protected static final String TAG = SearchListAdapter.class.getSimpleName();
	
	protected final String mDisplayColumnName;
	protected int mDisplayColumn;
	
	protected final int mPageLimit;
	protected int mItemCount = 0;
	protected int mCurrentScrollState;
	
	public SearchListAdapter(Context context, Cursor c, String valueColumn, String displayColumn, int pageLimit) {
		super(context, c, valueColumn, true);
		mDisplayColumnName = displayColumn;
		mPageLimit = pageLimit;
		mItemCount = pageLimit;
		mDisplayColumn = (c != null) ? c.getColumnIndexOrThrow(mDisplayColumnName) : -1;
	}
	
	@Override
	public int getCount() {
		if (mDataValid) {
			int dataCount = super.getCount();
			if (dataCount > 0) {
				if (mItemCount == 0) return dataCount;
				return Math.min(dataCount, mItemCount);
			}
        }
		return 0;
	}
	
	public boolean hasMoreData() {
		if (!mDataValid) return false;
		if (mItemCount == 0) return false;
		int dataCount = super.getCount();
		if (dataCount == 0) return false;
		return dataCount > mItemCount;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if ( scrollState == OnScrollListener.SCROLL_STATE_IDLE ) {
      		view.invalidateViews();
    	}
    	
		mCurrentScrollState = scrollState;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		
		boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;
		if (loadMore && mCurrentScrollState != SCROLL_STATE_IDLE) {
			if ((mPageLimit > 0) && (mItemCount < super.getCount())) {
				mItemCount += mPageLimit;
				notifyDataSetChanged();
				view.invalidateViews();
			}
		}
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView textViewItemTitle = (TextView)view.findViewById(android.R.id.text1);
		textViewItemTitle.setText(cursor.getString(mDisplayColumn));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		View view = layoutInflater.inflate(R.layout.simple_list_item, viewGroup, false);
		return view;
	}
}
