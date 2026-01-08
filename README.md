# RText
![Java Build](https://github.com/bobbylight/RText/actions/workflows/gradle.yml/badge.svg)
![Java Build](https://github.com/bobbylight/RText/actions/workflows/codeql-analysis.yml/badge.svg)

RText is a programmer's text editor written in Java.  It has all the features
you would expect:

* Syntax highlighting for 50+ languages
* Code folding
* Regex search and replace
* Find/replace in files
* Varying degrees of code completion for C, Java, Perl, JavaScript, and more
* Varying degrees of syntax checking for various languages ([JSHint](https://jshint.com/)
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

## Building

RText uses [Gradle](https://gradle.org/) to build.  To compile, run
all unit tests, and create the jar, and run:

    ./gradlew build installDist
    java -jar build/install/rtext/RText.jar

Note that RText requires Java 25 or later to build.

### Building the Windows application and installer

To create the Windows version of the application, run the `generateWindowsStarterExe`
task in addition to `installDist`.  This ensures a trimmed-down JRE is generated,
and a starter `RText.exe` file is added into `build/install/rtext`:

    ./gradlew clean build installDist generateWindowsStarterExe

The `generateWindowsStarterExe` task uses a JDK 21 install and `launch4j` as defined in
`gradle.properties`.

Note this gradle task runs `jlink` directly and uses `launch4j` rather than using
`jpackage` since our app needs loose files and `jpackage` seems to require all files
being wrapped into the generated .exe.  This is different than our macOS app task
(discussed below) which uses `jpackage`.

After building the installable image, you can create the win32 installer by
running the `MakeRTextInstaller.nsi` [NSIS](https://nsis.sourceforge.io/Main_Page)
script at the root of the project.

### Building the macOS application

To build the .app bundle into`build/install/RText-<version>.dmg`:

    ./gradlew clean build generateMacApp

The generated `RText-<version>.dmg` can be used to install `RText.app` to the Applications
folder.  Note that this app currently isn't signed, so Gatekeeper will likely prevent
you from installing.  To get around this you'll need to tweak your security policy to
allow installing of apps from outside the App Store.
