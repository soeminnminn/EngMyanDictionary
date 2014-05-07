package com.s16.engmyan.data;

import com.s16.engmyan.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import android.widget.TextView;

public class SearchListAdapter extends BaseAdapter implements Filterable
		, CursorFilter.CursorFilterClient {
	
	private final Context mContext;

	protected boolean mDataValid;
	protected Cursor mCursor;
	protected final String mDisplayColumnName;
	protected final String mValueColumnName;
	protected int mDisplayColumn;
	protected int mValueColumn;
	
	protected CursorFilter mCursorFilter;
	protected FilterQueryProvider mFilterQueryProvider;
	
	public SearchListAdapter(Context context, Cursor c, String valueColumn, String displayColumn) {
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
		TextView textViewItemTitle = (TextView)view.findViewById(android.R.id.text1);
		textViewItemTitle.setText(cursor.getString(mDisplayColumn));
	}
	
	protected View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
        return newView(context, cursor, parent);
    }

	protected View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		View view = layoutInflater.inflate(R.layout.simple_list_item, null);
		return view;
	}
	
	@Override
	public Filter getFilter() {
		if (mCursorFilter == null) {
            mCursorFilter = new CursorFilter(this);
        }
        return mCursorFilter;
	}
	
	@Override
	public Cursor getCursor() {
        return mCursor;
    }

	public void setFilterQueryProvider(FilterQueryProvider filterQueryProvider) {
        mFilterQueryProvider = filterQueryProvider;
    }
	
	public FilterQueryProvider getFilterQueryProvider() {
        return mFilterQueryProvider;
    }

	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (mFilterQueryProvider != null) {
            return mFilterQueryProvider.runQuery(constraint);
        }

        return mCursor;
    }
	
	@Override
	public CharSequence convertToString(Cursor cursor) {
        return cursor == null ? "" : cursor.toString();
    }

	@Override
	public void changeCursor(Cursor cursor) {
		Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
	}
	
	public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (newCursor != null) {
        	mDisplayColumn = newCursor.getColumnIndexOrThrow(mDisplayColumnName);
    		mValueColumn = newCursor.getColumnIndexOrThrow(mValueColumnName);
            mDataValid = true;
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
        	mDisplayColumn = -1;
        	mValueColumn = -1;
            mDataValid = false;
            // notify the observers about the lack of a data set
            notifyDataSetInvalidated();
        }
        return oldCursor;
    }
}
