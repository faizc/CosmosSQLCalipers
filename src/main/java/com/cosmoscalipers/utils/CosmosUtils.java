package com.cosmoscalipers.utils;

import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.models.*;
import com.cosmoscalipers.constant.Constants;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CosmosUtils {


    public static ThrottlingRetryOptions getRetryOptions(int maxRetryAttempts,
                                                         int retryWaitTimeInSeconds) {
        ThrottlingRetryOptions retryOptions = new ThrottlingRetryOptions();
        if(maxRetryAttempts != 0) {
            retryOptions.setMaxRetryAttemptsOnThrottledRequests(maxRetryAttempts);
        }
        if(retryWaitTimeInSeconds != 0) {
            retryOptions.setMaxRetryWaitTime(Duration.ofSeconds(retryWaitTimeInSeconds));
        }
        return retryOptions;
    }

    public static CosmosContainerProperties getCosmosContainerProperties(String collection) {
        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(collection, Constants.CONST_PARTITION_KEY);
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);

        List<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath(Constants.CONST_PARTITION_KEY + "/*");
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        List<ExcludedPath> excludedPaths = new ArrayList<>();
        ExcludedPath excludedPath2 = new ExcludedPath("/*");
        excludedPaths.add(excludedPath2);
        indexingPolicy.setExcludedPaths(excludedPaths);

        cosmosContainerProperties.setIndexingPolicy(indexingPolicy);
        return cosmosContainerProperties;
    }


}
