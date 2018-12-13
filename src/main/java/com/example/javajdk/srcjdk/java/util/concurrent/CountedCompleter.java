/*
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

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.example.javajdk.srcjdk.java.util.concurrent;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.*;

/**
 * A {@link ForkJoinTask} with a completion【完成】 action performed when
 * triggered and there are no remaining pending【直到】 actions.
 * CountedCompleters are in general more robust in the
 * presence of subtask stalls【摊位】 and blockage【阻塞】 than are other forms of
 * ForkJoinTasks, but are less intuitive【直观】 to program.  Uses of
 * CountedCompleter are similar to those of other completion based
 * components (such as {@link java.nio.channels.CompletionHandler})
 * except that 【除了…之外】multiple <em>pending</em> completions may be necessary
 * to trigger the completion action {@link #onCompletion(CountedCompleter)},
 * not just one.
 * CountedCompleter: 相对于其他更加健壮。除了多个完成必要完成的触发行为
 *
 * Unless initialized otherwise, the {@linkplain #getPendingCount pending
 * count} starts at zero, but may be (atomically) changed using
 * methods {@link #setPendingCount}, {@link #addToPendingCount}, and
 * {@link #compareAndSetPendingCount}. Upon invocation of {@link
 * #tryComplete}, if the pending action count is nonzero, it is
 * decremented; otherwise, the completion action is performed, and if
 * this completer itself has a completer, the process is continued
 * with its completer.  As is the case with related synchronization
 * components such as {@link java.util.concurrent.Phaser Phaser} and
 * {@link java.util.concurrent.Semaphore Semaphore}, these methods
 * affect only internal counts; they do not establish any further
 * internal bookkeeping【记账】. In particular, the identities of pending
 * tasks are not maintained. As illustrated below, you can create
 * subclasses that do record some or all pending tasks or their
 * results when needed.  As illustrated below, utility methods
 * supporting customization of completion traversals are also
 * provided. However, because CountedCompleters provide only basic
 * synchronization mechanisms, it may be useful to create further
 * abstract subclasses that maintain linkages, fields, and additional
 * support methods appropriate for a set of related usages.
 *
 * 除非另行初始化， 这个getPendingCount的pending的计数从0开始的， 它可能通过使用
 * setPendingCount, addToPendingCount, compareAndSetPendingCount进行原子性修改，
 * 如果调用tryComplete方法，只要pending的计数为0，它将会自减一，如果
 * completer它自己也有一个completer, 这个过程将会继续它的completer.这种情况下
 * 会关联一些同步组件Phaser和Semaphore,这些方法影响的只是内部计数， 他不会建立更深层次
 * 内部记账，特别，这个标识pending任务不会保持。，如下阐述，当你需要的时候，你可以创建一个子类去记录一下
 * 一些或者所有任务或者他们的结果，如下说明，工具方法是支持自定义Completion遍历，然而，
 * 因为CountedCompleters提供支持只是基础同步机制，他可能被用于创建更深层抽象子类去保持链接，
 * 字段和额外支持方法。
 *
 * <p>A concrete【实在的】 CountedCompleter class must define method {@link
 * #compute}, that should in most cases (as illustrated below), invoke
 * {@code tryComplete()} once before returning. The class may also
 * optionally override method {@link #onCompletion(CountedCompleter)}
 * to perform an action upon normal completion, and method
 * {@link #onExceptionalCompletion(Throwable, CountedCompleter)} to
 * perform an action upon any exception.
 * 一个具体的CountedCompleter类必须定义一个compute方法，一般如下情况，在返回之前调用一下
 * tryComplete方法，这个类也可以去重写onCompletion（执行一个正常操作过程）方法和 onExceptionCompletion方法（处理执行过程抛出异常）
 *
 * <p>CountedCompleters most often do not bear results, in which case
 * they are normally declared as {@code CountedCompleter<Void>}, and
 * will always return {@code null} as a result value.  In other cases,
 * you should override method {@link #getRawResult} to provide a
 * result from {@code join(), invoke()}, and related methods.  In
 * general, this method should return the value of a field (or a
 * function of one or more fields) of the CountedCompleter object that
 * holds the result upon completion. Method {@link #setRawResult} by
 * default plays no role【不起作用】 in CountedCompleters.  It is possible, but
 * rarely applicable, to override this method to maintain other
 * objects or fields holding result data.
 *
 * CountedCompleter大多数不需要结果，没有结果通常声明CountedCompleter<Void>， 将会一直
 * 返回null作为结果，在其他情况下，你应该重写getRawResult提供从join，invoke、相关的方法的一个结果
 * 一般来说，这个方法应该返回一个字段的值（或者一个函数一个或者多个值），setRawResult在CountedCompleters默认是不起作用的
 * 有这种可能，但是不常见去重写这个方法去另外的对象或字段来获取结果数据
 *
 *
 *
 * <p>A CountedCompleter that does not itself have a completer (i.e.,
 * one for which {@link #getCompleter} returns {@code null}) can be
 * used as a regular ForkJoinTask with this added functionality.
 * However, any completer that in turn【依次】 has another completer serves
 * only as an internal helper for other computations, so its own task
 * status (as reported in methods such as {@link ForkJoinTask#isDone})
 * is arbitrary【随意的】; this status changes only upon explicit invocations of
 * {@link #complete}, {@link ForkJoinTask#cancel},
 * {@link ForkJoinTask#completeExceptionally(Throwable)} or upon
 * exceptional completion of method {@code compute}. Upon any
 * exceptional completion, the exception may be relayed to a task's
 * completer (and its completer, and so on), if one exists and it has
 * not otherwise already completed. Similarly, cancelling an internal
 * CountedCompleter has only a local effect on that completer, so is
 * not often useful.
 *
 * 一个CountedCompleter它自己并没有completer,(例如，一个getCompleter返回null),它将会
 * 作为一个正常ForkJoinTask使用它的功能， 然而，任何completer轮流有其他completer服务，
 * 作为一个内部来帮助其他计算，所以他有自己的任务状态（如下方法 isDone） 都是随意的，这个状态
 * 的改变只有调用complete方法，cancel和CompleteExceptionally或引起异常的compute方法，
 * 一旦有任何异常，这些异常将会延迟到任务的completer（和它completer, 等等） ，如果一个村庄，
 * 它将不会完成， 类似，取消一个内部CountedCompleter将只会印象到那个completer,所以并不经常用。
 *
 * <p><b>Sample Usages.</b> //例子使用
 *
 * <p><b>Parallel recursive decomposition.</b> CountedCompleters may
 * be arranged in trees similar to those often used with {@link
 * RecursiveAction}s, although the constructions involved in setting
 * them up typically【典型】 vary【多样性】. Here, the completer of each task is its
 * parent in the computation tree. Even though they entail【牵涉】 a bit more
 * bookkeeping, CountedCompleters may be better choices when applying
 * a possibly time-consuming operation (that cannot be further
 * subdivided) to each element of an array or collection; especially
 * when the operation takes a significantly different amount of time
 * to complete for some elements than others, either because of
 * intrinsic【本质的】 variation【变量】 (for example I/O) or auxiliary【辅助】 effects such as
 * garbage collection.  Because CountedCompleters provide their own
 * continuations【连续】, other threads need not block waiting to perform【执行】
 * them.
 * 平行递归分解。CountedCompleters可能分成树，类似经常使用RecursiveAction, 虽然结构涉及到设置典型的多样性
 * 这里，completer的每一个任务都是它父类的计算树种，甚至会牵扯到一个位记账，CountedCompleters将会是更好的选择，
 * 当应用可能时间消耗操作上，（他不能再一次分割）一个数组或集合，尤其是这个操作对于某些应用任务非常重要，这是因为
 * 本质变量或辅助影响-像垃圾回收，因为CountedCompleters提供他自己连续性。其他线程不需要阻塞等待执行他们
 *
 * <p>For example, here is an initial version of a class that uses
 * divide-by-two recursive decomposition to divide work into single
 * pieces (leaf tasks). Even when work is split into individual calls,
 * tree-based techniques are usually preferable to directly forking
 * leaf tasks, because they reduce inter-thread communication and
 * improve load balancing. In the recursive case, the second of each
 * pair of subtasks to finish triggers completion of its parent
 * (because no result combination is performed, the default no-op
 * implementation of method {@code onCompletion} is not overridden).
 * A static utility method sets up the base task and invokes it
 * (here, implicitly using the {@link ForkJoinPool#commonPool()}).
 *
 * 举个例子，这里有个初始化版本类，他使用一分为二递归分解工作成为单个（叶子任务）。甚至当工作
 * 被分成单个调用，基于树形技术被经常用于直接去fork叶子任务，因为他减少内部线程联系和提供负载均衡，
 * 在这个递归例子中，这个两个每一对的子任务将会完成触发completion和它父类任务，（因为没有结果合并
 * 被执行，这个默认no-op实现方法onCompletion并没有重写），一个静态工具方法设置基础任务和调用它
 * （这里，隐式使用commonPool方法）
 *
 * <pre> {@code
 * class MyOperation<E> { void apply(E e) { ... }  }  //定义执行任务
 *
 * class ForEach<E> extends CountedCompleter<Void> {
 *
 *   public static <E> void forEach(E[] array, MyOperation<E> op) { //处理数据入口
 *     new ForEach<E>(null, array, op, 0, array.length).invoke();
 *   }
 *
 *   final E[] array; final MyOperation<E> op; final int lo, hi;   // lo 开始索引， hi结束索引
 *   ForEach(CountedCompleter<?> p, E[] array, MyOperation<E> op, int lo, int hi) {
 *     super(p);
 *     this.array = array; this.op = op; this.lo = lo; this.hi = hi;
 *   }
 *
 *   public void compute() { // version 1
 *     if (hi - lo >= 2) { // 表示可以分成单个任务
 *       int mid = (lo + hi) >>> 1; // 取中间值
 *       setPendingCount(2); // must set pending count before fork ，表示挂起几个任务
 *       new ForEach(this, array, op, mid, hi).fork(); // right child  右边任务
 *       new ForEach(this, array, op, lo, mid).fork(); // left child 左边任务
 *     }
 *     else if (hi > lo)
 *       op.apply(array[lo]); //表示只有一个任务了
 *     tryComplete();//尝试调用完成方法
 *   }
 * }}</pre>
 *
 * 第二版本
 * This design can be improved by noticing that in the recursive case,
 * the task has nothing to do after forking its right task, so can
 * directly invoke its left task before returning. (This is an analog【同源语】
 * of tail recursion removal.)  Also, because the task returns upon
 * executing its left task (rather than falling through to invoke
 * {@code tryComplete}) the pending count is set to one:
 *
 * 这个设计可以递归进一步优化，注意到这个任务在fork右边任务后没有任何事情做了，（这就类似末尾递归移除了）
 * 也就是， 因为这个任务执行它的左边任务（而直接调用），这个setPendingCount(1)
 * <pre> {@code
 * class ForEach<E> ...
 *   public void compute() { // version 2
 *     if (hi - lo >= 2) {
 *       int mid = (lo + hi) >>> 1;
 *       setPendingCount(1); // only one pending
 *       new ForEach(this, array, op, mid, hi).fork(); // right child
 *       new ForEach(this, array, op, lo, mid).compute(); // direct invoke
 *     }
 *     else {
 *       if (hi > lo)
 *         op.apply(array[lo]);
 *       tryComplete();
 *     }
 *   }
 * }</pre>
 *
 * As a further improvement, notice that the left task need not even exist.
 * Instead of creating a new one, we can iterate using the original task,
 * and add a pending count for each fork.  Additionally, because no task
 * in this tree implements an {@link #onCompletion(CountedCompleter)} method,
 * {@code tryComplete()} can be replaced with {@link #propagateCompletion}.
 * 作为更进步提升，注意这个左边任务甚至不需要存在，取而代之是创建一个新的，我可以的递归使用原始任务
 * 和添加一个pending count为每个fork， 除此之外，因为这个树没有任务实现了onCompletion方法，tryComplete
 * 方法将会替换成propagateCompletion
 *
 * <pre> {@code
 * class ForEach<E> ...
 *   public void compute() { // version 3
 *     int l = lo,  h = hi;
 *     while (h - l >= 2) {
 *       int mid = (l + h) >>> 1;
 *       addToPendingCount(1);
 *       new ForEach(this, array, op, mid, h).fork(); // right child
 *       h = mid;
 *     }
 *     if (h > l)
 *       op.apply(array[l]);
 *     propagateCompletion();
 *   }
 * }</pre>
 *
 * Additional improvements of such classes might entail precomputing
 * pending counts so that they can be established in constructors,
 * specializing classes for leaf steps, subdividing by say, four,
 * instead of two per iteration, and using an adaptive threshold
 * instead of always subdividing down to single elements.
 * 除此之外，优化这些类可能涉及到预先计算pending的计数，以便他们能够建立在结构上，具体到叶子步骤，
 * 被划分为 4分为2两个迭代，使用合适阈值替换分解成单个任务
 *
 *
 * <p><b>Searching.</b> A tree of CountedCompleters can search for a
 * value or property in different parts of a data structure, and
 * report a result in an {@link
 * java.util.concurrent.atomic.AtomicReference AtomicReference} as
 * soon as one is found. The others can poll the result to avoid
 * unnecessary work. (You could additionally {@linkplain #cancel
 * cancel} other tasks, but it is usually simpler and more efficient
 * to just let them notice that the result is set and if so skip
 * further processing.)  Illustrating again with an array using full
 * partitioning (again, in practice, leaf tasks will almost always
 * process more than one element):
 * 搜索， 一个CountedCompleter树可以在不同数据结构部分中检索到一个值或属性，然后通过AtomicReference只要一发现就会返回，
 * 其他情况弹出一个结果避免不必要的工作，（你可以增加其他cancel任务的方法），但是他经常是简单和有效让我注意到只要找到结果
 * 就立即跳出更深层次处理。 再一次阐述一个数组使用全部部分（实际上，叶子任务疆场处理多于一个元素的）
 *
 * <pre> {@code
 * class Searcher<E> extends CountedCompleter<E> {
 *   final E[] array; final AtomicReference<E> result; final int lo, hi;
 *   Searcher(CountedCompleter<?> p, E[] array, AtomicReference<E> result, int lo, int hi) {
 *     super(p);
 *     this.array = array; this.result = result; this.lo = lo; this.hi = hi;
 *   }
 *   public E getRawResult() { return result.get(); }
 *   public void compute() { // similar to ForEach version 3 //第三个版本
 *     int l = lo,  h = hi;
 *     while (result.get() == null && h >= l) {  //通过AtomicReference返回结果
 *       if (h - l >= 2) {
 *         int mid = (l + h) >>> 1;
 *         addToPendingCount(1);
 *         new Searcher(this, array, result, mid, h).fork();
 *         h = mid;
 *       }
 *       else {
 *         E x = array[l]; // 表示只有一个元素
 *         if (matches(x) && result.compareAndSet(null, x)) // 如果查询匹配到就直接返回
 *           quietlyCompleteRoot(); // root task is now joinable
 *         break;
 *       }
 *     }
 *     tryComplete(); // normally complete whether or not found 不管找没找到正常关闭
 *   }
 *   boolean matches(E e) { ... } // return true if found
 *
 *   public static <E> E search(E[] array) {
 *       return new Searcher<E>(null, array, new AtomicReference<E>(), 0, array.length).invoke();
 *   }
 * }}</pre>
 *
 * In this example, as well as【此外】 others in which tasks have no other
 * effects except to compareAndSet a common result, the trailing【追踪】
 * unconditional invocation of {@code tryComplete} could be made
 * conditional ({@code if (result.get() == null) tryComplete();})
 * because no further bookkeeping is required to manage completions
 * once the root task completes.
 * 在这个例子中，此外其它任务没有影响，除了compareAndSet一个公共的结果，这个追踪
 * 没有条件调用tryComplete可以作为条件，因为没有更深层次记账，只要这个root任务完成了
 *
 * <p><b>Recording subtasks.</b> CountedCompleter tasks that combine
 * results of multiple subtasks usually need to access these results
 * in method {@link #onCompletion(CountedCompleter)}. As illustrated in the following
 * class (that performs a simplified form of map-reduce where mappings
 * and reductions are all of type {@code E}), one way to do this in
 * divide and conquer designs is to have each subtask record its
 * sibling【兄弟】, so that it can be accessed in method {@code onCompletion}.
 * This technique applies to reductions in which the order of
 * combining left and right results does not matter; ordered
 * reductions require explicit left/right designations.  Variants of
 * other streamlinings seen in the above examples may also apply.
 *
 * 记录子任务， CountedCompleter任务会合并多个子任务的结果，经常需要获取onCompletion的方法。如下阐述
 * 执行一个简单形式map-reduce，他可以映射和降低所有类型E， 一种方式去这件事情是分开，设计每个都有他子任务记录
 * 他的兄弟， 所以它可以通过onCompletion获取，这个技术应用去减少合并左右结果的顺序，有序减少要求显示左右设计，
 * 其他流如下例子中看到
 * <pre> {@code
 * class MyMapper<E> { E apply(E v) {  ...  } }
 * class MyReducer<E> { E apply(E x, E y) {  ...  } }
 * class MapReducer<E> extends CountedCompleter<E> {
 *   final E[] array; final MyMapper<E> mapper;
 *   final MyReducer<E> reducer; final int lo, hi;
 *   MapReducer<E> sibling;
 *   E result;
 *   MapReducer(CountedCompleter<?> p, E[] array, MyMapper<E> mapper,
 *              MyReducer<E> reducer, int lo, int hi) {
 *     super(p);
 *     this.array = array; this.mapper = mapper;
 *     this.reducer = reducer; this.lo = lo; this.hi = hi;
 *   }
 *   public void compute() {
 *     if (hi - lo >= 2) {
 *       int mid = (lo + hi) >>> 1;
 *       MapReducer<E> left = new MapReducer(this, array, mapper, reducer, lo, mid);
 *       MapReducer<E> right = new MapReducer(this, array, mapper, reducer, mid, hi);
 *       left.sibling = right;
 *       right.sibling = left;
 *       setPendingCount(1); // only right is pending
 *       right.fork();
 *       left.compute();     // directly execute left
 *     }
 *     else {
 *       if (hi > lo)
 *           result = mapper.apply(array[lo]);
 *       tryComplete();
 *     }
 *   }
 *   public void onCompletion(CountedCompleter<?> caller) {
 *     if (caller != this) {
 *       MapReducer<E> child = (MapReducer<E>)caller;
 *       MapReducer<E> sib = child.sibling;
 *       if (sib == null || sib.result == null)
 *         result = child.result;
 *       else
 *         result = reducer.apply(child.result, sib.result);
 *     }
 *   }
 *   public E getRawResult() { return result; }
 *
 *   public static <E> E mapReduce(E[] array, MyMapper<E> mapper, MyReducer<E> reducer) {
 *     return new MapReducer<E>(null, array, mapper, reducer,
 *                              0, array.length).invoke();
 *   }
 * }}</pre>
 *
 * Here, method {@code onCompletion} takes a form common to many
 * completion designs that combine results. This callback-style method
 * is triggered once per task, in either of the two different contexts
 * in which the pending count is, or becomes, zero: (1) by a task
 * itself, if its pending count is zero upon invocation of {@code
 * tryComplete}, or (2) by any of its subtasks when they complete and
 * decrement the pending count to zero. The {@code caller} argument
 * distinguishes cases.  Most often, when the caller is {@code this},
 * no action is necessary. Otherwise the caller argument can be used
 * (usually via a cast) to supply a value (and/or links to other
 * values) to be combined.  Assuming proper use of pending counts, the
 * actions inside {@code onCompletion} occur (once) upon completion of
 * a task and its subtasks. No additional synchronization is required
 * within this method to ensure thread safety of accesses to fields of
 * this task or other completed tasks.
 * 这里， 这个onCompletion方法采用一种常用许多完成设计和合并结果， 这个回调风格方法
 * 将会在每一次任务中调用，在两个不同上下文，pending计数到0， 1、通过调用tryComplete
 * 2、任何子任务完成时候，将会减少pending 到0， 这个caller参数区分其他情况，大多数情况，
 * 当这个caller是他本身，没有行为是必须的，所以这个caller参数可以被用来提供一个值或者链接
 * 其他的值将会被合并， 假设使用pending技术，这个行为在onCompletion发生完成 任务和子任务，
 * 没有额外的同步被要求，这个方法保证线程安全获取这个任务的字段和其他完成任务。
 *
 * <p><b>Completion Traversals</b>. If using {@code onCompletion} to
 * process completions is inapplicable【不适用】 or inconvenient【不方便】, you can use
 * methods {@link #firstComplete} and {@link #nextComplete} to create
 * custom traversals.  For example, to define a MapReducer that only
 * splits out right-hand tasks in the form of the third ForEach
 * example, the completions must cooperatively reduce along
 * unexhausted（未尽的） subtask links, which can be done as follows:
 *
 * 完成遍历，如果使用onCompletion去处理完成将会不适用或不方便，你可以使用firstComplete方法和nextComplete方法去创建
 * 自定义遍历， 举个例子，去定义一个MapReducer，他只是将利用第三种形式左边任务进行分离， 这个完成必须合作地去减少未尽
 * 子任务链接， 如下所示
 *
 * <pre> {@code
 * class MapReducer<E> extends CountedCompleter<E> { // version 2
 *   final E[] array; final MyMapper<E> mapper;
 *   final MyReducer<E> reducer; final int lo, hi;
 *   MapReducer<E> forks, next; // record subtask forks in list
 *   E result;
 *   MapReducer(CountedCompleter<?> p, E[] array, MyMapper<E> mapper,
 *              MyReducer<E> reducer, int lo, int hi, MapReducer<E> next) {
 *     super(p);
 *     this.array = array; this.mapper = mapper;
 *     this.reducer = reducer; this.lo = lo; this.hi = hi;
 *     this.next = next;
 *   }
 *   public void compute() {
 *     int l = lo,  h = hi;
 *     while (h - l >= 2) {
 *       int mid = (l + h) >>> 1;
 *       addToPendingCount(1);
 *       (forks = new MapReducer(this, array, mapper, reducer, mid, h, forks)).fork();
 *       h = mid;
 *     }
 *     if (h > l)
 *       result = mapper.apply(array[l]);
 *     // process completions by reducing along and advancing subtask links
 *     for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
 *       for (MapReducer t = (MapReducer)c, s = t.forks;  s != null; s = t.forks = s.next)
 *         t.result = reducer.apply(t.result, s.result);
 *     }
 *   }
 *   public E getRawResult() { return result; }
 *
 *   public static <E> E mapReduce(E[] array, MyMapper<E> mapper, MyReducer<E> reducer) {
 *     return new MapReducer<E>(null, array, mapper, reducer,
 *                              0, array.length, null).invoke();
 *   }
 * }}</pre>
 *
 * <p><b>Triggers.</b> Some CountedCompleters are themselves never
 * forked, but instead serve as bits of plumbing in other designs;
 * including those in which the completion of one or more async tasks
 * triggers another async task. For example:
 * 触发， 有些CountedCompleters他自己从来就没有被fork,但取而代之是作为其他设计管道的位
 * 包括这些，这个completion一个或多个异步任务触发，其他异步任务，举个例子：
 * <pre> {@code
 * class HeaderBuilder extends CountedCompleter<...> { ... }
 * class BodyBuilder extends CountedCompleter<...> { ... }
 * class PacketSender extends CountedCompleter<...> {
 *   PacketSender(...) { super(null, 1); ... } // trigger on second completion
 *   public void compute() { } // never called
 *   public void onCompletion(CountedCompleter<?> caller) { sendPacket(); }
 * }
 * // sample use:
 * PacketSender p = new PacketSender();
 * new HeaderBuilder(p, ...).fork();
 * new BodyBuilder(p, ...).fork();
 * }</pre>
 *
 * @since 1.8
 * @author Doug Lea
 */
