package com.example.javajdk.srcjdk.java.util.arrayList;

/**
 * @ClassName RangeCheckMicroBenchmark
 * @Description TODO
 * @Author chen.liang
 * @Date 2018/12/8 13:54
 * @Version 1.0
 **/
import java.util.*;
import java.util.regex.Pattern;
import java.util.concurrent.CountDownLatch;

public class RangeCheckMicroBenchmark {
    abstract static class Job {
        private final String name;
        Job(String name) { this.name = name; }
        String name() { return name; }
        abstract void work() throws Throwable;
    }

    /**
     * 垃圾回收机制， 要等GC完成之后才继续执行否则等待
     */
    private static void collectAllGarbage() {
        final CountDownLatch drained = new CountDownLatch(1);
        try {
            System.gc();        // enqueue finalizable objects
            new Object() { protected void finalize() {
                drained.countDown(); }};
            System.gc();        // enqueue detector
            drained.await();    // wait for finalizer queue to drain
            System.gc();        // cleanup finalized objects
        } catch (InterruptedException e) { throw new Error(e); }
    }

    /**
     * Runs each job for long enough that all the runtime compilers
     * have had plenty of time to warm up, i.e. get around to
     * compiling everything worth compiling.
     * Returns array of average times per job per run.
     */
    private static long[] time0(Job ... jobs) throws Throwable {
        final long warmupNanos = 10L * 1000L * 1000L * 1000L;
        long[] nanoss = new long[jobs.length];
        for (int i = 0; i < jobs.length; i++) {
            collectAllGarbage();
            long t0 = System.nanoTime();
            long t;
            int j = 0;
            do { jobs[i].work(); j++; }
            while ((t = System.nanoTime() - t0) < warmupNanos);
            nanoss[i] = t/j; //取平均值
        }
        return nanoss;
    }

    private static void time(Job ... jobs) throws Throwable {

//        long[] warmup = time0(jobs); // Warm up run
        long[] nanoss = time0(jobs); // Real timing run
        long[] milliss = new long[jobs.length];
        double[] ratios = new double[jobs.length];

        final String nameHeader   = "Method";
        final String millisHeader = "Millis";
        final String ratioHeader  = "Ratio";

        int nameWidth   = nameHeader.length();
        int millisWidth = millisHeader.length();
        int ratioWidth  = ratioHeader.length();

        for (int i = 0; i < jobs.length; i++) { //获取最宽长度
            nameWidth = Math.max(nameWidth, jobs[i].name().length());

            milliss[i] = nanoss[i]/(1000L * 1000L);
            millisWidth = Math.max(millisWidth,
                    String.format("%d", milliss[i]).length());

            ratios[i] = (double) nanoss[i] / (double) nanoss[0];
            ratioWidth = Math.max(ratioWidth,
                    String.format("%.3f", ratios[i]).length());
        }

        String format = String.format("%%-%ds %%%dd %%%d.3f%%n",
                nameWidth, millisWidth, ratioWidth); //格式化字符串
        String headerFormat = String.format("%%-%ds %%%ds %%%ds%%n",
                nameWidth, millisWidth, ratioWidth); //某种格式
        System.out.printf(headerFormat, "Method", "Millis", "Ratio");

        // Print out absolute and relative times, calibrated against first job
        for (int i = 0; i < jobs.length; i++)
            System.out.printf(format, jobs[i].name(), milliss[i], ratios[i]);
    }

    private static String keywordValue(String[] args, String keyword) {
        for (String arg : args)
            if (arg.startsWith(keyword))
                return arg.substring(keyword.length() + 1);
        return null;
    }

    private static int intArg(String[] args, String keyword, int defaultValue) {
        String val = keywordValue(args, keyword);
        return val == null ? defaultValue : Integer.parseInt(val);
    }

    private static Pattern patternArg(String[] args, String keyword) {
        String val = keywordValue(args, keyword);
        return val == null ? null : Pattern.compile(val);  //Pattern.compile编译字符串
    }

