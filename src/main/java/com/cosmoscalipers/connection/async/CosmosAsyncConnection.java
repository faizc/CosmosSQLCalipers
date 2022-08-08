package com.cosmoscalipers.connection.async;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.CosmosContainerResponse;

public abstract class CosmosAsyncConnection {

    protected CosmosAsyncClient client;

    protected CosmosAsyncContainer container;

    protected CosmosAsyncDatabase database;

    public CosmosAsyncClient getConnection() {
        return client;
    }

    public void closeConnection() {
        if(client != null) {
            client.close();
        }
    }

    public CosmosAsyncContainer getContainer() {
        return container;
    }

    public CosmosAsyncDatabase getDatabase() {
        return database;
    }

    public CosmosContainerResponse deleteContainer(final String collection) {
        return database.getContainer(collection).delete().block();
    }

}
