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
 * Written by Josh Bloch of Google Inc. and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/.
 */

package com.example.javajdk.srcjdk.java.util;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

/**
 * Resizable-array implementation of the {@link Deque} interface.  Array
 * deques have no capacity restrictions; they grow as necessary to support
 * usage.  They are not thread-safe; in the absence of 【缺乏】external
 * synchronization, they do not support concurrent access by multiple threads.
 * Null elements are prohibited.  This class is likely to【有可能】 be faster than
 * {@link Stack} when used as a stack, and faster than {@link LinkedList}
 * when used as a queue.
 * 双向数组队列他实现Deque接口，长度可变的，但是他不是线程安全的，NUll也是不允许的，如果他作为Stack使用，可能比stack快，作为queue使用
 * 他可能也比queue快
 *
 *
 * <p>Most {@code ArrayDeque} operations run in amortized【平摊的】 constant time.
 * Exceptions include {@link #remove(Object) remove}, {@link
 * #removeFirstOccurrence removeFirstOccurrence}, {@link #removeLastOccurrence
 * removeLastOccurrence}, {@link #contains contains}, {@link #iterator
 * iterator.remove()}, and the bulk operations, all of which run in linear
 * time.
 * 很多操作都是平摊的常量时间，例外情况包括 remove、removeFirstOcurrentce、removeLastOccurrence、contains、iterator.remove()这
 * 些都是线性时间的
 *
 * <p>The iterators returned by this class's {@code iterator} method are
 * <i>fail-fast</i>: If the deque is modified at any time after the iterator
 * is created, in any way except through the iterator's own {@code remove}
 * method, the iterator will generally throw a {@link
 * ConcurrentModificationException}.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the
 * future.
 * 这个类支持fail-fast，当多线程修改该数组将会抛出ConcurrentModificationException，他不允许歧义，不确定行为
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw {@code ConcurrentModificationException} on a best-effort【尽力而为】 basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness: <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i>
 * 当时fail-fast并不能真正保证，一般来说很难做很强保证并发修改，快速失败建立在尽力而为基础上的，所以fail-fast应该用来检测bugs
 *
 * <p>This class and its iterator implement all of the
 * <em>optional</em> methods of the {@link Collection} and {@link
 * Iterator} interfaces.
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * 双向队列你可以想象它是一个圆圈
 * @author  Josh Bloch and Doug Lea
 * @since   1.6
 * @param <E> the type of elements held in this collection
 */
