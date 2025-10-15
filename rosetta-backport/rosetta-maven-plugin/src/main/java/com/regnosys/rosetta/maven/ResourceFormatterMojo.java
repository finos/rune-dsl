package com.regnosys.rosetta.maven;

import org.apache.maven.plugins.annotations.Mojo;

/**
 * <p>
 * Formatter Plugin: A Maven plugin to help with the formatting of resources.
 * </p>
 *
 * <p>
 * Given a path to a directory holding {@code .rosetta} resources, it formats
 * the files according to set formatting rules. Additionally, you can specify a
 * custom configuration file for formatting options using the
 * {@code formattingOptionsPath} parameter. If the {@code formattingOptionsPath}
 * is not provided, the plugin will use default formatting options.
 * </p>
 *
 *
 * <p>
 * To run the goal:
 * <ul>
 * <li>{@code mvn org.finos.rune:rune-maven-plugin:version:format -Dpath="path/to/directory"}</li>
 * <li>Optionally, provide a custom formatting options file using
 * {@code -DformattingOptionsPath="path/to/formattingOptions.json"}</li>
 * </ul>
 * </p>
 *
 * <p>
 * Example with both parameters:
 * <ul>
 * <li>{@code mvn org.finos.rune:rune-maven-plugin:version:format -Dpath="path/to/directory" -DformattingOptionsPath="path/to/formattingOptions.json"}</li>
 * </ul>
 * </p>
 */
@Mojo(name = "format")
public class ResourceFormatterMojo extends RuneFormatterMojo {
}