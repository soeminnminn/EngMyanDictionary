package com.s16.datatable;

import java.util.Iterator;

/**
 * Created by Soe Minn Minn on 6/17/2017.
 */

public final class DataColumnsIterator implements Iterator<DataColumn> {

    private final DataTable mTable;
    private int mColumnIndex = -1;

    DataColumnsIterator(DataTable table) {
        mTable = table;
    }

    @Override
    public boolean hasNext() {
        if (mTable.mColumns == null) return false;
        int idx = mColumnIndex + 1;
        return (idx < mTable.mColumns.size());
    }

    @Override
    public DataColumn next() {
        if (mTable.mColumns == null) return null;

        mColumnIndex++;
        if (mColumnIndex < mTable.mColumns.size()) {
            return mTable.mColumns.get(mColumnIndex);
        }
        return null;
    }

    @Override
    public void remove() {
        mColumnIndex = -1;
    }
}
