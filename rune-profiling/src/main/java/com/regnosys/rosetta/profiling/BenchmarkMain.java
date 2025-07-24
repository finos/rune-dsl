package com.regnosys.rosetta.profiling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.openjdk.jmh.profile.JavaFlightRecorderProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkMain {

	public static void main(String[] args) throws RunnerException, IOException {
		Path reportsRoot = Path.of("reports").toAbsolutePath();
		Path report = reportsRoot.resolve("benchmark.json");
		if (!Files.exists(report)) {
			Files.createDirectories(report.getParent());
			Files.createFile(report);
		}
		Path jfrResults = reportsRoot.resolve("profiling");

		Options opt = new OptionsBuilder()
	            .include(InitializationBenchmark.class.getCanonicalName())
	            .resultFormat(ResultFormatType.JSON)
	            .result(report.toString())
	            .addProfiler(JavaFlightRecorderProfiler.class, String.format("dir=%s", jfrResults))
                .warmupIterations(0)
                .measurementIterations(3)
	            .forks(3)
	            .build();

	    new Runner(opt).run();
	}
}
