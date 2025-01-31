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

public class SQLAsyncUpsert {
    private static Histogram sqlAsyncUpsertRequestUnits = null;
    private static Histogram sqlAsyncUpsertLatency = null;
    private static Meter throughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLSyncUpsert.class);

    public void execute(final CosmosAsyncConnection connection,
                        final List<String> orderIdList,
                        final int numberOfOps,
                        final MetricRegistry metrics) {
        sqlAsyncUpsertRequestUnits = metrics.histogram("Async upsert RUs");
        sqlAsyncUpsertLatency = metrics.histogram("Async upsert latency (ms)");
        throughput = metrics.meter("Async upsert throughput");
        updateOps(connection.getContainer(), orderIdList, numberOfOps);

    }

    private void updateOps(CosmosAsyncContainer container, List<String> orderIdList, int numberOfOps) {
        log("Running async upsert workload for " + numberOfOps + " docs...");

        orderIdList.stream()
                .forEach(item -> update(container, item));

    }

    private static void update(CosmosAsyncContainer container, String orderId) {

        CosmosItemResponse<Payload> cosmosItemResponse = container.readItem(orderId, new PartitionKey(orderId), Payload.class).block();
        assert cosmosItemResponse != null;

        Payload payload = cosmosItemResponse.getItem();
        payload.setPayload("Upserted:" + payload.getPayload());

        Mono<CosmosItemResponse<Payload>> itemResponseMono = container.upsertItem(payload);

        itemResponseMono.doOnError(throwable -> {
            log("Error doing async upsert for payloadId = " + payload.getId());
            log(throwable.getMessage());
        }).doOnSuccess(result -> {
            sqlAsyncUpsertRequestUnits.update( Math.round(result.getRequestCharge()) );
            sqlAsyncUpsertLatency.update(result.getDuration().toMillis());
            throughput.mark();
        }).publishOn(Schedulers.elastic()).block();


    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosException)throwable).getStatusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}