public abstract class CountedCompleter<T> extends ForkJoinTask<T> {  //CountedCompleter继承了ForkJoinTask
    private static final long serialVersionUID = 5232453752276485070L;

    /** This task's completer, or null if none */ //此任务的完成者，如果没有，则为null
    final CountedCompleter<?> completer;
    /** The number of pending tasks until completion */ //完成之前待处理任务的数量
    volatile int pending; //线程可见性

    /**
     * Creates a new CountedCompleter with the given completer
     * and initial pending count.
     * //使用给定的完成者和初始挂起计数创建一个新CountedCompleter
     *
     * @param completer this task's completer, or {@code null} if none
     * @param initialPendingCount the initial pending count
     */
    protected CountedCompleter(CountedCompleter<?> completer,
                               int initialPendingCount) {
        this.completer = completer;
        this.pending = initialPendingCount;
    }

    /**
     * Creates a new CountedCompleter with the given completer
     * and an initial pending count of zero.
     * 使用给定新的完成者创建一个新的CountedCompleter,初始化挂起计数为0
     *
     * @param completer this task's completer, or {@code null} if none
     */
    protected CountedCompleter(CountedCompleter<?> completer) {
        this.completer = completer;
    }

    /**
     * Creates a new CountedCompleter with no completer
     * and an initial pending count of zero.
     * //创建一个没有完成者的新CountedCompleter,初始化挂起计数为0
     */
    protected CountedCompleter() {
        this.completer = null;
    }

