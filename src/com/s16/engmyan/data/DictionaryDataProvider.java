package com.s16.engmyan.data;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

public class DictionaryDataProvider {
	
	protected static String TAG = DictionaryDataProvider.class.getSimpleName();
	
	private static final int DATABASE_VERSION = 1;
	
	public static String DICTIONARY_TABLE = "dictionary";
	
	public static String COLUMN_ID = "_id";
	public static String COLUMN_WORD = "word";
	
	public static String COLUMN_TITLE = "title";
	public static String COLUMN_DEFINITION = "definition";
	public static String COLUMN_FILENAME = "filename";
	public static String COLUMN_PICTURE = "picture";
	public static String COLUMN_SOUND = "sound";
	
	static String[] SELECT_WORD_COLUMNS;
	static String[] SELECT_DETAIL_COLUMNS;
	static {
		SELECT_WORD_COLUMNS = new String[] {
				  COLUMN_ID
				, COLUMN_WORD
		};
		
		SELECT_DETAIL_COLUMNS = new String[] {
				  COLUMN_ID
				, COLUMN_WORD
				, COLUMN_TITLE
				, COLUMN_DEFINITION
				, COLUMN_FILENAME
				, COLUMN_PICTURE
				, COLUMN_SOUND
		};
	}
	
	private final Context mContext;
	private final File mDbFile;

	private DatabaseHelper mDbHelper;
	private int mLimit = 50; 
	
	private static class DatabaseHelper extends SQLiteOpenHelper {		

		DatabaseHelper(Context context, String databasePath) {
			super(context, databasePath, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}	
	
	public DictionaryDataProvider(Context context, File dbFile) {
		mContext = context;
		mDbFile = dbFile;
	}
	
	protected Context getContext() {
		return mContext;
	}
	
	public SQLiteDatabase getDatabase() {
		if (mDbHelper == null) return null;
		SQLiteDatabase database = null;
		try {
			database = mDbHelper.getReadableDatabase();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		return database;
	}
	
	public DictionaryDataProvider open() {
		if(mDbHelper == null) {
			mDbHelper = new DatabaseHelper(getContext(), mDbFile.getPath());
		}
		return this;
	}
	
	public void close() {
		if (mDbHelper != null) mDbHelper.close();
	}
	
	public boolean isOpen() {
		SQLiteDatabase database = getDatabase();
		return (database != null) && (database.isOpen());
	}
	
	public void setLimit(int value) {
		mLimit = value;
	}
	
	public File getDatabaseFile() {
		return mDbFile;
	}
	
	public static boolean versionCheck(Context context, File dbFile, long version) {
		DatabaseHelper helper = new DatabaseHelper(context, dbFile.getPath());
		
		boolean retValue = false;
		try {
			SQLiteDatabase dataBase = helper.getReadableDatabase();
			String sql = " SELECT \"version\" FROM \"android_version\" LIMIT 1; ";
			Cursor cursor = dataBase.rawQuery(sql, null);
			if (cursor != null && cursor.moveToFirst()) {
				long dbVersion = cursor.getLong(0);
				retValue = (version == dbVersion);
			}
		} catch(SQLException ex) {
			ex.printStackTrace();
		} finally {
			helper.close();
		}
		return retValue;
	}
	
	public Cursor querySuggestWord() {
		
		final SQLiteDatabase database = getDatabase();
		if (database == null) return null;
		
		Cursor cursor = database.query(DICTIONARY_TABLE, SELECT_WORD_COLUMNS, null, null, null, null, COLUMN_WORD + " ASC", String.valueOf(mLimit));
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public Cursor query(String searchword) {
		
		final SQLiteDatabase database = getDatabase();
		if (database == null) return null;
		
		String selection = null;
		if (!TextUtils.isEmpty(searchword))  {
			selection = COLUMN_WORD + " LIKE '" + searchword.replace("'", "''") + "%'";
		}
		
		Cursor cursor = database.query(DICTIONARY_TABLE, SELECT_WORD_COLUMNS, selection, null
							, null, null, COLUMN_WORD + " ASC", String.valueOf(mLimit));
		
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public Cursor queryDefinition(long id) {
		
		final SQLiteDatabase database = getDatabase();
		if (database == null) return null;
		
		Cursor cursor = database.query(DICTIONARY_TABLE, SELECT_DETAIL_COLUMNS, COLUMN_ID + " IS ?", new String[] { String.valueOf(id) }
							, null, null, COLUMN_WORD + " ASC", String.valueOf(mLimit));
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
}
