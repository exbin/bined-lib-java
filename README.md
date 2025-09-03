BinEd - Binary/Hex Viewer/Editor Component Library
==================================================

Library for hex viewer/editor components written in Java.

Homepage: https://bined.exbin.org/library  

Published in Maven: https://mvnrepository.com/artifact/org.exbin.bined  

Downloads
---------

ZIP pack: https://bined.exbin.org/download/?group=library&variant=0  

Screenshot
----------

![BinEd-Example Screenshot](images/example_screenshot.png?raw=true)

Features
--------

  * Data as hexadecimal codes and text preview
  * Insert and overwrite edit modes
  * Support for selection and clipboard actions
  * Scrollbars fixed or optional, character/line or pixel precision
  * Support for showing unprintable/whitespace characters
  * Support for undo/redo
  * Support for charset/encoding selection
  * Codes can be also binary, octal or decimal
  * Support for customizable highlighting
  * Support for huge files
  * Delta mode - Only changes are stored in memory

Compiling
---------

Build commands: "gradle build" and "gradle distZip"

Java Development Kit (JDK) version 8 or later is required to build this project.

For project compiling Gradle 7.1 build system is used: https://gradle.org

You can either download and install gradle or use gradlew or gradlew.bat scripts to download separate copy of gradle to perform the project build.

On the first build there will be an attempt to download all required dependecy modules.

Alternative is to have all dependecy modules stored in local maven repository:

    git clone https://github.com/exbin/exbin-auxiliary-java.git
    cd exbin-auxiliary-java
    gradlew build publish
    cd ..

License
-------

Apache License, Version 2.0 - see LICENSE.txt  

