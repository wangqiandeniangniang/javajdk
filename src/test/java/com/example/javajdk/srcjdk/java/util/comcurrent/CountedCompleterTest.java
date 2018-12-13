package com.example.javajdk.srcjdk.java.util.comcurrent;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @ClassName CountedCompleterTest
 * @Description TODO
 * @Author chen.liang
 * @Date 2018/12/12 15:07
 * @Version 1.0
 **/
public class CountedCompleterTest {

    static CountedCompleter<Object> countedCompleter = new CountedCompleter<Object>() {
        @Override
        public void compute() {

        }
    };
    public static void main(String[] args) {
//       testAddToPendingCount();
//        testFactorialTask();
        testFactorialTaskD();
    }
    /**
     * 测试Unsafe方法不要创建，可以直接使用
     */

    public static void testAddToPendingCount() {

        countedCompleter.addToPendingCount(33);
        countedCompleter.compareAndSetPendingCount(22, 0); //表示如果当前挂起数量等于22，就是设置为0
        int i = countedCompleter.decrementPendingCountUnlessZero();// 32
        System.out.println(i);
        int pendingCount = countedCompleter.getPendingCount();
        System.out.println(pendingCount);
    }

    private static void testFactorialTask() {
        List<BigInteger> list = new ArrayList<>();
        for (int i = 3; i < 100; i++) {
            list.add(new BigInteger(Integer.toString(i)));
        }
        BigInteger sum = ForkJoinPool.commonPool().invoke(new FactorialTask(null, new AtomicReference<>(new BigInteger("0"))
                , list)); // forkJoinPool是一个工作池，所有子类实现我这个方法ForkJoinTask接口,就可以使用这个池子分发任务。
        System.out.println("sum of the factorials = " + sum );
    }

    private static class FactorialTask extends CountedCompleter<BigInteger> {// 斐波那契数列
        private static int SEQUENTIAL_THRESHOLD = 5;
        private List<BigInteger> integerList;

        private AtomicReference<BigInteger> result;

        private FactorialTask(CountedCompleter<BigInteger> parent, AtomicReference<BigInteger> result
                , List<BigInteger> integerList) {
            super(parent);
            this.integerList = integerList;
            this.result = result;
        }


        @Override
        public BigInteger getRawResult() {
            return result.get();
        }

        @Override
        public void compute() {
            //this example creates all sub-tasks in this while loop
            while (integerList.size() > SEQUENTIAL_THRESHOLD) {
                //最后5条数据
                List<BigInteger> newTaskList = integerList.subList(integerList.size() - SEQUENTIAL_THRESHOLD, integerList.size());

                //保存list
                integerList = this.integerList.subList(0, this.integerList.size() - SEQUENTIAL_THRESHOLD);

                addToPendingCount(1);
                FactorialTask task = new FactorialTask(this, result, newTaskList);
                task.fork();
            }
            //找出斐波那契数之和 this.integerList
            sumFactorials();
            propagateCompletion();
        }

        private void sumFactorials() {
            for (BigInteger i : integerList) {
                addFactorialToResult(CalcUtil.calculateFactorial(i));
            }

        }

        private void addFactorialToResult(BigInteger factorial) {
            result.getAndAccumulate(factorial, (b1, b2) -> b1.add(b2));
        }
    }


    //===================第二版======================

    private static void testFactorialTaskD() {
        ArrayList<BigInteger> list = new ArrayList<>();
        for (int i = 3; i < 20; i++) {
            list.add(new BigInteger(Integer.toString(i)));
        }

        ForkJoinPool.commonPool().invoke(new FactorialTaskD(null, list));
    }
    private static class FactorialTaskD extends CountedCompleter<Void> {
        private static int SEQUENTIAL_THRESHOLD = 5;
        private List<BigInteger> integerList;
        private int numberCalculated;

        private FactorialTaskD(CountedCompleter<Void> parent, List<BigInteger> integerList) {
            super(parent);
            this.integerList = integerList;
        }

        @Override
        public void compute() {
            if (integerList.size() <= SEQUENTIAL_THRESHOLD) {
                showFactorial();
            } else {
                int middle = integerList.size() / 2;
                List<BigInteger> rightList = integerList.subList(middle, integerList.size());
                List<BigInteger> leftList = integerList.subList(0, middle);
                addToPendingCount(2);
                FactorialTaskD taskRigth = new FactorialTaskD(this, rightList);
                FactorialTaskD taskLeft = new FactorialTaskD(this, leftList);
                taskLeft.fork();
                taskRigth.fork();
            }
            tryComplete();
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            if (caller == this) {
                System.out.printf("completed thread: %s numberCalculated=%s%n",
                        Thread.currentThread().getName(), numberCalculated);
            }
        }

        private void showFactorial() {
            //work-1 11 12 13 14
            //work-2: 3 4 5 6
            //work-3 7 8 9 10
            //main 15 16 17 18 19
            for (BigInteger i : integerList) {
                BigInteger factorial = CalcUtil.calculateFactorial(i);
                System.out.printf("%s!=%s , thread=%s%n", i, factorial, Thread.currentThread().getName());
                numberCalculated++;
            }
        }

    }


}

