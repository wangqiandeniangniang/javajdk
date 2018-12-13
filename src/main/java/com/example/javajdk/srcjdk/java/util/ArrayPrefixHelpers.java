/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package com.example.javajdk.srcjdk.java.util;

/*
 * Written by Doug Lea with assistance【帮助】 from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.CountedCompleter;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.DoubleBinaryOperator;

/**
 * ForkJoin tasks to perform Arrays.parallelPrefix【前缀】 operations.
 * 多任务执行数组前缀操作
 *
 * @author Doug Lea
 * @since 1.8
 */
class ArrayPrefixHelpers {
    private ArrayPrefixHelpers() {}; // non-instantiable 本身不能实例化

    /*
     * Parallel prefix (aka cumulate, scan) task classes
     * are based loosely on Guy Blelloch's original
     * algorithm (http://www.cs.cmu.edu/~scandal/alg/scan.html):  并行前缀扫描任务算法基于Guy Blelloch's原始的算法
     *  Keep dividing by two to threshold segment size, and then: 将它一分为二，设置两个阈值大小，然后
     *   Pass 1: Create tree of partial sums for each segment  //为每一部分创建一颗部分累计树
     *   Pass 2: For each segment, cumulate with offset of left sibling // 对于每一部分，积累左节点的偏移量
     *
     * This version improves performance within FJ framework mainly by
     * allowing the second pass of ready left-hand sides to proceed
     * even if【哪怕】 some right-hand side first passes are still executing.  // 这个版本通过FJ 框架允许第二次通过准备左边进行处理，哪怕一些右边第一次通过，仍然会被执行
     * It also combines first and second pass for leftmost segment, //它为最左边的部分合并第一个和第二通过的
     * and skips the first pass for rightmost segment (whose result is 对于最右边元素跳过第一个通过的（这个些不需要第二次通过）
     * not needed for second pass).  It similarly manages to avoid    //他类似管理去避免用户提供一个验证基础，为积累追溯他的部分和子任务，为第一个存在元素被使用
     * requiring that users supply an identity basis for accumulations
     * by tracking those segments/subtasks for which the first
     * existing element is used as base.
     *
     * Managing this relies on ORing【或运算】 some bits in the pendingCount for  //管理这些在阶段和状态：CUMULATE,SUMMED和FINISHED依赖于位或运算
     * phases/states: CUMULATE, SUMMED, and FINISHED. CUMULATE is the // CUMULATE是主要的位阶段。
     * main phase bit. When false, segments compute only their sum. //当为false时候， 部分计算只是它sum值，当为true，他们计算是数组元素
     * When true, they cumulate array elements. CUMULATE is set at  // 当根在开始第二次通过时候CUMULATE被设置，他会往下传播
     * root at beginning of second pass and then propagated down. But //对于子树lo==0(左树)他也可能在更早之前设置
     * it may also be set earlier for subtrees with lo==0 (the left
     * spine of tree). SUMMED is a one bit join count. For leafs, it// SUMMED 是一个位join 计算，对于叶子节点，计数时候他将会被设置
     * is set when summed. For internal nodes, it becomes true when // 对于内部节点， 当子节点被计数的时候他会变成true。
     * one child is summed.  When the second child finishes summing, // 当第二个孩子完成计数，我们将会移动tree去触发积累阶段。
     * we then moves up tree to trigger the cumulate phase. FINISHED // 完成阶段也是一个位join 计数。 对于叶子节点被cumulated,将会被设置
     * is also a one bit join count. For leafs, it is set when
     * cumulated. For internal nodes, it becomes true when one child
     * is cumulated.  When the second child finishes cumulating, it
     * then moves up tree, completing at the root.第二个
     *
     * To better exploit locality and reduce overhead, the compute
     * method loops starting with the current task, moving if possible
     * to one of its subtasks rather than forking.
     *
     * As usual for this sort of utility, there are 4 versions, that有四个版本方法
     * are simple copy/paste/adapt variants of each other.  (The
     * double and int versions differ from long version soley by
     * replacing "long" (with case-matching)). ，分别对应int、Long、Double
     */

