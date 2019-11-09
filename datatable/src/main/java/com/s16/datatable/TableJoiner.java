package com.s16.datatable;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.Iterator;

import static com.s16.datatable.Constants.NAME_QUOTES;

/**
 * Created by Soe Minn Minn on 6/17/2017.
 */

public final class TableJoiner {

    private final DataTable mTable;
    private DataTable mOtherTable;
    private String mJoinWith;
    private String mCondition;

    /* package */ TableJoiner(DataTable parent, DataTable other, DataColumn mainColumn, DataColumn otherColumn) {
        if (other == null) {
            throw new IllegalArgumentException("Other table not specified");
        }
        if (mainColumn == null) {
            throw new IllegalArgumentException("Main column not specified");
        }
        if (otherColumn == null) {
            throw new IllegalArgumentException("Other column not specified");
        }

        mTable = parent;
        mOtherTable = other;

        mCondition = NAME_QUOTES + mainColumn.getTable().getTableName() + NAME_QUOTES;
        mCondition += "." + NAME_QUOTES + mainColumn.getName() + NAME_QUOTES;
        mCondition += " = " + NAME_QUOTES + otherColumn.getTable().getTableName() + NAME_QUOTES;
        mCondition += "." + NAME_QUOTES + otherColumn.getName() + NAME_QUOTES;
    }

    /* package */ TableJoiner(DataTable parent, DataTable other, String condition) {
        if (other == null) {
            throw new IllegalArgumentException("Other table not specified");
        }
        if (TextUtils.isEmpty(condition)) {
            throw new IllegalArgumentException("Condition not specified");
        }

        mTable = parent;
        mOtherTable = other;
        mCondition = condition;
    }

    /* package */ TableJoiner(DataTable parent, String joinWith, String condition) {
        if (TextUtils.isEmpty(joinWith)) {
            throw new IllegalArgumentException("joinWith not specified");
        }
        if (TextUtils.isEmpty(condition)) {
            throw new IllegalArgumentException("Condition not specified");
        }

        mTable = parent;
        mJoinWith = joinWith;
        mCondition = condition;
    }

    private void assertTable() {
        if (mTable == null) {
            throw new IllegalStateException("Executable Table not specified");
        }
        mTable.assertTable();
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
        TableJoiner joiner = new TableJoiner(mOtherTable, other, mainColumn, otherColumn);
        mTable.addJoiner(joiner);
        return joiner;
    }

    public TableJoiner join(DataTable other, String condition) {
        if (other == null) {
            throw new IllegalArgumentException("Other table not specified");
        }
        assertTable();
        TableJoiner joiner = new TableJoiner(mTable, other, condition);
        mTable.addJoiner(joiner);
        return joiner;
    }

    public TableJoiner join(String joinWith, String condition) {
        if (TextUtils.isEmpty(joinWith)) {
            throw new IllegalArgumentException("joinWith not specified");
        }
        assertTable();
        TableJoiner joiner = new TableJoiner(mTable, joinWith, condition);
        mTable.addJoiner(joiner);
        return joiner;
    }

    public Executor from(SQLiteDatabase database) {
        assertTable();

        Executor executor = new Executor(mTable, true);
        executor.setDatabase(database);

        // put main table's columns
        Iterator<DataColumn> iterator = mTable.iterator();
        while(iterator.hasNext()) {
            executor.putColumn(iterator.next());
        }

        // put joined table's columns
        for(TableJoiner joiner : mTable.getJoinerList()) {
            if (joiner.getTable() != null) {
                iterator = joiner.getTable().iterator();
                while(iterator.hasNext()) {
                    executor.putColumn(iterator.next());
                }
            }
        }

        StringBuilder joinBuilder = new StringBuilder();
        joinBuilder.append(NAME_QUOTES + mTable.getTableName() + NAME_QUOTES);

        for(TableJoiner joiner : mTable.getJoinerList()) {
            joinBuilder.append(" JOIN ");
            if (joiner.getTable() != null) {
                joinBuilder.append(NAME_QUOTES + joiner.getTable().getTableName() + NAME_QUOTES);
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
