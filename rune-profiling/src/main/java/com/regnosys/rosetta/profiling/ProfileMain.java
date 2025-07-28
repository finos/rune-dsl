package com.regnosys.rosetta.profiling;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

public class ProfileMain {

	public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, URISyntaxException {
        InitializationBenchmark.BenchmarkParams params = new InitializationBenchmark.BenchmarkParams();
        params.doSetup();
        new InitializationBenchmark().benchmarkInitialize(params);
        params.doTearDown();
	}
}