    // see above
    static final int CUMULATE = 1;  // 累计常量
    static final int SUMMED   = 2; //求和常量
    static final int FINISHED = 4;  //完成常量

    /** The smallest subtask array partition size to use as threshold */
    static final int MIN_PARTITION = 16; //最小子任务List大小（阈值）

    static final class CumulateTask<T> extends CountedCompleter<Void> {
        final T[] array;  //数组
        final BinaryOperator<T> function; //二进制函数
        CumulateTask<T> left, right; //分为左右任务
        T in, out; // 出参和入参
        final int lo, hi, origin, fence, threshold; //当前索引， 最高索引， 阈值，元素大小

        /** Root task constructor */ //根任务的构造器
        public CumulateTask(CumulateTask<T> parent,
                            BinaryOperator<T> function,
                            T[] array, int lo, int hi) {
            super(parent); // 调用父类构造方法
            this.function = function;  //函数
            this.array = array; //数组
            this.lo = this.origin = lo; //开始索引和初始位置
            this.hi = this.fence = hi; //最大索引和当前元素大小
            int p;
            this.threshold =
                    (p = (hi - lo) / (ForkJoinPool.getCommonPoolParallelism() << 3)) // hi-lo/8*ForkJoinPool.getCommonPoolParallelism()
                    <= MIN_PARTITION ? MIN_PARTITION : p; //算出阈值 p和16  取最小
        }

        /** Subtask constructor */ //子任务
        CumulateTask(CumulateTask<T> parent, BinaryOperator<T> function,
                     T[] array, int origin, int fence, int threshold,
                     int lo, int hi) {
            super(parent);
            this.function = function;
            this.array = array;
            this.origin = origin;
            this.fence = fence;
            this.threshold = threshold;
            this.lo = lo;
            this.hi = hi;
        }

