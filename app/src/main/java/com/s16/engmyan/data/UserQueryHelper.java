package com.s16.engmyan.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.net.Uri;

public class UserQueryHelper extends ContextWrapper {

	public static UserQueryHelper getInstance(Context context) {
		return new UserQueryHelper(context);
	}
	
	public static Uri getFavoritesUri() {
		return UserDataProvider.TABLE_FAVORITES.getUri();
	}
	
	public static Uri getHistoriesUri() {
		return UserDataProvider.TABLE_HISTORIES.getUri();
	}

	private UserQueryHelper(Context context) {
		super(context);
	}

	protected Context getContext() {
		return getBaseContext();
	}
	
	public boolean isFavorited(long refId) {
		Cursor cursor = getContentResolver().query(getFavoritesUri()
				, new String[] { UserDataProvider.COLUMN_ID }
				, UserDataProvider.COLUMN_REFRENCE_ID + " IS ?", new String[] { String.valueOf(refId) }, null);
		long id = -1;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int colIdx = cursor.getColumnIndex(UserDataProvider.COLUMN_ID);
				if (colIdx != -1) {
					id = cursor.getLong(colIdx);
				}
			}
			cursor.close();
		}
		return (id != -1);
	}
	
	public Cursor getAllFavorites() {
		return getContentResolver().query(getFavoritesUri(), null, null, null, null);
	}
	
	public Uri createFavorite(String word, long refId) {
		ContentValues values = new ContentValues();
		values.put(UserDataProvider.COLUMN_WORD, word);
		values.put(UserDataProvider.COLUMN_REFRENCE_ID, refId);
		values.put(UserDataProvider.COLUMN_TIMESTAMP, System.currentTimeMillis());
		return getContentResolver().insert(getFavoritesUri(), values);
	}
	
	public int removeFavorite(long id) {
		return getContentResolver().delete(getFavoritesUri(), UserDataProvider.COLUMN_ID + " IS ?", new String[] { String.valueOf(id) });
	}
	
	public int removeFavoriteByRef(long id) {
		return getContentResolver().delete(getFavoritesUri(), UserDataProvider.COLUMN_REFRENCE_ID + " IS ?", new String[] { String.valueOf(id) });
	}
	
	public Cursor getAllHistories() {
		return getContentResolver().query(getHistoriesUri(), null, null, null, null);
	}
	
	public Uri createHistory(String word, long refId) {
		ContentValues values = new ContentValues();
		values.put(UserDataProvider.COLUMN_WORD, word);
		values.put(UserDataProvider.COLUMN_REFRENCE_ID, refId);
		values.put(UserDataProvider.COLUMN_TIMESTAMP, System.currentTimeMillis());
		return getContentResolver().insert(getHistoriesUri(), values);
	}
	
	public int removeAllHistory() {
		return getContentResolver().delete(getHistoriesUri(), null, null);
	}
}
