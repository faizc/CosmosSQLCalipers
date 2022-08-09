package com.cosmoscalipers.driver;

import com.cosmoscalipers.cli.KeyConfig;
import com.cosmoscalipers.connection.async.CosmosKeyAsyncConnection;
import com.cosmoscalipers.connection.sync.CosmosKeySyncConnection;
import com.cosmoscalipers.constant.Workflow;

public class KeyLoadRunner extends DefaultLoadRunner {

    private KeyConfig keyConfig;

    public KeyLoadRunner(final KeyConfig spnConfig) {
        this.keyConfig = spnConfig;
        setConfig(keyConfig);
    }

    @Override
    public Object getCosmosConnection(final Workflow workflow) {
        Object object = null;
        if(workflow == Workflow.ASYNC) {
            CosmosKeyAsyncConnection asyncConnection = CosmosKeyAsyncConnection.getInstance();
            asyncConnection.initConnection(keyConfig);
            object = asyncConnection;
        } else if(workflow == Workflow.SYNC) {
            CosmosKeySyncConnection syncConnection = CosmosKeySyncConnection.getInstance();
            syncConnection.initConnection(keyConfig);
            object = syncConnection;
        }
        return object;
    }

    @Override
    public void execute() throws Exception {
        execute(keyConfig.getOperation());
    }
}
