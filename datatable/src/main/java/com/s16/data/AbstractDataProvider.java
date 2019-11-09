package com.s16.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.s16.datatable.DataColumn;
import com.s16.datatable.DataTable;
import com.s16.datatable.annotation.Table;

public abstract class AbstractDataProvider extends ContentProvider implements BaseColumns {

	protected static final String TAG = AbstractDataProvider.class.getSimpleName();
	
	public static final String METHOD_BACKUP = "backup";
	public static final String METHOD_RESTORE = "restore";
	public static final String METHOD_SAVE = "save";
	public static final String METHOD_GETMAXID = "getMaxId";
	public static final String RETURN_KEY = "result";
	
	private static final String DEFAULT_DATABASE_NAME = "database";
	private static final int DEFAULT_DATABASE_VERSION = 1;
	
	protected static int LOWORD(int val) { return val & 0xffff; }
	protected static int HIWORD(int val) { return (val >> 0x10) & 0xffff; }
	protected static int MAKELPARAM(int low, int high) { return ((high << 0x10) | (low & 0xffff)); }

	public static final String PATH_ANY = "any";
	public static final int MATCH_ANY = 0;
	public static final int MATCH_ALL = 1;
	public static final int MATCH_ID = 2;

	protected final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	
	private SQLiteDatabase mDatabase;
	private String mDatabasePath;
	
