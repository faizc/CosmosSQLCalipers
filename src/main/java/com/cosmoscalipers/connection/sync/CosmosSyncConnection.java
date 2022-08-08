package com.cosmoscalipers.connection.sync;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosContainerResponse;

public abstract class CosmosSyncConnection {

    protected CosmosClient client;

    protected CosmosContainer container;

    protected CosmosDatabase database;

    public CosmosClient getConnection() {
        return client;
    }

    public void closeConnection() {
        if(client != null) {
            client.close();
        }
    }

    public CosmosContainer getContainer() {
        return container;
    }

    public CosmosDatabase getDatabase() {
        return database;
    }

    public CosmosContainerResponse deleteContainer(final String collection) {
        return database.getContainer(collection).delete();
    }


}