    /**
     * The main computation performed by this task.
     * 该任务执行主要的计算
     *
     */
    public abstract void compute();

    /**
     * Performs an action when method {@link #tryComplete} is invoked
     * and the pending count is zero, or when the unconditional
     * method {@link #complete} is invoked.  By default, this method
     * does nothing. You can distinguish cases by checking the
     * identity of the given caller argument. If not equal to {@code
     * this}, then it is typically a subtask that may contain results
     * (and/or links to other results) to combine.
     *  当tryComplete方法被调用和待处理计数为0，或者无条件方法complete方法被调用的时候，将会执行这个
     *  方法，你可以通过检查给定caller参数的验证来区分不同情况，如果不等于this， 通常一个子任务可能包含一个
     *  结果（或者其他结果的链接）去合并
     * @param caller the task invoking this method (which may
     * be this task itself) 这个任务调用这个方法，也可能任务是它自己
     */
    public void onCompletion(CountedCompleter<?> caller) {
    }

    /**
     * Performs an action when method {@link
     * #completeExceptionally(Throwable)} is invoked or method {@link
     * #compute} throws an exception, and this task has not already
     * otherwise completed normally. On entry to this method, this task
     * {@link ForkJoinTask#isCompletedAbnormally}.  The return value
     * of this method controls further propagation: If {@code true}
     * and this task has a completer that has not completed, then that
     * completer is also completed exceptionally, with the same
     * exception as this completer.  The default implementation of
     * this method does nothing except return {@code true}.
     * 当completeExceptionally被调用或compute抛出异常，也就是这个任务没有正常结束，将会执行这个操作
     * 为了进入这个方法，需要调用isCompletedAbnormally，这个方法返回值控制更深层传播，如果返回true，这个
     * 方法的完成者并没有完成，那时候这完成者也会异常地完成，这个完成者有相同异常。默认实现方法，并没有做
     * 任何事情，只是返回true
     *
     * @param ex the exception
     * @param caller the task invoking this method (which may
     * be this task itself) 这个任务调用这个方法，也可能任务是它自己
     * @return {@code true} if this exception should be propagated to this
     * task's completer, if one exists 如果此异常应传播到此任务的完成者（如果存在的话）
     */
    public boolean onExceptionalCompletion(Throwable ex, CountedCompleter<?> caller) {
        return true;
    }

