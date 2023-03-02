# Setup for developers
This guide is meant for everyone who wants to contribute to the Rosetta DSL and needs to get things up and running.

If this guide does not work for you, be sure to raise an issue. This way we can help you figure out what the problem is and update this guide to prevent the same problem for future users.

# 1. Building with Maven
Start by cloning the project: `git clone https://github.com/REGnosys/rosetta-dsl`

Our project runs with Java 11. Make sure that your Maven also uses this version of Java by running `mvn -v`.

To build the project, run `mvn clean install`.

# 2. Setting things up in Eclipse
## Install Eclipse IDE for Java and DSL Developers
Install the latest version of the "Eclipse IDE for Java and DSL Developers" using the [Eclipse Installer](https://www.eclipse.org/downloads/packages/installer).

## Install the Xsemantics plugin
We use the [Xsemantics DSL](https://github.com/eclipse/xsemantics) to define the type system of Rosetta. To enable language support for it in Eclipse, follow these steps:
1. Find out which version of Xsemantics you need by looking in the `pom.xml` file of the parent project. There should be a property called `xsemantics.version`.
2. Go to Help > Install New Software...
3. In 'Work with' fill in [https://download.eclipse.org/xsemantics/milestones/](https://download.eclipse.org/xsemantics/milestones/).
4. Install the appropriate version of XSemantics.

## Setup the project
1. **Open the project in Eclipse**: File > Open Projects from File System..., select the right folder, click Finish.
3. **Update Maven dependencies**: right click on the `com.regnosys.rosetta.parent` project > Maven > Update project... and finish.

### Troubleshooting
Make sure you have successfully run `mvn clean install`. (see section 1 of this guide)

If you're seeing 1000+ errors in the "Problems" window of Eclipse, try the following.
1. Disable auto-building. (Project > Build automatically)
2. Close Eclipse and open it again.
3. Update Maven dependencies again.
4. Re-enable auto-building.
