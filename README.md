BinEd - Binary/Hexadecimal Components Library
=============================================

Library for hexadecimal viewer/editor components written in Java.

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

Build commands: "gradle build" and "gradle distZip"

Java Development Kit (JDK) version 8 or later is required to build this project.

For project compiling Gradle 7.1 build system is used: https://gradle.org

You can either download and install gradle or use gradlew or gradlew.bat scripts to download separate copy of gradle to perform the project build.

On the first build there will be an attempt to download all required dependecy modules and currently it's necessary to execute build twice.

Alternative is to have all dependecy modules stored in local maven repository - Manually download all dependencies from GitHub (clone repositories from github.com/exbin - see. deps directory for names) and run "gradle publish" on each of them.

License
-------

Apache License, Version 2.0 - see LICENSE.txt  

