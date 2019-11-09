package com.s16.data;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class RecyclerViewHolder extends RecyclerView.ViewHolder {
	
	private final int mViewType;
	private long mDataId;
	
	public RecyclerViewHolder(View itemView) {
		super(itemView);
		mViewType = 0;
	}
	
	public RecyclerViewHolder(View itemView, int viewType) {
		super(itemView);
		mViewType = viewType;
	}
	
	public View getItemView() {
		return itemView;
	}
	
	public View findViewById(int id) {
		if (itemView != null) {
			return itemView.findViewById(id);
		}
		return null;
	}

	public int getViewType() {
		return mViewType;
	}

	public long getDataId() {
		return mDataId;
	}

	public void setDataId(long dataId) {
		mDataId = dataId;
	}
}