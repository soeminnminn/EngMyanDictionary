package com.s16.datatable;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.s16.datatable.annotation.Column;
import com.s16.datatable.annotation.PrimaryKey;
import com.s16.datatable.annotation.Table;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.s16.datatable.Constants.NAME_QUOTES;
import static com.s16.datatable.Constants.VND_PREFIX;

/**
 * Created by Soe Minn Minn on 6/17/2017.
 */

public class DataTable implements BaseColumns, Iterable<DataColumn> {

    private final Uri mBaseContentUri;
    private final String mTableName;
    private int mId;
    final ArrayList<DataColumn> mColumns = new ArrayList<DataColumn>();
    private Executor mExecutor;
    private List<TableJoiner> mJoinerList;

    public static DataTable newInstance(Class<?> cls) {
        return new DataTable(cls);
    }

    public static DataTable newInstance(Uri baseContentUri, String tableName) {
        return new DataTable(baseContentUri, tableName);
    }

    private DataTable(final Class<?> cls) {
        final AnnotatedElement element = cls;
        Table tableAnno = element.getAnnotation(Table.class);
        if (tableAnno == null) {
            throw new IllegalStateException("Class must defined Table Annotation!");
        }
        String contentUrl = tableAnno.contentUrl();
        if (contentUrl == null || "".equals(contentUrl)) {
            throw new IllegalStateException("Base Content Url not found!");
        }
        mBaseContentUri = Uri.parse(contentUrl);

        String tableName = tableAnno.name();
        mTableName = (tableName == null || "".equals(tableName)) ? cls.getSimpleName() : tableName;

        int tableId = tableAnno.id();
        mId = tableId == 0 ? mTableName.hashCode() : tableId;

        assertInstance(cls);

        mExecutor = new Executor(this, getTableName(), false);
        mJoinerList = new ArrayList<TableJoiner>();

        buildColumns(cls);
    }

    private DataTable(Uri baseContentUri, String tableName) {
        this(baseContentUri, tableName.hashCode(), tableName);
    }

    private DataTable(Uri baseContentUri, int id, String tableName) {
        if (baseContentUri == null) {
            throw new NullPointerException("Base Content Uri can not null!");
        }
        mBaseContentUri = baseContentUri;
        mTableName = tableName;
        mExecutor = new Executor(this, getTableName(), false);
        mJoinerList = new ArrayList<TableJoiner>();
        mId = id;
    }

    private void buildColumns(Class<?> cls) {
        Field[] fields = cls.getFields();
        for(Field field : fields) {
            final AnnotatedElement element = field;
            Column columnAnno = element.getAnnotation(Column.class);
            PrimaryKey primaryKeyAnno = element.getAnnotation(PrimaryKey.class);
            if (primaryKeyAnno != null || columnAnno != null) {
                String name = null;
                try {
                    name = (String) field.get(this);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                if (name != null && !"".equals(name)) {
                    if (primaryKeyAnno != null) {
                        String dataType = "".equals(primaryKeyAnno.dataType()) ? "INTEGER" : primaryKeyAnno.dataType();
                        boolean autoIncrement = primaryKeyAnno.autoIncrement();
                        addPrimaryKey(name, dataType, autoIncrement);

                    } else {
                        boolean isPrimaryKey = columnAnno.primaryKey();
                        String dataType = isPrimaryKey ? "INTEGER" : "TEXT";
                        if (!"".equals(columnAnno.dataType())) {
                            dataType = columnAnno.dataType();
                        }

                        boolean isAllowNull = isPrimaryKey ? false : !columnAnno.notNull();
                        boolean isAutoIncrement = isPrimaryKey ? columnAnno.autoIncrement() : false;
                        String defaultValue = isPrimaryKey ? null : columnAnno.defaultValue();
                        if (!isPrimaryKey) {
                            if ("NULL".equals(defaultValue)) {
                                defaultValue = isAllowNull ? "NULL" : null;
                            } else if ("".equals(defaultValue)) {
                                defaultValue = "''";
                            }
                        }
                        addColumn(name, dataType, isPrimaryKey, isAllowNull, isAutoIncrement, defaultValue);
                    }
                }
            }
        }
    }

    public int getId() {
        return mId;
    }

    public DataTable setId(int id) {
        mId = id;
        return this;
    }

    private void assertInstance(Class<?> cls) {
        try {
            cls.getField("INSTANCE");
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("INSTANCE not found in Table");
        }
    }

    protected void assertTable() {
        if (mTableName == null) {
            throw new IllegalStateException("Table not specified");
        }

        if (mColumns == null || mColumns.size() == 0) {
            throw new IllegalStateException("Columns not specified");
        }
    }

    protected boolean checkHasPrimaryKey() {
        if (mColumns == null || mColumns.size() == 0) {
            return false;
        }

        int keyCount = 0;
        for(int i = 0; i < mColumns.size(); i++) {
            if (mColumns.get(i).isPrimaryKey()) {
                keyCount++;
            }
        }

        if (keyCount > 1) {
            throw new IllegalStateException("Table does not allow more then one primary key.");
        }

        return (keyCount == 1);
    }

    @Override
    public String toString() {
        String value = mTableName;
        value += " (";
        for(int i = 0; i < mColumns.size(); i++) {
            if (i == 0) value += ", ";
            value += mColumns.toString();
        }
        value += ")";
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DataTable) {
            DataTable other = (DataTable)o;
            return mTableName.equals(other.getTableName());
        }
        return super.equals(o);
    }

