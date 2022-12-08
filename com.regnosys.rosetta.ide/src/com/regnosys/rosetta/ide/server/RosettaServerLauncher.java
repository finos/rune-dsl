package com.regnosys.rosetta.ide.server;

import org.eclipse.xtext.ide.server.ServerLauncher;

public class RosettaServerLauncher {
	public static void main(String[] args) {
		ServerLauncher.launch(RosettaServerLauncher.class.getName(), args, RosettaServerModule.create());
	}
}
