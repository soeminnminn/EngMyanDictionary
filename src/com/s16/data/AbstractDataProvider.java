package com.s16.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.text.TextUtils;

public abstract class AbstractDataProvider extends ContentProvider {

	protected static final String TAG = AbstractDataProvider.class.getSimpleName();
	
	public static final String METHOD_BACKUP = "backup";
	public static final String METHOD_RESTORE = "restore";
	public static final String METHOD_SAVE = "save";
	public static final String METHOD_GETMAXID = "getMaxId";
	public static final String METHOD_SET_ARGUMENTS = "setArguments";
	public static final String RETURN_KEY = "result";
	
	private static final String DEFAULT_DATABASE_NAME = "database";
	private static final int DEFAULT_DATABASE_VERSION = 1;
	
	protected static int LOWORD(int val) { return val & 0xffff; }
	protected static int HIWORD(int val) { return (val >> 0x10) & 0xffff; }
	protected static int MAKELPARAM(int low, int high) { return ((high << 0x10) | (low & 0xffff)); }
	
	private class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context, String databaseName, int version) {
			super(context, databaseName, null, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			if (!isReadonly()) {
				if (mTables == null) return;
				for(int i = 0; i < mTables.length; i++) {
					mTables[i].create(db);
				}
			}
			onCreateHelper(db);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (!isReadonly()) {
				if (mTables == null) return;
				for(int i = 0; i < mTables.length; i++) {
					mTables[i].drop(db);
				}
			}
			onUpdateHelper(db, oldVersion, newVersion);
			onCreate(db);
		}
	}
	
	private SQLiteDatabase mDatabase;
	private String mDatabasePath;
	private int mQueryLimit = -1;
	private String mArgs;
	private Bundle mExtra;
	
	private final String mDatabaseName;
	private final int mDatabaseVersion;
	private final boolean mIsReadonly;
	private final DataTable[] mTables;
	private DatabaseHelper mDbHelper;
	
	public AbstractDataProvider(File databaseFile, int version, boolean readonly)  {
		this(databaseFile != null ? databaseFile.getPath() : DEFAULT_DATABASE_NAME, version, readonly);
	}
	
	public AbstractDataProvider(String databaseName, int version, boolean readonly)  {
		super();
		
		mDatabaseName = databaseName != null ? databaseName : DEFAULT_DATABASE_NAME;
		mDatabaseVersion = version > 0 ? version : DEFAULT_DATABASE_VERSION;
		mIsReadonly = readonly;
		mTables = getAllTables();
	}
	
	protected abstract Uri getContentUri();
	
	protected abstract DataTable[] getAllTables();
	
	protected abstract DataTable getTable(Uri uri);
	
	protected abstract boolean useDistinct(Uri uri);
	
	@Override
	public abstract String getType(Uri uri);
	
	protected void onCreateHelper(SQLiteDatabase db) {
		
	}
	
	protected void onUpdateHelper(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

	protected DataTable getTable(String tableName) {
		if (mTables == null) return null;
		for(int i = 0; i < mTables.length; i++) {
			if (mTables[i].getTableName().equals(tableName)) {
				return mTables[i];
			}
		}
		return null;
	}
	
	protected SQLiteDatabase getDatabase() {
		if (mDatabase == null) {
			if(mDbHelper == null) return null;
			if (!isReadonly()) {
				try {
					mDatabase = mDbHelper.getWritableDatabase();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			} else {
				try {
					mDatabase = mDbHelper.getReadableDatabase();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		return mDatabase;
	}
	
	protected File getDatabasePath() {
		if (!TextUtils.isEmpty(mDatabasePath)) {
			return new File(mDatabasePath);
		}
		return null;
	}
	
	protected boolean isReadonly() {
		return mIsReadonly;
	}
	
	public boolean isOpen() {
		SQLiteDatabase database = getDatabase();
		return (database != null) && (database.isOpen());
	}
	
	public void close() {
		if (mDbHelper != null) mDbHelper.close();
	}
	
	public void setQueryLimit(int value) {
		mQueryLimit = value;
	}
	
	@Override
	public boolean onCreate() {
		if(mDbHelper == null) {
			mDbHelper = new DatabaseHelper(getContext(), mDatabaseName, mDatabaseVersion);
		}
		
		SQLiteDatabase database = getDatabase();
		if (database != null) {
			mDatabasePath = database.getPath();
		}
		
		return (database != null);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor result = null;
		final SQLiteDatabase database = getDatabase();
		if (database == null) return result;
		
		boolean distinct = useDistinct(uri);
		DataTable table = getTable(uri);
		if (table != null) {
			String limit = mQueryLimit > 0 ? String.valueOf(mQueryLimit) : null;
			
			if (projection == null) {
				result = table.from(database).where(selection, selectionArgs).query(distinct, sortOrder, limit);
			} else {
				result = table.from(database).where(selection, selectionArgs).query(distinct, projection, sortOrder, limit);	
			}
			if (result != null) {
				result.moveToFirst();
				result.setNotificationUri(getContext().getContentResolver(), uri);
			}
		}
		return result;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (isReadonly()) {
			return null;
		}
		
		final SQLiteDatabase database = getDatabase();
		if (database == null) return null;
		
		long rowID = 0;
		DataTable table = getTable(uri);
		if (table != null) {
			DataTable.DataColumn idColunm = table.getPrimaryKey();
			String idColunmName = idColunm != null ? idColunm.getName() : BaseColumns._ID;
			if (values.containsKey(idColunmName)) {
				long id = values.getAsLong(idColunmName);
				if (id < 0) {
					id = getMaxId(table.getTableName(), idColunmName);
					values.put(idColunmName, id + 1);
				}
			}

			rowID = table.from(database).insert(values);
		}
		if (rowID > 0) {
			Uri _uri = ContentUris.withAppendedId(uri, rowID);
	        getContext().getContentResolver().notifyChange(_uri, null);
	        return _uri;
		}
		
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if (isReadonly()) {
			return 0;
		}
		
		final SQLiteDatabase database = getDatabase();
		if (database == null) return 0;
		
		int count = 0;
		DataTable table = getTable(uri);
		if (table != null) {
			if (!TextUtils.isEmpty(selection))
				count = table.from(database).where(selection, selectionArgs).delete();
			else 
				count = table.from(database).delete();
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		if (isReadonly()) {
			return 0;
		}
		
		final SQLiteDatabase database = getDatabase();
		if (database == null) return 0;
		
		int count = 0;
		DataTable table = getTable(uri);
		if (table != null) {
			if (!TextUtils.isEmpty(selection))
				count = table.from(database).where(selection, selectionArgs).update(values);
			else 
				count = table.from(database).update(values);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return (int)count;
	}
	
	public int save(String tableName, ContentValues values, String selection,
			String[] selectionArgs) {
		if (isReadonly()) {
			return 0;
		}
		
		final SQLiteDatabase database = getDatabase();
		if (database == null) return 0;
		
		int count = 0;
		long rowID = 0;
		
		DataTable table = getTable(tableName);
		if (table != null) {
			DataTable.DataColumn idColunm = table.getPrimaryKey();
			String idColunmName = idColunm != null ? idColunm.getName() : BaseColumns._ID;
			count = table.from(database).where(selection, selectionArgs).update(values);
			if (count == 0) {
				if (values.containsKey(idColunmName)) {
					long id = values.getAsLong(idColunmName);
					if (id < 0) {
						id = getMaxId(table.getTableName(), idColunmName);
						values.put(idColunmName, id + 1);
					}
				}
				
				rowID = table.from(database).insert(values);
			} else {
				getContext().getContentResolver().notifyChange(table.getUri(), null);
			}
		}
		
		if (rowID > 0) {
			Uri _uri = ContentUris.withAppendedId(getContentUri(), rowID);
	        getContext().getContentResolver().notifyChange(_uri, null);
	        return 1;
		}
		
		return count;
	}
	
	public long getMaxId(String tableName, String idColumn) {
		final SQLiteDatabase database = getDatabase();
		if (database == null) return 0L;
		
		long id = 0L;
		Cursor idCursor = database.rawQuery("SELECT MAX(" + idColumn + ") FROM " + tableName + " LIMIT 1;" , null);
		if (idCursor != null) {
			if (idCursor.moveToFirst()) {
				id = idCursor.getInt(0);
			}
			idCursor.close();
		}
		return id;
	}
	
	@Override
    public int bulkInsert(Uri uri, ContentValues[] insertValuesArray) {
		if (isReadonly()) {
			return 0;
		}
		
		final SQLiteDatabase database = getDatabase();
		if (database == null) return -1;
		if (insertValuesArray == null) return -1;
		
		DataTable table = getTable(uri);
		if (table != null) {
			DataTable.DataColumn idColunm = table.getPrimaryKey();
			String idColunmName = idColunm != null ? idColunm.getName() : BaseColumns._ID;
			int count = insertValuesArray.length;
			database.beginTransaction();
			
			for (int i=0; i<count; i++) {
				int resultCount = 0;	
				ContentValues values = insertValuesArray[i];
				if (values.containsKey(idColunmName)) {
					long id = values.getAsLong(idColunmName);
					if (id > 0) {
						String selection = "`" + idColunmName + "` IS ?";
						resultCount = table.from(database).where(selection, new String[] { String.valueOf(id) }).update(values);
					}
				}
				
				if (resultCount == 0) {
					table.from(database).insert(values);
				}
			}
			
			database.setTransactionSuccessful();
			database.endTransaction();
			
			getContext().getContentResolver().notifyChange(uri, null);
			return count;
		}
		return -1;
	}
	
	@Override
	public Bundle call(String method, String arg, Bundle extras) {
		Bundle resultBundle = new Bundle();
		
		if (METHOD_BACKUP.equals(method)) {
			File srcDb = getDatabasePath();
			File destDb = new File(arg);
			boolean result = copyFile(srcDb, destDb);
			resultBundle.putBoolean(RETURN_KEY, result);
			
		} else if (METHOD_RESTORE.equals(method)) {
			File srcDb = new File(arg);
			File destDb = getDatabasePath();
			boolean result = copyFile(srcDb, destDb);
			resultBundle.putBoolean(RETURN_KEY, result);
			
		} else if (METHOD_SAVE.equals(method)) {
			ContentValues values = new ContentValues();
			String[] columns = extras.getStringArray("columns");
			for(String col : columns) {
				values.put(col, extras.getString(col));
			}
			String selection = extras.getString("selection", null);
			String[] selectionArgs = extras.getStringArray("selectionArgs");
			
			int result = save(arg, values, selection, selectionArgs);
			resultBundle.putInt(RETURN_KEY, result);
			
		} else if (METHOD_GETMAXID.equals(method)) {
			long id = getMaxId(arg, BaseColumns._ID);
			resultBundle.putLong(RETURN_KEY, id);
			
		} else if (METHOD_SET_ARGUMENTS.equals(method)) {
			mArgs = arg;
			mExtra = extras;
		}
		
		return resultBundle;
	}
	
	@SuppressWarnings("resource")
	protected boolean copyFile(File srcFile, File destFile) {
		try {
            File sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite() && sd.canRead()) {
	            FileChannel src = new FileInputStream(srcFile).getChannel();
	            FileChannel dst = new FileOutputStream(destFile).getChannel();
	            dst.transferFrom(src, 0, src.size());
	            src.close();
	            dst.close();
	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	return false;
	    }
		return true;
	}
	
	protected String getArguments() {
		return mArgs;
	}
	
	protected Bundle getArgumentsExtra() {
		return mExtra;
	}
}