public class ArrayDeque<E> extends AbstractCollection<E>
                           implements Deque<E>, Cloneable, Serializable
{//他实现Deque、Cloneable(支持克隆操作） 、Serializable(支持序列化)
    /**
     * The array in which the elements of the deque are stored.
     * The capacity of the deque is the length of this array, which is
     * always a power of two. The array is never allowed to become
     * full, except transiently within an addX method where it is
     * resized (see doubleCapacity) immediately upon becoming full,
     * thus avoiding head and tail wrapping around to equal each
     * other.  We also guarantee that all array cells not holding
     * deque elements are always null.
     * 他扩展长度为2 n次方，他不允许变成full，只要快要变满，就扩展原来两倍，他也保存所有元素不为空
     */
    transient Object[] elements; // non-private to simplify nested class access

    /**
     * The index of the element at the head of the deque (which is the
     * element that would be removed by remove() or pop()); or an
     * arbitrary number equal to tail if the deque is empty.
     * 表示开始位置指针，如果 任意位置指针等于tail，表示该双向队列为空
     */
    transient int head;

    /**
     * The index at which the next element would be added to the tail
     * of the deque (via addLast(E), add(E), or push(E)).
     * 尾部指针
     */
    transient int tail;

    /**
     * The minimum capacity that we'll use for a newly created deque.
     * Must be a power of 2.
     * 默认最小容量为8，一定是2的倍数
     */
    private static final int MIN_INITIAL_CAPACITY = 8;

    // ******  Array allocation and resizing utilities ******

    /**
     * Allocates empty array to hold the given number of elements.
     * 分配一个空的数组来保存一定数量的元素
     *
     * @param numElements  the number of elements to hold
     */
    private void allocateElements(int numElements) {
        int initialCapacity = MIN_INITIAL_CAPACITY;  //初始化最小的长度为8
        // Find the best power of two to hold elements.
        // Tests "<=" because arrays aren't kept full.
        if (numElements >= initialCapacity) { // >=表示不能让这个队列满了
            initialCapacity = numElements;
            initialCapacity |= (initialCapacity >>>  1);
            initialCapacity |= (initialCapacity >>>  2);
            initialCapacity |= (initialCapacity >>>  4);
            initialCapacity |= (initialCapacity >>>  8);
            initialCapacity |= (initialCapacity >>> 16);
            initialCapacity++;

            if (initialCapacity < 0)   // Too many elements, must back off【后退】由正转负
                initialCapacity >>>= 1;// Good luck allocating 2 ^ 30 elements （把最高位是0）
        }
        elements = new Object[initialCapacity];
    }

    /**
     * Doubles the capacity of this deque.  Call only when full, i.e.,
     * when head and tail have wrapped around to become equal.
     * 2倍容量，只有当head 和tail相等的时候
     */
    private void doubleCapacity() {
        assert head == tail;  //如果head==tail表示内存不足了（这个需要开启-ea才可以使用断言）
        int p = head;  //把head复制给p
        int n = elements.length; //获取元素长度 赋值给n
        int r = n - p; // number of elements to the right of p， 右边的元素
        int newCapacity = n << 1; //右移动一位， 表示是最高位了例如 0111 1111 1111 1111 1111 1111 1111 1111
        if (newCapacity < 0)
            throw new IllegalStateException("Sorry, deque too big");
        Object[] a = new Object[newCapacity];
        System.arraycopy(elements, p, a, 0, r);//右边队列元素，123(p指向3 p=2)456   r=4元素为 3456
        System.arraycopy(elements, 0, a, r, p);//左边队列元素 （p指向3 p=0）345612
        elements = a; //将改对象赋值给成员变量elements
        head = 0;//head指向0
        tail = n;//tail指向最后
    }

    /**
     * Copies the elements from our element array into the specified array,
     * in order (from first to last element in the deque).  It is assumed
     * that the array is large enough to hold all elements in the deque.
     *  复制队列中元素成数组
     * @return its argument
     */
    private <T> T[] copyElements(T[] a) {
        if (head < tail) {//head没指向左边
            System.arraycopy(elements, head, a, 0, size());
        } else if (head > tail) {// 如果head新增元素，那么 head 就指向了 size() - （在head新增的元素)  （可以看出一个圈）
            int headPortionLen = elements.length - head;
            System.arraycopy(elements, head, a, 0, headPortionLen); //head 到 0(初始head位置)的元素
            System.arraycopy(elements, 0, a, headPortionLen, tail);// 0 到 tail之间的元素
        }
        return a;
    }

    /**
     * Constructs an empty array deque with an initial capacity
     * sufficient to hold 16 elements.
     * 默认的长度为16
     */
    public ArrayDeque() {
        elements = new Object[16];
    }

    /**
     * Constructs an empty array deque with an initial capacity
     * sufficient to hold the specified number of elements.
     * 构造一个带大小的参数构造方法，不过最小长度是8
     * @param numElements  lower bound on initial capacity of the deque
     */
    public ArrayDeque(int numElements) {
        allocateElements(numElements);
    }

    /**
     * Constructs a deque containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.  (The first element returned by the collection's
     * iterator becomes the first element, or <i>front</i> of the
     * deque.)
     * 将collection集合元素放入到ArrayDeque队列的构造方法
     * @param c the collection whose elements are to be placed into the deque
     * @throws NullPointerException if the specified collection is null
     */
    public ArrayDeque(Collection<? extends E> c) {
        allocateElements(c.size()); //分配元素大小
        addAll(c);//将元素放入改ArrayDeque
    }

    // The main insertion and extraction methods are addFirst,
    // addLast, pollFirst, pollLast. The other methods are defined in
    // terms of these.

    /**
     * Inserts the specified element at the front of this deque.
     * 在前面插入一个元素，就是head 位置插入一个元素（元素不能为空）
     * @param e the element to add
     * @throws NullPointerException if the specified element is null
     */
    public void addFirst(E e) {
        if (e == null) //如果元素为null 将抛出异常
            throw new NullPointerException();
        elements[head = (head - 1) & (elements.length - 1)] = e; // 当head 等于0 ， elements.lenght-1=7,  这个算法刚好可以使用head（7 6 5 ..)
        if (head == tail)//需要两倍扩容
            doubleCapacity();
    }

    /**
     * Inserts the specified element at the end of this deque.
     *
     * <p>This method is equivalent to {@link #add}.
     *
     * 在末尾加入元素
     * @param e the element to add
     * @throws NullPointerException if the specified element is null
     */
    public void addLast(E e) {
        if (e == null)
            throw new NullPointerException();
        elements[tail] = e; //直接加入tail就可以加入元素了，tail指向下一个插入的元素
        if ( (tail = (tail + 1) & (elements.length - 1)) == head) //
            doubleCapacity();
    }

    /**
     * Inserts the specified element at the front of this deque.
     *  这个与addFirst区别就是多了返回一个boolean参数true
     * @param e the element to add
     * @return {@code true} (as specified by {@link Deque#offerFirst})
     * @throws NullPointerException if the specified element is null
     */
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    /**
     * Inserts the specified element at the end of this deque.
     *
     * 这个与addLast区别就是多了返回一个boolean参数true
     * @param e the element to add
     * @return {@code true} (as specified by {@link Deque#offerLast})
     * @throws NullPointerException if the specified element is null
     */
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    /**
     * 移除第一个头部元素，如果没有抛出异常
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E removeFirst() {
        E x = pollFirst(); //获取元素
        if (x == null)//元素为空
            throw new NoSuchElementException();
        return x;
    }

    /**
     * 移除尾部元素，如果没有抛出异常
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E removeLast() {
        E x = pollLast();
        if (x == null)
            throw new NoSuchElementException(); //如果没有改元素抛出 NoSuchElementException
        return x;
    }

    /**
     * 弹出head指向的元素
     * @return
     */
    public E pollFirst() {
        int h = head; //获取head索引 赋值给h
        @SuppressWarnings("unchecked")
        E result = (E) elements[h]; //获取该元素
        // Element is null if deque empty
        if (result == null)//如果元素为空返回空
            return null;
        elements[h] = null;     // Must null out slot 将该位置置null
        head = (h + 1) & (elements.length - 1);//同时head指针位置需要向后移动一位
        return result;
    }

    /**
     * 弹出tail-1指向的元素
     * @return
     */
    public E pollLast() {
        int t = (tail - 1) & (elements.length - 1); //指向上一次元素（也就是tail指向是下一个插入元素位置）
        @SuppressWarnings("unchecked")
        E result = (E) elements[t];
        if (result == null)
            return null;
        elements[t] = null;
        tail = t; //移动tail 的位置
        return result;
    }

    /**
     * 获取第一个元素，但是不需要删除， 没有抛出异常
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E getFirst() {
        @SuppressWarnings("unchecked")
        E result = (E) elements[head];
        if (result == null)
            throw new NoSuchElementException();
        return result;
    }

    /**
     * 获取最后的元素，没有抛出异常
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E getLast() {
        @SuppressWarnings("unchecked")
        E result = (E) elements[(tail - 1) & (elements.length - 1)]; //(tail - 1) & (elements.length - 1) 指向最后一个元素
        if (result == null)
            throw new NoSuchElementException();
        return result;
    }

    /**
     * 直接返回元素，不抛出异常，也就是，如果没有就返回null
     * 返回第一个元素
     * @return
     */
    @SuppressWarnings("unchecked")
    public E peekFirst() {
        // elements[head] is null if deque empty
        return (E) elements[head];
    }

    /**
     * 返回最后一个元素，不抛出异常，没有返回null
     * @return
     */
    @SuppressWarnings("unchecked")
    public E peekLast() {
        return (E) elements[(tail - 1) & (elements.length - 1)];
    }

    /**
     * Removes the first occurrence of the specified element in this
     * deque (when traversing the deque from head to tail).
     * If the deque does not contain the element, it is unchanged.
     * More formally, removes the first element {@code e} such that
     * {@code o.equals(e)} (if such an element exists).
     * Returns {@code true} if this deque contained the specified element
     * (or equivalently, if this deque changed as a result of the call).
     * 移除第一个元素出现该元素
     * @param o element to be removed from this deque, if present
     * @return {@code true} if the deque contained the specified element
     */
    public boolean removeFirstOccurrence(Object o) {
        if (o == null)
            return false;
        int mask = elements.length - 1; // mask 其实最后一个索引位置数字
        int i = head;//i 值为head
        Object x;
        while ( (x = elements[i]) != null) {
            if (o.equals(x)) { //如果存在该对象，删除他
                delete(i); //删除时候需要重新拼接列表
                return true; //返回true
            }
            i = (i + 1) & mask;//索引往前移动一位
        }
        return false;
    }

    /**
     * Removes the last occurrence of the specified element in this
     * deque (when traversing the deque from head to tail).
     * If the deque does not contain the element, it is unchanged.
     * More formally, removes the last element {@code e} such that
     * {@code o.equals(e)} (if such an element exists).
     * Returns {@code true} if this deque contained the specified element
     * (or equivalently, if this deque changed as a result of the call).
     *
     * 移除最后出现改元素对象， 类似String.LastValueOf
     * @param o element to be removed from this deque, if present
     * @return {@code true} if the deque contained the specified element
     */
    public boolean removeLastOccurrence(Object o) {
        if (o == null)
            return false;
        int mask = elements.length - 1; //获取掩码
        int i = (tail - 1) & mask; //获取最后元素位置
        Object x;
        while ( (x = elements[i]) != null) { //一直到为null
            if (o.equals(x)) {
                delete(i);
                return true;
            }
            i = (i - 1) & mask;
        }
        return false;
    }

    // *** Queue methods ***

    /**
     * Inserts the specified element at the end of this deque.
     *
     * <p>This method is equivalent to {@link #addLast}.
     *  add 最后添加元素
     * @param e the element to add
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws NullPointerException if the specified element is null
     */
    public boolean add(E e) {
        addLast(e);
        return true;
    }

    /**
     * Inserts the specified element at the end of this deque.
     *
     * <p>This method is equivalent to {@link #offerLast}.
     *   这个方法与offerlast方法和 add方法一样
     * @param e the element to add
     * @return {@code true} (as specified by {@link Queue#offer})
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e) {
        return offerLast(e);
    }

    /**
     * Retrieves and removes the head of the queue represented by this deque.
     *
     * This method differs from {@link #poll poll} only in that it throws an
     * exception if this deque is empty.
     *  移除第一个元素，并返回，如果不存在抛出异常
     * <p>This method is equivalent to {@link #removeFirst}.
     *
     * @return the head of the queue represented by this deque
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E remove() {
        return removeFirst();
    }

    /**
     * Retrieves and removes the head of the queue represented by this deque
     * (in other words, the first element of this deque), or returns
     * {@code null} if this deque is empty.
     *
     * <p>This method is equivalent to {@link #pollFirst}.
     *  弹出第一个元素不会抛出异常
     * @return the head of the queue represented by this deque, or
     *         {@code null} if this deque is empty
     */
    public E poll() {
        return pollFirst();
    }

    /**
     * Retrieves, but does not remove, the head of the queue represented by
     * this deque.  This method differs from {@link #peek peek} only in
     * that it throws an exception if this deque is empty.
     *
     * <p>This method is equivalent to {@link #getFirst}.
     *  获取第一个元素，没有抛出异常
     * @return the head of the queue represented by this deque
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E element() {
        return getFirst();
    }

    /**
     * Retrieves, but does not remove, the head of the queue represented by
     * this deque, or returns {@code null} if this deque is empty.
     *
     * <p>This method is equivalent to {@link #peekFirst}.
     * 获取第一个元素，没有不会抛出异常
     * @return the head of the queue represented by this deque, or
     *         {@code null} if this deque is empty
     */
    public E peek() {
        return peekFirst();
    }

    // *** Stack methods ***

    /**
     * Pushes an element onto the stack represented by this deque.  In other
     * words, inserts the element at the front of this deque.
     *
     * <p>This method is equivalent to {@link #addFirst}.
     * 加入元素，如果为null 抛出空指针异常
     * @param e the element to push
     * @throws NullPointerException if the specified element is null
     */
    public void push(E e) {
        addFirst(e);
    }

    /**
     * Pops an element from the stack represented by this deque.  In other
     * words, removes and returns the first element of this deque.
     *
     * <p>This method is equivalent to {@link #removeFirst()}.
     * 移除第一个元素， 没有抛出异常，其实很多方法相同，对应不同场景，比stack、queue
     * @return the element at the front of this deque (which is the top
     *         of the stack represented by this deque)
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E pop() {
        return removeFirst();
    }

    private void checkInvariants() {
        assert elements[tail] == null; //断言指向下一个尾部元素为空
        assert head == tail ? elements[head] == null :
            (elements[head] != null &&
             elements[(tail - 1) & (elements.length - 1)] != null); // 如果（head==tail) 说明还剩一个位置,
        assert elements[(head - 1) & (elements.length - 1)] == null; // head 往前一个元素也为null
    }

    /**
     * Removes the element at the specified position in the elements array,
     * adjusting head and tail as necessary.  This can result in motion of
     * elements backwards or forwards in the array.
     * 删除一个元素之后，需要移动元素
     * <p>This method is called delete rather than remove to emphasize
     * that its semantics differ from those of {@link List#remove(int)}.
     *  如果元素tail指针往前移动为true， head指针往后移动为false
     * @return true if elements moved backwards
     */
    private boolean delete(int i) {
        checkInvariants(); //检查不变规则
        final Object[] elements = this.elements; //得到这个元素 elements
        final int mask = elements.length - 1; //获取掩码 （索引最大）
        final int h = head; //head位置 h 例如 1（h指向1 h=0）23（删除元素3，i=2）4567 (t指向8位置， t=7)
        final int t = tail; //tail位置 t
        final int front = (i - h) & mask; // 前面部分元素 front= 2
        final int back  = (t - i) & mask;//后面部分的元素 back = 5

        // Invariant: head <= i < tail mod circularity
        if (front >= ((t - h) & mask))//(t - h) & mask)表示当前元素个数， front也表示当前元素位置，如果这个条件成立，元素超出范围，只有线程修改。
            throw new ConcurrentModificationException();

        // Optimize for least element motion
        if (front < back) { //这个用了到底移动那边元素个数最小（移动最小）  //（如果元素是顺时针，那么这移动就是逆时针的）
            if (h <= i) { // h表示没有head没有0的左边， 没有变成elements.length -1
                System.arraycopy(elements, h, elements, h + 1, front);
            } else { // Wrap around 表示head已经超过0，跑到左边了
                System.arraycopy(elements, 0, elements, 1, i); //先复制右边
                elements[0] = elements[mask]; //最后一个元素赋值0索引位置
                System.arraycopy(elements, h, elements, h + 1, mask - h);//复制左边元素
            }
            elements[h] = null; //h位置元素制空
            head = (h + 1) & mask;//head指针改变，往后移动一位
            return false;
        } else {//（如果元素是顺时针，那么这移动就是顺时针的）
            if (i < t) { // Copy the null tail as well 表示刚刚好，i在tail前面
                System.arraycopy(elements, i + 1, elements, i, back);
                tail = t - 1; //修改tail 指针指向的位置
            } else { // Wrap around 在head增加元素过多会出现这种情况
                System.arraycopy(elements, i + 1, elements, i, mask - i); //复制左边元素
                elements[mask] = elements[0]; //将0号元素赋值给 最大索引元素
                System.arraycopy(elements, 1, elements, 0, t); //将左边元素进行移动
                tail = (t - 1) & mask;//修改tail指针指向
            }
            return true;
        }
    }

    // *** Collection Methods ***

    /**
     * Returns the number of elements in this deque.
     *  获取元素大小
     * @return the number of elements in this deque
     */
    public int size() {
        return (tail - head) & (elements.length - 1);
    }

    /**
     * Returns {@code true} if this deque contains no elements.
     * 判断元素是否 为空
     * @return {@code true} if this deque contains no elements
     */
    public boolean isEmpty() {
        return head == tail;
    }

    /**
     * Returns an iterator over the elements in this deque.  The elements
     * will be ordered from first (head) to last (tail).  This is the same
     * order that elements would be dequeued (via successive calls to
     * {@link #remove} or popped (via successive calls to {@link #pop}).
     * 获取得到迭代器
     *
     * @return an iterator over the elements in this deque
     */
    public Iterator<E> iterator() {
        return new DeqIterator();
    }

    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    private class DeqIterator implements Iterator<E> {
        /**
         * Index of element to be returned by subsequent call to next.
         * 将本对象 head 赋值给cursor
         */
        private int cursor = head;

        /**
         * Tail recorded at construction (also in remove), to stop
         * iterator and also to check for comodification.
         * 可以用检测并发修改
         */
        private int fence = tail;

        /**
         * Index of element returned by most recent call to next.
         * Reset to -1 if element is deleted by a call to remove.
         * 表示最近获取的元素的索引，如果为-1表示该元素被删除， 很类似 AbstractList
         */
        private int lastRet = -1;

        public boolean hasNext() {
            return cursor != fence;
        } //表示cursor没有指向fence

        public E next() {
            if (cursor == fence) //如果没有下一个抛出NoSuchElementException
                throw new NoSuchElementException();
            @SuppressWarnings("unchecked")
            E result = (E) elements[cursor]; //获取第一个元素
            // This check doesn't catch all possible comodifications,
            // but does catch the ones that corrupt traversal
            if (tail != fence || result == null) //表示并发修改导致该元素可能被删除
                throw new ConcurrentModificationException();
            lastRet = cursor;// 将这个元素赋值这个lastRet
            cursor = (cursor + 1) & (elements.length - 1); //更新cursor位置
            return result;
        }

        public void remove() { //移除元素
            if (lastRet < 0) //没有遍历
                throw new IllegalStateException();
            if (delete(lastRet)) { // if left-shifted, undo increment in next() 删除元素
                cursor = (cursor - 1) & (elements.length - 1);//更新head索引
                fence = tail;//tail的指针
            }
            lastRet = -1;
        }

        /**
         * customer的参数是 accept方法的形参
         * rrayDeque<String> arrayDeque = new ArrayDeque<>();
         * Spliterator<String> spliterator = arrayDeque.spliterator();
         * arrayDeque.add("111");
         * spliterator.forEachRemaining(str -> System.out.println(str))；
         * action相当于入参，然后执行 accept调用
         * @param action
         */
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            Object[] a = elements;
            int m = a.length - 1, f = fence, i = cursor; //m 为mask， f=tail  i=head
            cursor = f; // cursor 变成tail
            while (i != f) { //表示head和tail没有重叠， 还有元素
                @SuppressWarnings("unchecked") E e = (E)a[i];
                i = (i + 1) & m;//获取下一个元素的索引
                if (e == null) //如果这个元素为空
                    throw new ConcurrentModificationException();
                action.accept(e);//将这个元素放入到action函数中
            }
        }
    }

    private class DescendingIterator implements Iterator<E> {
        /*
         * This class is nearly a mirror-image of DeqIterator, using
         * tail instead of head for initial cursor, and head instead of
         * tail for fence.
         * 他DeqIterator的倒序迭代
         */
        private int cursor = tail; // 当前游标的位置 （末尾）
        private int fence = head; //最后位置 （开始）
        private int lastRet = -1; // 保存最近元素的索引（-1）表示删除之后

        public boolean hasNext() {
            return cursor != fence;
        }

        public E next() {
            if (cursor == fence)
                throw new NoSuchElementException();
            cursor = (cursor - 1) & (elements.length - 1);// 下一个游标位置
            @SuppressWarnings("unchecked")
            E result = (E) elements[cursor]; //获取对应的元素
            if (head != fence || result == null) //被修改了
                throw new ConcurrentModificationException();
            lastRet = cursor; //保存最近元素的索引
            return result;
        }

        public void remove() { //移除元素
            if (lastRet < 0)
                throw new IllegalStateException();
            if (!delete(lastRet)) {//删除元素
                cursor = (cursor + 1) & (elements.length - 1); //变更游标的位置
                fence = head; //结束位置
            }
            lastRet = -1;
        }
    }

    /**
     * Returns {@code true} if this deque contains the specified element.
     * More formally, returns {@code true} if and only if this deque contains
     * at least one element {@code e} such that {@code o.equals(e)}.
     *
     * @param o object to be checked for containment in this deque
     * @return {@code true} if this deque contains the specified element
     */
    public boolean contains(Object o) { //包含某个元素
        if (o == null)
            return false;
        int mask = elements.length - 1;
        int i = head;
        Object x;
        while ( (x = elements[i]) != null) {
            if (o.equals(x))
                return true;
            i = (i + 1) & mask;
        }
        return false;
    }

    /**
     * Removes a single instance of the specified element from this deque.
     * If the deque does not contain the element, it is unchanged.
     * More formally, removes the first element {@code e} such that
     * {@code o.equals(e)} (if such an element exists).
     * Returns {@code true} if this deque contained the specified element
     * (or equivalently, if this deque changed as a result of the call).
     *
     * <p>This method is equivalent to {@link #removeFirstOccurrence(Object)}.
     * 移除第一个等于这个对象的元素
     * @param o element to be removed from this deque, if present
     * @return {@code true} if this deque contained the specified element
     */
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    /**
     * Removes all of the elements from this deque.
     * The deque will be empty after this call returns.
     * 清除元素
     */
    public void clear() {
        int h = head;
        int t = tail;
        if (h != t) { // clear all cells
            head = tail = 0;//把头尾制空
            int i = h;
            int mask = elements.length - 1;
            do {
                elements[i] = null; //同时将对应值设置为null
                i = (i + 1) & mask;
            } while (i != t);
        }
    }

    /**
     * Returns an array containing all of the elements in this deque
     * in proper sequence (from first to last element).
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this deque.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     * 将集合变成数组的桥梁
     * @return an array containing all of the elements in this deque
     */
    public Object[] toArray() {
        return copyElements(new Object[size()]);
    }

    /**
     * Returns an array containing all of the elements in this deque in
     * proper sequence (from first to last element); the runtime type of the
     * returned array is that of the specified array.  If the deque fits in
     * the specified array, it is returned therein.  Otherwise, a new array
     * is allocated with the runtime type of the specified array and the
     * size of this deque.
     *
     * <p>If this deque fits in the specified array with room to spare
     * (i.e., the array has more elements than this deque), the element in
     * the array immediately following the end of the deque is set to
     * {@code null}.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose {@code x} is a deque known to contain only strings.
     * The following code can be used to dump the deque into a newly
     * allocated array of {@code String}:
     *
     *  返回特定类型数组
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     *
     * @param a the array into which the elements of the deque are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose
     * @return an array containing all of the elements in this deque
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this deque
     * @throws NullPointerException if the specified array is null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        int size = size();
        if (a.length < size)
            a = (T[])java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        copyElements(a);
        if (a.length > size) //长度位置设置为null
            a[size] = null;
        return a;
    }

    // *** Object methods ***

    /**
     * Returns a copy of this deque.
     * 返回一个复制的队列
     *
     * @return a copy of this deque
     */
    public ArrayDeque<E> clone() {
        try {
            @SuppressWarnings("unchecked")
            ArrayDeque<E> result = (ArrayDeque<E>) super.clone();
            result.elements = Arrays.copyOf(elements, elements.length);
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    private static final long serialVersionUID = 2340985798034038923L;

    /**
     * Saves this deque to a stream (that is, serializes it).
     *
     * 重写序列化数据
     * @serialData The current size ({@code int}) of the deque,
     * followed by all of its elements (each an object reference) in
     * first-to-last order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        s.defaultWriteObject();

        // Write out size
        s.writeInt(size()); //写出大小

        // Write out elements in order.
        int mask = elements.length - 1; //写出元素
        for (int i = head; i != tail; i = (i + 1) & mask)
            s.writeObject(elements[i]);
    }

    /**
     * Reconstitutes this deque from a stream (that is, deserializes it).
     * 从流中读取对象
     */
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();

        // Read in size and allocate array
        int size = s.readInt();
        allocateElements(size);
        head = 0;
        tail = size;

        // Read in all elements in the proper order.
        for (int i = 0; i < size; i++)
            elements[i] = s.readObject();
    }

    /**
     * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
     * and <em>fail-fast</em> {@link Spliterator} over the elements in this
     * deque.
     *
     * 快速失败list迭代器
     * <p>The {@code Spliterator} reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, {@link Spliterator#ORDERED}, and
     * {@link Spliterator#NONNULL}.  Overriding implementations should document
     * the reporting of additional characteristic values.
     *
     * @return a {@code Spliterator} over the elements in this deque
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return new DeqSpliterator<E>(this, -1, -1);
    }

    static final class DeqSpliterator<E> implements Spliterator<E> { //DeqIterator
        private final ArrayDeque<E> deq;
        private int fence;  // -1 until first use
        private int index;  // current index, modified on traverse/split

        /** Creates new spliterator covering the given array and range */
        DeqSpliterator(ArrayDeque<E> deq, int origin, int fence) {
            this.deq = deq;
            this.index = origin; // index = -1
            this.fence = fence; // fence = -1
        }

        private int getFence() { // force initialization
            int t;
            if ((t = fence) < 0) { // 初始化fence、和index
                t = fence = deq.tail;
                index = deq.head;
            }
            return t;
        }

        public DeqSpliterator<E> trySplit() {
            int t = getFence(), h = index, n = deq.elements.length; // t =tail, h = head
            if (h != t && ((h + 1) & (n - 1)) != t) { // 当前head和下一个head+1 不会等于tail
                if (h > t) // 所以head越过索引0位置，跑到0的左边位置（编排是逆时针）
                    t += n;
                int m = ((h + t) >>> 1) & (n - 1); //切分一半
                return new DeqSpliterator<>(deq, h, index = m); //左边一半
            }
            return null;
        }

        /**
         * 函数调用
         * @param consumer
         */
        public void forEachRemaining(Consumer<? super E> consumer) {
            if (consumer == null)
                throw new NullPointerException();
            Object[] a = deq.elements; //获取元素
            int m = a.length - 1, f = getFence(), i = index;// m mask掩码，
            index = f;
            while (i != f) { // f = tail  i = head
                @SuppressWarnings("unchecked") E e = (E)a[i];
                i = (i + 1) & m;
                if (e == null)
                    throw new ConcurrentModificationException();
                consumer.accept(e);
            }
        }

        /**
         * 有boolean返回值
         * @param consumer
         * @return
         */
        public boolean tryAdvance(Consumer<? super E> consumer) {
            if (consumer == null) // 如果consumer为null
                throw new NullPointerException();
            Object[] a = deq.elements; //获取元素
            int m = a.length - 1, f = getFence(), i = index; // m mask掩码
            if (i != fence) {
                @SuppressWarnings("unchecked") E e = (E)a[i];
                index = (i + 1) & m;
                if (e == null)
                    throw new ConcurrentModificationException();
                consumer.accept(e);
                return true;
            }
            return false;
        }

        public long estimateSize() {
            int n = getFence() - index;
            if (n < 0) //左边
                n += deq.elements.length;
            return (long) n;
        }

        @Override
        public int characteristics() { //特性
            return Spliterator.ORDERED | Spliterator.SIZED |
                Spliterator.NONNULL | Spliterator.SUBSIZED;
        }
    }

}