	private class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, mDatabaseName, null, mDatabaseVersion);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			if (mTables == null) return;
			for(int i = 0; i < mTables.length; i++) {
				mTables[i].create(db);
			}
			onCreateHelper(db);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (mTables == null) return;
			for(int i = 0; i < mTables.length; i++) {
				mTables[i].drop(db);
			}
			onUpdateHelper(db, oldVersion, newVersion);
			onCreate(db);
		}
	}
	
	private final String mDatabaseName;
	private final int mDatabaseVersion;
	private final DataTable[] mTables;
	private DatabaseHelper mDbHelper;
	
	public AbstractDataProvider()  {
		super();
		mDatabaseName = getDatabaseName();
		mDatabaseVersion = getDatabaseVersion();
		mTables = getAllTables();

		buildUriMatcher();
	}

	private void assertMembers() {
		if (Uri.EMPTY.equals(getContentUri())) {
			throw new IllegalStateException("CONTENT_URI not specified");
		}
		if (mTables == null || mTables.length == 0) {
			throw new IllegalStateException("DataTable not found in contents");
		}
	}
	
	protected String getDatabaseName() {
		return DEFAULT_DATABASE_NAME;
	}
	
	protected int getDatabaseVersion() {
		return DEFAULT_DATABASE_VERSION;
	}
	
	protected Uri getContentUri() {
		try {
			Field field = getClass().getField("CONTENT_URI");
			if (field != null) {
				return (Uri)field.get(this);
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return Uri.EMPTY;
	}
	
	protected DataTable[] getAllTables() {
		if (mTables != null) {
			return mTables;
		}
		List<DataTable> tablesList = new ArrayList<DataTable>();
		Class<?>[] classes = getClass().getClasses();
		for(Class<?> cls : classes) {
			final AnnotatedElement element = cls;
			Table tableAnno = element.getAnnotation(Table.class);
			if (tableAnno != null) {
				DataTable table = null;
				Field field = null;

				try {
					field = cls.getField("INSTANCE");
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}

				if (field != null) {
					try {
						table = (DataTable)field.get(null);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					if (table != null) {
						tablesList.add(table);
					}
				}
			}
		}
		DataTable[] tables = new DataTable[tablesList.size()];
		tables = tablesList.toArray(tables);
		return tables;
	}
	
	protected DataTable getTable(Uri uri) {
		int matchId = URI_MATCHER.match(uri);
		if (matchId != -1) {
			return mTables[LOWORD(matchId) - 1];
		}
		throw new IllegalArgumentException("Unsupported URI: " + uri);
	}

	protected void buildUriMatcher() {
		assertMembers();
		Uri contentUri = getContentUri();
		URI_MATCHER.addURI(contentUri.getAuthority(), PATH_ANY, MAKELPARAM(0, MATCH_ANY));
		for(int i=0; i<mTables.length; i++) {
			URI_MATCHER.addURI(contentUri.getAuthority(), mTables[i].getTableName(), MAKELPARAM(i + 1, MATCH_ALL));
			URI_MATCHER.addURI(contentUri.getAuthority(), mTables[i].getTableName() + "/#", MAKELPARAM(i + 1, MATCH_ID));
		}
	}
	
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

	protected int getUriType(Uri uri) {
		int matchId = URI_MATCHER.match(uri);
		if (matchId != -1) {
			return HIWORD(matchId);
		}
		return -1;
	}
	
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
			try {
				mDatabase = mDbHelper.getWritableDatabase();
			} catch (SQLException ex) {
				ex.printStackTrace();
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
	
	public boolean isOpen() {
		SQLiteDatabase database = getDatabase();
		return (database != null) && (database.isOpen());
	}
	
	public void close() {
		if (mDbHelper != null) mDbHelper.close();
	}
	
	@Override
	public boolean onCreate() {
		if(mDbHelper == null) {
			mDbHelper = new DatabaseHelper(getContext());
		}
		
		SQLiteDatabase database = getDatabase();
		if (database != null) {
			mDatabasePath = database.getPath();
		}
		
		return (database != null);
	}

	protected Cursor queryAny(String methodName, String selection, String[] selectionArgs, String sortOrder) {
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor result = null;
		final SQLiteDatabase database = getDatabase();
		if (database == null) return result;

		int uriType = getUriType(uri);
		if (uriType == MATCH_ANY) {
			String method = uri.getQueryParameter("method");
			return queryAny(method, selection, selectionArgs, sortOrder);

		} else {
			boolean distinct = useDistinct(uri);
			DataTable table = getTable(uri);
			if (table != null) {
				if (projection == null) {
					result = table.from(database).where(selection, selectionArgs).query(distinct, sortOrder, null);
				} else {
					result = table.from(database).where(selection, selectionArgs).query(distinct, projection, sortOrder, null);
				}
				if (result != null) {
					result.moveToFirst();
					result.setNotificationUri(getContext().getContentResolver(), uri);
				}
			}
			return result;
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final SQLiteDatabase database = getDatabase();
		if (database == null) return null;
		
		long rowID = 0;
		DataTable table = getTable(uri);
		if (table != null) {
			DataColumn idColumn = table.getPrimaryKey();
			String idColumnName = idColumn != null ? idColumn.getName() : BaseColumns._ID;
			if (values.containsKey(idColumnName)) {
				long id = values.getAsLong(idColumnName);
				if (id < 0) {
					id = getMaxId(table.getTableName(), idColumnName);
					values.put(idColumnName, id + 1);
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
		final SQLiteDatabase database = getDatabase();
		if (database == null) return 0;
		
		int count = 0;
		long rowID = 0;
		
		DataTable table = getTable(tableName);
		if (table != null) {
			DataColumn idColumn = table.getPrimaryKey();
			String idColumnName = idColumn != null ? idColumn.getName() : BaseColumns._ID;
			count = table.from(database).where(selection, selectionArgs).update(values);
			if (count == 0) {
				if (values.containsKey(idColumnName)) {
					long id = values.getAsLong(idColumnName);
					if (id < 0) {
						id = getMaxId(table.getTableName(), idColumnName);
						values.put(idColumnName, id + 1);
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
		final SQLiteDatabase database = getDatabase();
		if (database == null) return -1;
		if (insertValuesArray == null) return -1;
		
		DataTable table = getTable(uri);
		if (table != null) {
			DataColumn idColumn = table.getPrimaryKey();
			String idColumnName = idColumn != null ? idColumn.getName() : BaseColumns._ID;
			int count = insertValuesArray.length;
			database.beginTransaction();
			
			for (int i=0; i<count; i++) {
				int resultCount = 0;	
				ContentValues values = insertValuesArray[i];
				if (values.containsKey(idColumnName)) {
					long id = values.getAsLong(idColumnName);
					if (id > 0) {
						String selection = "`" + idColumnName + "` IS ?";
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
			String selection = extras.getString("selection");
			String[] selectionArgs = extras.getStringArray("selectionArgs");
			
			int result = save(arg, values, selection, selectionArgs);
			resultBundle.putInt(RETURN_KEY, result);
			
		} else if (METHOD_GETMAXID.equals(method)) {
			long id = getMaxId(arg, BaseColumns._ID);
			resultBundle.putLong(RETURN_KEY, id);
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
}
