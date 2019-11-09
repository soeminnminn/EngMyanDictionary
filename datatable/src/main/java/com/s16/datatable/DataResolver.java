package com.s16.datatable;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by Soe Minn Minn on 6/17/2017.
 */

public final class DataResolver {

    private final ContentResolver mContentResolver;
    private final DataTable mTable;

    /* package */ DataResolver(ContentResolver contentResolver, DataTable table) {
        mContentResolver = contentResolver;
        mTable = table;
    }

    /**
     * See {@link ContentResolver#query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) }
     */
    public Cursor query(@Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if (projection == null) {
            projection = mTable.getColumnNames();
        }
        return mContentResolver.query(mTable.getUri(), projection, selection, selectionArgs, sortOrder);
    }

    /**
     * See {@link ContentResolver#insert(Uri uri, ContentValues values) }
     */
    public Uri insert(@Nullable ContentValues values) {
        return mContentResolver.insert(mTable.getUri(), values);
    }

    /**
     * See {@link ContentResolver#update(Uri uri, ContentValues values, String selection, String[] selectionArgs) }
     */
    public int update(@Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return mContentResolver.update(mTable.getUri(), values, selection, selectionArgs);
    }

    /**
     * See {@link ContentResolver#delete(Uri uri, String selection, String[] selectionArgs) }
     */
    public int delete(@Nullable String selection, @Nullable String[] selectionArgs) {
        return mContentResolver.delete(mTable.getUri(), selection, selectionArgs);
    }

}