    /**
     * Returns the completer established in this task's constructor,
     * or {@code null} if none.
     * 返回建立这个任务的构造器的完成者，如果没有则返回null
     * @return the completer
     */
    public final CountedCompleter<?> getCompleter() {
        return completer;
    }

    /**
     * Returns the current pending count.
     * 返回当前挂起数量
     * @return the current pending count
     */
    public final int getPendingCount() {
        return pending;
    }

    /**
     * Sets the pending count to the given value.
     * 设置当前挂起值
     * @param count the count
     */
    public final void setPendingCount(int count) {
        pending = count;
    }

    /**
     * Adds (atomically) the given value to the pending count.
     * 挂起数值+传入值（原子操作）
     * @param delta the value to add
     */
    public final void addToPendingCount(int delta) {
        U.getAndAddInt(this, PENDING, delta); //原子性相加 PENDING字段偏移位置
    }

    /**
     * Sets (atomically) the pending count to the given count only if
     * it currently holds the given expected value.
     *  如果当前期望值与当前挂起数值相等才会设置这个count值
     * @param expected the expected value
     * @param count the new value
     * @return {@code true} if successful
     */
    public final boolean compareAndSetPendingCount(int expected, int count) {
        return U.compareAndSwapInt(this, PENDING, expected, count); // Unsafe不是系统类加载
    }