        @SuppressWarnings("unchecked")
        public final void compute() {
            final BinaryOperator<T> fn;//函数
            final T[] a;  //定义数组变量
            if ((fn = this.function) == null || (a = this.array) == null)
                throw new NullPointerException();    // hoist checks
            int th = threshold, org = origin, fnc = fence, l, h;
            CumulateTask<T> t = this; //任务
            outer: while ((l = t.lo) >= 0 && (h = t.hi) <= a.length) {// 判断当前索引在0-size
                if (h - l > th) { // 如果大于阈值 需要划分任务
                    CumulateTask<T> lt = t.left, rt = t.right, f;
                    if (lt == null) {                // first pass 第一次通过
                        int mid = (l + h) >>> 1;
                        f = rt = t.right =
                                new CumulateTask<T>(t, fn, a, org, fnc, th, mid, h); //t 是本对象， fn函数、a数组、org原始索引，th阈值，mid 中间索引， h最高索引 ，f为右任务 ，rt
                        t = lt = t.left  =
                                new CumulateTask<T>(t, fn, a, org, fnc, th, l, mid);// t是本对象 fn函数、a数组、org原始索引， fnc：边界（size()), th阈值、l低值索引、mid 中间索引  t左边任务（lt）
                    }
                    else {                           // possibly refork
                        T pin = t.in;  //  t.in 复制给 pin
                        lt.in = pin; // pin复制左边任务
                        f = t = null; // f（右边对象） 和 t（左边对象）为null
                        if (rt != null) { //右边对象不为空
                            T lout = lt.out; //  把左边out赋值给lout
                            rt.in = (l == org ? lout :
                                     fn.apply(pin, lout)); // l低值索引， org原始索引， rt.in 左边in为 lout(右边.out),
                            for (int c;;) {//遍历
                                if (((c = rt.getPendingCount()) & CUMULATE) != 0) //c 获取挂起数量与CUMULATE取并 （说明这个pendingcount奇数）
                                    break; // pendingcount 奇数直接break
                                if (rt.compareAndSetPendingCount(c, c|CUMULATE)){//pendingCount加一
                                    t = rt; //左边对象赋值本对象
                                    break; //跳出
                                }
                            }
                        }

                        for (int c;;) {
                            if (((c = lt.getPendingCount()) & CUMULATE) != 0) //左边对象的pendingcount的奇数
                                break;//如果是奇数跳出
                            if (lt.compareAndSetPendingCount(c, c|CUMULATE)) { //比较设置，如果这个值设置成功，也就是这个值没有被其他线程修改过
                                if (t != null)// 本对象不为空（rt不为空）
                                    f = t; // 将本对象赋值给f（右边对象）
                                t = lt; //左边对象赋值给(t)
                                break;
                            }
                        }
                        if (t == null) //本对象为null直接跳出
                            break;
                    }
                    if (f != null) //右边对象不为空
                        f.fork();  //右边对象fork
                }
                else { //直接执行
                    int state; // Transition to sum, cumulate, or both 状态
                    for (int b;;) {
                        if (((b = t.getPendingCount()) & FINISHED) != 0)  // FINISHED =0100 所以大于4
                            break outer;                      // already done
                        state = ((b & CUMULATE) != 0? FINISHED :  //奇数表示 state= FINISHED, 否则偶数 低级索引>原始索引 2： 6  （b=4）
                                 (l > org) ? SUMMED : (SUMMED|FINISHED));//
                        if (t.compareAndSetPendingCount(b, b|state))// 设置b = 4；
                            break;
                    }

                    T sum;
                    if (state != SUMMED) {// 如果不是summed
                        int first;
                        if (l == org) {                       // leftmost; no in   表示低级索引===org
                            sum = a[org]; // 把 位置赋值给sum
                            first = org + 1; // 索引加一 赋值first
                        }
                        else {
                            sum = t.in; //把低级索引和原始索引不相等， 把 t.in 赋值 给sum
                            first = l; // 把1赋值给first
                        }
                        for (int i = first; i < h; ++i)       // cumulate
                            a[i] = sum = fn.apply(sum, a[i]); //求i之前累积和赋值索引i值
                    }
                    else if (h < fnc) {                       // skip rightmost  state==summed , 跳出右边， 最高索引小于数组边界
                        sum = a[l];  // 表示l 低值
                        for (int i = l + 1; i < h; ++i)       // sum only
                            sum = fn.apply(sum, a[i]);
                    }
                    else
                        sum = t.in;
                    t.out = sum;
                    for (CumulateTask<T> par;;) {             // propagate
                        if ((par = (CumulateTask<T>)t.getCompleter()) == null) {
                            if ((state & FINISHED) != 0)      // enable join
                                t.quietlyComplete();
                            break outer;
                        }
                        int b = par.getPendingCount();
                        if ((b & state & FINISHED) != 0)
                            t = par;                          // both done
                        else if ((b & state & SUMMED) != 0) { // both summed
                            int nextState; CumulateTask<T> lt, rt;
                            if ((lt = par.left) != null &&
                                (rt = par.right) != null) {
                                T lout = lt.out;
                                par.out = (rt.hi == fnc ? lout :
                                           fn.apply(lout, rt.out));
                            }
                            int refork = (((b & CUMULATE) == 0 &&
                                           par.lo == org) ? CUMULATE : 0);
                            if ((nextState = b|state|refork) == b ||
                                par.compareAndSetPendingCount(b, nextState)) {
                                state = SUMMED;               // drop finished
                                t = par;
                                if (refork != 0)
                                    par.fork();
                            }
                        }
                        else if (par.compareAndSetPendingCount(b, b|state))
                            break outer;                      // sib not ready
                    }
                }
            }
        }
    }

