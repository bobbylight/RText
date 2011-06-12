================================
= RText Readme                 =
= http://rtext.sourceforge.net =
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

1.  Editing multiple documents at once.
2.  Syntax highlighting for over 25 popular programming languages.
3.  Bracket Matching.
4.  CTags support.
5.  Macros.
6.  Printing and Print Preview.
7.  Find/Replace/Find in Files, including full regex support.
8.  Undo/Redo.
9.  Online HTML Help.
10. Customizable GUI (Look and Feel, icon set, font, background, etc.).
11. Code completion (certain languages only).

Syntax highlighting can be added for new languages relatively easily, and
a plugin API is being developed to add new functionality to RText with ease.


2. Requirements
---------------
RText requires only a 1.4 Java Runtime Environment (JRE) or higher.  No third
party libraries are required.  I recommend using the most recent JRE available
for optimum performance.  You can get the latest JRE for Microsoft Windows
and other platforms here:  http://www.java.com

Using the Source Browser plugin (which comes standard with RText) requires
either Exuberant Ctags, or the classic "ctags" UNIX command.  If you do not
have either installed, RText will still work normally, but the Source Browser
will be disabled.  You can download Exuberant CTags here:
http://sourceforge.net/projects/ctags


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
RText is distributed under the GNU General Public License; please see the
file named License.txt distributed with this program, or visit
http://www.gnu.org.

The editor used in RText is RSyntaxTextArea, an open source, syntax
highlighting text component licensed under the LGPL.  This license is
included with RText in the file RSyntaxTextArea.License.txt.  For more
information on RSyntaxTextArea, please visit
http://fifesoft.com/rsyntaxtextarea.


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
- linostar:                    Arabic Translation
- sonyichi:                    Simplified Chinese Translation
- kp200906, liou xiao:         Traditional Chinese Translation
- Rogier, roel, sebastiaan:    Dutch Translation
- pivwan:                      French Translation
- bikerpete:                   German Translation
- flatron:                     Hungarian Translation
- sonny, gri3fon:              Indonesian Translation
- Luca, stepagweb:             Italian Translation
- tomoM:                       Japanese Translation
- Kwangsub, sbrownii:          Korean Translation
- Maciej:                      Polish Translation
- Marcos, leandro:             Portuguese Translation
- vladimir:                    Russian Translation
- phrodo:                      Spanish Translation
- burak:                       Turkish Translation


8. Special Thanks
-----------------
- Sourceforge.net for hosting RText
- Steve Christensen for creating Solaris packages.
