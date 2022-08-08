package com.cosmoscalipers.driver;

import com.cosmoscalipers.cli.SPNConfig;
import com.cosmoscalipers.connection.async.CosmosSPNAsyncConnection;
import com.cosmoscalipers.connection.sync.CosmosSPNSyncConnection;
import com.cosmoscalipers.constant.Workflow;

import java.io.Closeable;

public class SPNLoadRunner extends DefaultLoadRunner {

    private SPNConfig spnConfig;

    private Closeable clientConnection;

    public SPNLoadRunner(final SPNConfig spnConfig) {
        this.spnConfig = spnConfig;
        setConfig(spnConfig);
    }

    @Override
    public Object getCosmosConnection(final Workflow workflow) {
        Object object = null;
        if(workflow == Workflow.ASYNC) {
            CosmosSPNAsyncConnection.getInstance().initConnection(spnConfig);
            object = CosmosSPNAsyncConnection.getInstance();
        } else {
            CosmosSPNSyncConnection.getInstance().initConnection(spnConfig);
            object = CosmosSPNSyncConnection.getInstance();
        }
        return object;
    }


    @Override
    public void execute() throws Exception {
        execute(spnConfig.getOperation());
    }
}