    /**
     * If the pending count is nonzero, (atomically) decrements it.
     * 如果这个挂起数值不是原子性，将会原子性递减
     * @return the initial (undecremented) pending count holding on entry
     * to this method
     */
    public final int decrementPendingCountUnlessZero() {
        int c;
        do {} while ((c = pending) != 0 &&
                     !U.compareAndSwapInt(this, PENDING, c, c - 1));
        return c;
    }

    /**
     * Returns the root of the current computation; i.e., this
     * task if it has no completer, else its completer's root.
     * 返回当前计算的根， 即， 这个任务没有完成者，他就是根
     * @return the root of the current computation
     */
    public final CountedCompleter<?> getRoot() {
        CountedCompleter<?> a = this, p;
        while ((p = a.completer) != null) //找到 a.completer等于空
            a = p;
        return a; //返回
    }

    /**
     * If the pending count is nonzero, decrements the count;
     * otherwise invokes {@link #onCompletion(CountedCompleter)}
     * and then similarly tries to complete this task's completer,
     * if one exists, else marks this task as complete.
     *
     * 如果挂起数值是非零，递减这个数值，否则调用这个onCompletion和类似如果存在该完成去
     * 尝试去完成这个任务的完成者，或者标记这个任务为完成。
     */
    public final void tryComplete() {
        CountedCompleter<?> a = this, s = a;
        for (int c;;) {
            if ((c = a.pending) == 0) { //如果等于0
                a.onCompletion(s); //调用onCompleteion方法
                if ((a = (s = a).completer) == null) { //r如果这个completer为null
                    s.quietlyComplete(); //开始失败返回
                    return;
                }
            }
            else if (U.compareAndSwapInt(a, PENDING, c, c - 1))//如果不为0 ，就递减一，返回
                return;
        }
    }

