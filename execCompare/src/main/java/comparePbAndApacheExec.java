import org.apache.commons.exec.*;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Requirement:
 *
 * Learn the APIs of Apache Exec and ProcessBuilder
 * Design experiments to compare the performance of Apache Exec and ProcessBuilder
 * (e.g., calling an external program with/without capturing stdout and stderr)
 * You need to benchmark the two sets of APIs to come to certain conclusions.
 * Technically, this is not a coding task. You need to come up with multiple usage
 * scenarios of calling external programs, and write benchmark code, and run these benchmarks.
 *
 * The performance number I am interested in includes (1) time, (2) number of threads used, and
 * (3) memory usage if you managed to measure it (the third is optional).
 * **/

public class comparePbAndApacheExec {
    private static final int timeOut = 600000;
    public static void main(String[] args) throws Exception {
        int argsNumber = 0;
        if (args.length != 1 && args.length != 2) {
            System.err.println("Invalid length of argument. Please read README file for instruction");
            return;
        } else if (args.length == 1) {
            if (args[0] == "apacheExecNArgsNoStd" || args[0] == "pbNArgsNoStd") {
                System.err.println("Invalid length of argument for n number of args " +
                        "and no stdout/stderr case. It only accepts two args.");
                return;
            }
        } else {
            if (args[0] == "apacheExecStd" || args[0] == "pbStd") {
                System.err.println("Invalid length of argument for " +
                        "stdout/stderr case. It only accepts one args.");
                return;
            }

            try {
                argsNumber = Integer.parseInt(args[1]);
            } catch (NumberFormatException e)
            {
                System.err.println("Invalid argument for n number of args " +
                        "and no stdout/stderr case. Please read README file for instruction.");
                return;
            }
        }

        switch (args[0]) {
            case "apacheExecStd":
                apacheExecStd();
                break;
            case "pbStd":
                pbStd();
                break;
            case "apacheExecNArgsNoStd":
                apacheExecNArgsNoStd(argsNumber);
                break;
            case "pbNArgsNoStd":
                pbNArgsNoStd(argsNumber);
                break;
            default:
                System.err.println("Invalid argument: " + args[0]);
                return;
        }
    }

    public static class CollectingLogOutputStream extends LogOutputStream {
        private final List<String> lines = new LinkedList<String>();
        @Override protected void processLine(String line, int level) {
            lines.add(line);
        }
        public List<String> getLines() {
            return lines;
        }
    }

    /**
     * Call ProcessBuilder API to create a sub-process and call an external java program with stderr and stdout.
     * Redirect the stdout and stderr stream to src/benchmark/output directory
     * Log runtime to src/benchmark/output/result.txt file
     **/
    static void pbStd() throws IOException, InterruptedException {
        System.out.println("pid " + getProcessId());
        String[] cmd = {
                "java",
                "-cp",
                "src/benchmark/",
                "benchmark",
                "stdBenchmark",
                "pbStd"
        };
        File outputFile = new File("src/benchmark/output/OutputLog.txt");
        File errorFile = new File("src/benchmark/output/ErrLog.txt");
        ProcessBuilder pb = new ProcessBuilder(cmd).redirectErrorStream(false);

        pb.redirectOutput(outputFile);
        pb.redirectError(errorFile);
        Process p = pb.start();

        int rc = p.waitFor();
        if (rc != 0)
            System.err.println("Unexpected exit code: " + rc);
    }

    /**
     * Call Apache Commons Exec API to create a sub-process and call an external java program with stderr and stdout.
     * Redirect the stdout and stderr stream to src/benchmark/output directory
     * Log runtime to src/benchmark/output/result.txt file
     **/
    static int apacheExecStd() throws InterruptedException, IOException {
        System.out.println("pid " + getProcessId());
        int exitValue;
        CommandLine cmd = new CommandLine("java");
        cmd.addArgument("-cp");
        cmd.addArgument("src/benchmark/");
        cmd.addArgument("benchmark");
        cmd.addArgument("stdBenchmark");
        cmd.addArgument("apacheExecStd");
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        CollectingLogOutputStream stdout = new CollectingLogOutputStream();
        CollectingLogOutputStream stderr = new CollectingLogOutputStream();
        PrintWriter stdoutPrint = new PrintWriter("src/benchmark/output/OutputLog.txt");
        PrintWriter stderrPrint = new PrintWriter("src/benchmark/output/ErrLog.txt");

        PumpStreamHandler streamHandler = new PumpStreamHandler(stdout, stderr);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeOut);
        Executor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);
        executor.setWatchdog(watchdog);
        try {
            executor.execute(cmd, resultHandler);
        } catch (ExecuteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        resultHandler.waitFor();
        List<String> stdOutList = stdout.getLines();
        for (String s : stdOutList) {
            stdoutPrint.println(s);
        }
        List<String> stdErrList = stderr.getLines();
        for (String s : stdErrList) {
            stderrPrint.println(s);
        }
        exitValue = resultHandler.getExitValue();
        stderrPrint.close();
        stdoutPrint.close();
        return exitValue;
    }

    /**
     * Call Apache Commons Exec API to create a sub-process and call an external java program without stderr and stdout.
     * @arg {Number of arguments inserted when calling the external java program} argNumber
     **/
    static void apacheExecNArgsNoStd(int argNumber) throws IOException {
        int exitValue;
        System.out.println("pid " + getProcessId());
        CommandLine cmd = new CommandLine("java");
        cmd.addArgument("-cp");
        cmd.addArgument("src/benchmark/");
        cmd.addArgument("benchmark");
        cmd.addArgument("nArgsNoStdBenchmark");
        cmd.addArgument("apacheExecNArgsNoStd");
        for (int i = 0; i < argNumber; i++) {
            cmd.addArgument("testArg");
        }

        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeOut);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWatchdog(watchdog);
        exitValue = executor.execute(cmd);
        if (exitValue != 0) {
            System.err.println("Unexpected exit value: " + exitValue);
        }
    }

    /**
     * Call ProcessBuilder API to create a sub-process and call an external java program without stderr and stdout.
     * @arg {Number of arguments inserted when calling the external java program} argNumber
     **/
    static void pbNArgsNoStd(int argNumber) throws IOException, InterruptedException {
        System.out.println("pid " + getProcessId());
        List<String> cmd = new ArrayList<>();
        cmd.add("java");
        cmd.add("-cp");
        cmd.add("src/benchmark/");
        cmd.add("benchmark");
        cmd.add("nArgsNoStdBenchmark");
        cmd.add("pbNArgsNoStd");

        for (int i = 0; i < argNumber; i++) {
            cmd.add("testArg");
        }
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p = pb.start();

        int rc = p.waitFor();
        if (rc != 0)
            System.err.println("Unexpected exit code: " + rc);
    }

    /**
     * Get parent process id.
     **/
    private static String getProcessId() {
        return Long.toString(ProcessHandle.current().pid());
    }


    /**
     * Not sure how to use it... Commented.
     * **/
//    private static void getThreadInfo() {
//        int nbRunning = 0;
//
//        for (Thread t : Thread.getAllStackTraces().keySet()) {
//            nbRunning ++;
//            System.out.println("check current thread name and count " + t.getState() + nbRunning);
//        }
//    }
}