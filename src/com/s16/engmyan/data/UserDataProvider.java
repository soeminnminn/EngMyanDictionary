package com.s16.engmyan.data;

import java.util.Date;

import com.s16.engmyan.Utility;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

public class UserDataProvider extends ContentProvider {
	protected static String TAG = UserDataProvider.class.getSimpleName();
	
	// All URIs share these parts
	public static final String AUTHORITY = "com.s16.engmyan.data.userdataprovider";
	private static final String SCHEME = "content://";
	
	// URIs
	private static final String FAVORITES = "favorites";
	private static final String URL_FAVORITES = SCHEME + AUTHORITY + "/" + FAVORITES;
	public static final Uri URI_FAVORITES = Uri.parse(URL_FAVORITES);
	
	private static final String HISTORIES = "histories";
	private static final String URL_HISTORIES = SCHEME + AUTHORITY + "/" + HISTORIES;
	public static final Uri URI_HISTORIES = Uri.parse(URL_HISTORIES);
	
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "user_data";
	
	static String FAVORITES_TABLE = "tblFavorites";
	static String HISTORIES_TABLE = "tblHistories";
	
	public static String COLUMN_ID = "_id";
	public static String COLUMN_WORD = "word";
	public static String COLUMN_REFRENCE_ID = "refrence_id";
	public static String COLUMN_TIMESTAMP = "timestamp";
	
	static String[] FAVORITES_COLUMNS;
	static String[] HISTORIES_COLUMNS;
	static {
		FAVORITES_COLUMNS = new String[] {
				  COLUMN_ID
				, COLUMN_WORD
				, COLUMN_REFRENCE_ID
				, COLUMN_TIMESTAMP
		};
		
		HISTORIES_COLUMNS = new String[] {
				  COLUMN_ID
				, COLUMN_WORD
				, COLUMN_REFRENCE_ID
				, COLUMN_TIMESTAMP
		};
	}
	
	private static final String HISTORIES_TABLE_CREATE =
			 "CREATE TABLE IF NOT EXISTS " + HISTORIES_TABLE + " (" +
			  COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			  COLUMN_WORD + " VARCHAR," +
			  COLUMN_REFRENCE_ID + " INTEGER NOT NULL UNIQUE," +
			  COLUMN_TIMESTAMP + " INTEGER NOT NULL" + 
			  " );";
	
	private static final String FAVORITES_TABLE_CREATE =
			  "CREATE TABLE IF NOT EXISTS " + FAVORITES_TABLE + " (" +
			  COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			  COLUMN_WORD + " VARCHAR," +
			  COLUMN_REFRENCE_ID + " INTEGER NOT NULL UNIQUE," +
			  COLUMN_TIMESTAMP + " INTEGER NOT NULL" + 
			  " );";
	 
	private static final String HISTORIES_TABLE_DROP = 
			 "DROP TABLE IF EXISTS " + 
			 HISTORIES_TABLE + ";";
	
	private static final String FAVORITES_TABLE_DROP = 
			 "DROP TABLE IF EXISTS " + 
			 FAVORITES_TABLE + ";";
	
	private DatabaseHelper mDbHelper;
	
	private int mFavoriteLimit = 50;
	private int mHistoryLimit = 20;
	
	public static boolean isFavorited(Context context, long refId) {
		Cursor cursor = context.getContentResolver().query(URI_FAVORITES
				, new String[] { COLUMN_ID }
				, COLUMN_REFRENCE_ID + " IS ?", new String[] { String.valueOf(refId) }, null);
		return (cursor != null) && (!Utility.isNull(cursor, COLUMN_ID));
	}
	
	public static Cursor getAllFavorites(Context context) {
		return context.getContentResolver().query(URI_FAVORITES, null, null, null, null);
	}
	
