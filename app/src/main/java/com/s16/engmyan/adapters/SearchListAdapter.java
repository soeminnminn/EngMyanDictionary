package com.s16.engmyan.adapters;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.s16.data.CursorRecyclerViewAdapter;
import com.s16.data.RecyclerViewHolder;
import com.s16.engmyan.R;


public class SearchListAdapter extends CursorRecyclerViewAdapter<RecyclerViewHolder> {

	protected static final String TAG = SearchListAdapter.class.getSimpleName();
	
	private final int VIEW_ITEM = 1;
	private final int VIEW_PROG = 0;
	
	protected final String mDisplayColumnName;
	protected int mDisplayColumn;

	private final int mPageLimit;
	private int mItemCount = 0;
	private int mVisibleThreshold = 5;
	private boolean mLoading;
	private final Handler mHandler;
	
	public SearchListAdapter(Context context, Cursor cursor, String displayColumn, RecyclerView recyclerView, int pageLimit) {
		super(context, cursor, false);
		
		mDisplayColumnName = displayColumn;
		mDisplayColumn = (cursor != null) ? cursor.getColumnIndexOrThrow(mDisplayColumnName) : -1;
		
		mPageLimit = pageLimit;
		mItemCount = pageLimit;
		
		mHandler = new Handler();
		if (recyclerView != null && recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
			final LinearLayoutManager linearLayoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
			recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
				
				@Override
				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
					super.onScrolled(recyclerView, dx, dy);
					int totalItemCount = linearLayoutManager.getItemCount();
					int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
					if (!mLoading && mPageLimit > 0 && totalItemCount <= (lastVisibleItem + mVisibleThreshold)) {
						mLoading = true;
						postNotifyDataSetChanged();
					}
			     }
			  });
		}
	}
	
	private void postNotifyDataSetChanged() {
		if (!mDataValid) return;
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				mItemCount += mPageLimit;
				notifyDataSetChanged();
				mLoading = false;
			}
		}, 1000);
	}
	
	@Override
	public void changeCursor(Cursor cursor) {
		mItemCount = mPageLimit;
		super.changeCursor(cursor);
	}
	
	@Override
	public Cursor swapCursor(Cursor newCursor) {
		mItemCount = mPageLimit;
		return super.swapCursor(newCursor);
	}
	
	@Override
	public int getItemCount() {
		if (mDataValid) {
			int dataCount = super.getItemCount();
			if (dataCount > 0) {
				if (mItemCount > 0 && mItemCount < dataCount) {
					return mItemCount + 1;
				}
				return dataCount;
			}
        }
		return 0;
	}
	
	@Override
	public int getItemViewType(int position) {
		return mItemCount <= position ? VIEW_PROG : VIEW_ITEM;
	}

	@Override
	public RecyclerViewHolder onCreateViewHolder(LayoutInflater inflater,
			ViewGroup parent, int viewType) {
		if (viewType == VIEW_PROG) {
			return new RecyclerViewHolder(inflater.inflate(R.layout.list_item_progress, parent, false), viewType);	
		}
		return new RecyclerViewHolder(inflater.inflate(R.layout.list_item_simple_divider, parent, false), viewType);
	}

	@Override
	public void onBindViewHolder(Context context,
			RecyclerViewHolder viewHolder, Cursor cursor) {
		
		if (viewHolder != null) {
			if (viewHolder.getItemViewType() == VIEW_ITEM && cursor != null) {
				TextView text1 = (TextView)viewHolder.findViewById(android.R.id.text1);
				if (mDisplayColumn != -1) {
					text1.setText(cursor.getString(mDisplayColumn));
				}
			}
		}
	}
	
}