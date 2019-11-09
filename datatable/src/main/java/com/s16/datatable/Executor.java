package com.s16.datatable;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.s16.datatable.Constants.NAME_QUOTES;

/**
 * Created by Soe Minn Minn on 6/17/2017.
 */

public final class Executor {

    private final DataTable mTable;
    private Map<String, String> mProjectionMap = new HashMap<String, String>();
    private StringBuilder mSelection = new StringBuilder();
    private List<String> mSelectionArgs = new ArrayList<String>();

    private SQLiteDatabase mDatabase;
    private String mExecTableName;
    private final boolean mIsReadOnly;

    /* package */ Executor(DataTable parent, boolean isReadOnly) {
        mTable = parent;
        mExecTableName = mTable.getTableName();
        mIsReadOnly = isReadOnly;
    }

    /* package */ Executor(DataTable parent, String tableName, boolean isReadOnly) {
        mTable = parent;
        mExecTableName = tableName;
        mIsReadOnly = isReadOnly;
    }

    private void assertTable() {
        if (mTable == null) {
            throw new IllegalStateException("Executable Table not specified");
        }
        mTable.assertTable();
    }

    private void assertDatabase() {
        if (mDatabase == null) {
            throw new IllegalStateException("Database not specified");
        }
    }

    /* package */ void setDatabase(SQLiteDatabase database) {
        mDatabase = database;
    }

    public Executor where(String id) {
        assertTable();
        mSelection.append("(").append(mTable.getPrimaryKey().getName() + "=?").append(")");
        mSelectionArgs.add(id);
        return this;
    }

    /* package */ void mapColumns(String[] columns) {
        for (int i = 0; i < columns.length; i++) {
            final String target = mProjectionMap.get(columns[i]);
            if (target != null) {
                columns[i] = target;
            }
        }
    }

    /**
     * Reset any internal state, allowing this builder to be recycled.
     */
    /* package */ void reset() {
        mSelection.setLength(0);
        mSelectionArgs.clear();
    }

    /* package */ void setExecTableName(String tableName) {
        mExecTableName = tableName;
    }

    /* package */ String getExecTableName() {
        return mExecTableName;
    }

    /* package */ void putColumn(DataColumn column) {
        mProjectionMap.put(column.getName(), NAME_QUOTES + column.getTable().getTableName() +
                NAME_QUOTES + "." + NAME_QUOTES + column.getName() + NAME_QUOTES);
    }

    /* package */ Map<String, String> getProjectionMap() {
        return mProjectionMap;
    }

    /**
     * Return selection string for current internal state.
     *
     * @see #getSelectionArgs()
     */
    /* package */ String getSelection() {
        return mSelection.toString();
    }

    /**
     * Return selection arguments for current internal state.
     *
     * @see #getSelection()
     */
    /* package */ String[] getSelectionArgs() {
        return mSelectionArgs.toArray(new String[mSelectionArgs.size()]);
    }

    /**
     * Map a column.
     */
    public Executor map(String fromColumn, String toClause) {
        mProjectionMap.put(fromColumn, toClause + " AS " + fromColumn);
        return this;
    }

    /**
     * Append the given selection clause to the internal state. Each clause is
     * surrounded with parenthesis and combined using {@code AND}.
     */
    public Executor where(String selection, String... selectionArgs) {
        assertTable();
        if (TextUtils.isEmpty(selection)) {
            if (selectionArgs != null && selectionArgs.length > 0) {
                throw new IllegalArgumentException("Valid selection required when including arguments!");
            }

            // Shortcut when clause is empty
            return this;
        }

        if (mSelection.length() > 0) {
            mSelection.append(" AND ");
        }

        mSelection.append("(").append(selection).append(")");
        if (selectionArgs != null) {
            for(String arg : selectionArgs) {
                mSelectionArgs.add(arg);
            }
        }

        return this;
    }

    /**
     * Execute query using the current internal state as {@code WHERE} clause.
     */
    public Cursor query(boolean distinct, String orderBy, String limit) {
        return query(distinct, mTable.getColumnNames(), null, null, orderBy, limit);
    }

    /**
     * Execute query using the current internal state as {@code WHERE} clause.
     */
    public Cursor query(String orderBy, String limit) {
        return query(mTable.getColumnNames(), null, null, orderBy, limit);
    }

    /**
     * Execute query using the current internal state as {@code WHERE} clause.
     */
    public Cursor query(String[] columns, String orderBy, String limit) {
        return query(columns, null, null, orderBy, limit);
    }

    /**
     * Execute query using the current internal state as {@code WHERE} clause.
     */
    public Cursor query(boolean distinct, String[] columns, String orderBy, String limit) {
        return query(distinct, columns, null, null, orderBy, limit);
    }

    /**
     * Execute query using the current internal state as {@code WHERE} clause.
     */
    public Cursor query(String[] columns, String groupBy,
                        String having, String orderBy, String limit) {
        return query(false, columns, groupBy, having, orderBy, limit);
    }

    /**
     * Execute query using the current internal state as {@code WHERE} clause.
     */
    public Cursor query(boolean distinct, String[] columns, String groupBy,
                        String having, String orderBy, String limit) {
        assertTable();
        assertDatabase();
        if (columns != null) mapColumns(columns);
        Cursor cursor = mDatabase.query(distinct, mExecTableName, columns, getSelection(), getSelectionArgs(), groupBy, having,
                orderBy, limit);
        return cursor;
    }

    /**
     * Execute update using the current internal state as {@code WHERE} clause.
     */
    public long save(ContentValues values) {
        if (values == null) {
            throw new IllegalArgumentException("Values not specified");
        }
        assertTable();
        if (mIsReadOnly) {
            throw new IllegalStateException("Table is readonly");
        }

        assertDatabase();
        long result = mDatabase.update(mExecTableName, values, getSelection(), getSelectionArgs());
        if (result < 1) {
            result = mDatabase.insert(mExecTableName, null, values);
        }
        return result;
    }

    /**
     * Execute update using the current internal state as {@code WHERE} clause.
     */
    public long insert(ContentValues values) {
        if (values == null) {
            throw new IllegalArgumentException("Values not specified");
        }
        assertTable();
        if (mIsReadOnly) {
            throw new IllegalStateException("Table is readonly");
        }

        assertDatabase();
        return mDatabase.insert(mExecTableName, null, values);
    }

    /**
     * Execute update using the current internal state as {@code WHERE} clause.
     */
    public long insert(String nullColumnHack, ContentValues values) {
        if (values == null) {
            throw new IllegalArgumentException("Values not specified");
        }
        assertTable();
        if (mIsReadOnly) {
            throw new IllegalStateException("Table is readonly");
        }

        assertDatabase();
        return mDatabase.insert(mExecTableName, nullColumnHack, values);
    }

    /**
     * Execute update using the current internal state as {@code WHERE} clause.
     */
    public int update(ContentValues values) {
        if (values == null) {
            throw new IllegalArgumentException("Values not specified");
        }
        assertTable();
        if (mIsReadOnly) {
            throw new IllegalStateException("Table is readonly");
        }

        assertDatabase();
        return mDatabase.update(mExecTableName, values, getSelection(), getSelectionArgs());
    }

    /**
     * Execute delete using the current internal state as {@code WHERE} clause.
     */
    public int delete() {
        assertTable();
        if (mIsReadOnly) {
            throw new IllegalStateException("Table is readonly");
        }

        assertDatabase();
        return mDatabase.delete(mExecTableName, getSelection(), getSelectionArgs());
    }

}
