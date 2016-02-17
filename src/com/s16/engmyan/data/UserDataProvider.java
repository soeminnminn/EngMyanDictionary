package com.s16.engmyan.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.s16.data.AbstractDataProvider;
import com.s16.data.DataTable;

public class UserDataProvider extends AbstractDataProvider 
		implements DataContents {

	protected static String TAG = UserDataProvider.class.getSimpleName();
	
	public static final String AUTHORITY = "com.s16.engmyan.data.userdataprovider";
	private static final String SCHEME = "content://";
	public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY);
	
	public static final DataTable TABLE_FAVORITES = DataTable.newInstance(CONTENT_URI, FAVORITES_TABLE)
			.addPrimaryKey(COLUMN_ID, "INTEGER", false)
			.addColumn(COLUMN_WORD, "VARCHAR")
			.addColumn(COLUMN_REFRENCE_ID, "INTEGER", false)
			.addColumn(COLUMN_TIMESTAMP, "INTEGER", false);
	
	public static final DataTable TABLE_HISTORIES = DataTable.newInstance(CONTENT_URI, HISTORIES_TABLE)
			.addPrimaryKey(COLUMN_ID, "INTEGER", false)
			.addColumn(COLUMN_WORD, "VARCHAR")
			.addColumn(COLUMN_REFRENCE_ID, "INTEGER", false)
			.addColumn(COLUMN_TIMESTAMP, "INTEGER", false);
	
	private static final String HISTORIES_TRIGGER_CREATE = 
			"CREATE TRIGGER IF NOT EXISTS \"limit_" + HISTORIES_TABLE + "\" AFTER INSERT ON \"" + HISTORIES_TABLE + "\"" +
			" FOR EACH ROW" +
			" WHEN (SELECT COUNT(\"" + COLUMN_ID + "\") FROM \"" + HISTORIES_TABLE + "\") > 100" +
			" BEGIN " +
			" DELETE FROM \"" + HISTORIES_TABLE + "\"" +
			" WHERE \"" + COLUMN_TIMESTAMP + "\" IS (SELECT MIN(\"" + COLUMN_TIMESTAMP + "\") FROM \"" + HISTORIES_TABLE + "\");" +
			" END";
	
	private static final String HISTORIES_TRIGGER_DROP = 
			 "DROP TRIGGER IF EXISTS \"limit_" + HISTORIES_TABLE + "\";";
	
	private static final String FAVORITES_TRIGGER_CREATE = 
			"CREATE TRIGGER IF NOT EXISTS \"limit_" + FAVORITES_TABLE + "\" AFTER INSERT ON \"" + FAVORITES_TABLE + "\"" +
			" FOR EACH ROW" +
			" WHEN (SELECT COUNT(\"" + COLUMN_ID + "\") FROM \"" + FAVORITES_TABLE + "\") > 100" +
			" BEGIN " +
			" DELETE FROM \"" + FAVORITES_TABLE + "\"" +
			" WHERE \"" + COLUMN_TIMESTAMP + "\" IS (SELECT MIN(\"" + COLUMN_TIMESTAMP + "\") FROM \"" + FAVORITES_TABLE + "\");" +
			" END";
	
	private static final String FAVORITES_TRIGGER_DROP = 
			 "DROP TRIGGER IF EXISTS \"limit_" + FAVORITES_TABLE + "\";";
	
	public static DataTable[] TABLES = new DataTable[] {
		TABLE_FAVORITES, TABLE_HISTORIES
	};
	
	static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		for(int i=0; i<TABLES.length; i++) {
			DataTable table = TABLES[i].setId(i + 1);
			URI_MATCHER.addURI(AUTHORITY, table.getTableName(), MAKELPARAM(table.getId(), MATCH_ALL));
			URI_MATCHER.addURI(AUTHORITY, table.getTableName() + "/#", MAKELPARAM(table.getId(), MATCH_ID));
		}
	}
	
	public UserDataProvider() {
		super(USER_DATABASE_NAME, USER_DATABASE_VERSION, false);
	}
	
	@Override
	protected void onCreateHelper(SQLiteDatabase db) {
		super.onCreateHelper(db);
		
		db.execSQL(HISTORIES_TRIGGER_CREATE);
		db.execSQL(FAVORITES_TRIGGER_CREATE);
	}
	
	@Override
	protected void onUpdateHelper(SQLiteDatabase db, int oldVersion,
			int newVersion) {
		
		db.execSQL(HISTORIES_TRIGGER_DROP);
		db.execSQL(FAVORITES_TRIGGER_DROP);
		
		super.onUpdateHelper(db, oldVersion, newVersion);
	}
	
	@Override
	protected Uri getContentUri() {
		return CONTENT_URI;
	}

	@Override
	protected DataTable[] getAllTables() {
		return TABLES;
	}

	@Override
	protected DataTable getTable(Uri uri) {
		int matchId = URI_MATCHER.match(uri);
		if (matchId != -1) {
			return TABLES[LOWORD(matchId) - 1];
		}
		throw new IllegalArgumentException("Unsupported URI: " + uri);
	}
	
	@Override
	protected boolean useDistinct(Uri uri) {
		return false;
	}

	@Override
	public String getType(Uri uri) {
		int matchId = URI_MATCHER.match(uri);
		if (matchId != -1) {
			if (matchId == 0) {
				return ContentResolver.CURSOR_DIR_BASE_TYPE;
			}
			DataTable table = getTable(uri);
			int typeId = HIWORD(matchId);
			if (typeId == MATCH_ID) {
				return table.getContentTypeItem();
			} else {
				return table.getContentTypeDir();
			}
		}
		throw new IllegalArgumentException("Unsupported URI: " + uri);
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int updateCount = 0;
		DataTable table = getTable(uri);
		if (table != null) {
			if (values.containsKey(COLUMN_REFRENCE_ID)) {
				long refid = values.getAsLong(COLUMN_REFRENCE_ID);
				if (refid > 0) {
					updateCount = super.update(uri, values, COLUMN_REFRENCE_ID + " IS ?", new String[] { String.valueOf(refid) }); 		
				}
			}

			if (updateCount > 0) {
				getContext().getContentResolver().notifyChange(table.getUri(), null);
				return uri;
			}
		}
		
		return super.insert(uri, values);
	}

}
