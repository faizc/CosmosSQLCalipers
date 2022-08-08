package com.cosmoscalipers.connection.async;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.cosmoscalipers.cli.KeyConfig;
import com.cosmoscalipers.constant.Constants;
import com.cosmoscalipers.utils.CosmosUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class CosmosKeyAsyncConnection extends CosmosAsyncConnection {

    private static CosmosKeyAsyncConnection instance;

    private KeyConfig keyConfig;

    private CosmosKeyAsyncConnection() {
    }

    public CosmosAsyncClient initConnection(final KeyConfig keyConfig) {
        if (client == null) {
            //
            this.keyConfig = keyConfig;
            //
            ThrottlingRetryOptions retryOptions = CosmosUtils.getRetryOptions(
                    keyConfig.getMaxRetryAttempts(),
                    keyConfig.getRetryWaitTimeInSeconds());
            //
            client = new CosmosClientBuilder()
                    .endpoint(keyConfig.getHost())
                    .key(keyConfig.getKey())
                    .directMode(DirectConnectionConfig.getDefaultConfig().setIdleConnectionTimeout(Duration.ofMinutes(15)))
                    .throttlingRetryOptions(retryOptions)
                    .consistencyLevel(keyConfig.getConsistencyLevel())
                    .buildAsyncClient();
            //
            createDatabase();
            //
            if(keyConfig.isDeleteContainer()) {
                CosmosContainerResponse response = deleteContainer(keyConfig.getCollection());
            }
            //
            createContainer();
        }
        return client;
    }

    public static CosmosKeyAsyncConnection getInstance() {
        //
        if (instance == null) {
            instance = new CosmosKeyAsyncConnection();
        }
        return instance;
    }

    public CosmosDatabaseResponse createDatabase() {
        CosmosDatabaseResponse response = client.createDatabaseIfNotExists(keyConfig.getDatabase()).block();
        this.database = client.getDatabase(keyConfig.getDatabase());
        return response;
    }

    public CosmosContainerResponse createContainer() {
        CosmosAsyncDatabase db = client.getDatabase(keyConfig.getDatabase());
        CosmosContainerProperties cosmosContainerProperties = CosmosUtils.
                getCosmosContainerProperties(keyConfig.getCollection());
        CosmosContainerResponse response = db.createContainerIfNotExists(cosmosContainerProperties,
                ThroughputProperties.createManualThroughput(keyConfig.getProvisionedRUs())).block();
        this.container = db.getContainer(keyConfig.getCollection());
        return response;
    }

}
