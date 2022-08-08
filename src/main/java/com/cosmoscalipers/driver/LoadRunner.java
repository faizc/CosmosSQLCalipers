package com.cosmoscalipers.driver;

import com.cosmoscalipers.constant.OperationType;

public interface LoadRunner<T> {

    void execute() throws Exception;

    void execute(final OperationType operationType) throws Exception;

}
