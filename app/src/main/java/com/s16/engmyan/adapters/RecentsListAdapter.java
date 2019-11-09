package com.s16.engmyan.adapters;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.s16.data.CursorAdapter;
import com.s16.engmyan.R;

public class RecentsListAdapter extends CursorAdapter {

	protected final String mDisplayColumnName;
	protected int mDisplayColumn;
	
	public RecentsListAdapter(Context context, Cursor c, String displayColumn) {
		super(context, c, BaseColumns._ID, false);
		mDisplayColumnName = displayColumn;
		mDisplayColumn = (c != null) ? c.getColumnIndexOrThrow(displayColumn) : -1;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		return layoutInflater.inflate(R.layout.list_item_simple, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView textViewItemTitle = (TextView)view;
		textViewItemTitle.setText(cursor.getString(mDisplayColumn));
	}
}
