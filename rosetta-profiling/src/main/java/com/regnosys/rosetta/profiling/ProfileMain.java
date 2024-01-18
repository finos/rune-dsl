package com.regnosys.rosetta.profiling;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

public class ProfileMain {

	public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, URISyntaxException {
        MyBenchmark.BenchmarkParams params = new MyBenchmark.BenchmarkParams();
        params.doSetup();
        new MyBenchmark().testMethod(params);
        params.doTearDown();
	}
}
