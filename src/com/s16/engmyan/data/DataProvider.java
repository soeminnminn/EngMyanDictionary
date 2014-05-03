package com.s16.engmyan.data;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

public class DataProvider {
	
	protected static String TAG = DataProvider.class.getSimpleName();
	
	private static final int DATABASE_VERSION = 1;
	
	public static String TABLE_DICTIONARY = "dictionary";
	
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
	private SQLiteDatabase mDb;
	private int mLimit = 50; 
	// SELECT _id, word, definition, filename, picture, sound FROM dictionary
	
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
	
	public DataProvider(Context context, File dbFile) {
		mContext = context;
		mDbFile = dbFile;
	}
	
	public DataProvider open()
			throws SQLException {
		if(mDbHelper == null) {
			mDbHelper = new DatabaseHelper(mContext, mDbFile.getPath());
			mDb = mDbHelper.getWritableDatabase();
		}
		return this;
	}
	
	public void close() {
		if (mDbHelper != null) mDbHelper.close();
	}
	
	public boolean isOpen() {
		return (mDb != null) && (mDb.isOpen());
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
			SQLiteDatabase dataBase = helper.getWritableDatabase();
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
	
	public static boolean isNull(Cursor cursor, String column) {
		if (cursor == null) return true;
		if ((column == null) || (column == "")) return true;
		int columnIndex = cursor.getColumnIndex(column);
		if (columnIndex < 0) return true;
		return cursor.isNull(columnIndex);
	}
	
	protected String buildSelectWordStatement(String searchword) {
		String sql = "";
		for(String col : SELECT_WORD_COLUMNS) {
			sql += sql == "" ? col : ", " + col;
		}
		
		sql = "SELECT " + sql + " FROM " + TABLE_DICTIONARY;
		if (!TextUtils.isEmpty(searchword))  {
			sql += " WHERE " + COLUMN_WORD + " LIKE '" + searchword + "%'";
		}
		
		sql += " ORDER BY " + COLUMN_WORD + " ASC";
		if(mLimit > 0) {
			sql += " LIMIT " + mLimit;
		}
		sql += ";";
		
		return sql;
	}
	
	protected String buildSelectDefinitionStatement(long id) {
		String sql = "";
		for(String col : SELECT_DETAIL_COLUMNS) {
			sql += sql == "" ? col : ", " + col;
		}
		
		sql = "SELECT " + sql + " FROM " + TABLE_DICTIONARY;
		if (id > -1)  {
			sql += " WHERE " + COLUMN_ID + " = " + id + " LIMIT 1";
		} else {
			sql += " ORDER BY " + COLUMN_ID + " ASC";
			if (mLimit > 0) {
				sql += " LIMIT " + mLimit;
			}	
		}
		sql += ";";
		
		return sql;
	}
	
	public Cursor querySuggestWord() 
			throws SQLException {
		
		String sql = buildSelectWordStatement(null);
		Cursor mCursor = mDb.rawQuery(sql, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public Cursor query(String searchword) 
			throws SQLException {
		
		String sql = buildSelectWordStatement(searchword);
		if(mLimit > 0) {
			sql += " LIMIT " + mLimit;
		}
		
		Cursor mCursor = mDb.rawQuery(sql, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public Cursor queryDefinition(long id) {
		String sql = buildSelectDefinitionStatement(id);
		if(mLimit > 0) {
			sql += " LIMIT " + mLimit;
		}
		
		Cursor mCursor = mDb.rawQuery(sql, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
}
