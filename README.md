HeapLib
=======

HeapLib a tool/library for analyzing JVM heap dumps. 
HeapLib is using modified heap parser from [Apache NetBeans](https://netbeans.apache.org/) 
(same library is used by [VisualVM](https://visualvm.github.io/)).

Main driver for this project was a need in automated analyzing of heap dump on remote servers.

In addition for original Heap class implementation from NetBeans library this project 
has an alteranative which:

 * Works with heap dump file without any additional on disk files
 * Process GZip compressed heap dump files directly
 * Cannot calculate retained size or walk object references in backward direction
 
Besides an API, you can use executable jar for console to execute OQL/JavaScript.