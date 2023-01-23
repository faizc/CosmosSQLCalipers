package com.cosmoscalipers.connection.sync;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.ThroughputProperties;
import com.cosmoscalipers.cli.KeyConfig;
import com.cosmoscalipers.utils.CosmosUtils;

import java.time.Duration;

public final class CosmosKeySyncConnection extends CosmosSyncConnection {

    private static CosmosKeySyncConnection instance;
    private KeyConfig keyConfig;

    private CosmosKeySyncConnection()  {
    }

    public CosmosClient initConnection(final KeyConfig keyConfig) {
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
                    .buildClient();
            // Create database
            createDatabase();
            //
            if(keyConfig.isDeleteContainer()) {
                CosmosContainerResponse response = deleteContainer(keyConfig.getCollection());
            }
            // create database
            createContainer();

        }
        return client;
    }

    public static CosmosKeySyncConnection getInstance() {
        //
        if (instance == null) {
            instance = new CosmosKeySyncConnection();
        }
        return instance;
    }


    public CosmosDatabaseResponse createDatabase() {
        CosmosDatabaseResponse response = client.createDatabaseIfNotExists(keyConfig.getDatabase());
        this.database = client.getDatabase(keyConfig.getDatabase());
        System.out.println("database "+database.getId());
        return response;
    }

    public CosmosContainerResponse createContainer() {
        CosmosDatabase db = client.getDatabase(keyConfig.getDatabase());
        CosmosContainerProperties cosmosContainerProperties = CosmosUtils.
                getCosmosContainerProperties(keyConfig.getCollection());
        CosmosContainerResponse response = db.createContainerIfNotExists(cosmosContainerProperties,
                ThroughputProperties.createManualThroughput(keyConfig.getProvisionedRUs()));
        this.container = db.getContainer(keyConfig.getCollection());
        System.out.println("Container "+container.getId());
        return response;
    }

}
