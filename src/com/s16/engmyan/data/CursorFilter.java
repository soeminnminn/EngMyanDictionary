package com.s16.engmyan.data;

import android.database.Cursor;
import android.widget.Filter;

class CursorFilter extends Filter {
	
	static abstract interface CursorFilterClient {
		public abstract CharSequence convertToString(Cursor paramCursor);
		public abstract Cursor runQueryOnBackgroundThread(CharSequence paramCharSequence);
		public abstract Cursor getCursor();
		public abstract void changeCursor(Cursor paramCursor);
	}
	
	CursorFilterClient mClient;
	
	public CursorFilter(CursorFilterClient client) {
		mClient = client;
	}

	@Override
	protected FilterResults performFiltering(CharSequence constraint) {
		Cursor cursor = this.mClient.runQueryOnBackgroundThread(constraint);
	    Filter.FilterResults results = new Filter.FilterResults();
	    if (cursor != null) {
	      results.count = cursor.getCount();
	      results.values = cursor;
	    } else {
	      results.count = 0;
	      results.values = null;
	    }
	    return results;
	}

	@Override
	protected void publishResults(CharSequence constraint, FilterResults results) {
		Cursor oldCursor = this.mClient.getCursor();
	    if ((results.values != null) && (results.values != oldCursor))
	      this.mClient.changeCursor((Cursor)results.values);
	}
	
}
