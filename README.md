# CompareProcessBuilderAndApacheExec
This repo contains code to compare the performance of [ProcessBuilder](https://docs.oracle.com/javase/7/docs/api/java/lang/ProcessBuilder.html) and [Apache Commons Exec](https://commons.apache.org/proper/commons-exec/)

## Environment
[Java version "12.0.1"](https://www.java.com/en/)

[JConsole](http://openjdk.java.net/tools/svc/jconsole/)

[IntelliJ IDEA 2020.4](https://www.jetbrains.com/idea/)

[Maven](http://maven.apache.org/POM/4.0.0)

[Apache Commons Exec](https://commons.apache.org/proper/commons-exec/)
```
<dependencies>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-exec</artifactId>
            <version>1.3</version>
        </dependency>
    </dependencies>
```

## Download 
```
https://github.com/dasong2296/CompareProcessBuilderAndApacheExec.git
```
Use IntelliJ IDEA to open the project.

## Set up
Compile the benchmark file first.
```
cd CompareProcessBuilderAndApacheExec/execCompare/src/benchmark
javac benchmark.java 
```
`CompareProcessBuilderAndApacheExec/execCompare/src/benchmark/output/result.txt` records the runtime of each test.
## Create run configurations
To run different test scenarios, users need to create different run configurations by attaching program arguments. Detail will be presented below.

## Benchmark Introduction
Curretly, this code supports user to compare Apache Commons Exec and ProcessBuilder from the following perspectives:
1. Compare two APIs with stdout and stderr. 
2. Compare two APIs without stdout and stderr. And pass n number of arguments when open the external Java program to test if arguments number affecting APIs performance.


### With Stdout and Stderr
The benchmark function for this case is called `stdBenchmark`. This function uses two threads to output the stdout and stderr separately.

#### Apache Commons Exec
Set program argument as `apacheExecStd`. Then run the program. Parent process id will be printed in the console as `pid xxxx`. Run `jconsole pid` to open jconsole to monitor the thread and memory usage. The stdout and stderr stream can be found at `CompareProcessBuilderAndApacheExec/execCompare/src/benchmark/output`.
#### ProcessBuilder
Set program argument as `pbStd`. Then run the program. Parent process id will be printed in the console as `pid xxxx`. Run `jconsole pid` to open jconsole to monitor the thread and memory usage. The stdout and stderr stream can be found at `CompareProcessBuilderAndApacheExec/execCompare/src/benchmark/output`.

### No Stdout and Stderr, Pass N arguments
The benchmark function for this case is called `nArgsNoStdBenchmark`. This function creates a List<String> and uses nested loop to perform some time-consuming implementations to the List<String>. 

#### Apache Commons Exec
Set program argument as `apacheExecNArgsNoStd argsNumber`. Noted that the second argument `argsNumber` is an integer. This means the number of arguments user would like to pass into the external Java program. Then run the program. Parent process id will be printed in the console as `pid xxxx`. Run `jconsole pid` to open jconsole to monitor the thread and memory usage.

#### ProcessBuilder
Set program argument as `pbNArgsNoStd argsNumber`. Noted that the second argument `argsNumber` is an integer. This means the number of arguments user would like to pass into the external Java program. Then run the program. Parent process id will be printed in the console as `pid xxxx`. Run `jconsole pid` to open jconsole to monitor the thread and memory usage.

## Benchmark Result
The following data is my benchmark:
1. [Runtime](https://github.com/dasong2296/CompareProcessBuilderAndApacheExec/blob/main/execCompare/src/benchmark/output/result.txt)
2. [Thread and Memory Usage](https://github.com/dasong2296/CompareProcessBuilderAndApacheExec/tree/main/execCompare/src/benchmark/output/JconsoleOutput)
### With Stdout and Stderr
1. As you can see above, there is a significant time difference between Apache Commons Exec(16.95s) and ProcessBuilder(44.27s) under this case. By comparing the thread difference between two APIs, I found that ProcessBuilder only has one thread to handle the stdout/err streams. However, Apache Commons Exec has two stream handler threads to handle the stdout/err streams. I assume there will be the third one if I added stdin to the code. To magnify this difference, I used two threads in `stdBenchmark` to pass the std streams back to parent process. ProcessBuilder only has one thread, so this thread has to receive the stdout and stderr from the sub-process, and also redirect the streams to targeted files. One thread can only work on one thing at one moment. But Apache Commons Exec has threads to handle stdin, stdout, and stderr independently. In this case, stdout and stderr steams can be received and written to target file simultaneously. It significantly increased the speed.
2. Apache Commons Exec stream handler returns all stdout and stderr streams once the external program ended and write the big chunk of data in one time, meanwhile, ProcessBuilder can redirect the streams to the targeted file while the external program running. This difference is the reason why Apache Commons Exec used much more memory than ProcessBuilder. I believe I can write or override some methods in Apache Commons Exec to improve the memory usage performance. By piping the sdtout and sdterr streams to the target file while receiving the streams, the memory to store stream buffer would be decreased. However, it will cause more I/O cost because I have to perform `write` way more times.

### No Stdout and Stderr, Pass N arguments
1. I cannot find obvious difference when I passed different numbers of arguments to Apache Commons Exec and ProcessBuilder.
2. Apache Commons Exec costs a little more threads, memory, and time than ProcessBuilder. 

## Benchmark Conclusion
When calling an external program with stdin/out/err especially multi-threads stdin/out/err, Apache Commons Exec is obviously a better choice than ProcessBuilder. Plus, by reading the documentation of [ProcessBuilder](https://docs.oracle.com/javase/7/docs/api/java/lang/ProcessBuilder.html) and [Apache Commons Exec](https://commons.apache.org/proper/commons-exec/), I found that the latter one should be a better choice if user would like to implement more detail settings or implementations. However, if the requirement of executing an external program is simple, the former one will be a better choice since it consuming less resources.