    /**
     * Equivalent to {@link #tryComplete} but does not invoke {@link
     * #onCompletion(CountedCompleter)} along the completion path:
     * If the pending count is nonzero, decrements the count;
     * otherwise, similarly tries to complete this task's completer, if
     * one exists, else marks this task as complete. This method may be
     * useful in cases where {@code onCompletion} should not, or need
     * not, be invoked for each completer in a computation.
     *
     *  这个方法等同于tryComplete,只是不会调用伴随完成路径onCompletion
     *  如果挂起数量是非零，递减这个数字，否则如果存在完成了，类似尝试完成这个任务的完成者，
     *  否则标记这个任务为已经完成，这个方法可能用于当在一个计算中onCompletion不应该或不需要被每一个完成者调用
     */
    public final void propagateCompletion() {
        CountedCompleter<?> a = this, s = a;
        for (int c;;) {
            if ((c = a.pending) == 0) {//这个方法不会调用onComplete
                if ((a = (s = a).completer) == null) {
                    s.quietlyComplete();
                    return;
                }
            }
            else if (U.compareAndSwapInt(a, PENDING, c, c - 1))
                return;
        }
    }

    /**
     * Regardless of【不管】 pending count, invokes
     * {@link #onCompletion(CountedCompleter)}, marks this task as
     * complete and further triggers {@link #tryComplete} on this
     * task's completer, if one exists.  The given rawResult is
     * used as an argument to {@link #setRawResult} before invoking
     * {@link #onCompletion(CountedCompleter)} or marking this task
     * as complete; its value is meaningful only for classes
     * overriding {@code setRawResult}.  This method does not modify
     * the pending count.
     *
     * <p>This method may be useful when forcing completion as soon as
     * any one (versus all) of several subtask results are obtained.
     * However, in the common (and recommended) case in which {@code
     * setRawResult} is not overridden, this effect can be obtained
     * more simply using {@code quietlyCompleteRoot();}.
     * 不管这个挂起数值，调用onCompletion方法，标记这个任务为完成和更深层次触发这个任务的完成者
     * 的tryComplete方法（如果存在的话），这里在调用onComplete之前或标记这个任务为完成
     * 给定一个rawResult作为setRawResult的入参，它的值一样只是为这些类重写setRawResult方法，这个
     * 方法不会修改挂起数值
     *
     *
     * @param rawResult the raw result
     */
    public void complete(T rawResult) {
        CountedCompleter<?> p;
        setRawResult(rawResult); //初始化环境 就是创建环境
        onCompletion(this); //执行完成操作
        quietlyComplete(); //快速完成
        if ((p = completer) != null)
            p.tryComplete();
    }

