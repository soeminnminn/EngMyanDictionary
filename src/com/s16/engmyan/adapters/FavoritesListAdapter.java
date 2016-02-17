package com.s16.engmyan.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.s16.data.CursorAdapter;
import com.s16.engmyan.R;

public class FavoritesListAdapter extends CursorAdapter {

	protected final String mDisplayColumnName;
	protected int mDisplayColumn;
	protected boolean mIsCheckable;
	
	public FavoritesListAdapter(Context context, Cursor c, String displayColumn) {
		super(context, c, BaseColumns._ID, false);
		
		mDisplayColumnName = displayColumn;
		mDisplayColumn = (c != null) ? c.getColumnIndexOrThrow(displayColumn) : -1;
	}
	
	private Drawable getCheckMarkDrawable(Context context) {
		int[] attrs = new int[] { android.R.attr.listChoiceIndicatorMultiple };
		Drawable drawable = null;
		TypedArray ta = context.getTheme().obtainStyledAttributes(attrs);
		drawable = ta.getDrawable(0);
		ta.recycle();
		return drawable;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		return layoutInflater.inflate(R.layout.list_item_simple_selectable, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		CheckedTextView textViewItemTitle = (CheckedTextView)view;
		textViewItemTitle.setText(cursor.getString(mDisplayColumn));
		if (mIsCheckable) {
			textViewItemTitle.setCheckMarkDrawable(getCheckMarkDrawable(context));
		} else {
			textViewItemTitle.setCheckMarkDrawable(null);
		}
	}
	
	public boolean getCheckable() {
		return mIsCheckable;
	}
	
	public void setCheckable(boolean value) {
		mIsCheckable = value;
		notifyDataSetInvalidated();
	}
}
