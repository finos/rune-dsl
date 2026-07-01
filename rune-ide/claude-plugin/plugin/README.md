# rune-lsp

Rune DSL language server (the same Xtext/LSP4J server that backs `rune-ide/vscode`) for Claude Code, providing code intelligence over `.rosetta` files: diagnostics, go to definition, hover, find references and semantic tokens.

Unlike a repo-specific dev tool, this plugin works in **any** project that has `.rosetta` files, not just this repo — `bin/rune-lsp` resolves the language server from Maven Central rather than assuming a rune-dsl checkout is present.

## Supported Extensions

`.rosetta`

## Requirements

- Java 21
- Maven (`mvn` on PATH)

`org.finos.rune:rune-ide` is published to Maven Central on every rune-dsl release (see `.github/workflows/release.yml`) and contains the LSP entry point. `bin/rune-lsp` resolves it (and its dependencies) via a throwaway Maven project on first run, caches the resulting classpath under `${CLAUDE_PLUGIN_DATA}` (falling back to `~/.cache/rune-dsl-ls`), and reuses that cache on subsequent starts — so only the very first launch pays for dependency resolution. Set `RUNE_DSL_LS_VERSION` to pin a specific `rune-ide` version instead of always using the latest release.

This is a stopgap: once rune-dsl ships a prebuilt `rune-dsl-ls` jar as a GitHub release asset, this script will switch to downloading and running that jar directly, dropping the Maven/network dependency on startup.

## Using it outside this repo

Since this plugin doesn't depend on `$CLAUDE_PROJECT_DIR`, you can register it while working in a *different* repo that contains Rune models. On this machine, from that other repo:

```bash
claude plugin marketplace add /path/to/rune-dsl/rune-ide/claude-plugin --scope user
claude plugin install rune-lsp@rune-dsl --scope user
```

(This repo's own `.claude/settings.json` only auto-enables the plugin at project scope, i.e. when the project root is this rune-dsl checkout.)

## More Information

- [rune-ide/vscode](../../vscode) — the VS Code extension this LSP integration is derived from
- [Eclipse Xtext](https://www.eclipse.org/Xtext/) / [LSP4J](https://github.com/eclipse-lsp4j/lsp4j)
