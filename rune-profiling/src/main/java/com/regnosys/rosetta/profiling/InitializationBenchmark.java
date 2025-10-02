package com.regnosys.rosetta.profiling;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.xtext.ide.server.LanguageServerImpl;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import com.google.common.base.Charsets;
import com.google.common.collect.Streams;
import com.google.common.io.Resources;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.ide.server.RosettaServerModule;

public class InitializationBenchmark {
	
	@State(Scope.Benchmark)
	public static class BenchmarkParams {
		public String workspaceName = "hero-model";
		public Path workspacePath = Path.of("test-data/test-project").toAbsolutePath();
		public List<String> fileURIs = new ArrayList<>();
		@Inject
		public LanguageServerImpl server;
		@Inject
		public BenchmarkLanguageClient client;
		@Inject
		public RosettaBuiltinsService builtins;
				
		@Setup(Level.Invocation)
        public void doSetup() throws InterruptedException, ExecutionException, IOException, URISyntaxException {
            Injector injector = Guice.createInjector(RosettaServerModule.create());
            injector.injectMembers(this);
            
            // Create workspace folder
            Files.createDirectories(workspacePath);
            for (URL url : getOriginalWorkspaceURLs()) {
            	try (InputStream in = url.openStream()) {
            		Path target = workspacePath.resolve(Path.of(url.toURI()).getFileName());
            	    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            	    fileURIs.add(target.toUri().toString());
            	}
            }
            
            // Connect client to server and initialize server.
            server.connect(ServiceEndpoints.toServiceObject(client, LanguageClient.class));
        	server.supportedMethods();
        	
        	// Initialization
        	System.out.println("initialize");
        	InitializeParams initParams = new InitializeParams();
        	initParams.setWorkspaceFolders(List.of(
        			new WorkspaceFolder(
        					workspacePath.toUri().toString(),
        					workspaceName
        				)
        			));
      		server.initialize(initParams).get();
      		System.out.println("initialized");
      		server.initialized(new InitializedParams());
      		System.out.println("Setup done");
        }
		
		@TearDown(Level.Invocation)
        public void doTearDown() throws InterruptedException, ExecutionException, IOException {
			// Shut down
			System.out.println("shutdown");
	  		server.shutdown().get();
	  		
	  		// Delete workspace folder
	  		if (Files.exists(workspacePath)) {
	  			Files.walkFileTree(workspacePath, new SimpleFileVisitor<Path>() {
	  			   @Override
	  			   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	  			       Files.delete(file);
	  			       return FileVisitResult.CONTINUE;
	  			   }

	  			   @Override
	  			   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
	  			       Files.delete(dir);
	  			       return FileVisitResult.CONTINUE;
	  			   }
	  			});
			}
        }
		
		private List<URL> getOriginalWorkspaceURLs() throws IOException {
			Path root;
			try {
				root = Path.of(Resources.getResource("rosetta/" + workspaceName).toURI());
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
			return Streams.concat(
					Files.walk(root, 1)
						.filter(p -> p.toString().endsWith(".rosetta"))
						.map(p -> {
							try {
								return p.toUri().toURL();
							} catch (MalformedURLException e) {
								throw new RuntimeException(e);
							}
						}),
                    Stream.of(builtins.basicTypesURL, builtins.annotationsURL)
				).collect(Collectors.toList());
		}
	}

    @Benchmark
    public void benchmarkInitialize(BenchmarkParams params) throws IOException, InterruptedException, ExecutionException {
    	LanguageServerImpl languageServer = params.server;
    	BenchmarkLanguageClient languageClient = params.client;
  		
  		// Wait for diagnostics
  		System.out.println("Wait for diagnostics");
  		languageServer.getRequestManager().runRead((cancelIndicator) -> languageClient.getDiagnostics()).get();
  		System.out.println("Got diagnostics");
    }
}
