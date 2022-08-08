package com.cosmoscalipers.utils;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.cosmoscalipers.constant.Constants;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CosmosUtils {

    public static CosmosAsyncClient buildCosmosAsyncClient(ConnectionMode connectionMode,
                                                           int maxPoolSize,
                                                           int maxRetryAttempts,
                                                           int retryWaitTimeInSeconds,
                                                           String hostName,
                                                           String masterKey,
                                                           ConsistencyLevel consistencyLevel) {

        ThrottlingRetryOptions retryOptions = getRetryOptions(maxRetryAttempts, retryWaitTimeInSeconds);

        return new CosmosClientBuilder()
                .endpoint(hostName)
                .key(masterKey)
                .directMode(DirectConnectionConfig.getDefaultConfig().setIdleConnectionTimeout(Duration.ofMinutes(15)))
                .throttlingRetryOptions(retryOptions)
                .consistencyLevel(consistencyLevel)
                .buildAsyncClient();

    }

    public static CosmosClient buildCosmosClient(ConnectionMode connectionMode,
                                                 int maxPoolSize,
                                                 int maxRetryAttempts,
                                                 int retryWaitTimeInSeconds,
                                                 String hostName,
                                                 String masterKey,
                                                 ConsistencyLevel consistencyLevel) {

        ThrottlingRetryOptions retryOptions = getRetryOptions(maxRetryAttempts, retryWaitTimeInSeconds);

        return new CosmosClientBuilder()
                .endpoint(hostName)
                .key(masterKey)
                .directMode(DirectConnectionConfig.getDefaultConfig().setIdleConnectionTimeout(Duration.ofMinutes(15)))
                .throttlingRetryOptions(retryOptions)
                .consistencyLevel(consistencyLevel)
                .buildClient();

    }

    public static ThrottlingRetryOptions getRetryOptions(int maxRetryAttempts,
                                                         int retryWaitTimeInSeconds) {
        ThrottlingRetryOptions retryOptions = new ThrottlingRetryOptions();
        if(maxRetryAttempts != 0) {
            retryOptions.setMaxRetryAttemptsOnThrottledRequests(maxRetryAttempts);
        }
        if(retryWaitTimeInSeconds != 0) {
            retryOptions.setMaxRetryWaitTime(Duration.ofSeconds(retryWaitTimeInSeconds));
        }
        return retryOptions;
    }


    public static void deleteContainer(CosmosAsyncDatabase db,
                                       String collection) {

        CosmosAsyncContainer container;

        try {
            container = db.getContainer(collection);
            container.delete().block();
        } catch (Exception e) {
            System.out.println("Container " + collection + " doesn't exist");
        }

    }

    public static void deleteContainer(CosmosDatabase db,
                                       String collection) {

        CosmosContainer container;

        try {
            container = db.getContainer(collection);
            container.delete();
        } catch (Exception e) {
            System.out.println("Container " + collection + " doesn't exist");
        }

    }

    public static CosmosContainer setupContainer(CosmosDatabase db,
                                                 String collection,
                                                 int provisionedRUs) {

        CosmosContainer container;
        CosmosContainerProperties cosmosContainerProperties = getCosmosContainerProperties(collection);
        CosmosContainerResponse cosmosContainerResponse = db.createContainerIfNotExists(cosmosContainerProperties,
                ThroughputProperties.createManualThroughput(provisionedRUs));
        container = db.getContainer(collection);

        return container;

    }

    public static CosmosAsyncContainer setupContainer(CosmosAsyncDatabase db,
                                                      String collection,
                                                      int provisionedRUs) {

        CosmosAsyncContainer container;
        CosmosContainerProperties cosmosContainerProperties = getCosmosContainerProperties(collection);
        CosmosContainerResponse cosmosContainerResponse = db.createContainerIfNotExists(cosmosContainerProperties, ThroughputProperties.createManualThroughput(provisionedRUs)).block();

        container = db.getContainer(collection);

        return container;

    }

    public static CosmosContainerProperties getCosmosContainerProperties(String collection) {
        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(collection, Constants.CONST_PARTITION_KEY);
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);

        List<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath(Constants.CONST_PARTITION_KEY + "/*");
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        List<ExcludedPath> excludedPaths = new ArrayList<>();
        ExcludedPath excludedPath2 = new ExcludedPath("/*");
        excludedPaths.add(excludedPath2);
        indexingPolicy.setExcludedPaths(excludedPaths);

        cosmosContainerProperties.setIndexingPolicy(indexingPolicy);
        return cosmosContainerProperties;
    }


    public static CosmosDatabase getDB(CosmosClient client, String database) {
        CosmosDatabaseResponse databaseResponse = null;
        try {
            databaseResponse = client.createDatabaseIfNotExists(database);
        } catch (CosmosException e) {
            e.printStackTrace();
            teardown(client, true);
        }

        return client.getDatabase(database);
    }

    public static CosmosAsyncDatabase getDB(CosmosAsyncClient client,
                                            String database) {
        CosmosDatabaseResponse databaseResponse = null;
        try {
            databaseResponse = client.createDatabaseIfNotExists(database).block();
        } catch (CosmosException e) {
            e.printStackTrace();
            teardown(client, true);
        }

        return client.getDatabase(database);
    }

    public static void teardown(Closeable client,
                                boolean isShutdown) {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (isShutdown) {
            System.exit(0);
        }
    }
}