    static final class LongCumulateTask extends CountedCompleter<Void> {
        final long[] array;
        final LongBinaryOperator function;
        LongCumulateTask left, right;
        long in, out;
        final int lo, hi, origin, fence, threshold;

        /** Root task constructor */
        public LongCumulateTask(LongCumulateTask parent,
                                LongBinaryOperator function,
                                long[] array, int lo, int hi) {
            super(parent);
            this.function = function; this.array = array;
            this.lo = this.origin = lo; this.hi = this.fence = hi;
            int p;
            this.threshold =
                    (p = (hi - lo) / (ForkJoinPool.getCommonPoolParallelism() << 3))
                    <= MIN_PARTITION ? MIN_PARTITION : p;
        }

        /** Subtask constructor */
        LongCumulateTask(LongCumulateTask parent, LongBinaryOperator function,
                         long[] array, int origin, int fence, int threshold,
                         int lo, int hi) {
            super(parent);
            this.function = function; this.array = array;
            this.origin = origin; this.fence = fence;
            this.threshold = threshold;
            this.lo = lo; this.hi = hi;
        }

        public final void compute() {
            final LongBinaryOperator fn;
            final long[] a;
            if ((fn = this.function) == null || (a = this.array) == null)
                throw new NullPointerException();    // hoist checks
            int th = threshold, org = origin, fnc = fence, l, h;
            LongCumulateTask t = this;
            outer: while ((l = t.lo) >= 0 && (h = t.hi) <= a.length) {
                if (h - l > th) {
                    LongCumulateTask lt = t.left, rt = t.right, f;
                    if (lt == null) {                // first pass
                        int mid = (l + h) >>> 1;
                        f = rt = t.right =
                                new LongCumulateTask(t, fn, a, org, fnc, th, mid, h);
                        t = lt = t.left  =
                                new LongCumulateTask(t, fn, a, org, fnc, th, l, mid);
                    }
                    else {                           // possibly refork
                        long pin = t.in;
                        lt.in = pin;
                        f = t = null;
                        if (rt != null) {
                            long lout = lt.out;
                            rt.in = (l == org ? lout :
                                     fn.applyAsLong(pin, lout));
                            for (int c;;) {
                                if (((c = rt.getPendingCount()) & CUMULATE) != 0)
                                    break;
                                if (rt.compareAndSetPendingCount(c, c|CUMULATE)){
                                    t = rt;
                                    break;
                                }
                            }
                        }
                        for (int c;;) {
                            if (((c = lt.getPendingCount()) & CUMULATE) != 0)
                                break;
                            if (lt.compareAndSetPendingCount(c, c|CUMULATE)) {
                                if (t != null)
                                    f = t;
                                t = lt;
                                break;
                            }
                        }
                        if (t == null)
                            break;
                    }
                    if (f != null)
                        f.fork();
                }
                else {
                    int state; // Transition to sum, cumulate, or both
                    for (int b;;) {
                        if (((b = t.getPendingCount()) & FINISHED) != 0)
                            break outer;                      // already done
                        state = ((b & CUMULATE) != 0? FINISHED :
                                 (l > org) ? SUMMED : (SUMMED|FINISHED));
                        if (t.compareAndSetPendingCount(b, b|state))
                            break;
                    }

                    long sum;
                    if (state != SUMMED) {
                        int first;
                        if (l == org) {                       // leftmost; no in
                            sum = a[org];
                            first = org + 1;
                        }
                        else {
                            sum = t.in;
                            first = l;
                        }
                        for (int i = first; i < h; ++i)       // cumulate
                            a[i] = sum = fn.applyAsLong(sum, a[i]);
                    }
                    else if (h < fnc) {                       // skip rightmost
                        sum = a[l];
                        for (int i = l + 1; i < h; ++i)       // sum only
                            sum = fn.applyAsLong(sum, a[i]);
                    }
                    else
                        sum = t.in;
                    t.out = sum;
                    for (LongCumulateTask par;;) {            // propagate
                        if ((par = (LongCumulateTask)t.getCompleter()) == null) {
                            if ((state & FINISHED) != 0)      // enable join
                                t.quietlyComplete();
                            break outer;
                        }
                        int b = par.getPendingCount();
                        if ((b & state & FINISHED) != 0)
                            t = par;                          // both done
                        else if ((b & state & SUMMED) != 0) { // both summed
                            int nextState; LongCumulateTask lt, rt;
                            if ((lt = par.left) != null &&
                                (rt = par.right) != null) {
                                long lout = lt.out;
                                par.out = (rt.hi == fnc ? lout :
                                           fn.applyAsLong(lout, rt.out));
                            }
                            int refork = (((b & CUMULATE) == 0 &&
                                           par.lo == org) ? CUMULATE : 0);
                            if ((nextState = b|state|refork) == b ||
                                par.compareAndSetPendingCount(b, nextState)) {
                                state = SUMMED;               // drop finished
                                t = par;
                                if (refork != 0)
                                    par.fork();
                            }
                        }
                        else if (par.compareAndSetPendingCount(b, b|state))
                            break outer;                      // sib not ready
                    }
                }
            }
        }
    }