	public static Uri createFavorite(Context context, String word, long refId) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_WORD, word);
		values.put(COLUMN_REFRENCE_ID, refId);
		values.put(COLUMN_TIMESTAMP, (new Date()).getTime());
		return context.getContentResolver().insert(URI_FAVORITES, values);
	}
	
	public static int removeFavorite(Context context, long id) {
		return context.getContentResolver().delete(URI_FAVORITES, COLUMN_ID + " IS ?", new String[] { String.valueOf(id) });
	}
	
	public static Cursor getAllHistories(Context context) {
		return context.getContentResolver().query(URI_HISTORIES, null, null, null, null);
	}
	
	public static Uri createHistory(Context context, String word, long refId) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_WORD, word);
		values.put(COLUMN_REFRENCE_ID, refId);
		values.put(COLUMN_TIMESTAMP, (new Date()).getTime());
		return context.getContentResolver().insert(URI_HISTORIES, values);
	}
	
	public static int removeAllHistory(Context context) {
		return context.getContentResolver().delete(URI_HISTORIES, null, null);
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {		

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(HISTORIES_TABLE_CREATE);
			db.execSQL(FAVORITES_TABLE_CREATE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL(HISTORIES_TABLE_DROP);
			db.execSQL(FAVORITES_TABLE_DROP);
			onCreate(db);
		}
	}
	
	public UserDataProvider() {
		super();
	}
	
	public SQLiteDatabase getDatabase() {
		SQLiteDatabase database = null;
		if(mDbHelper == null) return database;
		
		try {
			database = mDbHelper.getWritableDatabase();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		return database;
	}
	
	public boolean isOpen() {
		SQLiteDatabase database = getDatabase();
		return (database != null) && (database.isOpen());
	}
	
	public void close() {
		if (mDbHelper != null) mDbHelper.close();
	}
	
	public void setFavoriteLimit(int value) {
		mFavoriteLimit = value;
	}
	
	public void setHistoryLimit(int value) {
		mHistoryLimit = value;
	}

	@Override
	public boolean onCreate() {
		if(mDbHelper == null) {
			mDbHelper = new DatabaseHelper(getContext());
		}
		return true;
	}

	@Override
	public String getType(Uri uri) {
		if (uri.equals(URI_FAVORITES)) {
			return FAVORITES;
		} else if (uri.equals(URI_HISTORIES)) {
			return HISTORIES;
		} 
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		Cursor result = null;
		final SQLiteDatabase database = getDatabase();
		if (database == null) return result;
		
		if (uri.equals(URI_FAVORITES)) {
			
			if (projection == null) projection = FAVORITES_COLUMNS;
			if (TextUtils.isEmpty(sortOrder)) sortOrder = COLUMN_WORD + " ASC";
			
			result = database.query(FAVORITES_TABLE, projection, selection, selectionArgs
					, null, null, sortOrder, String.valueOf(mFavoriteLimit));
			
			if (result != null) {
				result.moveToFirst();
				result.setNotificationUri(getContext().getContentResolver(), URI_FAVORITES);
			}
			
		} else if (uri.equals(URI_HISTORIES)) {
			
			if (projection == null) projection = HISTORIES_COLUMNS;
			if (TextUtils.isEmpty(sortOrder)) sortOrder = COLUMN_TIMESTAMP + " DESC";
			
			result = database.query(HISTORIES_TABLE, projection, selection, selectionArgs
					, null, null, sortOrder, String.valueOf(mHistoryLimit));
			
			if (result != null) {
				result.moveToFirst();
				result.setNotificationUri(getContext().getContentResolver(), URI_HISTORIES);
			}
			
		}
		return result;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (values.get(COLUMN_TIMESTAMP) == null) {
			values.put(COLUMN_TIMESTAMP, (new Date()).getTime());
		}
		
		final SQLiteDatabase database = getDatabase();
		if (database == null) return null;
		
		if (uri.equals(URI_FAVORITES)) {
			
			long result = database.update(FAVORITES_TABLE, values, COLUMN_REFRENCE_ID + " IS ?", new String[] { values.getAsString(COLUMN_REFRENCE_ID) });
			if (result < 1) database.insert(FAVORITES_TABLE, null, values);
			
		}  else if (uri.equals(URI_HISTORIES)) {
			
			long result = database.update(HISTORIES_TABLE, values, COLUMN_REFRENCE_ID + " IS ?", new String[] { values.getAsString(COLUMN_REFRENCE_ID) });
			if (result < 1) database.insert(HISTORIES_TABLE, null, values);
			
		}
		return uri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		final SQLiteDatabase database = getDatabase();
		if (database == null) return -1;
		
		if (uri.equals(URI_FAVORITES)) {
			return database.delete(FAVORITES_TABLE, selection, selectionArgs);
		}  else if (uri.equals(URI_HISTORIES)) {
			return database.delete(HISTORIES_TABLE, selection, selectionArgs);
		}
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		final SQLiteDatabase database = getDatabase();
		if (database == null) return -1;
		
		if (values.get(COLUMN_TIMESTAMP) == null) {
			values.put(COLUMN_TIMESTAMP, (new Date()).getTime());
		}
		
		if (uri.equals(URI_FAVORITES)) {
			return database.update(FAVORITES_TABLE, values, selection, selectionArgs);
		}  else if (uri.equals(URI_HISTORIES)) {
			return database.update(HISTORIES_TABLE, values, selection, selectionArgs);
		}
		return 0;
	}
}
