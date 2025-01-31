package com.cosmoscalipers.workload;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.cosmoscalipers.connection.async.CosmosAsyncConnection;
import com.cosmoscalipers.pojo.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class SQLAsyncPointRead {

    private static Histogram requestUnits = null;
    private static Histogram readLatency = null;
    private static Meter throughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLAsyncPointRead.class);

    public void execute(final CosmosAsyncConnection connection,
                        final List<String> payloadIdList,
                        final int numberOfOps,
                        final MetricRegistry metrics) {
        requestUnits = metrics.histogram("Async point read RUs");
        readLatency = metrics.histogram("Async point read latency (ms)");
        throughput = metrics.meter("Async point read throughput");
        readOps(connection.getContainer(), payloadIdList, numberOfOps);
    }

    private void readOps(CosmosAsyncContainer container, List<String> orderIdList, int numberOfOps) {
        log("Running async point read workload for " + numberOfOps + " docs...");

        orderIdList.stream()
                .forEach(item -> read(container, item));

    }

    private static void read(CosmosAsyncContainer container, String orderId) {

        Mono<CosmosItemResponse<Payload>> cosmosItemResponse = container.readItem(orderId, new PartitionKey(orderId), Payload.class);

        cosmosItemResponse.doOnSuccess(itemResponse -> {
                            requestUnits.update(Math.round(itemResponse.getRequestCharge()));
                            readLatency.update(itemResponse.getDuration().toMillis());
                            throughput.mark();
                            //log( itemResponse.properties().toJson()  );

                        }
                ).publishOn(Schedulers.elastic())
                .block();

    }

    private static void log(String msg, Throwable throwable) {
        log(msg + ": " + ((CosmosException) throwable).getStatusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}