    /**
     * Get the String of Uri.
     */
    public String getUriString() {
        return getUri().toString();
    }

    /**
     * Get the Uri for this table.
     */
    public Uri getUri() {
        Uri.Builder builder = mBaseContentUri.buildUpon();
        builder.appendPath(mTableName);
        return builder.build();
    }

    /**
     * Get the Uri for this table.
     */
    public Uri getUri(String uriString) {
        if (TextUtils.isEmpty(uriString)) return null;
        Uri.Builder builder = Uri.parse(uriString).buildUpon();
        builder.appendPath(mTableName);
        return builder.build();
    }

    /**
     * Get the name of table.
     */
    public String getTableName() {
        return mTableName;
    }

    /**
     * Add primary key column to table.
     */
    public DataTable addPrimaryKey(String columnName, String sqlDataType, boolean isAutoIncrement) {
        return addColumn(columnName, sqlDataType, true, false, isAutoIncrement, null);
    }

    /**
     * Add column to table.
     */
    private DataTable addColumn(DataColumn column) {
        column.setIndex(mColumns.size());
        mExecutor.putColumn(column);
        mColumns.add(column);
        return this;
    }

    /**
     * Add column to table.
     */
    public DataTable addColumn(String columnName, String sqlDataType) {
        return addColumn(columnName, sqlDataType, false, true, false, null);
    }

    /**
     * Add column to table.
     */
    public DataTable addColumn(String columnName, String sqlDataType, boolean isAllowNull) {
        return addColumn(columnName, sqlDataType, false, isAllowNull, false, null);
    }

    /**
     * Add column to table.
     */
    public DataTable addColumn(String columnName, String sqlDataType, boolean isAllowNull, String defaultValue) {
        return addColumn(columnName, sqlDataType, false, isAllowNull, false, defaultValue);
    }

    /**
     * Add column to table.
     */
    public DataTable addColumn(String columnName, String sqlDataType, boolean isPrimaryKey
            , boolean isAllowNull, boolean isAutoIncrement, String defaultValue) {

        if (isPrimaryKey && checkHasPrimaryKey()) {
            throw new IllegalStateException("Table does not allow more then one primary key.");
        }

        DataColumn column = new DataColumn(this, columnName, sqlDataType);
        column.setIsPrimaryKey(isPrimaryKey);
        column.setIsAllowNull(isAllowNull);
        column.setIsAutoIncrement(isAutoIncrement);
        column.setDefaultValue(defaultValue);
        return addColumn(column);
    }

