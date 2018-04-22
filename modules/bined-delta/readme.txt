Concept:

- there are data segments of specified length
- segment can either point to file or to memory block
- when file is saved, both types of segments must be processed, both for document and undo
- files are available as file sources
- data blobs are available as data sources

Tasks to be done:
- file loading/saving with percentage and control button
- create repository of data sources and segments
- provide operations across multiple segments allowing shared data, automatic merging and copy-on-write optimizations
- consider search tree for better performance
