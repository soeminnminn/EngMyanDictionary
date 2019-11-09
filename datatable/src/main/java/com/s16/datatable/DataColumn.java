package com.s16.datatable;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.s16.datatable.Constants.NAME_QUOTES;

/**
 * Created by Soe Minn Minn on 6/17/2017.
 */

public final class DataColumn {

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

    /* package */ DataColumn(DataTable table, String columnName, String sqlDataType) {
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

    /* package */ DataColumn setIndex(int value) {
        mIndex = value;
        return this;
    }

    public int getIndex() {
        return mIndex;
    }

    /* package */ DataColumn setName(String value) {
        mName = value;
        return this;
    }

    public String getName() {
        return mName;
    }

    /* package */ DataColumn setDataType(String value) {
        mDataType = value;
        return this;
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

    /* package */ DataColumn setIsPrimaryKey(boolean value) {
        mIsPrimaryKey = value;
        return this;
    }

    public boolean isPrimaryKey() {
        return mIsPrimaryKey;
    }

    /* package */ DataColumn setIsAllowNull(boolean value) {
        mIsAllowNull = value;
        return this;
    }

    public boolean isAllowNull() {
        return mIsAllowNull;
    }

    /* package */ DataColumn setIsAutoIncrement(boolean value) {
        mIsAutoIncrement = value;
        return this;
    }

    public boolean isAutoIncrement() {
        return mIsAutoIncrement;
    }

    /* package */ DataColumn setDefaultValue(String value) {
        mDefaultValue = value;
        return this;
    }

    public String getDefaultValue() {
        return mDefaultValue;
    }

    /* package */ DataColumn setExtended(String value) {
        mExtended = value;
        return this;
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
