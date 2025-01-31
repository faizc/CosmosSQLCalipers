package com.cosmoscalipers.workload;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.cosmoscalipers.connection.sync.CosmosSyncConnection;
import com.cosmoscalipers.pojo.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SQLSyncPointRead  {

    private static Histogram requestUnits = null;
    private static Histogram readLatency = null;
    private static Meter throughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLSyncPointRead.class);

    public void execute(final CosmosSyncConnection connection,
                        final List<String> payloadIdList,
                        final int numberOfOps,
                        final MetricRegistry metrics) {
        requestUnits = metrics.histogram("Sync point read RUs");
        readLatency = metrics.histogram("Sync point read latency (ms)");
        throughput = metrics.meter("Sync point read throughput");
        readOps(connection.getContainer(), payloadIdList, numberOfOps);
    }

    private void readOps(CosmosContainer container, List<String> payloadIdList, int numberOfOps) {
        log("Running sync point read workload " + numberOfOps + " docs...");

        payloadIdList.stream()
                .forEach(item -> read(container, item));

    }

    private static void read(CosmosContainer container, String payloadId) {

        try {
            CosmosItemResponse cosmosItemResponse = container.readItem(payloadId, new PartitionKey(payloadId), Payload.class);

            requestUnits.update( Math.round(cosmosItemResponse.getRequestCharge()) );
            readLatency.update(cosmosItemResponse.getDuration().toMillis());
            throughput.mark();
            //log( cosmosItemResponse.properties().toJson()  );
            //log( cosmosItemResponse.cosmosResponseDiagnosticsString() );
            //log( cosmosItemResponse.getDiagnostics() );
        } catch(Exception e) {
            if(e instanceof CosmosException) {
                log(e.getStackTrace());
            }
            else {
                System.out.println(e.getStackTrace());
            }
        }

    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosException)throwable).getStatusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}
