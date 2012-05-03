
                         J A Z Z Y : Java Spell Checker


  What is it?
  -----------

  Jazzy is a 100% pure Java library implementing a spell checking algorithm
  similar to aspell. It may be used to spell check a variety of sources.

  The Latest Version
  ------------------

  The latest version is available from the Jazzy project web site 
  ( http://sourceforge.net/projects/jazzy ).

  Requirements
  ------------

  The following requirements exist for installing Jazzy:

   o  Java Interpreter:

      A fully compliant Java 1.1 Runtime environment is needed for the core engine of Jazzy 
      to operate. (For example: for use in a servlet)
      
      A fully compliant Java 1.3 Runtime environment is needed for the swing components of
      Jazzy to operate.

   o  Java JFC (Swing components):

      These GUI extentions are required for proper GUI functionality. However, core 
      spell check functionality can work without Swing Components. 

  Installation Instructions and Documentation
  -------------------------------------------

  There are two ways to install Jazzy. One from a pre packaged version and the other is to 
  compile the sources from CVS.

  If you have downloaded the source code from CVS, please cd to the jazzy directory and run ant.
  
  
  Building Jazzy From the Source
  ------------------------------
  1) In order to build the jedit plugin, you will need to have the jedit.jar in your classpath, 
	 otherwise you will get a lot of compile errors. All of the build errors relate to the files
	 under 
		src/com/swabunga/spell/jedit/*
	 The easiest way to get jedit.jar onto your machine is to install jedit. You can find it at
		http://sourceforge.net/projects/jedit

  2) In order to build the sample applet spell checker, you will need to sign the jar with "jazzykey". 
	 In order to sign the jar, you will need a key called "jazzykey". 
	 On some systems, you should be able to run the following command to get the key built: 
		keytool -genkey -alias jazzyKey 
		when prompted for the password, use the same one that's in build.xml (search for "sign")
	 For more information on creating keys, see: 
		http://java.sun.com/docs/books/tutorial/security1.2/toolsign/step3.html
		

  3) The ant target that builds most things is "binary-release", but you may want to build a smaller
	 target. 
	 The current default target is "library-all", which may be sufficient if you just want the jar.

  4) In order to see the applet demo, you will have to have a web server running. Point it to the 
	 www directory to see the index.html page and the applet demo.html page  

  Licensing and legal issues
  --------------------------

  Jazzy is licensed under the LGPL. See LICENSE.txt for license restrictions.