    /**
     * Get the column of given name from table.
     */
    public DataColumn getColumn(String columnName) {
        int colIndex = indexOf(columnName);
        if (colIndex > -1) {
            return mColumns.get(colIndex);
        }
        return null;
    }

    /**
     * Get the primary key column from table.
     */
    public DataColumn getPrimaryKey() {
        assertTable();
        if (!checkHasPrimaryKey()) return null;

        DataColumn keyColumn = null;
        for(int i = 0; i < mColumns.size(); i++) {
            DataColumn column = mColumns.get(i);
            if (column.isPrimaryKey()) {
                keyColumn = column;
                break;
            }
        }

        return keyColumn;
    }

    /**
     * Get the columns name array from table.
     */
    public String[] getColumnNames() {
        assertTable();
        String[] columns = new String[mColumns.size()];
        for(int i = 0; i < mColumns.size(); i++) {
            columns[i] = mColumns.get(i).getName();
        }
        return columns;
    }

    /**
     * Get the count of columns from table.
     */
    public int getColumnCount() {
        return mColumns.size();
    }

    @Override
    public Iterator<DataColumn> iterator() {
        return new DataColumnsIterator(this);
    }

    /**
     * Get the URI pattern is for more than one row.
     */
    public String getContentTypeDir() {
        String name = getTableName().toLowerCase();
        if (name.endsWith("y")) {
            return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + VND_PREFIX + "." + name.substring(0, name.length() - 1) + "ies";
        } else if (name.endsWith("s")) {
            return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + VND_PREFIX + "." + name;
        } else {
            return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + VND_PREFIX + "." + name + "s";
        }
    }

    /**
     * Get the URI pattern is for a single row.
     */
    public String getContentTypeItem() {
        String name = getTableName().toLowerCase();
        if (name.endsWith("s")) {
            return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + VND_PREFIX + "." + name.substring(0, name.length() - 1);
        }
        return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + VND_PREFIX + "." + name;
    }

