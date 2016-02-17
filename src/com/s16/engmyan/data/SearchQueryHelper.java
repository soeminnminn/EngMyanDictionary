package com.s16.engmyan.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

public class SearchQueryHelper {

	private static String[] SELECT_WORD_COLUMNS;
	private static String[] SELECT_DETAIL_COLUMNS;
	
	static {
		SELECT_WORD_COLUMNS = new String[] {
				  DataContents.COLUMN_ID
				, DataContents.COLUMN_WORD
				, DataContents.COLUMN_STRIPWORD
		};
		
		SELECT_DETAIL_COLUMNS = new String[] {
				  DataContents.COLUMN_ID
				, DataContents.COLUMN_WORD
				, DataContents.COLUMN_STRIPWORD
				, DataContents.COLUMN_TITLE
				, DataContents.COLUMN_DEFINITION
				, DataContents.COLUMN_KEYWORDS
				, DataContents.COLUMN_SYNONYM
				, DataContents.COLUMN_FILENAME
				, DataContents.COLUMN_PICTURE
				, DataContents.COLUMN_SOUND
		};
	}
	
	private final Context mContext;

	private int mLimit = 1000;
	private int mSuggestLimit = 100;
	
	public static SearchQueryHelper getInstance(Context context) {
		return new SearchQueryHelper(context);
	}
	
	public static Uri getContentUri() {
		return DictionaryDataProvider.TABLE_DICTIONARY.getUri();
	}

	private SearchQueryHelper(Context context) {
		mContext = context;
	}

	protected Context getContext() {
		return mContext;
	}
	
	protected ContentResolver getContentResolver() {
		return getContext().getContentResolver();
	}
	
	public void setLimit(int value) {
		mLimit = value;
	}
	
	public void setSuggestLimit(int value) {
		mSuggestLimit = value;
	}
	
	public Cursor querySuggestWord() {
		String sortOrder = DataContents.COLUMN_STRIPWORD + " ASC";
		if (mSuggestLimit > 0) {
			sortOrder += " LIMIT " + mSuggestLimit;
		}
		
		return getContentResolver().query(getContentUri(), SELECT_WORD_COLUMNS, null, null
				, sortOrder);
	}
	
	public Cursor query(CharSequence constraint) {
		if (!TextUtils.isEmpty(constraint))  {
			String searchword = constraint.toString();
			searchword = searchword.replace("'", "''").replace("%", "").replace("_", "").trim();
			if ((searchword.indexOf('*') > -1) || (searchword.indexOf('?') > -1)) {
				searchword = searchword.replace('?', '_');
				searchword = searchword.replace('*', '%');
			} else {
				searchword = searchword + "%";
			}
			String selection = DataContents.COLUMN_STRIPWORD + " LIKE '" + searchword + "'";
			
			String sortOrder = DataContents.COLUMN_STRIPWORD + " ASC";
			if (mLimit > 0) {
				sortOrder += " LIMIT " + mLimit;
			}
			
			return getContentResolver().query(getContentUri(), SELECT_WORD_COLUMNS, selection, null
					, sortOrder);
		}
		return null;
	}
	
	public Cursor stripQuery(CharSequence constraint) {
		if (!TextUtils.isEmpty(constraint))  {
			String searchword = constraint.toString();
			searchword = searchword.replace("'", "''").replace("%", "").replace("_", "").trim();
			String selection = DataContents.COLUMN_STRIPWORD + " LIKE '" + searchword + "'";
			
			return getContentResolver().query(getContentUri(), SELECT_WORD_COLUMNS, selection, null
					, null);
		}
		return null;
	}
	
	public Cursor exactQuery(CharSequence constraint) {
		if (!TextUtils.isEmpty(constraint))  {
			String searchword = constraint.toString();
			searchword = searchword.replace("'", "''").replace("%", "").replace("_", "").trim();
			String selection = DataContents.COLUMN_STRIPWORD + " IS '" + searchword + "'";
			
			return getContentResolver().query(getContentUri(), SELECT_WORD_COLUMNS, selection, null
					, null);
		}
		return null;
	}
	
	public Cursor queryDefinition(long id) {
		return getContentResolver().query(getContentUri(), SELECT_DETAIL_COLUMNS, DataContents.COLUMN_ID + " IS ?"
				, new String[] { String.valueOf(id) }, null);
	}
}
