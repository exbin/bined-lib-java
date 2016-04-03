XBUP: eXtensible Binary Universal Protocol
==========================================

The goal of this project is to design and to provide an open-source support for unified general binary data representation format.

This should provide following advantages:

 * Advanced Data Structures - Unified structure should allow to combine various types of data together
 * Efficiency - Optional compression and encryption on multiple levels should allow effective representation of binary data
 * Flexibility - General framework should provide data transformations/processing and compatibility issues solving capability
 * Comprehensibility - Catalog of data types, metadata, relations and abstraction should allow better understanding of data

Homepage: http://xbup.exbin.org
Version: 0.2.0-SNAPSHOT

This repository contains Java implementation of the protocol and support tools and sample files.

Structure
---------

As the project is currently in alpha stage, repository contains complete resources for distribution package with following folders:

 * bin - Executable applications
 * doc - Documentation
 * lib - Library files
 * resources - Related resource files, like sample files, images, etc.

License
-------

Project uses various libraries with specific licenses and some tools are licensed with multiple licenses with exceptions for specific modules to cover license requirements for used libraries.

Main license is: GNU/LGPL (see gpl-3.0.txt AND lgpl-3.0.txt)
License for documentation: GNU/FDL (see doc/fdl-1.3.txt)