    /**
     * If this task's pending count is zero, returns this task;
     * otherwise decrements its pending count and returns {@code
     * null}. This method is designed to be used with {@link
     * #nextComplete} in completion traversal loops.
     * 如果这个任务的挂起数值为0，返回该对象，否则继续递减这个任务的挂起数值然后返回null
     * 在遍历完成这个方法被设计nextComplete使用
     * @return this task, if pending count was zero, else {@code null}
     */
    public final CountedCompleter<?> firstComplete() {
        for (int c;;) {
            if ((c = pending) == 0)
                return this;
            else if (U.compareAndSwapInt(this, PENDING, c, c - 1)) //pending count减一
                return null;
        }
    }

    /**
     * If this task does not have a completer, invokes {@link
     * ForkJoinTask#quietlyComplete} and returns {@code null}.  Or, if
     * the completer's pending count is non-zero, decrements that
     * pending count and returns {@code null}.  Otherwise, returns the
     * completer.  This method can be used as part of a completion
     * traversal loop for homogeneous 【同质】task hierarchies:
     * 如果这个任务并没有完成者，调用ForkJoinTask的quietlyComplete然后返回null'，或者
     * 如果这个完成者的挂起数值是非零，递减一然后返回null，否则返回完成者，
     * 这个方法为同质任务继承被用于作为完成遍历一部分
     * <pre> {@code
     * for (CountedCompleter<?> c = firstComplete();
     *      c != null;
     *      c = c.nextComplete()) {
     *   // ... process c ...
     * }}</pre>
     *
     * @return the completer, or {@code null} if none
     */
    public final CountedCompleter<?> nextComplete() {
        CountedCompleter<?> p;
        if ((p = completer) != null)//完成者不为空
            return p.firstComplete();
        else {//为空
            quietlyComplete();
            return null;
        }
    }

