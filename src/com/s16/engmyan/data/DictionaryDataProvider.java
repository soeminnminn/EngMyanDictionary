package com.s16.engmyan.data;

import java.io.File;

import com.s16.engmyan.Constants;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

public class DictionaryDataProvider {
	
	protected static String TAG = DictionaryDataProvider.class.getSimpleName();
	
	private static final int DATABASE_VERSION = Constants.DATA_VERSION;
	
	public static String DICTIONARY_TABLE = "dictionary";
	
	public static String COLUMN_ID = "_id";
	public static String COLUMN_WORD = "word";
	public static String COLUMN_STRIPWORD = "stripword";
	
	public static String COLUMN_TITLE = "title";
	public static String COLUMN_DEFINITION = "definition";
	public static String COLUMN_KEYWORDS = "keywords";
	public static String COLUMN_SYNONYM = "synonym";
	public static String COLUMN_FILENAME = "filename";
	public static String COLUMN_PICTURE = "picture";
	public static String COLUMN_SOUND = "sound";
	
	static String[] SELECT_WORD_COLUMNS;
	static String[] SELECT_DETAIL_COLUMNS;
	static {
		SELECT_WORD_COLUMNS = new String[] {
				  COLUMN_ID
				, COLUMN_WORD
				, COLUMN_STRIPWORD
		};
		
		SELECT_DETAIL_COLUMNS = new String[] {
				  COLUMN_ID
				, COLUMN_WORD
				, COLUMN_STRIPWORD
				, COLUMN_TITLE
				, COLUMN_DEFINITION
				, COLUMN_KEYWORDS
				, COLUMN_SYNONYM
				, COLUMN_FILENAME
				, COLUMN_PICTURE
				, COLUMN_SOUND
		};
	}
	
	private final Context mContext;
	private File mDbFile;

	private DatabaseHelper mDbHelper;
	private int mLimit = 1000;
	private int mSuggestLimit = 50;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {

		public int version = DATABASE_VERSION;
		
		DatabaseHelper(Context context, String databasePath) {
			super(context, databasePath, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i("DatabaseHelper.onUpgrade", "Old Version : " + oldVersion + ", New Version : " + newVersion);
			version = oldVersion;
		}
	}	
	
	public DictionaryDataProvider(Context context) {
		super();
		mContext = context;
	}
	
	public DictionaryDataProvider(Context context, File dbFile) {
		this(context);
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
		if(mDbHelper == null && mDbFile != null) {
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
	
	protected String getLimitStr() {
		return mLimit > 0 ? String.valueOf(mLimit) : null;
	}
	
	public void setSuggestLimit(int value) {
		mSuggestLimit = value;
	}
	
	protected String getSuggestLimitStr() {
		return mSuggestLimit > 0 ? String.valueOf(mSuggestLimit) : null;
	}
	
	public File getDatabaseFile() {
		return mDbFile;
	}
	
	public void setDatabaseFile(File file) {
		mDbFile = file;
	}
	
	public static boolean versionCheck(Context context, File dbFile) {
		DatabaseHelper helper = new DatabaseHelper(context, dbFile.getPath());
		
		boolean retValue = false;
		try {
			SQLiteDatabase dataBase = helper.getReadableDatabase();
			if (helper.version != DATABASE_VERSION) return false;
			
			int dbVersion = dataBase.getVersion();
			Log.i(TAG, "Old Version : " + dbVersion + ", New Version : " + DATABASE_VERSION);
			retValue = (dbVersion == DATABASE_VERSION);
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
		
		Cursor cursor = database.query(DICTIONARY_TABLE, SELECT_WORD_COLUMNS, null, null, null, null
				, COLUMN_STRIPWORD + " ASC", getSuggestLimitStr());
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
			searchword = searchword.replace("'", "''").replace("%", "").replace("_", "").trim();
			if ((searchword.indexOf('*') > -1) || (searchword.indexOf('?') > -1)) {
				searchword = searchword.replace('?', '_');
				searchword = searchword.replace('*', '%');
			} else {
				searchword = searchword + "%";
			}
			
			selection = COLUMN_STRIPWORD + " LIKE '" + searchword + "'";
			
			Cursor cursor = database.query(DICTIONARY_TABLE, SELECT_WORD_COLUMNS, selection
					, null, null, null, COLUMN_STRIPWORD + " ASC", getLimitStr());

			if (cursor != null) {
				cursor.moveToFirst();
				return cursor;
			}
		}
		
		return null;
	}
	
	public Cursor stripQuery(String searchword) {
		
		final SQLiteDatabase database = getDatabase();
		if (database == null) return null;
		
		String selection = "";
		if (!TextUtils.isEmpty(searchword)) {
			searchword = searchword.replace("'", "''").replace("%", "").replace("_", "").trim();
			selection += COLUMN_STRIPWORD + " LIKE '" + searchword + "'";
			
			Cursor cursor = database.query(DICTIONARY_TABLE, SELECT_WORD_COLUMNS, selection
					, null, null, null, null, null);

			if (cursor != null) {
				cursor.moveToFirst();
				return cursor;
			}
		}
		
		return null;
	}
	
	public Cursor exactQuery(String searchword) {
		final SQLiteDatabase database = getDatabase();
		if (database == null) return null;
		
		String selection = "";
		if (!TextUtils.isEmpty(searchword)) {
			searchword = searchword.replace("'", "''").replace("%", "").replace("_", "").trim().toLowerCase();
			selection += COLUMN_STRIPWORD + " IS '" + searchword + "'";
		
			Cursor cursor = database.query(DICTIONARY_TABLE, SELECT_WORD_COLUMNS, selection
								, null, null, null, null, null);
			
			if (cursor != null) {
				cursor.moveToFirst();
				return cursor;
			}
		}
		return null;
	}
	
	public Cursor queryDefinition(long id) {
		
		final SQLiteDatabase database = getDatabase();
		if (database == null) return null;
		
		Cursor cursor = database.query(DICTIONARY_TABLE, SELECT_DETAIL_COLUMNS, COLUMN_ID + " IS ?"
							, new String[] { String.valueOf(id) }, null, null, null, "1");
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
}
