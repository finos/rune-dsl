package com.regnosys.rosetta.profiling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.jsonrpc.Endpoint;

public class BenchmarkLanguageClient implements Endpoint {
	private static class Notification {
		public final String method;
		public final Object parameter;
		
		public Notification(String method, Object parameter) {
			this.method = method;
			this.parameter = parameter;
		}
	}
	
	private List<Notification> notifications = new ArrayList<>();

	@Override
	public CompletableFuture<?> request(String method, Object parameter) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void notify(String method, Object parameter) {
		System.out.println("Received message: " + method);
		this.notifications.add(new Notification(method, parameter));
	}
	
	public Map<String, List<Diagnostic>> getDiagnostics() {
		Map<String, List<Diagnostic>> result = new HashMap<>();
		for (Notification not : notifications) {
			if (not.parameter instanceof PublishDiagnosticsParams) {
				PublishDiagnosticsParams params = (PublishDiagnosticsParams)not.parameter;
				result.put(params.getUri(), params.getDiagnostics());
			}
		}
		return result;
	}
}
