RText is a programmer's text editor written in Java.  It has all the features
you would expect:

* Syntax highlighting for 40+ languages
* Code folding
* Regex search and replace
* Find/replace in files
* Varying degrees of code completion for C, Java, Perl, JavaScript, and more
* Varying degrees of syntax checking for various languages ([JSHint](http://jshint.com/)
  integration for JavaScript, compiler checking for Perl, XML well-formedness checking, etc.)
* Programmatic macros, write them in either JavaScript or Groovy
* Application lifecycle (bootstrap, plugin loading, cleanup, shutdown)
* Preference loading and saving
* User-configurable key bindings
* Standard modals for Options, Help, About, Printing
* File chooser (richer feature set and improved usability over JFileChooser)
* Dockable windows
* External tools
* Much more

# Building

RText uses [Gradle](http://gradle.org/) to build.  To compile, run
all unit tests, and create the jar, run:

    ./gradlew build

Note that RText only requires Java 5.  To that end, the boot classpath will be set to accommodate
this if a variable `java5CompileBootClasspath` is set to the location of `rt.jar` in a Java 5 JDK.
This can be added to `<maven-home>/gradle.properties` if desired, to avoid diffs in the project's
`gradle.properties`.

To build and run an installable image:

    ./gradlew clean build
    ./gradlew installDist
    java -jar ./build/install/rtext/RText.jar

After building the installable image, you can create the win32 installer by
running the `MakeRTextInstaller.nsi` [NSIS](http://nsis.sourceforge.net/Main_Page)
script at the root of the project.

Building the OS X package is a little wonky at the moment.  I am working
on finding the best way to build an App bundle via Gradle.  For now, the
easiest way to do so is:

    ./gradlew clean build installDist
    vi build.gradle
    <un-comment the macAppBundle plugin at the top and config at the bottom>
    ./gradlew createApp
    cp -R ./build/install/rtext ./build/macApp/RText.app/Contents/
    mv ./build/macApp/RText.app/Contents/rtext ./build/macApp/RText.app/Contents/Java

This should create a functional `RText.app`, but note the application icon
is not correct yet.
