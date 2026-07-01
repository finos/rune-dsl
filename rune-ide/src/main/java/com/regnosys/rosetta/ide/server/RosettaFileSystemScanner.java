/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.ide.server;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.util.IFileSystemScanner;
import org.eclipse.xtext.util.UriExtensions;

import jakarta.inject.Inject;

/**
 * Xtext's default {@link IFileSystemScanner.JavaIoFileSystemScanner} walks every directory under
 * a project's source folder unconditionally, including build-output directories such as
 * {@code target}. For Maven-based Rune projects, that means resources copied into
 * {@code target/} (e.g. {@code target/test-classes/**&#47;*.rosetta}) get indexed alongside their
 * originals under {@code src/}, which the language server then reports as duplicate types and
 * functions. Skip well-known build-output directories instead.
 */
public class RosettaFileSystemScanner implements IFileSystemScanner {
	private static final Set<String> EXCLUDED_DIRECTORY_NAMES = Set.of("target", "build", "node_modules", ".git");

	@Inject
	private UriExtensions uriExtensions;

	@Override
	public void scan(URI root, IAcceptor<URI> acceptor) {
		File file = new File(root.toFileString());
		scanRec(file, acceptor);
	}

	private void scanRec(File file, IAcceptor<URI> acceptor) {
		if (file.isDirectory() && EXCLUDED_DIRECTORY_NAMES.contains(file.getName())) {
			return;
		}
		// we need to convert the given file to a decoded emf file uri
		// e.g. file:///Users/x/y/z
		// or file:///C:/x/y/z
		Path path = Paths.get(file.getAbsoluteFile().toURI());
		URI uri = uriExtensions.toEmfUri(path.toUri());
		acceptor.accept(uri);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File f : files) {
					scanRec(f, acceptor);
				}
			}
		}
	}
}
