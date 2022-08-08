package com.cosmoscalipers.cli;

import com.cosmoscalipers.constant.Constants;
import picocli.CommandLine;

@CommandLine.Command(
        name = Constants.CONST_SUBCOMMAND_SPN,
        description = "Azure SPN based authentication test")
public class SPNConfig extends Config {
    @CommandLine.Option(names = {"-t", "--tenantId"}, description = "Tenant Id", required = true, paramLabel = "<Tenant Id>")
    private String tenantId;
    @CommandLine.Option(names = {"-cl", "--clientId"}, description = "Client Id", required = true, paramLabel = "<Client Id>")
    private String clientId;
    @CommandLine.Option(names = {"-cs", "--clientSecret"}, description = "Client Secret", required = true, paramLabel = "<Client Secret>")
    private String clientSecret;
    @CommandLine.Option(names = {"-rg", "--resourceGroup"}, description = "Resource group for the Cosmos account", required = true, paramLabel = "<resource group>")
    private String resourceGroup;
    @CommandLine.Option(names = {"-sub", "--subscriptionId"}, description = "Subscription Id to be used", required = true, paramLabel = "<subscriptionId>")
    private String subscriptionId;

    public String getClientSecret() {
        return clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getResourceGroup() {
        return resourceGroup;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }
}
