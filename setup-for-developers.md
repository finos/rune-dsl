# Setup for developers
This guide is meant for everyone who wants to contribute to the Rosetta DSL and needs to get things up and running.

If this guide does not work for you, be sure to raise an issue. This way we can help you figure out what the problem is and update this guide to prevent the same problem for future users.

# 1. Building with Maven
Start by cloning the project: `git clone https://github.com/REGnosys/rosetta-dsl`

Our project runs with Java 11. Make sure that your Maven also uses this version of Java by running `mvn -v`.

To build the project, run `mvn clean install`.

### Troubleshooting
Our project is build with the Maven [Tycho](https://www.eclipse.org/tycho/sitedocs/tycho-maven-plugin/index.html) plugin. If you have ever used this plugin before with a different version, you might be seeing errors such as `[ERROR] Internal error: java.lang.IllegalArgumentException: bundleLocation not found: ...`. This happens because switching Tycho versions sometimes corrupts your Maven repository. There are two possible solutions:
1. If you don't need Tycho for other projects, you can simply delete your `~/.m2/repository` folder and retry. Maven will reinstall the necessary dependencies.
2. If you want to remain compatible with multiple Tycho versions, you can install the Maven dependencies of the Rosetta DSL in another repository by passing the command line option `-Dmaven.repo.local=/a/path/to/my/m2repo` to Maven. In this case, also make sure to add this folder to the classpath in Eclipse for the next step!

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
2. **Load the target platform**: in the `com.regnosys.rosetta.target` project, there is a file called `com.regnosys.rosetta.target.target`. Open it and click on the button in the right top corner to set it as the active target platform.
3. **Update Maven dependencies**: right click on the `com.regnosys.rosetta.parent` project > Maven > Update project... and finish.

### Troubleshooting
Make sure you have successfully run `mvn clean install`. (see section 1 of this guide)

If you're seeing 1000+ errors in the "Problems" window of Eclipse, try the following.
1. Disable auto-building. (Project > Build automatically)
2. Close Eclipse and open it again.
3. Open the `com.regnosys.rosetta.target.target` file and wait for Eclipse to resolve it. (might take a couple of minutes)
4. Reload the target platform.
5. Update Maven dependencies again.
6. Re-enable auto-building.

# 3. Running a Rosetta IDE locally
Once you are all set-up, you should be able to run an Eclipse IDE with Rosetta support locally. Follow these steps.

1. Right click the `com.regnosys.rosetta` project > Run as > Eclipse Application.
2. Once the new window opens, create an empty project with File > New > Project..., select the wizard called "Project" under "General", choose a name and finish.
3. Create a file called `test.rosetta` and paste the following content:
```
namespace com.world.hello

type Foo:
  a int (1..1)
  b boolean (0..*)
```
You will notice `int` and `boolean` are not being recognized. To fix that, you need to add two files to your project:

4. In the `com.regnosys.rosetta.lib` project, navigate to `src/main/java/model`. There should be two files there: `annotations.rosetta` and `basictypes.rosetta`.
5. Copy them and paste them in your newly created project.

Now your project should not contain any errors. Notice that the Java code generator will automatically kick in and generate a `Foo` class.
