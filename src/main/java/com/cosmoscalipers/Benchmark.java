package com.cosmoscalipers;

import com.cosmoscalipers.cli.Command;
import com.cosmoscalipers.cli.KeyConfig;
import com.cosmoscalipers.cli.SPNConfig;
import com.cosmoscalipers.constant.Constants;
import com.cosmoscalipers.driver.KeyLoadRunner;
import com.cosmoscalipers.driver.SPNLoadRunner;
import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

public class Benchmark {
    public static void main(String... args) throws Exception {
        // If no arguments passed then show the help options
        if(args.length==0) {
            CommandLine.usage(new Command(), System.out);
            return;
        }

        ParseResult parseResult = null;
        CommandLine commandLine = null;
        try {
            commandLine = new CommandLine(new Command());
            //execute(commandLine, args);
            parseResult = commandLine.parseArgs(args);
        } catch (CommandLine.ParameterException paramEx) {
            CommandLine.ParameterException ex = paramEx;
            try {
                commandLine.getParameterExceptionHandler().handleParseException(ex, args);
                return;
            } catch (Exception exception) {
            }
        } catch (CommandLine.ExecutionException execEx) {
            CommandLine.ExecutionException ex = execEx;
            try {
                Exception cause = ex.getCause() instanceof Exception ? (Exception)ex.getCause() : ex;
                commandLine.getExecutionExceptionHandler().handleExecutionException((Exception)cause, ex.getCommandLine(), parseResult);
                return;
            } catch (Exception exception) {
            }
        } catch (Exception exception) {
            return;
        }

        String subCommand = parseResult.subcommand().commandSpec().name();
        if(Constants.CONST_SUBCOMMAND_SPN.equals(subCommand)) {
            //Initiate the SPN based benchmarking
            new SPNLoadRunner((SPNConfig)handleParseResult(parseResult)).execute();
        } else {
            new KeyLoadRunner((KeyConfig) handleParseResult(parseResult)).execute();
        }

    }

    private static Object handleParseResult(final ParseResult parsed) {
        return parsed.subcommand().commandSpec().userObject();
    }

}