    static final class DoubleCumulateTask extends CountedCompleter<Void> {
        final double[] array;
        final DoubleBinaryOperator function;
        DoubleCumulateTask left, right;
        double in, out;
        final int lo, hi, origin, fence, threshold;

        /** Root task constructor */
        public DoubleCumulateTask(DoubleCumulateTask parent,
                                  DoubleBinaryOperator function,
                                  double[] array, int lo, int hi) {
            super(parent);
            this.function = function; this.array = array;
            this.lo = this.origin = lo; this.hi = this.fence = hi;
            int p;
            this.threshold =
                    (p = (hi - lo) / (ForkJoinPool.getCommonPoolParallelism() << 3))
                    <= MIN_PARTITION ? MIN_PARTITION : p;
        }

        /** Subtask constructor */
        DoubleCumulateTask(DoubleCumulateTask parent, DoubleBinaryOperator function,
                           double[] array, int origin, int fence, int threshold,
                           int lo, int hi) {
            super(parent);
            this.function = function; this.array = array;
            this.origin = origin; this.fence = fence;
            this.threshold = threshold;
            this.lo = lo; this.hi = hi;
        }

        public final void compute() {
            final DoubleBinaryOperator fn;
            final double[] a;
            if ((fn = this.function) == null || (a = this.array) == null)
                throw new NullPointerException();    // hoist checks
            int th = threshold, org = origin, fnc = fence, l, h;
            DoubleCumulateTask t = this;
            outer: while ((l = t.lo) >= 0 && (h = t.hi) <= a.length) {
                if (h - l > th) {
                    DoubleCumulateTask lt = t.left, rt = t.right, f;
                    if (lt == null) {                // first pass
                        int mid = (l + h) >>> 1;
                        f = rt = t.right =
                                new DoubleCumulateTask(t, fn, a, org, fnc, th, mid, h);
                        t = lt = t.left  =
                                new DoubleCumulateTask(t, fn, a, org, fnc, th, l, mid);
                    }
                    else {                           // possibly refork
                        double pin = t.in;
                        lt.in = pin;
                        f = t = null;
                        if (rt != null) {
                            double lout = lt.out;
                            rt.in = (l == org ? lout :
                                     fn.applyAsDouble(pin, lout));
                            for (int c;;) {
                                if (((c = rt.getPendingCount()) & CUMULATE) != 0)
                                    break;
                                if (rt.compareAndSetPendingCount(c, c|CUMULATE)){
                                    t = rt;
                                    break;
                                }
                            }
                        }
                        for (int c;;) {
                            if (((c = lt.getPendingCount()) & CUMULATE) != 0)
                                break;
                            if (lt.compareAndSetPendingCount(c, c|CUMULATE)) {
                                if (t != null)
                                    f = t;
                                t = lt;
                                break;
                            }
                        }
                        if (t == null)
                            break;
                    }
                    if (f != null)
                        f.fork();
                }
                else {
                    int state; // Transition to sum, cumulate, or both
                    for (int b;;) {
                        if (((b = t.getPendingCount()) & FINISHED) != 0)
                            break outer;                      // already done
                        state = ((b & CUMULATE) != 0? FINISHED :
                                 (l > org) ? SUMMED : (SUMMED|FINISHED));
                        if (t.compareAndSetPendingCount(b, b|state))
                            break;
                    }

                    double sum;
                    if (state != SUMMED) {
                        int first;
                        if (l == org) {                       // leftmost; no in
                            sum = a[org];
                            first = org + 1;
                        }
                        else {
                            sum = t.in;
                            first = l;
                        }
                        for (int i = first; i < h; ++i)       // cumulate
                            a[i] = sum = fn.applyAsDouble(sum, a[i]);
                    }
                    else if (h < fnc) {                       // skip rightmost
                        sum = a[l];
                        for (int i = l + 1; i < h; ++i)       // sum only
                            sum = fn.applyAsDouble(sum, a[i]);
                    }
                    else
                        sum = t.in;
                    t.out = sum;
                    for (DoubleCumulateTask par;;) {            // propagate
                        if ((par = (DoubleCumulateTask)t.getCompleter()) == null) {
                            if ((state & FINISHED) != 0)      // enable join
                                t.quietlyComplete();
                            break outer;
                        }
                        int b = par.getPendingCount();
                        if ((b & state & FINISHED) != 0)
                            t = par;                          // both done
                        else if ((b & state & SUMMED) != 0) { // both summed
                            int nextState; DoubleCumulateTask lt, rt;
                            if ((lt = par.left) != null &&
                                (rt = par.right) != null) {
                                double lout = lt.out;
                                par.out = (rt.hi == fnc ? lout :
                                           fn.applyAsDouble(lout, rt.out));
                            }
                            int refork = (((b & CUMULATE) == 0 &&
                                           par.lo == org) ? CUMULATE : 0);
                            if ((nextState = b|state|refork) == b ||
                                par.compareAndSetPendingCount(b, nextState)) {
                                state = SUMMED;               // drop finished
                                t = par;
                                if (refork != 0)
                                    par.fork();
                            }
                        }
                        else if (par.compareAndSetPendingCount(b, b|state))
                            break outer;                      // sib not ready
                    }
                }
            }
        }
    }

