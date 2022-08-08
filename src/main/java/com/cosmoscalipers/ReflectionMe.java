package com.cosmoscalipers;

import com.codahale.metrics.MetricRegistry;
import com.cosmoscalipers.cli.SPNConfig;
import com.cosmoscalipers.connection.async.CosmosAsyncConnection;
import com.cosmoscalipers.connection.async.CosmosSPNAsyncConnection;
import com.cosmoscalipers.constant.OperationType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectionMe {
    public static void main(String... args) throws Exception {
        ReflectionMe me = new ReflectionMe();
        me.execute();
    }

    public void execute() throws Exception {
        List<String> lst = new ArrayList<>();

        //
        OperationType operationType = OperationType.SQL_ASYNC_POINT_READ;
        //
        Class clazz = operationType.getSqlExecutors().get(0);
        Class parameterTypes[] = new Class[4];
        parameterTypes[0] = CosmosAsyncConnection.class;
        parameterTypes[1] = List.class;
        parameterTypes[2] = Integer.TYPE;
        parameterTypes[3] = MetricRegistry.class;
        Object params[] = new Object[4];
        SPNConfig command = new SPNConfig();

        params[0] = getConnection();
        params[1] = lst;
        params[2] = 1;
        params[3] = new MetricRegistry();
        Method method = clazz.getDeclaredMethod("execute", parameterTypes);
        method.invoke(clazz.newInstance(), params);
    }

    public Object getConnection() {
        CosmosSPNAsyncConnection.getInstance().initConnection(new SPNConfig());
        return CosmosSPNAsyncConnection.getInstance();
    }
}
