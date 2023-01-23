package com.cosmoscalipers.cli;

import com.azure.cosmos.ConsistencyLevel;
import com.cosmoscalipers.constant.OperationType;
import com.cosmoscalipers.constant.ReportingFormat;
import picocli.CommandLine.Option;

public class Config {
    @Option(names = {"-e", "--endpoint"}, description = "Cosmos Service endpoint", required = true, paramLabel = "https://<ENDPOINT>.documents.azure.com:443/")
    private String host;
    @Option(names = {"-d", "--database"}, description = "Cosmos Database Name", required = true, paramLabel = "<Database Name>")
    private String database;
    @Option(names = {"-c", "--collection"}, description = "Cosmos collection name", required = true, paramLabel = "<Collection Name>")
    private String collection;
    //   private String masterKey;
    @Option(names = {"-n", "--numberofdocument"}, description = "Number of documents to be tested", required = true, paramLabel = "<Number of documents>", type = Integer.class)
    private int numberOfDocuments;
    @Option(names = {"-p", "--payloadsize"}, description = "Document size", required = true, paramLabel = "<Payload Size in KBs>", type = Integer.class)
    private int payloadSize;
    @Option(names = {"-l", "--consistency"},
            description = {
                    "Consistency level to be used (${COMPLETION-CANDIDATES}). " +
                            "If omitted the Session consistency would be used. "
            })
    private ConsistencyLevel consistencyLevel = ConsistencyLevel.SESSION;
    @Option(names = {"-ru", "--provisionedRUs"}, description = "RUs to be provisioned when Cosmos container is created", required = false, paramLabel = "<Provisioned RU's>", type = Integer.class, defaultValue = "1000")
    private int provisionedRUs;
    private int maxPoolSize;
    @Option(names = {"-r", "--retry"}, description = "Number of retry attempts", required = false, paramLabel = "<Maximum retry attempts>", type = Integer.class, defaultValue = "0")
    private int maxRetryAttempts;
    @Option(names = {"-rwi", "--retry-wait-interval"}, description = "max retry wait interval (in seconds)", required = false, paramLabel = "<Retry wait time in sec.>", type = Integer.class, defaultValue = "0")
    private int retryWaitTimeInSeconds;
    @Option(names = {"-o", "--operation"},
            description = {
                    "Primary operation being used (${COMPLETION-CANDIDATES}). " +
                            "If omitted the SQL_ALL operation would be used. "
            })
    private OperationType operation = OperationType.SQL_ALL;
    @Option(names = {"-rf", "--reporting"},
            description = {
                    "Reporting format to be used (${COMPLETION-CANDIDATES}). " +
                            "If omitted the CONSOLE operation would be used. "
            })
    private ReportingFormat reporter = ReportingFormat.CONSOLE;
    @Option(names = {"-dc", "--deleteContainer"}, description = "Delete and recreate the container (true/false), defaults to true", required = false, paramLabel = "<delete container (true/false)>", defaultValue = "true")
    private boolean deleteContainer;

    public String getHost() {
        return host;
    }

    public String getDatabase() {
        return database;
    }

    public String getCollection() {
        return collection;
    }

    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    public int getNumberOfDocuments() {
        return numberOfDocuments;
    }

    public int getPayloadSize() {
        return payloadSize;
    }

    public OperationType getOperation() {
        return operation;
    }

    public int getRetryWaitTimeInSeconds() {
        return retryWaitTimeInSeconds;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public int getProvisionedRUs() {
        return provisionedRUs;
    }

    public ReportingFormat getReporter() {
        return reporter;
    }

    public boolean isDeleteContainer() {
        return deleteContainer;
    }
}
