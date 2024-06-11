# Quick start guide!

Do you want to edit rosetta files? You are in the right place and will be up and running in a few minutes.

## Prerequsites

- You must have a developer background and have a good understanding about node, npm, java and maven.
- You must have VSCode installed and be familiar with the basics
- Must have java 11 instaled and on the system path
- Must have Maven installed
- Must have node/npm installed


## Instructions

### Clone the DSL Repo

```
git clone https://github.com/finos/rune-dsl.git
```

### Build and run

```
mvn clean install && cd rosetta-ide/vscode && npm install
```

### Start VS Code

You can now start vscode from the `vscode` dir and hit the `F5` key to start an instance of VS Code.


All done! Create a rosetta file and start modelling.

### Generate vsix to install

Running the below command will generate a file called `rosetta-language-5.0.0.vsix` which you can install in vs code.

```
npm run build
```

### Notes

> Copy the basic type and annotation files into your workspace `com.regnosys.rosetta.lib/src/main/java/model/basictypes.rosetta`

> You will notice a `src-gen` folder appear in the root folder of vscode where the JAVA code bindings are.

> This plugin forks a Java process that starts a LanguageServer. If vs code crashes, it may leave a phantom process around. Find and kill it.

> This plugin has only been tested on a mac. If you want to test on windows, please go for it! If it has issues, raise it on github and we can work on it.