    static final class IntCumulateTask extends CountedCompleter<Void> {
        final int[] array;
        final IntBinaryOperator function;
        IntCumulateTask left, right;
        int in, out;
        final int lo, hi, origin, fence, threshold;

        /** Root task constructor */
        public IntCumulateTask(IntCumulateTask parent,
                               IntBinaryOperator function,
                               int[] array, int lo, int hi) {
            super(parent);
            this.function = function; this.array = array;
            this.lo = this.origin = lo; this.hi = this.fence = hi;
            int p;
            this.threshold =
                    (p = (hi - lo) / (ForkJoinPool.getCommonPoolParallelism() << 3))
                    <= MIN_PARTITION ? MIN_PARTITION : p;
        }

        /** Subtask constructor */
        IntCumulateTask(IntCumulateTask parent, IntBinaryOperator function,
                        int[] array, int origin, int fence, int threshold,
                        int lo, int hi) {
            super(parent);
            this.function = function; this.array = array;
            this.origin = origin; this.fence = fence;
            this.threshold = threshold;
            this.lo = lo; this.hi = hi;
        }

        public final void compute() {
            final IntBinaryOperator fn;
            final int[] a;
            if ((fn = this.function) == null || (a = this.array) == null)
                throw new NullPointerException();    // hoist checks
            int th = threshold, org = origin, fnc = fence, l, h;
            IntCumulateTask t = this;
            outer: while ((l = t.lo) >= 0 && (h = t.hi) <= a.length) {
                if (h - l > th) {
                    IntCumulateTask lt = t.left, rt = t.right, f;
                    if (lt == null) {                // first pass
                        int mid = (l + h) >>> 1;
                        f = rt = t.right =
                                new IntCumulateTask(t, fn, a, org, fnc, th, mid, h);
                        t = lt = t.left  =
                                new IntCumulateTask(t, fn, a, org, fnc, th, l, mid);
                    }
                    else {                           // possibly refork
                        int pin = t.in;
                        lt.in = pin;
                        f = t = null;
                        if (rt != null) {
                            int lout = lt.out;
                            rt.in = (l == org ? lout :
                                     fn.applyAsInt(pin, lout));
                            for (int c;;) {
                                if (((c = rt.getPendingCount()) & CUMULATE) != 0)
                                    break;
                                if (rt.compareAndSetPendingCount(c, c|CUMULATE)){
                                    t = rt;
                                    break;
                                }
                            }
                        }
                        for (int c;;) {
                            if (((c = lt.getPendingCount()) & CUMULATE) != 0)
                                break;
                            if (lt.compareAndSetPendingCount(c, c|CUMULATE)) {
                                if (t != null)
                                    f = t;
                                t = lt;
                                break;
                            }
                        }
                        if (t == null)
                            break;
                    }
                    if (f != null)
                        f.fork();
                }
                else {
                    int state; // Transition to sum, cumulate, or both
                    for (int b;;) {
                        if (((b = t.getPendingCount()) & FINISHED) != 0)
                            break outer;                      // already done
                        state = ((b & CUMULATE) != 0? FINISHED :
                                 (l > org) ? SUMMED : (SUMMED|FINISHED));
                        if (t.compareAndSetPendingCount(b, b|state))
                            break;
                    }

                    int sum;
                    if (state != SUMMED) {
                        int first;
                        if (l == org) {                       // leftmost; no in
                            sum = a[org];
                            first = org + 1;
                        }
                        else {
                            sum = t.in;
                            first = l;
                        }
                        for (int i = first; i < h; ++i)       // cumulate
                            a[i] = sum = fn.applyAsInt(sum, a[i]);
                    }
                    else if (h < fnc) {                       // skip rightmost
                        sum = a[l];
                        for (int i = l + 1; i < h; ++i)       // sum only
                            sum = fn.applyAsInt(sum, a[i]);
                    }
                    else
                        sum = t.in;
                    t.out = sum;
                    for (IntCumulateTask par;;) {            // propagate
                        if ((par = (IntCumulateTask)t.getCompleter()) == null) {
                            if ((state & FINISHED) != 0)      // enable join
                                t.quietlyComplete();
                            break outer;
                        }
                        int b = par.getPendingCount();
                        if ((b & state & FINISHED) != 0)
                            t = par;                          // both done
                        else if ((b & state & SUMMED) != 0) { // both summed
                            int nextState; IntCumulateTask lt, rt;
                            if ((lt = par.left) != null &&
                                (rt = par.right) != null) {
                                int lout = lt.out;
                                par.out = (rt.hi == fnc ? lout :
                                           fn.applyAsInt(lout, rt.out));
                            }
                            int refork = (((b & CUMULATE) == 0 &&
                                           par.lo == org) ? CUMULATE : 0);
                            if ((nextState = b|state|refork) == b ||
                                par.compareAndSetPendingCount(b, nextState)) {
                                state = SUMMED;               // drop finished
                                t = par;
                                if (refork != 0)
                                    par.fork();
                            }
                        }
                        else if (par.compareAndSetPendingCount(b, b|state))
                            break outer;                      // sib not ready
                    }
                }
            }
        }
    }
}
