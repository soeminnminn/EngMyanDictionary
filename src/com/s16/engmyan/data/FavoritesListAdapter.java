package com.s16.engmyan.data;

import com.s16.data.CursorAdapter;
import com.s16.engmyan.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

public class FavoritesListAdapter extends CursorAdapter {

	protected final String mDisplayColumnName;
	protected int mDisplayColumn;
	protected boolean mIsCheckable;
	
	public FavoritesListAdapter(Context context, Cursor c, String valueColumn, String displayColumn) {
		super(context, c, valueColumn, true);
		mDisplayColumnName = displayColumn;
		mDisplayColumn = (c != null) ? c.getColumnIndexOrThrow(displayColumn) : -1;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		CheckedTextView textViewItemTitle = (CheckedTextView)view.findViewById(android.R.id.text1);
		textViewItemTitle.setText(cursor.getString(mDisplayColumn));
		if (mIsCheckable) {
			textViewItemTitle.setCheckMarkDrawable(R.drawable.btn_check_holo_light);
		} else {
			textViewItemTitle.setCheckMarkDrawable(null);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		View view = layoutInflater.inflate(R.layout.simple_selectable_list_item, null);
		return view;
	}
	
	public boolean getCheckable() {
		return mIsCheckable;
	}
	
	public void setCheckable(boolean value) {
		mIsCheckable = value;
	}
}
