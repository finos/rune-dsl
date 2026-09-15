package com.regnosys.rosetta.config.file;

public class FileBasedRuneConfigurationRuntimeException extends RuntimeException {
  public FileBasedRuneConfigurationRuntimeException(String message, Exception e) {
    super(message, e);
  }
}
