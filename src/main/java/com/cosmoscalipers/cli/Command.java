package com.cosmoscalipers.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "cosmosbenchmark", subcommands = {
        SPNConfig.class, KeyConfig.class
})
public class Command{
}
