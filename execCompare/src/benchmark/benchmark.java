import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class benchmark {
    public static void main(String[] args) {
        switch (args[0]) {
            case "stdBenchmark":
                stdBenchmark(args[1]);
                break;
            case "nArgsNoStdBenchmark":
                nArgsNoStdBenchmark(args);
                break;
            default:
                System.err.println("Invalid argument");
                return;
        }
    }

    private static void stdBenchmark(String rowName) {
        // using two thread:
        // processBuilder using 1 thread to handle std streams, 接受的parent如果只有一个，只能一个thread处理两个stream
        // apache exec using 2 threads to handle stdout and stderr seperately，两个thread同时接受，两个thread处理两个stream
        final long start = System.nanoTime();
        final int loopTime = 5000000;
        Thread thread1 = new Thread(){
            public void run() {
                for(int i = 0; i < loopTime; i++) {
                    System.out.println("out");
                }
            }
        };

        Thread thread2 = new Thread(){
            public void run(){
                for(int i = 0; i < loopTime; i++) {
                    System.err.println("error");
                }
            }
        };

        thread1.start();
        thread2.start();
        try {
            thread1.join();
            thread2.join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        final long end = System.nanoTime();
        final double totalTime = (end - start) / 1000000000.0;
        writeOneRowData(rowName, totalTime);
    }

    private static void nArgsNoStdBenchmark(String[] args) {
        final int loopTime = 100000;
        List<String> tempStrList = new ArrayList<String>();
        final int argNumber = args.length - 2;
        final String rowName = args[1] + " loop " + loopTime +
                " times with argument number: " + String.valueOf(argNumber);
        final long start = System.nanoTime();

        for (int i = 0; i < loopTime; i++) {
            tempStrList.add("a");
            tempStrList.forEach(s -> {
                boolean isContained = tempStrList.contains("a");
            });
            Collections.sort(tempStrList);
        }
        final long end = System.nanoTime();
        final double totalTime = (end - start) / 1000000000.0;

        writeOneRowData(rowName, totalTime);
    }

    private static void writeOneRowData(String rowName, double totalTime) {
        try (
                FileWriter stdoutPrint = new FileWriter("src/benchmark/output/result.txt", true);
                BufferedWriter bw = new BufferedWriter(stdoutPrint);
                PrintWriter out = new PrintWriter(bw);
        ) {
            out.println(rowName + " time is: " + totalTime + "s");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}