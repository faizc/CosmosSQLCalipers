package com.cosmoscalipers.cli;

import com.cosmoscalipers.constant.Constants;
import picocli.CommandLine;

@CommandLine.Command(
        name= Constants.CONST_SUBCOMMAND_KEY,
        description="Azure master key based authentication test")
public class KeyConfig extends Config {
    @CommandLine.Option(names = {"-k", "--key"}, description = "Access key", required = true, paramLabel = "<Access Key>")
    private String key;

    public String getKey() {
        return key;
    }
}
