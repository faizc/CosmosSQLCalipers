package com.cosmoscalipers.workload;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.cosmoscalipers.connection.sync.CosmosSyncConnection;
import com.cosmoscalipers.pojo.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SQLSyncReadAllItems {

    private static Histogram requestUnits = null;
    private static Histogram readLatency = null;
    private static Meter throughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLSyncReadAllItems.class);

    public void execute(final CosmosSyncConnection connection,
                        final List<String> payloadIdList,
                        final int numberOfOps,
                        final MetricRegistry metrics) {
        requestUnits = metrics.histogram("Sync readAllItems() RUs");
        readLatency = metrics.histogram("Sync readAllItems() latency (ms)");
        throughput = metrics.meter("Sync readAllItems() throughput");
        readOps(connection.getContainer(), payloadIdList, numberOfOps);
    }

    private void readOps(CosmosContainer container, List<String> payloadIdList, int numberOfOps) {
        log("Running sync readAllItems() workload for " + numberOfOps + " docs...");

        payloadIdList.stream()
                .forEach(item -> read(container, item));

    }

    private static void read(CosmosContainer container, String payloadId) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setMaxDegreeOfParallelism(2);
        options.setQueryMetricsEnabled(true);

        long startTime = System.currentTimeMillis();
        CosmosPagedIterable<Payload> pagedIterable = container.readAllItems(new PartitionKey(payloadId), options, Payload.class);

        pagedIterable.iterableByPage().forEach(payloadFeedResponse -> {
            requestUnits.update(Math.round(payloadFeedResponse.getRequestCharge()));
            payloadFeedResponse.getResults()
                    .stream()
                    .forEach(payload -> {
                        throughput.mark();
                            }

                    );
        });

        long difference = System.currentTimeMillis()  - startTime;
        readLatency.update(difference);

    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosException)throwable).getStatusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}
