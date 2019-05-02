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
all unit tests, and create the jar, and run:

    ./gradlew build installDist
    java -jar build/install/rtext/RText.jar

Note that RText requires Java 11 or later to build.

## Building the Windows application and installer

To create the Windows version of the application, run the `generateWindowsStarterExe`
task in addition to `installDist`.  This ensures a trimmed-down JRE is generated,
and a starter `RText.exe` file is added into `build/install/rtext`:

    ./gradlew build installDist generateWindowsStarterExe

The `generateWindowsStarterExe` task uses a JDK 11 install and `launch4j` as defined in
`gradle.properties`.

After building the installable image, you can create the win32 installer by
running the `MakeRTextInstaller.nsi` [NSIS](http://nsis.sourceforge.net/Main_Page)
script at the root of the project.

## Building the OS X application

Building the OS X package has just been revamped.  Everything seems to be
working, except for the fact that the app icon in the doc doesn't take
(the default Java icon is used).  The .app bundle uses the proper icon
however.

Here's how to build the .app bundle into `build/install/RText.app`:

    ./gradlew clean build generateMacApp

Then, open `build/install` in Finder.  Right-click `RText.app` ->
`Get Info`.  Drag-and-drop `./mac/RText.icns` on top of the icon in the
top-left of this dialog to update the app bundle's icon.  Now you can
double-click RText.app to run it.
