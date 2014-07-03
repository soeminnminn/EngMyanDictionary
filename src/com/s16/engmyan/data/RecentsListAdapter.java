package com.s16.engmyan.data;

import com.s16.data.CursorAdapter;
import com.s16.engmyan.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RecentsListAdapter extends CursorAdapter {

	protected final String mDisplayColumnName;
	protected int mDisplayColumn;
	
	public RecentsListAdapter(Context context, Cursor c, String valueColumn, String displayColumn) {
		super(context, c, valueColumn, true);
		mDisplayColumnName = displayColumn;
		mDisplayColumn = (c != null) ? c.getColumnIndexOrThrow(displayColumn) : -1;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView textViewItemTitle = (TextView)view.findViewById(android.R.id.text1);
		textViewItemTitle.setText(cursor.getString(mDisplayColumn));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		View view = layoutInflater.inflate(R.layout.simple_list_item, null);
		return view;
	}
}
