BinEd - Binary/Hexadecimal Editor - Libraries
=============================================

Libraries for hexadecimal viewer/editor component written in Java.

Homepage: https://bined.exbin.org/library  

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

Java Development Kit (JDK) version 8 or later is required to build this project.

For project compiling Gradle 4 build system is used. You can either download and install gradle and run

  gradle build

command in project folder or gradlew or gradlew.bat scripts to download separate copy of gradle to perform the project build.

Currently it might be necessary to use local Maven - Manually download all dependecies from GitHub (clone repositories from github.com/exbin - see. deps directory for names) and run "gradle publish" on each of them.

License
-------

Apache License, Version 2.0 - see LICENSE-2.0.txt  

