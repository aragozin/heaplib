﻿HeapLib
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

Using HeapLib from Java code
----------------------------

HeapLib is a library you can use from Java code.
You can find few examples in `hprof-heap/src/test/java/org/gridkit/jvmtool/heapdump/example/`
path formatted as JUnit tests.

Using HeapLib CLI
-----------------

You can use executable jar to execute OQL/JavaScript scripts from console.


To get executable java build project with `mvn clean package`. Executable jar would be placed under name `heap-cli/target/heap-cli-0.1-SNAPSHOT.jar`.

Below few commands support by executable:

 * `java -jar heap-cli-*.jar --commands` print list of supported command
 * `java -jar heap-cli-*.jar histo -d dump.hprof` produce class histogram from path
 * `java -jar heap-cli-*.jar exec --noindex -d dump.hprof -j script.oql` execute script on heap dump open in diskless mode (no on-disk indexes should be generated)

Few script examples could be would under `hprof-oql-engine/src/test/resources/oql/` path.

