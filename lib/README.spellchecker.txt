RSyntaxTextArea SpellChecker README
-----------------------------------
Last modified: 08/26/2012

By popular demand, here is the beginnings of a spell checker add-on for
RSyntaxTextArea.  For programming languages, it spell-checks text in comments,
and when editing plain text files, the entire file is spell-checked.  Spelling
errors are squiggle-underlined with the color of your choosing, and hovering
the mouse over a misspelled word displays a tooltip with suggested fixes (if
any).

This add-on is based on Jazzy, a Java spell checker.  Indeed, 99% of the code
is just Jazzy, ever-so-slightly modified.  See http://jazzy.sourceforge.net
for more information about the Jazzy library.

Included with this distribution is an English dictionary (both American and
British).  The easiest method to add spell checking to RSTA is as follows:

     import org.fife.ui.rsyntaxtextarea.spell.*;
     // ...
     File zip = new File("location/of/included/english_dic.zip");
     boolean american = true; // "false" will use British English
     SpellingParser parser = SpellingParser.
                                 createEnglishSpellingParser(zip, american);
     textArea.addParser(parser);

See class org.fife.ui.rsyntaxtextarea.spell.demo.SpellingParserDemo for a
working example.

If you wish to compile SpellChecker from source, the easiest way to do so
is via the included Ant build script.  The default target builds the jar.

This project depends on its sister RSyntaxTextArea project.  It is recommended
that you check the two projects out side by side.  Then, to build:
  
   cd RSyntaxTextArea
   ant
   cd ../SpellChecker
   ant


This add-on is licensed under the LGPL; see the included
SpellChecker.License.txt file.
Jazzy is licensed under the LGPL; see Jazzy.LICENSE.txt.

Translators:
   Chinese (Simplified):   Terrance Chen
   Korean:                 Changkyoon
   Polish:                 Chris
   Portuguese (Brazil):    Patricia Thaine
   Russian:                Nadiya Holub
   Spanish:                Leonardo Aguado
