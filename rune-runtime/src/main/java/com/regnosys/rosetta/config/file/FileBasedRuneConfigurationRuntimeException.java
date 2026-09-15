package com.regnosys.rosetta.config.file;

import java.net.URL;

/**
 * A {@code rune-config.yml} that could not be read. {@link #getFile()} is the one that failed, which a
 * caller reading several of them (a project's own configuration and its dependencies') needs in order
 * to say which.
 */
public class FileBasedRuneConfigurationRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final transient URL file;

  public FileBasedRuneConfigurationRuntimeException(String message, Exception e) {
    this(message, null, e);
  }

  public FileBasedRuneConfigurationRuntimeException(String message, URL file, Exception e) {
    super(message, e);
    this.file = file;
  }

  /** The configuration file that could not be read, or null where the failure names no single file. */
  public URL getFile() {
    return file;
  }
}
