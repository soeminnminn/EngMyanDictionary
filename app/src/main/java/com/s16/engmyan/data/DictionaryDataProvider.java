package com.s16.engmyan.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.s16.app.ReflectionUtils;
import com.s16.datatable.DataTable;
import com.s16.engmyan.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

public class DictionaryDataProvider extends ContentProvider
        implements DataContents {

    protected static final String TAG = DictionaryDataProvider.class.getSimpleName();

    public static final String AUTHORITY = "com.s16.engmyan.data.dictionarydataprovider";
    private static final String SCHEME = "content://";
    public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY);

    private static final int DATABASE_VERSION = Constants.DATA_VERSION;

    public static final DataTable TABLE_DICTIONARY = DataTable.newInstance(CONTENT_URI, DICTIONARY_TABLE)
            .addPrimaryKey(COLUMN_ID, "INTEGER", false)
            .addColumn(COLUMN_WORD, "VARCHAR")
            .addColumn(COLUMN_STRIPWORD, "VARCHAR")
            .addColumn(COLUMN_TITLE, "TEXT")
            .addColumn(COLUMN_DEFINITION, "TEXT")
            .addColumn(COLUMN_KEYWORDS, "TEXT")
            .addColumn(COLUMN_SYNONYM, "TEXT")
            .addColumn(COLUMN_FILENAME, "VARCHAR")
            .addColumn(COLUMN_PICTURE, "BOOLEAN")
            .addColumn(COLUMN_SOUND, "BOOLEAN");

    public static DataTable[] TABLES = new DataTable[] {
            TABLE_DICTIONARY
    };

    static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        for(int i=0; i<TABLES.length; i++) {
            DataTable table = TABLES[i].setId(i + 1);
            URI_MATCHER.addURI(AUTHORITY, table.getTableName(), MATCH_ALL);
            URI_MATCHER.addURI(AUTHORITY, table.getTableName() + "/#", MATCH_ID);
        }
    }

    private static class ReadOnlyDbHelper extends SQLiteOpenHelper {

        private int version = DATABASE_VERSION;

        public ReadOnlyDbHelper(Context context, File dbFile) {
            super(context, dbFile.getPath(), null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            version = oldVersion;
        }

        public int getVersion() {
            return version;
        }
    }

    private ReadOnlyDbHelper mDbHelper;
    private SQLiteDatabase mDatabase;

    private static final Method METHOD_isDatabaseIntegrityOk = ReflectionUtils.getMethod(
            SQLiteDatabase.class, "isDatabaseIntegrityOk");

    public static boolean versionCheck(Context context, File dbFile) {
        final ReadOnlyDbHelper helper = new ReadOnlyDbHelper(context, dbFile);

        boolean retValue = false;
        try {
            SQLiteDatabase dataBase = helper.getReadableDatabase();
            if (helper.getVersion() != DATABASE_VERSION) return false;
            if (!ReflectionUtils.invoke(dataBase, true, METHOD_isDatabaseIntegrityOk))
                return false;

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

    protected SQLiteDatabase getDatabase() {
        if (mDatabase == null) {
            if (mDbHelper == null) return null;
            try {
                mDatabase = mDbHelper.getReadableDatabase();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return mDatabase;
    }

    protected DataTable getTable(Uri uri) {
        int matchId = URI_MATCHER.match(uri);
        if (matchId != -1) {
            return TABLE_DICTIONARY;
        }
        return null;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    public void open(Uri url) {
        mDbHelper = new ReadOnlyDbHelper(getContext(), new File(url.toString()));
    }

    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
            mDatabase = null;
            mDbHelper = null;
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int matchId = URI_MATCHER.match(uri);
        if (matchId == MATCH_ID) {
            return TABLE_DICTIONARY.getContentTypeItem();
        } else if (matchId == MATCH_ALL) {
            return TABLE_DICTIONARY.getContentTypeDir();
        }
        return null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor result = null;
        final SQLiteDatabase database = getDatabase();
        if (database == null) return result;

        DataTable table = getTable(uri);
        if (table != null) {
            if (projection == null) {
                result = table.from(database).where(selection, selectionArgs).query(false, sortOrder, null);
            } else {
                result = table.from(database).where(selection, selectionArgs).query(false, projection, sortOrder, null);
            }
            if (result != null) {
                result.moveToFirst();
                result.setNotificationUri(getContext().getContentResolver(), uri);
            }
        }
        return result;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if (METHOD_OPEN.equals(method)) {
            open(Uri.parse(arg));
            return Bundle.EMPTY;

        } else if (METHOD_CLOSE.equals(method)) {
            close();
            return Bundle.EMPTY;
        }

        return super.call(method, arg, extras);
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {

        String fileName = "";
        List<String> segments = uri.getPathSegments();
        for(int i=0; i<segments.size(); i++) {
            if (fileName.length() == 0) {
                fileName += segments.get(i);
            } else {
                fileName += "/" + segments.get(i);
            }
        }

        if (!TextUtils.isEmpty(fileName)) {
            Log.i(TAG, "assetsFileName = " + fileName);
            try {
                AssetFileDescriptor descriptor = getContext().getAssets().openFd(fileName);
                return descriptor.getParcelFileDescriptor();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return super.openFile(uri, mode);
    }
}
