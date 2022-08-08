package com.cosmoscalipers.connection;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Context;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.cosmos.fluent.models.SqlContainerGetResultsInner;
import com.azure.resourcemanager.cosmos.fluent.models.SqlDatabaseGetResultsInner;
import com.azure.resourcemanager.cosmos.models.*;
import com.cosmoscalipers.cli.Config;
import com.cosmoscalipers.constant.Constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ManagementOperations {

    private static ManagementOperations instance;

    private AzureResourceManager azureResourceManager;

    private Config config;

    public static ManagementOperations getInstance() {
        //
        if (instance == null) {
            instance = new ManagementOperations();
        }
        return instance;
    }

    public void initialize(final String clientId,
                           final String clientSecret,
                           final String tenantId,
                           final Config config,
                           final String subscriptionId) {
        //
        this.config = config;
        // Authenticate
        AzureProfile profile = new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);
        //
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();
        //
        azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(clientSecretCredential, profile)
                .withSubscription(subscriptionId);
    }

    public void createCosmosDatabase(final String resourceGroup) {
        // Extract the account name from the endpoint
        final String accountName = extractAccountName(config.getHost());
        //
        SqlDatabaseCreateUpdateParameters databaseParams = new SqlDatabaseCreateUpdateParameters()
                .withTags(mapOf())
                .withResource(new SqlDatabaseResource().withId(config.getDatabase()))
                .withOptions(new CreateUpdateOptions());
        SqlDatabaseGetResultsInner dbResult = azureResourceManager.cosmosDBAccounts()
                .manager()
                .serviceClient()
                .getSqlResources()
                .createUpdateSqlDatabase(resourceGroup,
                        accountName,
                        config.getDatabase(),
                        databaseParams,
                        Context.NONE);
    }

    /* Extract the account name from the endpoint.
     */
    public String extractAccountName(final String endpoint) {
        Pattern pattern = Pattern.compile("https?://([^/]+).documents.azure.com:443/");
        Matcher matcher = pattern.matcher(endpoint);
        String accountName = "";
        while (matcher.find()) {
            accountName = matcher.group(1);
        }
        return accountName;
    }

    public void deleteCosmosCollection(final String resourceGroup) {
        // Extract the account name from the endpoint
        final String accountName = extractAccountName(config.getHost());
        //
        azureResourceManager.cosmosDBAccounts()
                .manager()
                .serviceClient()
                .getSqlResources()
                .deleteSqlContainer(resourceGroup,
                        accountName,
                        config.getDatabase(),
                        config.getCollection(),
                        Context.NONE);
    }


    public void createCosmosCollection(final String resourceGroup) {
        // Extract the account name from the endpoint
        final String accountName = extractAccountName(config.getHost());
        //
        IndexingPolicy indexingPolicy = new IndexingPolicy()
                .withAutomatic(true)
                .withIndexingMode(IndexingMode.CONSISTENT)
                .withIncludedPaths(
                        Arrays
                                .asList(
                                        new IncludedPath()
                                                .withPath(Constants.CONST_PARTITION_KEY + "/*")
                                ))
                .withExcludedPaths(Arrays.asList(new ExcludedPath().withPath("/*")));
        //
        ContainerPartitionKey partitionKey = new ContainerPartitionKey()
                .withPaths(Arrays.asList(Constants.CONST_PARTITION_KEY))
                .withKind(PartitionKind.HASH);
        //
        SqlContainerResource sqlContainerResource = new SqlContainerResource()
                .withId(config.getCollection())
                .withIndexingPolicy(indexingPolicy)
                .withPartitionKey(partitionKey);
        //
        SqlContainerCreateUpdateParameters sqlContainerCreateUpdateParameters = new SqlContainerCreateUpdateParameters()
                .withTags(mapOf())
                .withResource(sqlContainerResource);
        //
        SqlContainerGetResultsInner containerResult = azureResourceManager.cosmosDBAccounts()
                //cosmosDBAccount
                .manager()
                .serviceClient()
                .getSqlResources()
                .createUpdateSqlContainer(resourceGroup,
                        accountName,
                        config.getDatabase(),
                        config.getCollection(),
                        sqlContainerCreateUpdateParameters.withOptions(
                                new CreateUpdateOptions().withAutoscaleSettings(
                                        new AutoscaleSettings().withMaxThroughput(1000)
                                )),
                        Context.NONE);
    }

    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }

}
