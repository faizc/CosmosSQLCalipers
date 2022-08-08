package com.cosmoscalipers.constant;

import com.cosmoscalipers.workload.*;

import java.util.Arrays;
import java.util.List;

public enum OperationType {

    SQL_ASYNC_PARTITION_KEY_READ("SQL_ASYNC_PARTITION_KEY_READ", Workflow.ASYNC, Arrays.asList(SQLAsyncRead.class)),
    SQL_SYNC_PARTITION_KEY_READ("SQL_SYNC_PARTITION_KEY_READ", Workflow.SYNC, Arrays.asList(SQLSyncRead.class)),
    SQL_SYNC_POINT_READ("SQL_SYNC_POINT_READ", Workflow.SYNC, Arrays.asList(SQLSyncPointRead.class)),
    SQL_ASYNC_POINT_READ("SQL_ASYNC_POINT_READ", Workflow.ASYNC, Arrays.asList(SQLAsyncPointRead.class)),
    SQL_ALL("SQL_ALL", Workflow.BOTH, Arrays.asList(/*SQLSyncRead.class, SQLSyncPointRead.class, SQLSyncReadAllItems.class, SQLSyncUpsert.class, SQLSyncReplace.class, SQLAsyncRead.class, SQLAsyncPointRead.class, SQLAsyncReadAllItems.class, SQLAsyncUpsert.class, SQLAsyncReplace.class*/)),
    ALL_SYNC_OPS("ALL_SYNC_OPS", Workflow.SYNC, Arrays.asList(SQLSyncRead.class, SQLSyncPointRead.class, SQLSyncReadAllItems.class, SQLSyncUpsert.class, SQLSyncReplace.class)),
    ALL_ASYNC_OPS("ALL_ASYNC_OPS", Workflow.ASYNC, Arrays.asList(SQLAsyncRead.class, SQLAsyncPointRead.class, SQLAsyncReadAllItems.class, SQLAsyncUpsert.class, SQLAsyncReplace.class)),
    SQL_SYNC_UPSERT("SQL_SYNC_UPSERT", Workflow.SYNC, Arrays.asList(SQLSyncUpsert.class)),
    SQL_ASYNC_UPSERT("SQL_ASYNC_UPSERT", Workflow.ASYNC, Arrays.asList(SQLAsyncUpsert.class)),
    SQL_SYNC_REPLACE("SQL_SYNC_REPLACE", Workflow.SYNC, Arrays.asList(SQLSyncReplace.class)),
    SQL_ASYNC_REPLACE("SQL_ASYNC_REPLACE", Workflow.ASYNC, Arrays.asList(SQLAsyncReplace.class)),
    SQL_SYNC_READ_ALL_ITEMS("SQL_SYNC_READ_ALL_ITEMS", Workflow.SYNC, Arrays.asList(SQLSyncReadAllItems.class)),
    SQL_ASYNC_READ_ALL_ITEMS("SQL_ASYNC_READ_ALL_ITEMS", Workflow.ASYNC, Arrays.asList(SQLAsyncReadAllItems.class)),
    SQL_SYNC_DELETE("SQL_SYNC_DELETE", Workflow.SYNC, Arrays.asList(SQLSyncDelete.class)),
    SQL_ASYNC_DELETE("SQL_ASYNC_DELETE", Workflow.ASYNC, Arrays.asList(SQLAsyncDelete.class));

    private final String operation;

    private Workflow workflow;

    private List<Class> sqlExecutors;

    private OperationType(final String operation, final Workflow workflow, final List<Class> sqlExecutors) {
        this.operation = operation;
        this.workflow = workflow;
        this.sqlExecutors = sqlExecutors;
    }

    public List<Class> getSqlExecutors() {
        return sqlExecutors;
    }

    public String getOperation() {
        return operation;
    }

    public Workflow getWorkflow() {
        return workflow;
    }
}
