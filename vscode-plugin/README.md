# Quick start guide!

Do you want to edit rosetta files? You are in the right place and will be up and running in a few minutes.

## Prerequsites

- You must have VSCode installed
- Must have java 11 instaled and on the class path
- Must have Maven installed
- Must have node/npm installed


## Instructions

### Clone the DSL Repo

```
git clone git@github.com:REGnosys/rosetta-dsl.git
```

### Build and run

```
mvn clean install && cd vscode-plugin && npm install
```

### Start VS Code

You can now start vscode from the `vscode-plugin` dir and hit the `F5` key to start an instance of VS Code.


All done! Create a rosetta file and start modelling.

### 

### Notes

> Copy the basic type and annotation files into your workspace `com.regnosys.rosetta.lib/src/main/java/model/basictypes.rosetta`

> You will notice a `src-gen` folder appear in the root folder of vscode where the JAVA code bindings are.


> This plugin has only been tested on a mac. If you want to test on windows, please go for it! If it has issues, raise it on github and we can work on it.