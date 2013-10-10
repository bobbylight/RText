================================
= RText Readme                 =
= http://fifesoft.com/rtext    =
================================

Contents
--------
 1. About RText
 2. Requirements
 3. Usage
 4. License
 5. Getting Support
 6. The Author
 7. Contributors
 8. Special Thanks



1. About RText
--------------
RText is a programmer's text editor.  It is designed to be efficient, powerful,
intuitive and user-friendly, delivering a productive and enjoyable programming
experience.  Since it is written in Java, RText works on all platforms you need
it to: Windows, Linux, UNIX, Mac OS X, etc.

RText currently has many features, including (but not limited to):

1.  Syntax highlighting for over 30 popular programming languages.
2.  Code folding.
3.  Editing multiple documents at once.
4.  Bracket matching.
5.  CTags support.
6.  Macros.
7.  Printing and Print Preview.
8.  Find/Replace/Find in Files, including full regex support.
9.  Undo/Redo.
10. Online HTML Help.
11. Customizable GUI (Look and Feel, icon set, font, background, etc.).
12. Code completion (certain languages only).

Syntax highlighting can be added for new languages relatively easily, and
a plugin API is being developed to add new functionality to RText with ease.


2. Requirements
---------------
RText requires only a 1.5 Java Runtime Environment (JRE) or higher; however,
using the most recent JRE available always gives the best performance and most
features.  You can get the latest JRE for Microsoft Windows and other platforms
here:  http://www.java.com

Using the Source Browser plugin (which comes standard with RText) requires
either Exuberant Ctags, or the classic "ctags" UNIX command.  If you do not
have either installed, RText will still work normally, but the Source Browser
will be disabled.  You can download Exuberant CTags here:
http://sourceforge.net/projects/ctags

Code completion and syntax checking for some languages, such as Java and Perl,
requires you to configure RText to know where the compiler and libraries live.
This can be done from the Options dialog (Edit -> Options).


3. Usage
--------
Windows:  After running the RText installer, you can run the program either
          from your Start menu or directly from the RText icon on your desktop.
Mac OS X: Drag and drop the RText application (RText.app) into your
          Applications folder.  You can also drag it into your taskbar; then
          starting RText is as easy as clicking on its icon.
*nix:     Unzip the RText binary image into a directory, say
          "/users/userid/rtext/".  You can then either run RText directly from
          its main jar (e.g. "java -jar <rtext-path>/rtext/rtext.jar") or
          create a one-line shell script that runs the above command, placing
          that script in your ~/bin directory.

If you have problems getting RText to run, please ask for help in RText's
support forums:
http://sourceforge.net/tracker/?atid=610805&group_id=95266&func=browse


4. License
-----------
As of version 2.0.2, RText is distributed under a modified BSD license; please
see the file named License.txt distributed with this program, or visit
http://www.opensource.org/licenses/BSD-3-Clause.

All libraries used by RText (which are listed below) come with their license
files included in both the binary and source packages.

The editor used in RText is RSyntaxTextArea, an open source, syntax
highlighting text component licensed under a modified BSD license.  This
license is included with RText in the file RSyntaxTextArea.License.txt.  For
more information on RSyntaxTextArea, please visit
http://fifesoft.com/rsyntaxtextarea.

RSyntaxTextArea's RSTALanguageSupport (included as part of the language support
plugin via language_support.jar) includes Mozilla Rhino for JavaScript code
completion and parsing, which is licensed under the MPL 1.1.  For more
information, please visit http://www.mozilla.org/rhino/.
  
RText links against RSyntaxTextArea's SpellChecker plugin, which is built on
top of Jazzy, an open source spell checker library for Java applications.
Since Jazzy is licensed under the LGPL 2.1, the rsta_spellchecker.jar is also
licensed under LGPL 2.1.  For more information, please visit the
RSyntaxTextArea project at http://fifesoft.com/rsyntaxtextarea.

The source code tidying plugin uses JTidy, a Java port of HTMLTidy which uses
an "MIT-like license."  See http://jtidy.sourceforge.net/ for more information.


5. Getting Support
------------------
Try the project's home page:  http://sourceforge.net/projects/rtext
There you will find forums where you can post questions, file bug
reports, submit feature requests, etc.


6. The Author
-------------
Robert Futrell
robert_futrell@users.sourceforge.net


7. Contributors
---------------
- Mawaheb, linostar:           Arabic Translation
- peter barnes, Terrance:      Simplified Chinese Translation
- kp200906, liou xiao:         Traditional Chinese Translation
- Rogier, roel, sebastiaan:    Dutch Translation
- Pat, pivwan:                 French Translation
- Domenic, bikerpete:          German Translation
- Zityi, flatron:              Hungarian Translation
- sonny, gri3fon:              Indonesian Translation
- Argaar, Luca, stepagweb:     Italian Translation
- Josh, tomoM:                 Japanese Translation
- Changkyoon, Kwangsub:        Korean Translation
- Chris, Maciej:               Polish Translation
- Pat, Marcos, leandro:        Portuguese Translation
- Nadiya, Vladimir:            Russian Translation
- Leonardo:                    Spanish Translation
- Cahit, burak:                Turkish Translation


8. Special Thanks
-----------------
- Sourceforge.net for hosting RText's bug tracker and other tools.
- Steve Christensen for creating Solaris packages.
- Rob Manning for getting RText into Maven.