    /**
     * 表示执行那些job
     * @param filter
     * @param jobs
     * @return
     */
    private static Job[] filter(Pattern filter, Job[] jobs) {
        if (filter == null) return jobs;
        Job[] newJobs = new Job[jobs.length];
        int n = 0;
        for (Job job : jobs)
            if (filter.matcher(job.name()).find())
                newJobs[n++] = job;
        // Arrays.copyOf not available in JDK 5
        Job[] ret = new Job[n];
        System.arraycopy(newJobs, 0, ret, 0, n);
        return ret;
    }

    private static void deoptimize(ArrayList<Integer> list) {
        for (Integer x : list)
            if (x == null)
                throw new Error();
    }

    /**
     * Usage: [iterations=N] [size=N] [filter=REGEXP]
     */
    public static void main(String[] args) throws Throwable {
        final int iterations = intArg(args, "iterations", 30000); //初始化参数迭代次数为30000次  args内容可能是["iterations20000","size1000", "filter*/"]
        final int size       = intArg(args, "size", 1000); //元素大小为1000
        final Pattern filter = patternArg(args, "filter");//过滤

        final ArrayList<Integer> list = new ArrayList<Integer>();
        final Random rnd = new Random();
        for (int i = 0; i < size; i++)
            list.add(rnd.nextInt());

        final Job[] jobs = {
                new Job("get") { void work() { //测试get方法
                    for (int i = 0; i < iterations; i++) {
                        for (int k = 0; k < size; k++)
                            if (list.get(k) == 42)
                                throw new Error();
                    }
                    deoptimize(list);}}, //遍历集合
                new Job("set") { void work() { //测试set方法
                    Integer[] xs = list.toArray(new Integer[size]); //变成数组
                    for (int i = 0; i < iterations; i++) {
                        for (int k = 0; k < size; k++)
                            list.set(k, xs[k]);
                    }
                    deoptimize(list);}},
                new Job("get/set") { void work() {//测试get/set方法
                    for (int i = 0; i < iterations; i++) {
                        for (int k = 0; k < size; k++)
                            list.set(k, list.get(size - k - 1));
                    }
                    deoptimize(list);}},
                new Job("add/remove at end") { void work() {//测试添加和移除方法
                    Integer x = rnd.nextInt();
                    for (int i = 0; i < iterations; i++) {
                        for (int k = 0; k < size - 1; k++) {
                            list.add(size, x);
                            list.remove(size);
                        }
                    }
                    deoptimize(list);}},
                new Job("subList get") { void work() {// 子list get方法
                    List<Integer> sublist = list.subList(0, list.size());
                    for (int i = 0; i < iterations; i++) {
                        for (int k = 0; k < size; k++)
                            if (sublist.get(k) == 42)
                                throw new Error();
                    }
                    deoptimize(list);}},
                new Job("subList set") { void work() { //字符串set方法
                    List<Integer> sublist = list.subList(0, list.size());
                    Integer[] xs = sublist.toArray(new Integer[size]);
                    for (int i = 0; i < iterations; i++) {
                        for (int k = 0; k < size; k++)
                            sublist.set(k, xs[k]);
                    }
                    deoptimize(list);}},
                new Job("subList get/set") { void work() {
                    List<Integer> sublist = list.subList(0, list.size());
                    for (int i = 0; i < iterations; i++) {
                        for (int k = 0; k < size; k++)
                            sublist.set(k, sublist.get(size - k - 1));
                    }
                    deoptimize(list);}},
                new Job("subList add/remove at end") { void work() {
                    List<Integer> sublist = list.subList(0, list.size());
                    Integer x = rnd.nextInt();
                    for (int i = 0; i < iterations; i++) {
                        for (int k = 0; k < size - 1; k++) {
                            sublist.add(size, x);
                            sublist.remove(size);
                        }
                    }
                    deoptimize(list);}}
        };

        time(filter(filter, jobs));
    }
}
