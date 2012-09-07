RSTAUI Readme
-------------
Please contact me if you are using RSyntaxTextArea in your project!  I like
to know when people are finding it useful.  Please send mail to:
robert -at- fifesoft dot com.


* About RSTAUI

  This is a library for adding the following dialogs to an application using
  RSyntaxTextArea as an editor:
  
     * Find Dialog
     * Replace Dialog
     * Go to Line Dialog
  
  Searching support is fully featured - regex searches, match case, whole word,
  searching forward and backward, mark all occurrences.  Note that the actual
  searching functionality is handled in the RSyntaxTextArea project (see the
  SearchEngine class); this library just wraps that functionality in a UI.

* License

  RSTAUI is licensed under a modified BSD license.  Please see the included
  RSTAUI.License.txt file.

* Compiling

  If you wish to compile RSTAUI from source, the easiest way to do so
  is via the included Ant build script.  The default target builds the jar.
  
  This project depends on the sister RSyntaxTextArea and AutoComplete projects.
  It is recommended that you check all three projects out side by side.  Then,
  to build:
  
     cd RSyntaxTextArea
     ant
     cd ../AutoComplete
     ant
     cd ../RSTAUI
     ant

* Example Usage

  Compile and run the demo class included with the source distribution:
  
     org.fife.rsta.ui.demo.RSTAUIDemoApp
  
  It is a simple example of using the Find and Replace dialogs.  It currently
  does not demo "Mark All".     
      
* Feedback

  I hope you find RSyntaxTextArea useful.  Bug reports, feature requests, and
  just general questions are always welcome.  Ways you can submit feedback:
  
    * http://forum.fifesoft.com (preferred)
         Has a forum for RSyntaxTextArea and related projects, where you can
         ask questions and get feedback quickly.

    * http://sourceforge.net/projects/rsyntaxtextarea
         Has a tracker for bug reports, feature requests, etc.

    * http://fifesoft.com/rsyntaxtextarea
         Project home page, which contains general information and example
         source code.

* Thanks
  
  Icons in this package (such as lightbulb.png) come from Eclipse and are
  licensed under the EPL (http://www.eclipse.org/legal/epl-v10.html).
  
  Translations:
     
     * Arabic:                 Linostar
     * Chinese:                Terrance, peter_barnes, Sunquan, sonyichi, zvest
     * Chinese (Traditional):  kin Por Fok, liou xiao
     * Dutch:                  Roel, Sebastiaan, lovepuppy
     * French:                 PivWan
     * German:                 bikerpete
     * Hungarian:              flatron
     * Indonesian:             azis, Sonny
     * Italian:                Luca, stepagweb
     * Japanese:               izumi, tomoM
     * Korean:                 Changkyoon, sbrownii
     * Polish:                 Chris, Maciej Mrug
     * Portuguese (Brazil):    Pat, Marcos Parmeggiani, Leandro
     * Russian:                Nadiya, Vladimir
     * Spanish:                Leonardo, phrodo, daloporhecho
     * Turkish:                Burak