    /**
     * Equivalent to {@code getRoot().quietlyComplete()}.
     * 等同于 getRoot().quietlyComplete()方法
     */
    public final void quietlyCompleteRoot() {
        for (CountedCompleter<?> a = this, p;;) {
            if ((p = a.completer) == null) { //直到completer为空
                a.quietlyComplete();
                return;
            }
            a = p;
        }
    }

    /**
     * If this task has not completed, attempts to process at most the
     * given number of other unprocessed tasks for which this task is
     * on the completion path, if any are known to exist.
     * 如果这个任务没有完成，尝试去帮助在这个完成路径下处理至多这个给定数量其他未处理任务，如果有存在的话
     * @param maxTasks the maximum number of tasks to process.  If
     *                 less than or equal to zero, then no tasks are
     *                 processed.
     */
    public final void helpComplete(int maxTasks) {
        Thread t; ForkJoinWorkerThread wt;
        if (maxTasks > 0 && status >= 0) { // status线程池状态
            if ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) //判断当前线程是否是活动线程
                (wt = (ForkJoinWorkerThread)t).pool.
                    helpComplete(wt.workQueue, this, maxTasks);//如果是从线程写任务
            else
                ForkJoinPool.common.externalHelpComplete(this, maxTasks);
        }
    }

    /**
     * Supports ForkJoinTask exception propagation.
     * 支持异常传播
     */
    void internalPropagateException(Throwable ex) {
        CountedCompleter<?> a = this, s = a;
        while (a.onExceptionalCompletion(ex, s) &&
               (a = (s = a).completer) != null && a.status >= 0 && //completer不为null， status大于0
               a.recordExceptionalCompletion(ex) == EXCEPTIONAL) //异常
            ;
    }

    /**
     * Implements execution conventions for CountedCompleters.
     * 实现一个执行CountedCompleters方法
     */
    protected final boolean exec() {
        compute();
        return false;
    }

    /**
     * Returns the result of the computation. By default
     * returns {@code null}, which is appropriate for {@code Void}
     * actions, but in other cases should be overridden, almost
     * always to return a field or function of a field that
     * holds the result upon completion.
     * 返回一个计算结果，默认返回null， 这个合适用void操作，在其他情况下应该被重写，至少返回一个
     * 字段或保存值一个字段的函数
     *
     * @return the result of the computation
     */
    public T getRawResult() { return null; }

    /**
     * A method that result-bearing CountedCompleters may optionally
     * use to help maintain result data.  By default, does nothing.
     * Overrides are not recommended. However, if this method is
     * overridden to update existing objects or fields, then it must
     * in general be defined to be thread-safe.
     *
     * 这个方法没有结果返回，用于帮助保持结果数据，然而如果需要更已经存在的对象或字段，必须重写这方法，
     * 通常必须被定义成线程安全
     */
    protected void setRawResult(T t) { }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U; //定义一个Unsafe
    private static final long PENDING; //定义pendding字段的偏移量
    static {
        try {
            U = sun.misc.Unsafe.getUnsafe(); //这个必须是要系统加载器进行加载的
            PENDING = U.objectFieldOffset
                (CountedCompleter.class.getDeclaredField("pending"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
