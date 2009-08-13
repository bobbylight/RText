RSyntaxTextArea SpellChecker README
-----------------------------------
Last modified: 7/26/2009

Note that this is preview software and may well contain bugs!

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
     textArea.setParser(parser);

See class org.fife.ui.rsyntaxtextarea.spell.demo.SpellingParserDemo for a
working example.

This add-on is licensed under the LGPL, as is Jazzy itself and RSyntaxTextArea.
See the included LICENSE.txt file.
