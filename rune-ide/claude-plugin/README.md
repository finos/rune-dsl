# rune-dsl Claude Code marketplace

A local Claude Code plugin marketplace for this repo, analogous to `rune-ide/vscode` for VS Code.

Currently ships one plugin:

- [`plugin/`](./plugin) (`rune-lsp`) — wires up the Rune DSL language server for `.rosetta` files. It resolves the language server from Maven Central at runtime, so it works in any repo with `.rosetta` files, not just this one (see the plugin's README for how to register it elsewhere).

This marketplace is registered for the whole team via `extraKnownMarketplaces`/`enabledPlugins` in the repo's `.claude/settings.json`. Contributors who trust this project folder in Claude Code are prompted to install it automatically; see [Plugin marketplaces](https://code.claude.com/docs/en/plugin-marketplaces) for how that mechanism works.

To manage it manually:

```bash
claude plugin marketplace add ./rune-ide/claude-plugin
claude plugin install rune-lsp@rune-dsl
```
