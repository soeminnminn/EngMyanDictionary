package com.s16.engmyan.data;

import com.s16.engmyan.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

public class FavoritesListAdapter extends BaseAdapter {

	private final Context mContext;
	protected boolean mDataValid;
	protected Cursor mCursor;
	protected final String mDisplayColumnName;
	protected final String mValueColumnName;
	protected int mDisplayColumn;
	protected int mValueColumn;
	protected boolean mIsCheckable;
	
	public FavoritesListAdapter(Context context, Cursor c, String valueColumn, String displayColumn) {
		mContext = context;
		
		mValueColumnName = valueColumn;
		mDisplayColumnName = displayColumn;
		
		boolean cursorPresent = c != null;
		mCursor = c;
		mDataValid = cursorPresent;
		mValueColumn = cursorPresent ? c.getColumnIndexOrThrow(valueColumn) : -1;
		mDisplayColumn = cursorPresent ? c.getColumnIndexOrThrow(displayColumn) : -1;
	}
	
	protected Context getContext() {
		return mContext;
	}
	
	@Override
	public int getCount() {
		int count = 0;
		if (mDataValid && mCursor != null) {
			count = mCursor.getCount();
        }
		return count;
	}

	@Override
	public Object getItem(int position) {
		if (mDataValid && mCursor != null) {
            mCursor.moveToPosition(position);
            return mCursor;
        } else {
            return null;
        }
	}

	@Override
	public long getItemId(int position) {
		if (mDataValid && mCursor != null) {
            if (mCursor.moveToPosition(position)) {
                return mCursor.getLong(mValueColumn);
            } else {
                return 0;
            }
        } else {
            return 0;
        }
	}
	
	@Override
    public boolean hasStableIds() {
        return true;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        View v;
        if (convertView == null) {
            v = newView(getContext(), mCursor, parent);
        } else {
            v = convertView;
        }
        bindView(v, getContext(), mCursor);
        return v;
	}
	
	@Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (mDataValid) {
            mCursor.moveToPosition(position);
            View v;
            if (convertView == null) {
                v = newDropDownView(getContext(), mCursor, parent);
            } else {
                v = convertView;
            }
            bindView(v, getContext(), mCursor);
            return v;
        } else {
            return null;
        }
    }

	protected void bindView(View view, Context context, Cursor cursor) {
		CheckedTextView textViewItemTitle = (CheckedTextView)view.findViewById(android.R.id.text1);
		textViewItemTitle.setText(cursor.getString(mDisplayColumn));
		if (mIsCheckable) {
			textViewItemTitle.setCheckMarkDrawable(R.drawable.btn_check_holo_light);
		} else {
			textViewItemTitle.setCheckMarkDrawable(null);
		}
	}
	
	protected View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
        return newView(context, cursor, parent);
    }

	protected View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
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
