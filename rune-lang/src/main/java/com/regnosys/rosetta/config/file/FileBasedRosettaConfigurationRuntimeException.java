package com.regnosys.rosetta.config.file;

public class FileBasedRosettaConfigurationRuntimeException extends RuntimeException {
  public FileBasedRosettaConfigurationRuntimeException(String message, Exception e) {
    super(message, e);
  }
}