    /**
     * Get the index of column from table.
     */
    public int indexOf(String columnName) {
        if (TextUtils.isEmpty(columnName)) return -1;
        for(int i = 0; i < mColumns.size(); i++) {
            if (mColumns.get(i).getName().equals(columnName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the index of column from table.
     */
    public int indexOf(DataColumn column) {
        if (column == null) return -1;
        for(int i = 0; i < mColumns.size(); i++) {
            if (mColumns.get(i).getName().equals(column.getName())) {
                return i;
            }
        }
        return -1;
    }

    protected String createStatement(boolean withIsNotExist) {
        assertTable();
        String sql = "";
        sql += "CREATE TABLE ";
        if (withIsNotExist) {
            sql += "IF NOT EXISTS ";
        }
        sql += NAME_QUOTES + mTableName + NAME_QUOTES + " (";

        for(int i = 0; i < mColumns.size(); i++) {
            DataColumn column = mColumns.get(i);

            if (i > 0) sql += ",";
            sql += column.toString();
        }

        sql += ");";

        Log.w("createStatement", sql);
        return sql;
    }

    protected String dropStatement(boolean withIsExist) {
        assertTable();
        return "DROP TABLE " + (withIsExist ? "IF EXISTS " : "") + NAME_QUOTES + mTableName + NAME_QUOTES + ";";
    }

    /**
     * Get Query Builder
     */
    public SQLiteQueryBuilder getQueryBuilder() {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(mTableName);
        builder.setProjectionMap(mExecutor.getProjectionMap());
        return builder;
    }

    @SuppressWarnings("unchecked")
    protected <T> T typeConvertOrDefault(Object value, T defVal) {
        if (value == null) return defVal;
        if (defVal.getClass().equals(value.getClass())) {
            return (T)value;
        }
        return defVal;
    }

    /**
     * Make ContentValues
     */
    public ContentValues makeContentValues(Object... values)
            throws IllegalArgumentException {
        if (values == null || values.length != getColumnCount()) {
            throw new IllegalArgumentException("Values not specified");
        }
        ContentValues contentValues = new ContentValues();

        for(int i=0; i<mColumns.size(); i++) {
            DataColumn column = mColumns.get(i);
            if (column.isPrimaryKey() && column.isAutoIncrement()) continue;
            Object val = values[i];
            if (val == null) {
                contentValues.putNull(column.getName());
            } else if (val instanceof Short) {
                contentValues.put(column.getName(), typeConvertOrDefault(val, Short.MIN_VALUE));
            } else if (val instanceof Integer) {
                contentValues.put(column.getName(), typeConvertOrDefault(val, Integer.MIN_VALUE));
            } else if (val instanceof Long) {
                contentValues.put(column.getName(), typeConvertOrDefault(val, Long.MIN_VALUE));
            } else if (val instanceof Float) {
                contentValues.put(column.getName(), typeConvertOrDefault(val, Float.MIN_VALUE));
            } else if (val instanceof Double) {
                contentValues.put(column.getName(), typeConvertOrDefault(val, Double.MIN_VALUE));
            } else if (val instanceof Boolean) {
                contentValues.put(column.getName(), typeConvertOrDefault(val, Boolean.FALSE));
            } else if (val instanceof String) {
                contentValues.put(column.getName(), typeConvertOrDefault(val, (String)""));
            } else if (val instanceof byte[]) {
                contentValues.put(column.getName(), typeConvertOrDefault(val, (byte[])null));
            }
        }
        return contentValues;
    }

    /**
     * Make executable
     */
    public Executor from(SQLiteDatabase database) {
        if (database == null) {
            throw new IllegalArgumentException("SQLiteDatabase not specified");
        }
        assertTable();
        mExecutor.reset();
        mExecutor.setDatabase(database);
        return mExecutor;
    }

    /**
     * Access data from ContentResolver
     */
    public DataResolver from(ContentResolver contentResolver) {
        return new DataResolver(contentResolver, this);
    }

    /* package */ void addJoiner(TableJoiner value) {
        mJoinerList.add(value);
    }

    /* package */ List<TableJoiner> getJoinerList() {
        return mJoinerList;
    }

    /**
     * Set Join Condition
     */
    public TableJoiner join(DataTable other, DataColumn mainColumn, DataColumn otherColumn) {
        if (other == null) {
            throw new IllegalArgumentException("Other table not specified");
        }
        assertTable();
        TableJoiner joiner = new TableJoiner(this, other, mainColumn, otherColumn);
        mJoinerList.add(joiner);
        return joiner;
    }

    /**
     * Set Join Condition
     */
    public TableJoiner join(DataTable other, String condition) {
        if (other == null) {
            throw new IllegalArgumentException("Other table not specified");
        }
        assertTable();
        TableJoiner joiner = new TableJoiner(this, other, condition);
        mJoinerList.add(joiner);
        return joiner;
    }

    /**
     * Set Join Condition
     */
    public TableJoiner join(String joinWith, String condition) {
        if (TextUtils.isEmpty(joinWith)) {
            throw new IllegalArgumentException("joinWith not specified");
        }
        assertTable();
        TableJoiner joiner = new TableJoiner(this, joinWith, condition);
        mJoinerList.add(joiner);
        return joiner;
    }

    /**
     * Execute create table.
     */
    public void create(SQLiteDatabase db) {
        create(db, true);
    }

    /**
     * Execute create table.
     */
    public void create(SQLiteDatabase db, boolean checkIsNotExist) {
        assertTable();
        db.execSQL(createStatement(checkIsNotExist));
    }

    /**
     * Execute drop table.
     */
    public void drop(SQLiteDatabase db) {
        drop(db, true);
    }

    /**
     * Execute drop table.
     */
    public void drop(SQLiteDatabase db, boolean checkIsExist) {
        assertTable();
        db.execSQL(dropStatement(checkIsExist));
    }
}
