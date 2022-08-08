# CosmosSQLCalipers
CosmosSQLCalipers is a basic Cosmos SQL API benchmarking utility. It enables developers to model and understand the impact of the following parameters:

+ Document sizes
+ Partition key based SELECT queries vs point reads
+ Impact of sync versus async APIs
+ Request unit (RUs) consumption
+ Network latencies and throughput
+ Supports masterkey and SPN based authentication

This enables developers to get a preview into the overall scalability, response times and cost considerations when evaluating Cosmos SQL API. 

##### *Use Azure SDK v4*
Project upgraded to [v4.33.1](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos/CHANGELOG.md#4331-2022-07-22). Following consistency levels are running fine:
* STRONG
* BOUNDED_STALENESS
* SESSION
* CONSISTENCY_PREFIX
* EVENTUAL

## Overview
The utility executes the following workflow:
* Provisions a collection based on input parameters
* Allocates the provisioned RUs
* Creates and inserts documents into the collection
* Executes the query workload based on the operation specified in the input parameters
* Deletes the documents explicitly at the end of the exercise 

Indexing policy has a significant impact on RU consumption. So for the sake of simplicity, CosmosSQLCalipers deploys a single index for partition key lookups. Collected metrics include:
* Throughput
* Network latencies
* RUs consumed

Operations that can be exercised using this tool includes:
1. SQL_ASYNC_PARTITION_KEY_READ: Executes partition key based queries asynchronously  
1. SQL_SYNC_PARTITION_KEY_READ: Executes partition key based queries synchronously
1. SQL_ASYNC_POINT_READ: Executes point read operations asynchronously
1. SQL_SYNC_POINT_READ: Executes point read operations synchronously
1. SQL_ASYNC_UPSERT: Executes upsert operations asynchronously
1. SQL_SYNC_UPSERT: Executes upsert operations synchronously
1. SQL_ASYNC_REPLACE: Executes replace operations asynchronously
1. SQL_SYNC_REPLACE: Executes replace operations synchronously 
1. ALL_SYNC_OPS: Executes all synchronous operations serially. The execution cycle starts with partition key based reads followed by point reads.  
1. ALL_ASYNC_OPS: Executes all asynchronous operations serially. The execution cycle starts with partition key based reads followed by point reads.
1. SQL_ALL: Executes async partition key read, sync partition key read, async point read and sync point read operations serially.

SQL API workflows executed
1. SQL_ASYNC_PARTITION_KEY_READ and SQL_SYNC_PARTITION_KEY_READ options
    1. createItem()
    1. queryItem()
    1. deleteItem()
1. SQL_ASYNC_POINT_READ and SQL_SYNC_POINT_READ options
    1. createItem()
    1. readItem()
    1. deleteItem()
1. SQL_ASYNC_READ_ALL_ITEMS and SQL_SYNC_READ_ALL_ITEMS options
    1. createItem()
    1. readAllItems()
    1. deleteItem()
1. SQL_ASYNC_UPSERT and SQL_SYNC_UPSERT options
    1. createItem()
    1. upsertItem()
    1. deleteItem()
1. SQL_ASYNC_REPLACE and SQL_SYNC_REPLACE options 
    1. createItem()
    1. replaceItem()
    1. deleteItem()
1. ALL_SYNC_OPS and ALL_ASYNC_OPS options
    1. createItem()
    1. queryItem()
    1. readItem()
    1. readAllItems()
    1. upsertItem()
    1. replaceItem()
    1. deleteItem()
1. SQL_ALL
    1. Invokes all async and sync ops


## Instructions
1. Create a Cosmos DB SQL API account
    1. Assign consistency level you want to test with
    1. Select region depending on your deployment preferences
    1. Single or multiple regions
    1. Choose an active/active or active/passive topology
2. Run the benchmark test against the provisioned Cosmos account.     

## Program arguments

Execute Cosmos Benchmark utility using two different modes now, one with SPN based authentication and other with Master key based authentication
```
Usage: cosmosbenchmark [COMMAND]
Commands:
  spn        Azure SPN based authentication test
  masterkey  Azure master key based authentication test
```

Usage command for 'spn'
```
Usage: cosmosbenchmark spn [-dc] -c=<Collection Name> -cl=<Client Id>
                           -cs=<Client Secret> -d=<Database Name> -e=https:
                           //<ENDPOINT>.documents.azure.com:443/
                           [-l=<consistencyLevel>] -n=<Number of documents>
                           [-o=<operation>] -p=<Payload Size in KBs>
                           [-r=<Maximum retry attempts>] [-rf=<reporter>]
                           -rg=<resource group> [-ru=<Provisioned RU's>]
                           [-rwi=<Retry wait time in sec.>]
                           -sub=<subscriptionId> -t=<Tenant Id>
Azure SPN based authentication test
  -c, --collection=<Collection Name>
         Cosmos collection name
      -cl, --clientId=<Client Id>
         Client Id
      -cs, --clientSecret=<Client Secret>
         Client Secret
  -d, --database=<Database Name>
         Cosmos Database Name
      -dc, --deleteContainer
         Delete and recreate the container (true/false), defaults to true
  -e, --endpoint=https://<ENDPOINT>.documents.azure.com:443/
         Cosmos Service endpoint
  -l, --consistency=<consistencyLevel>
         Consistency level to be used (Strong, BoundedStaleness, Session,
           Eventual, ConsistentPrefix). If omitted the Session consistency
           would be used and should be weaker than account level consistency
  -n, --numberofdocument=<Number of documents>
         Number of documents to be tested
  -o, --operation=<operation>
         Primary operation being used (SQL_ASYNC_PARTITION_KEY_READ,
           SQL_SYNC_PARTITION_KEY_READ, SQL_SYNC_POINT_READ,
           SQL_ASYNC_POINT_READ, SQL_ALL, ALL_SYNC_OPS, ALL_ASYNC_OPS,
           SQL_SYNC_UPSERT, SQL_ASYNC_UPSERT, SQL_SYNC_REPLACE,
           SQL_ASYNC_REPLACE, SQL_SYNC_READ_ALL_ITEMS,
           SQL_ASYNC_READ_ALL_ITEMS, SQL_SYNC_DELETE, SQL_ASYNC_DELETE). If
           omitted the SQL_ALL operation would be used.
  -p, --payloadsize=<Payload Size in KBs>
         Document size
  -r, --retry=<Maximum retry attempts>
         Number of retry attempts
      -rf, --reporting=<reporter>
         Reporting format to be used (CONSOLE, CSV). If omitted the CONSOLE
           operation would be used.
      -rg, --resourceGroup=<resource group>
         Resource group for the Cosmos account
      -ru, --provisionedRUs=<Provisioned RU's>
         RUs to be provisioned when Cosmos container is created
      -rwi, --retry-wait-interval=<Retry wait time in sec.>
         max retry wait interval (in seconds)
      -sub, --subscriptionId=<subscriptionId>
         Subscription Id to be used
  -t, --tenantId=<Tenant Id>
         Tenant Id
```

Usage command for 'masterkey' 
````
Usage: cosmosbenchmark masterkey [-dc] -c=<Collection Name> -d=<Database
                                 Name> -e=https://<ENDPOINT>.documents.azure.
                                 com:443/ -k=<Access Key>
                                 [-l=<consistencyLevel>] -n=<Number of
                                 documents> [-o=<operation>] -p=<Payload Size
                                 in KBs> [-r=<Maximum retry attempts>]
                                 [-rf=<reporter>] [-ru=<Provisioned RU's>]
                                 [-rwi=<Retry wait time in sec.>]
Azure master key based authentication test
  -c, --collection=<Collection Name>
                           Cosmos collection name
  -d, --database=<Database Name>
                           Cosmos Database Name
      -dc, --deleteContainer
                           Delete and recreate the container (true/false),
                             defaults to true
  -e, --endpoint=https://<ENDPOINT>.documents.azure.com:443/
                           Cosmos Service endpoint
  -k, --key=<Access Key>   Access key
  -l, --consistency=<consistencyLevel>
                           Consistency level to be used (Strong,
                             BoundedStaleness, Session, Eventual,
                             ConsistentPrefix). If omitted the Session
                             consistency would be used.
  -n, --numberofdocument=<Number of documents>
                           Number of documents to be tested
  -o, --operation=<operation>
                           Primary operation being used
                             (SQL_ASYNC_PARTITION_KEY_READ,
                             SQL_SYNC_PARTITION_KEY_READ, SQL_SYNC_POINT_READ,
                             SQL_ASYNC_POINT_READ, SQL_ALL, ALL_SYNC_OPS,
                             ALL_ASYNC_OPS, SQL_SYNC_UPSERT, SQL_ASYNC_UPSERT,
                             SQL_SYNC_REPLACE, SQL_ASYNC_REPLACE,
                             SQL_SYNC_READ_ALL_ITEMS, SQL_ASYNC_READ_ALL_ITEMS,
                             SQL_SYNC_DELETE, SQL_ASYNC_DELETE). If omitted the
                             SQL_ALL operation would be used.
  -p, --payloadsize=<Payload Size in KBs>
                           Document size
  -r, --retry=<Maximum retry attempts>
                           Number of retry attempts
      -rf, --reporting=<reporter>
                           Reporting format to be used (CONSOLE, CSV). If
                             omitted the CONSOLE operation would be used.
      -ru, --provisionedRUs=<Provisioned RU's>
                           RUs to be provisioned when Cosmos container is
                             created
      -rwi, --retry-wait-interval=<Retry wait time in sec.>
                           max retry wait interval (in seconds)
````

## Building and running

````
mvn clean package
````

### Executing using the 'masterkey' option 
````
mvn exec:java -Dexec.mainClass="com.cosmoscalipers.Benchmark" -Dexec.cleanupDaemonThreads=false -Dexec.args="masterkey --endpoint=<cosmos-endpoint> --database=<database-name> --collection=<container-name> --consistency=SESSION --numberofdocument=1 --payloadsize=1 --key=<access key for the account>"
````

### Executing using the 'spn' option

#### Following are few pre-requisites before you execute the application
* The Service Principal needs to be created beforehand and make sure it has been assigned "Cosmos DB Operator" role on the Cosmos account
* In order for the CRUD operations to work on the Cosmos database, you would need to assign the required roles to the service principal, the details of which are available [here](https://docs.microsoft.com/en-us/azure/cosmos-db/how-to-setup-rbac#using-azure-powershell-1) 

````
mvn exec:java -Dexec.mainClass="com.cosmoscalipers.Benchmark" -Dexec.cleanupDaemonThreads=false -Dexec.args="spn --subscriptionId=<subscriptionId> --resourceGroup=<resourceGroup> --clientId=<client-id> --clientSecret=<client-secret> --tenantId=<tenant-id> --endpoint=<cosmos-endpoint> --database=<database-name> --collection=<container-name> --consistency=SESSION --numberofdocument=100 --payloadsize=1 --operation SQL_ASYNC_POINT_READ"
````