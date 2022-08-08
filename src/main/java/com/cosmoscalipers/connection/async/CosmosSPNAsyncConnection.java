package com.cosmoscalipers.connection.async;

import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.cosmoscalipers.cli.SPNConfig;
import com.cosmoscalipers.connection.ManagementOperations;
import com.cosmoscalipers.utils.CosmosUtils;

import java.time.Duration;

public final class CosmosSPNAsyncConnection extends CosmosAsyncConnection {

    private static CosmosSPNAsyncConnection instance;

    private SPNConfig spnConfig;

    private ManagementOperations managementOperations;

    private CosmosSPNAsyncConnection()  {
    }

    public void initConnection(final SPNConfig spnConfig) {
        if (client == null) {
            //
            this.spnConfig = spnConfig;
            //
            managementOperations = ManagementOperations.getInstance();
            managementOperations.initialize(spnConfig.getClientId(),
                    spnConfig.getClientSecret(),
                    spnConfig.getTenantId(),
                    spnConfig,
                    spnConfig.getSubscriptionId());
            //
            ThrottlingRetryOptions retryOptions = CosmosUtils.getRetryOptions(
                    spnConfig.getMaxRetryAttempts(),
                    spnConfig.getRetryWaitTimeInSeconds());
            //
            ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                    .clientId(spnConfig.getClientId())
                    .clientSecret(spnConfig.getClientSecret())
                    .tenantId(spnConfig.getTenantId())
                    .build();
            //
            client = new CosmosClientBuilder()
                    .endpoint(spnConfig.getHost())
                    .credential(clientSecretCredential)
                    .directMode(DirectConnectionConfig.getDefaultConfig().setIdleConnectionTimeout(Duration.ofMinutes(15)))
                    .throttlingRetryOptions(retryOptions)
                    .consistencyLevel(spnConfig.getConsistencyLevel())
                    .buildAsyncClient();
            // Create database
            createDatabase();
            //
            if(spnConfig.isDeleteContainer()) {
                ManagementOperations.getInstance().deleteCosmosCollection(spnConfig.getResourceGroup());
            }
            // create database
            createContainer();
        }
    }

    public static CosmosSPNAsyncConnection getInstance() {
        //
        if (instance == null) {
            instance = new CosmosSPNAsyncConnection();
        }
        return instance;
    }

    public String createDatabase() {
        //
        managementOperations.createCosmosDatabase(spnConfig.getResourceGroup());
        this.database = client.getDatabase(spnConfig.getDatabase());
        System.out.println("database Id "+database.getId());
        return database.getId();
    }

    public String createContainer() {
        //
        managementOperations.createCosmosCollection(spnConfig.getResourceGroup());
        CosmosAsyncDatabase db = client.getDatabase(spnConfig.getDatabase());
        this.container = db.getContainer(spnConfig.getCollection());
        System.out.println("Container Id "+container.getId());
        return container.getId();
    }

}
