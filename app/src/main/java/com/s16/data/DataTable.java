package com.s16.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class DataTable implements Iterable<DataTable.DataColumn> {
	
	protected static final String VND_PREFIX = "vnd.datatable";
	protected static final String NAME_QUOTES = "`";
	
	protected static final String[] DATA_TYPES = new String[] {
		"VARCHAR", "NVARCHAR", "TEXT", "INTEGER", "REAL", "FLOAT", 
		"BOOLEAN", "CLOB", "BLOB", "TIMESTAMP", "NUMERIC", 
		"VARYING CHARACTER", "NATIONAL VARYING CHARACTER", "NONE"
	};
	
	private final Uri mBaseContentUri;
	private final String mTableName;
	private int mId;
	private final ArrayList<DataColumn> mColumns = new ArrayList<DataColumn>();
	private SQLiteDatabase mDatabase;
	private Executor mExecutor;
	private List<TableJoiner> mJoinerList;
	
	public static DataTable newInstance(Uri baseContentUri, String tableName) {
		return new DataTable(baseContentUri, tableName); 
	}
	
	public static DataTable newInstance(Uri baseContentUri, int id, String tableName) {
		return new DataTable(baseContentUri, id, tableName); 
	}
	
	private DataTable(Uri baseContentUri, String tableName) {
		this(baseContentUri, tableName.hashCode(), tableName);
	}
	
	private DataTable(Uri baseContentUri, int id, String tableName) {
		if (baseContentUri == null) {
			throw new NullPointerException();
		}
		mBaseContentUri = baseContentUri;
		mTableName = tableName;
		mExecutor = new Executor(getTableName(), false);
		mJoinerList = new ArrayList<TableJoiner>();
		mId = id;
	}
	
	public int getId() {
		return mId;
	}

	public DataTable setId(int id) {
		mId = id;
		return this;
	}

	protected void assertTable() {
        if (mTableName == null) {
            throw new IllegalStateException("Table not specified");
        }
        
        if (mColumns == null || mColumns.size() == 0) {
            throw new IllegalStateException("Columns not specified");
        }
    }
	
	protected void assertDatabase() {
        if (mDatabase == null) {
            throw new IllegalStateException("Databse not specified");
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
		column.mIndex = mColumns.size();
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
		column.mIsPrimaryKey = isPrimaryKey;
		column.mIsAllowNull = isAllowNull;
		column.mIsAutoIncrement = isAutoIncrement;
		column.mDefaultValue = defaultValue;
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
        return new DataColumnsIterator();
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
    public Executor from(SQLiteDatabase db) {
    	if (db == null) {
    		throw new IllegalArgumentException("SQLiteDatabase not specified");
    	}
    	assertTable();
    	mExecutor.reset();
    	mDatabase = db;
    	return mExecutor;
    }
    
    /**
     * Set Join Condition
     */
    public TableJoiner join(DataTable other, DataColumn mainColumn, DataColumn otherColumn) {
    	if (other == null) {
    		throw new IllegalArgumentException("Other table not specified");
    	}
    	assertTable();
    	TableJoiner joiner = new TableJoiner(other, mainColumn, otherColumn);
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
    	TableJoiner joiner = new TableJoiner(other, condition);
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
    	TableJoiner joiner = new TableJoiner(joinWith, condition);
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

	public class DataColumn {
		
		private int mIndex;
		private String mName;
		private String mDataType;
		private boolean mIsPrimaryKey;
		private boolean mIsAllowNull;
		private boolean mIsAutoIncrement;
		private String mDefaultValue;
		private String mExtended;
		private final DataTable mTable;
		private String mActualType;
		
		private DataColumn(DataTable table, String columnName, String sqlDataType) {
			mTable = table;
			mName = columnName;
			mDataType = sqlDataType;
			mIsAllowNull = true;
			mIndex = -1;
			
			if (!TextUtils.isEmpty(mDataType)) {
				String type = mDataType.trim();
				Pattern pattern = Pattern.compile("^([^\\s]+)([\\s]+\\(|\\()([\\d]+)\\)$");
				Matcher m = pattern.matcher(type);
				if (m.matches()) {
					type = m.group(0);
				}
				mActualType = type.trim().toUpperCase();
			}
		}
		
		public DataTable getTable() {
			return mTable;
		}
		
		public int getIndex() {
			return mIndex;
		}
		
		public String getName() {
			return mName;
		}
		
		public String getDataType() {
			return mDataType;
		}
		
		public String getActualDataType() {
			return mActualType;
		}
		
		public Class<?> getJavaType() {
			String dataType = getActualDataType();
			if (!TextUtils.isEmpty(dataType)) {
				if ("TEXT".equals(dataType) || "VARCHAR".equals(dataType) || "NVARCHAR".equals(dataType)) {
					return String.class;
				} else if ("INTEGER".equals(dataType)) {
					return Long.class;
				} else if ("FLOAT".equals(dataType)) {
					return Float.class;
				} else if ("REAL".equals(dataType) || "NUMERIC".equals(dataType)) {
					return Double.class;
				} else if ("BOOLEAN".equals(dataType)) {
					return Boolean.class;
				} else {
					return String.class;
				}
			}
			return Object.class;
		}
		
		public boolean isPrimaryKey() {
			return mIsPrimaryKey;
		}
		
		public boolean isAllowNull() {
			return mIsAllowNull;
		}
		
		public boolean isAutoIncrement() {
			return mIsAutoIncrement;
		}
		
		public String getDefaultValue() {
			return mDefaultValue;
		}
		
		public String getExtended() {
			return mExtended;
		}
		
		@Override
	    public String toString() {
			return NAME_QUOTES + mName + NAME_QUOTES
					+ " " + mDataType
					+ (mIsPrimaryKey ? " PRIMARY KEY" : "")
					+ (mIsAutoIncrement ? " AUTOINCREMENT" : "")
					+ (!mIsAllowNull ? " NOT NULL" : "")
					+ (mDefaultValue != null ? " DEFAULT " + mDefaultValue : "")
					+ (mExtended != null ? " " + mExtended : "");
		}
	}
	
	public class DataColumnsIterator implements Iterator<DataColumn> {

		private int mColumnIndex = -1;
		
		@Override
		public boolean hasNext() {
			if (mColumns == null) return false;
			int idx = mColumnIndex + 1;
			return (idx < mColumns.size());
		}

		@Override
		public DataColumn next() {
			if (mColumns == null) return null;
			
			mColumnIndex++;
			if (mColumnIndex < mColumns.size()) {
				return mColumns.get(mColumnIndex);
			}
			return null;
		}

		@Override
		public void remove() {
			mColumnIndex = -1;
		}
	}
	
	public class TableJoiner {

		private DataTable mOtherTable;
		private String mJoinWith;
		private String mCondition;
		
		private TableJoiner(DataTable other, DataColumn mainColumn, DataColumn otherColumn) {
			if (other == null) {
				throw new IllegalArgumentException("Other table not specified");
			}
			if (mainColumn == null) {
				throw new IllegalArgumentException("Main column not specified");
			}
			if (otherColumn == null) {
				throw new IllegalArgumentException("Other column not specified");
			}
			mOtherTable = other;
			
			mCondition = "`" + mainColumn.getTable().getTableName() + "`";
			mCondition += ".`" + mainColumn.getName() + "`";
			mCondition += " = `" + otherColumn.getTable().getTableName() + "`";
			mCondition += ".`" + otherColumn.getName() + "`";
		}
		
		private TableJoiner(DataTable other, String condition) {
			if (other == null) {
				throw new IllegalArgumentException("Other table not specified");
			}
			if (TextUtils.isEmpty(condition)) {
				throw new IllegalArgumentException("Condition not specified");
			}
			
			mOtherTable = other;
			mCondition = condition;
		}
		
		private TableJoiner(String joinWith, String condition) {
			if (TextUtils.isEmpty(joinWith)) {
				throw new IllegalArgumentException("joinWith not specified");
			}
			if (TextUtils.isEmpty(condition)) {
				throw new IllegalArgumentException("Condition not specified");
			}
			
			mJoinWith = joinWith;
			mCondition = condition;
		}
		
		public DataTable getTable() {
			return mOtherTable;
		}
		
		public String getJoinable() {
			return mJoinWith;
		}
		
		public String getCondition() {
			return mCondition;
		}
		
		public TableJoiner join(DataTable other, DataColumn mainColumn, DataColumn otherColumn) {
			if (other == null) {
	    		throw new IllegalArgumentException("Other table not specified");
	    	}
	    	assertTable();
	    	TableJoiner joiner = new TableJoiner(other, mainColumn, otherColumn);
	    	mJoinerList.add(joiner);
	    	return joiner;
	    } 
		
		public TableJoiner join(DataTable other, String condition) {
	    	if (other == null) {
	    		throw new IllegalArgumentException("Other table not specified");
	    	}
	    	assertTable();
	    	TableJoiner joiner = new TableJoiner(other, condition);
	    	mJoinerList.add(joiner);
	    	return joiner;
	    }
		
		public TableJoiner join(String joinWith, String condition) {
	    	if (TextUtils.isEmpty(joinWith)) {
	    		throw new IllegalArgumentException("joinWith not specified");
	    	}
	    	assertTable();
	    	TableJoiner joiner = new TableJoiner(joinWith, condition);
	    	mJoinerList.add(joiner);
	    	return joiner;
	    }
		
		public Executor from(SQLiteDatabase db) {
			Executor executor = new Executor(true);
			
			// put main table's columns
			Iterator<DataColumn> iter = iterator();
			while(iter.hasNext()) {
				executor.putColumn(iter.next());
			}
			
			// put joined table's columns
			for(TableJoiner joiner : mJoinerList) {
				if (joiner.getTable() != null) {
					iter = joiner.getTable().iterator();
					while(iter.hasNext()) {
						executor.putColumn(iter.next());
					}
				}
			}
			
			StringBuilder joinBuilder = new StringBuilder();
			joinBuilder.append("`" + getTableName() + "`");
			
			for(TableJoiner joiner : mJoinerList) {
				joinBuilder.append(" JOIN ");
				if (joiner.getTable() != null) {
					joinBuilder.append("`" + joiner.getTable().getTableName() + "`");
				} else {
					joinBuilder.append(joiner.getJoinable());
				}
				joinBuilder.append(" ON ");
				joinBuilder.append("(" + joiner.getCondition() + ")");
			}
			
			executor.setExecTableName(joinBuilder.toString());
			return executor;
		}
	}
	
	public class Executor {
		
		private Map<String, String> mProjectionMap = new HashMap<String, String>();
	    private StringBuilder mSelection = new StringBuilder();
	    private List<String> mSelectionArgs = new ArrayList<String>();
	    
	    private String mExecTableName;
	    private final boolean mIsReadOnly;
	    
	    private Executor(boolean isReadOnly) {
	    	mExecTableName = getTableName();
	    	mIsReadOnly = isReadOnly;
	    }
	    
	    private Executor(String tableName, boolean isReadOnly) {
	    	mExecTableName = tableName;
	    	mIsReadOnly = isReadOnly;
	    }
	    
	    public Executor where(String id) {
	    	assertTable();
	    	mSelection.append("(").append(getPrimaryKey().getName() + "=?").append(")");
	    	mSelectionArgs.add(id);
	    	return this;
	    }
	    
	    protected void mapColumns(String[] columns) {
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
	    protected void reset() {
	        mSelection.setLength(0);
	        mSelectionArgs.clear();
	    }
	    
	    protected void setExecTableName(String tableName) {
	    	mExecTableName = tableName;
	    }
	    
	    protected String getExecTableName() {
	    	return mExecTableName;
	    }
	    
	    protected void putColumn(DataColumn column) {
	    	mProjectionMap.put(column.getName(), "`" + column.getTable().getTableName() + "`.`" + column.getName() + "`");
	    }
	    
	    protected Map<String, String> getProjectionMap() {
	    	return mProjectionMap;
	    }
	    
	    /**
	     * Return selection string for current internal state.
	     *
	     * @see #getSelectionArgs()
	     */
	    protected String getSelection() {
	        return mSelection.toString();
	    }

	    /**
	     * Return selection arguments for current internal state.
	     *
	     * @see #getSelection()
	     */
	    protected String[] getSelectionArgs() {
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
	                throw new IllegalArgumentException("Valid selection required when including arguments=");
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
	        return query(distinct, getColumnNames(), null, null, orderBy, limit);
	    }
	    
	    /**
	     * Execute query using the current internal state as {@code WHERE} clause.
	     */
	    public Cursor query(String orderBy, String limit) {
	        return query(getColumnNames(), null, null, orderBy, limit);
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
}
