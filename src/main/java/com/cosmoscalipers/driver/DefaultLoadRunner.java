package com.cosmoscalipers.driver;

import com.azure.cosmos.ConsistencyLevel;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.cosmoscalipers.cli.Config;
import com.cosmoscalipers.cli.SPNConfig;
import com.cosmoscalipers.connection.async.CosmosAsyncConnection;
import com.cosmoscalipers.connection.sync.CosmosSyncConnection;
import com.cosmoscalipers.constant.Constants;
import com.cosmoscalipers.constant.OperationType;
import com.cosmoscalipers.constant.ReportingFormat;
import com.cosmoscalipers.constant.Workflow;
import com.cosmoscalipers.workload.AsyncCreateDocs;
import com.cosmoscalipers.workload.SyncCreateDocs;

import java.io.File;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class DefaultLoadRunner implements LoadRunner {

    private static final MetricRegistry metrics = new MetricRegistry();

    private Config config;

    @Override
    public void execute(final OperationType operationType) throws Exception {
        //
        ScheduledReporter reporter = startReport(config.getReporter());
        //
        if (operationType.getWorkflow() == Workflow.BOTH) {
            executeWorkflow(OperationType.ALL_SYNC_OPS);
            //
            executePostWorkflow(Workflow.SYNC);
            executeWorkflow(OperationType.ALL_ASYNC_OPS);
            //
            executePostWorkflow(Workflow.ASYNC);
        } else {
            executeWorkflow(operationType);
            //
            executePostWorkflow(operationType.getWorkflow());
        }
        //
        publishMetrics(reporter);
    }

    private void executePostWorkflow(final Workflow workflow) throws Exception {
        //
        if (workflow == Workflow.ASYNC) {
            ((CosmosAsyncConnection) getCosmosConnection(workflow)).closeConnection();
        } else if (workflow == Workflow.SYNC) {
            ((CosmosSyncConnection) getCosmosConnection(workflow)).closeConnection();
        }
        System.out.println("Payload size (in bytes) "+config.getPayloadSize());
    }

    protected void setConfig(final Config config) {
        this.config = config;
    }

    public abstract Object getCosmosConnection(final Workflow workflow);

    private void executeWorkflow(final OperationType operationType) throws Exception {
        //
        List<String> payloadIdList = executeCreateDocs(operationType.getWorkflow(),
                config.getPayloadSize(),
                config.getNumberOfDocuments());
        //
        List<Class> lstClasses = operationType.getSqlExecutors();
        for (final Class clazz : lstClasses) {
            executeWorkflow(clazz,
                    operationType,
                    payloadIdList,
                    config.getNumberOfDocuments());
        }
        //
        if (operationType.getWorkflow() == Workflow.ASYNC) {
            executeWorkflow(OperationType.SQL_ASYNC_DELETE.getSqlExecutors().get(0),
                    OperationType.SQL_ASYNC_DELETE,
                    payloadIdList,
                    config.getNumberOfDocuments());
        } else if (operationType.getWorkflow() == Workflow.SYNC) {
            executeWorkflow(OperationType.SQL_SYNC_DELETE.getSqlExecutors().get(0),
                    OperationType.SQL_SYNC_DELETE,
                    payloadIdList,
                    config.getNumberOfDocuments());
        }
    }

    private List<String> executeCreateDocs(final Workflow workflow,
                                           final int payloadSize,
                                           final int numberOfOps)
            throws Exception {
        Class clazz = (workflow == Workflow.ASYNC) ?
                AsyncCreateDocs.class : SyncCreateDocs.class;
        //
        Class parameterTypes[] = new Class[4];
        parameterTypes[0] = (workflow == Workflow.ASYNC) ?
                CosmosAsyncConnection.class : CosmosSyncConnection.class;
        parameterTypes[1] = Integer.TYPE;
        parameterTypes[2] = Integer.TYPE;
        parameterTypes[3] = MetricRegistry.class;
        //
        Object params[] = new Object[4];
        SPNConfig command = new SPNConfig();
        params[0] = getCosmosConnection(workflow);
        params[1] = numberOfOps;
        params[2] = payloadSize;
        params[3] = metrics;
        Method method = clazz.getDeclaredMethod("execute", parameterTypes);
        return (List<String>) method.invoke(clazz.newInstance(), params);
    }

    private void executeWorkflow(final Class clazz,
                                 final OperationType operationType,
                                 final List<String> payloadIdList,
                                 final int numberOfOps)
            throws Exception {
        //
        Class parameterTypes[] = new Class[4];
        parameterTypes[0] = (operationType.getWorkflow() == Workflow.ASYNC) ?
                CosmosAsyncConnection.class : CosmosSyncConnection.class;
        parameterTypes[1] = List.class;
        parameterTypes[2] = Integer.TYPE;
        parameterTypes[3] = MetricRegistry.class;
        //
        Object params[] = new Object[4];
        SPNConfig command = new SPNConfig();
        params[0] = getCosmosConnection(operationType.getWorkflow());
        params[1] = payloadIdList;
        params[2] = numberOfOps;
        params[3] = metrics;
        Method method = clazz.getDeclaredMethod("execute", parameterTypes);
        method.invoke(clazz.newInstance(), params);
    }

    private static void publishMetrics(ScheduledReporter reporter) {
        try {
            Thread.sleep(5 * 1000);
            reporter.report();
            reporter.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ScheduledReporter startReport(ReportingFormat reportingFormat) {

        ScheduledReporter reporter = null;
        ConsistencyLevel consistencyLevel = config.getConsistencyLevel();
        String consistency = consistencyLevel.toString().toLowerCase();
        String dirPostfix = consistency + "_consistency_" + LocalDate.now().format(DateTimeFormatter.ofPattern("MMddyyyy"));

        if (reportingFormat == ReportingFormat.CONSOLE) {
            reporter = ConsoleReporter.forRegistry(metrics)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build();

        } else if (reportingFormat == ReportingFormat.CSV) {
            File directory = new File(Constants.CONST_CSVFILES_LOCATION + dirPostfix);
            if (!directory.exists()) {
                boolean result = directory.mkdir();
            }
            reporter = CsvReporter.forRegistry(metrics)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build(directory);
        }
        return reporter;
    }

}