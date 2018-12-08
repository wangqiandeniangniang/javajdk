/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Resizable-array implementation of the <tt>List</tt> interface.  Implements
 * all optional list operations, and permits all elements, including
 * <tt>null</tt>.  In addition to implementing the <tt>List</tt> interface,
 * this class provides methods to manipulate the size of the array that is
 * used internally to store the list.  (This class is roughly equivalent to
 * <tt>Vector</tt>, except that it is unsynchronized.)
 *
 * 实现List的可变大小数组， 实现List所有可选操作， 允许null值，他粗略等于Vector，只是它不是线程安全的
 *
 * <p>The <tt>size</tt>, <tt>isEmpty</tt>, <tt>get</tt>, <tt>set</tt>,
 * <tt>iterator</tt>, and <tt>listIterator</tt> operations run in constant
 * time.  The <tt>add</tt> operation runs in <i>amortized constant time</i>,
 * that is, adding n elements requires O(n) time.  All of the other operations
 * run in linear time (roughly speaking).  The constant factor is low compared
 * to that for the <tt>LinkedList</tt> implementation.
 * size、isEmpty、get、set、iterator、listInterator操作都是常量时间，add操作是常量均摊时间（O(n))
 * 所有其他操作都是线性时间
 *
 * <p>Each <tt>ArrayList</tt> instance has a <i>capacity</i>.  The capacity is
 * the size of the array used to store the elements in the list.  It is always
 * at least as large as the list size.  As elements are added to an ArrayList,
 * its capacity grows automatically.  The details of the growth policy are not
 * specified beyond the fact that adding an element has constant amortized
 * time cost.
 *  每个ArrayList都有容量字段，它会随着元素增加而自动扩容，这就是新增元素是恒定分摊时间原因
 *
 * <p>An application can increase the capacity of an <tt>ArrayList</tt> instance
 * before adding a large number of elements using the <tt>ensureCapacity</tt>
 * operation.  This may reduce the amount of incremental reallocation.
 * 如果当开始就知道有大量的元素，可以使用ensureCapacity操作，来减少扩容分配次数
 *
 *
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access an <tt>ArrayList</tt> instance concurrently,
 * and at least one of the threads modifies the list structurally, it
 * <i>must</i> be synchronized externally.  (A structural modification is
 * any operation that adds or deletes one or more elements, or explicitly
 * resizes the backing array; merely setting the value of an element is not
 * a structural modification.)  This is typically accomplished by
 * synchronizing on some object that naturally encapsulates the list.
 * 注意这个ArrayList不是线程安全的，如果有多个线程访问的需要在外部保证同步性（如果是结构性修改-就是改变List元素的长度），
 *
 * If no such object exists, the list should be "wrapped" using the
 * {@link Collections#synchronizedList Collections.synchronizedList}
 * method.  This is best done at creation time, to prevent accidental
 * unsynchronized access to the list:<pre>
 *   List list = Collections.synchronizedList(new ArrayList(...));</pre>
 *   在创建时候利用外部同步来保证线程安全
 *
 * <p><a name="fail-fast">
 * The iterators returned by this class's {@link #iterator() iterator} and
 * {@link #listIterator(int) listIterator} methods are <em>fail-fast</em>:</a>
 * if the list is structurally modified at any time after the iterator is
 * created, in any way except through the iterator's own
 * {@link ListIterator#remove() remove} or
 * {@link ListIterator#add(Object) add} methods, the iterator will throw a
 * {@link ConcurrentModificationException}.  Thus, in the face of
 * concurrent modification, the iterator fails quickly and cleanly, rather
 * than risking arbitrary, non-deterministic behavior at an undetermined
 * time in the future.
 * iterator 和 listIterator 都是支持快速失败的，防止多线程修改之后不确定的结果
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw {@code ConcurrentModificationException} on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness:  <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i>
 *
 * 快速失败并不能完全保证非同步并发的修改，只是尽最大努力，所以这个一般用于检测bug用的
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see     Collection
 * @see     List
 * @see     LinkedList
 * @see     Vector
 * @since   1.2
 */

public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{ // 继承 AbstractList抽象类， 实现List接口， 随机访问， 克隆， 序列化
    private static final long serialVersionUID = 8683452581122892189L;

    /**
     * Default initial capacity.
     * 默认容量为10
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * Shared empty array instance used for empty instances.
     * 空实例分享一个空数组实例
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     * Shared empty array instance used for default sized empty instances. We
     * distinguish this from EMPTY_ELEMENTDATA to know how much to inflate when
     * first element is added.
     * 空实例分享一个默认容量空数组实例， 与空数组不同就是，如果第一个元素被添加之后自动扩展当前默认容量大小
     */
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer. Any
     * empty ArrayList with elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
     * will be expanded to DEFAULT_CAPACITY when the first element is added.
     * 数组buffer（ArrayList数据的副本），他长度为ArrayList的容量大小， 如果第一个元素被添加之后自动扩展当前默认容量大小
     */
    transient Object[] elementData; // non-private to simplify nested class access

    /**
     * The size of the ArrayList (the number of elements it contains).
     *  ArrayList的元素的个数
     * @serial
     */
    private int size;

    /**
     * Constructs an empty list with the specified initial capacity.
     *  带有初始化容量的构造方法
     * @param  initialCapacity  the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity
     *         is negative
     */
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) { //如果大于0，创建一个elementData对象（ArrayList的副本
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) { //如果是0，空数组
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        }
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     * 如果不传初始化，默认10容量
     */
    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *  带有集合参数的构造方法，这个集合的元素将会放入到ArrayList中
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    public ArrayList(Collection<? extends E> c) {
        elementData = c.toArray();  //将集合元素变成数组
        if ((size = elementData.length) != 0) { //如果不为空，进行copy
            // c.toArray might (incorrectly) not return Object[] (see 6260652)
            if (elementData.getClass() != Object[].class) // 不能向下转型 如果不是Object[] 例如它是String[] 不能放入Object对象
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            // replace with empty array.
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }

    /**
     * Trims the capacity of this <tt>ArrayList</tt> instance to be the
     * list's current size.  An application can use this operation to minimize
     * the storage of an <tt>ArrayList</tt> instance.
     * 将容量变成实际元素长度
     */
    public void trimToSize() {
        modCount++; //记录结构修改的次数，一般用来并发修改检测
        if (size < elementData.length) { //如果长度小于容量大小
            elementData = (size == 0) // 可能没有元素
              ? EMPTY_ELEMENTDATA
              : Arrays.copyOf(elementData, size); //复制元素
        }
    }

    /**
     * Increases the capacity of this <tt>ArrayList</tt> instance, if
     * necessary, to ensure that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *  扩容
     * @param   minCapacity   the desired minimum capacity
     */
    public void ensureCapacity(int minCapacity) {
        int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA) //是否默认长度
            // any size if not default element table
            ? 0
            // larger than default for default empty table. It's already
            // supposed to be at default size.
            : DEFAULT_CAPACITY; //如果是设置默认长度为10

        if (minCapacity > minExpand) { //如果扩容大于默认长度，以实际为准
            ensureExplicitCapacity(minCapacity);
        }
    }

    /**
     * 内部扩容容量
     * @param minCapacity
     */
    private void ensureCapacityInternal(int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) { //如果是默认长度， 就在默认长度和minCapacity选择最大
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }

        ensureExplicitCapacity(minCapacity);
    }

    /**
     * 显示扩容
     * @param minCapacity
     */
    private void ensureExplicitCapacity(int minCapacity) {
        modCount++; //修改列表的长度

        // overflow-conscious code
        if (minCapacity - elementData.length > 0)//最小长度大于当前的长度
            grow(minCapacity);
    }

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     * 最大数组长度， 可能出现OOM
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     * 扩容
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length; //原来容量长度
        int newCapacity = oldCapacity + (oldCapacity >> 1);// 3/2 的oldCapacity
        if (newCapacity - minCapacity < 0) // newCapacity还不满足要求
            newCapacity = minCapacity; // 就用传入容量长度
        if (newCapacity - MAX_ARRAY_SIZE > 0) // 如果他大于最大容量
            newCapacity = hugeCapacity(minCapacity); //最大容量处理
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    /**
     * 最大容量处理
     * @param minCapacity
     * @return
     */
    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow  内存泄漏
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE : //数组最长为Integer.Value
            MAX_ARRAY_SIZE; //数组
    }

    /**
     * Returns the number of elements in this list.
     *  返回元素的大小
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     * true 元素为空
     * @return <tt>true</tt> if this list contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     * 包含某个元素 返回true ，否则返回false
     *
     * @param o element whose presence in this list is to be tested
     * @return <tt>true</tt> if this list contains the specified element
     */
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     * 返回第一个某个元素的索引位置， -1表示没有找到
     */
    public int indexOf(Object o) {
        if (o == null) { //为空，查看list是否有空元素
            for (int i = 0; i < size; i++)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = 0; i < size; i++)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1; //如果没有返回-1
    }

    /**
     * Returns the index of the last occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the highest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     * 返回最后出现某个元素的索引， 与前面indexOf不同，这里从尾部开始遍历
     */
    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = size-1; i >= 0; i--)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = size-1; i >= 0; i--)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    /**
     * Returns a shallow copy of this <tt>ArrayList</tt> instance.  (The
     * elements themselves are not copied.)
     *  浅拷贝， 他引用对象不会copy，也就是共用一个对象，对象的修改会相互影响
     * @return a clone of this <tt>ArrayList</tt> instance
     */
    public Object clone() {
        try {
            ArrayList<?> v = (ArrayList<?>) super.clone();
            v.elementData = Arrays.copyOf(elementData, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }

    /**
     * Returns an array containing all of the elements in this list
     * in proper sequence (from first to last element).
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this list.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * 将list转换成新建的数组，所以对于数组元素的修改会影响到原来的List
     * @return an array containing all of the elements in this list in
     *         proper sequence
     */
    public Object[] toArray() {
        return Arrays.copyOf(elementData, size);
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence (from first to last element); the runtime type of the returned
     * array is that of the specified array.  If the list fits in the
     * specified array, it is returned therein.  Otherwise, a new array is
     * allocated with the runtime type of the specified array and the size of
     * this list.
     *
     * <p>If the list fits in the specified array with room to spare
     * (i.e., the array has more elements than the list), the element in
     * the array immediately following the end of the collection is set to
     * <tt>null</tt>.  (This is useful in determining the length of the
     * list <i>only</i> if the caller knows that the list does not contain
     * any null elements.)
     * 传入复制数组的类型，然后返回，如果太长就最后一位设置null， 作为结束符
     *
     * @param a the array into which the elements of the list are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this list
     * @throws NullPointerException if the specified array is null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
        System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size) //如果长度大于本身长度，就在最后设置为null
            a[size] = null;
        return a;
    }

    // Positional Access Operations

    /**
     * 返回某个索引位置的元素
     * @param index
     * @return
     */
    @SuppressWarnings("unchecked")
    E elementData(int index) {
        return (E) elementData[index];
    }

    /**
     * Returns the element at the specified position in this list.
     *  这个需要检查是否越界，返回元素
     * @param  index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E get(int index) {
        rangeCheck(index); //检查越界

        return elementData(index);
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * 替换特定索引位置的元素
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E set(int index, E element) {
        rangeCheck(index);//检查是否越界

        E oldValue = elementData(index); //获取旧值
        elementData[index] = element; //替换旧值
        return oldValue; //返回旧值
    }

    /**
     * Appends the specified element to the end of this list.
     * 将元素添加到最后位置
     *
     * @param e element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!  内部扩容，modCount也是需要计数的
        elementData[size++] = e; //在最后增加元素
        return true;
    }

    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * 在特定索引位置增加元素
     *
     * @param index index at which the specified element is to be inserted
     *
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public void add(int index, E element) {
        rangeCheckForAdd(index); //检查是否越界

        ensureCapacityInternal(size + 1);  // Increments modCount!! 扩容
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index); //将后面的元素往后移动
        elementData[index] = element; //当前位置设置元素
        size++;
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *  移除特定位置元素
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E remove(int index) {
        rangeCheck(index);

        modCount++; // 修改数据结构，这个数需要累加
        E oldValue = elementData(index); //获取这个位置的元素

        int numMoved = size - index - 1; //移动元素的个数
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved); //移动最后元素
        elementData[--size] = null; // clear to let GC do its work  设置为null 让gc回收

        return oldValue;// 返回移除的值
    }

    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present.  If the list does not contain the element, it is
     * unchanged.  More formally, removes the element with the lowest index
     * <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
     * (if such an element exists).  Returns <tt>true</tt> if this list
     * contained the specified element (or equivalently, if this list
     * changed as a result of the call).
     *
     * 移除第一个出现某个元素
     * @param o element to be removed from this list, if present
     * @return <tt>true</tt> if this list contained the specified element
     */
    public boolean remove(Object o) {
        if (o == null) { //移除元素为null
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) { //遍历移除该元素,移除元素
                    fastRemove(index);
                    return true;
                }
        } else {
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {
                    fastRemove(index);
                    return true;
                }
        }
        return false;
    }

    /*
     * Private remove method that skips bounds checking and does not
     * return the value removed.
     * 跳过越界检查，跟remove(int index)一样的
     */
    private void fastRemove(int index) {
        modCount++; //操作数增加
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // clear to let GC do its work
    }

    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     * 清空元素
     */
    public void clear() {
        modCount++; //结构改变，这个需要增加

        // clear to let GC do its work
        for (int i = 0; i < size; i++) //所有的元素都设置为null
            elementData[i] = null;

        size = 0; //长度设置为0
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the
     * specified collection's Iterator.  The behavior of this operation is
     * undefined if the specified collection is modified while the operation
     * is in progress.  (This implies that the behavior of this call is
     * undefined if the specified collection is this list, and this
     * list is nonempty.)
     *
     * 添加集合所有元素
     * @param c collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(Collection<? extends E> c) {
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(size + numNew);  // Increments modCount
        System.arraycopy(a, 0, elementData, size, numNew); //将元素复制到最后
        size += numNew;
        return numNew != 0; //如果有复制元素返回true
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in the list in the order that they are returned by the
     * specified collection's iterator.
     * 在特定位置插入元素， 这个这些元素插入的顺序跟iterator有关
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param c collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index); //检查数据是否越界

        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(size + numNew);  // Increments modCount  扩容

        int numMoved = size - index;//移动元素的数量
        if (numMoved > 0)
            System.arraycopy(elementData, index, elementData, index + numNew,
                             numMoved); // 复制后面的元素

        System.arraycopy(a, 0, elementData, index, numNew); //复制插入的元素
        size += numNew;  //修改size长度
        return numNew != 0;
    }

    /**
     * Removes from this list all of the elements whose index is between
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     * Shifts any succeeding elements to the left (reduces their index).
     * This call shortens the list by {@code (toIndex - fromIndex)} elements.
     * (If {@code toIndex==fromIndex}, this operation has no effect.)
     * 移除这个区间的元素[开始索引， 结束索引) （开始索引位置元素会被移除，结束索引不会移除）
     * @throws IndexOutOfBoundsException if {@code fromIndex} or
     *         {@code toIndex} is out of range
     *         ({@code fromIndex < 0 ||
     *          fromIndex >= size() ||
     *          toIndex > size() ||
     *          toIndex < fromIndex})
     */
    protected void removeRange(int fromIndex, int toIndex) {
        modCount++;  //结构性修改，需要自增
        int numMoved = size - toIndex; //
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                         numMoved); //将会toIndex到最后的元素移动前面去

        // clear to let GC do its work
        int newSize = size - (toIndex-fromIndex); //剩下元素的长度
        for (int i = newSize; i < size; i++) {// 最后设置为null
            elementData[i] = null;
        }
        size = newSize;
    }

    /**
     * Checks if the given index is in range.  If not, throws an appropriate
     * runtime exception.  This method does *not* check if the index is
     * negative: It is always used immediately prior to an array access,
     * which throws an ArrayIndexOutOfBoundsException if index is negative.
     * 是判断索引是否越界
     */
    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * A version of rangeCheck used by add and addAll.
     * 添加元素索引检查
     */
    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * Constructs an IndexOutOfBoundsException detail message.
     * Of the many possible refactorings of the error handling code,
     * this "outlining" performs best with both server and client VMs.
     * 打印索引越界异常
     */
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    /**
     * Removes from this list all of its elements that are contained in the
     * specified collection.
     *
     * @param c collection containing elements to be removed from this list
     * @return {@code true} if this list changed as a result of the call
     * @throws ClassCastException if the class of an element of this list
     *         is incompatible with the specified collection
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this list contains a null element and the
     *         specified collection does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>),
     *         or if the specified collection is null
     * @see Collection#contains(Object)
     *
     * 移除集合中包含的所有元素
     */
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c); // 判断对象是否为空
        return batchRemove(c, false); //批量移除
    }

    /**
     * Retains only the elements in this list that are contained in the
     * specified collection.  In other words, removes from this list all
     * of its elements that are not contained in the specified collection.
     *
     * @param c collection containing elements to be retained in this list
     * @return {@code true} if this list changed as a result of the call
     * @throws ClassCastException if the class of an element of this list
     *         is incompatible with the specified collection
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this list contains a null element and the
     *         specified collection does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>),
     *         or if the specified collection is null
     * @see Collection#contains(Object)
     *
     * 保留集合中包含的所有元素
     */
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, true);
    }

    private boolean batchRemove(Collection<?> c, boolean complement) {
        final Object[] elementData = this.elementData; // 获取元素
        int r = 0, w = 0; // r w=0
        boolean modified = false; //默认不修改
        try {
            for (; r < size; r++)//变量当前list对象的元素
                if (c.contains(elementData[r]) == complement) //（complement为false时候list-c 差集, complement为true list与c交集）
                    elementData[w++] = elementData[r];//
        } finally {
            // Preserve behavioral compatibility with AbstractCollection,
            // even if c.contains() throws.
            if (r != size) { // 表示没有读到末尾，读取到末尾就不需要拷贝
                System.arraycopy(elementData, r,
                                 elementData, w,
                                 size - r);
                w += size - r; //更新 这个元素大小
            }
            if (w != size) {
                // clear to let GC do its work
                for (int i = w; i < size; i++)
                    elementData[i] = null;//将后面的元素设置null
                modCount += size - w;// 这里modCount不是加一了
                size = w;// 更新当前集合的大小
                modified = true; //返回true
            }
        }
        return modified;
    }

    /**
     * Save the state of the <tt>ArrayList</tt> instance to a stream (that
     * is, serialize it).
     *
     * 序列化重写，序列化调用这个接口
     * @serialData The length of the array backing the <tt>ArrayList</tt>
     *             instance is emitted (int), followed by all of its elements
     *             (each an <tt>Object</tt>) in the proper order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException{
        // Write out element count, and any hidden stuff
        int expectedModCount = modCount; //在序列化的时候来设置modCount值，其实就是一种乐观锁
        s.defaultWriteObject();

        // Write out size as capacity for behavioural compatibility with clone()
        s.writeInt(size); //写出大小

        // Write out all elements in the proper order.
        for (int i=0; i<size; i++) {// 一个个元素写出
            s.writeObject(elementData[i]);
        }

        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * Reconstitute the <tt>ArrayList</tt> instance from a stream (that is,
     * deserialize it).
     * 反序列化， 重组
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        elementData = EMPTY_ELEMENTDATA; //默认是空数组

        // Read in size, and any hidden stuff
        s.defaultReadObject();

        // Read in capacity
        s.readInt(); // ignored  这是因为在s.defaultReadObject()的时候已经size字段反序列化了

        if (size > 0) { //这个好像是clone，直接用size而不是容量
            // be like clone(), allocate array based upon size not capacity
            ensureCapacityInternal(size); //扩容

            Object[] a = elementData;
            // Read in all elements in the proper order.
            for (int i=0; i<size; i++) {
                a[i] = s.readObject();
            }
        }
    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence), starting at the specified position in the list.
     * The specified index indicates the first element that would be
     * returned by an initial call to {@link ListIterator#next next}.
     * An initial call to {@link ListIterator#previous previous} would
     * return the element with the specified index minus one.
     * 返回对应某个位置List迭代器
     * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public ListIterator<E> listIterator(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: "+index);
        return new ListItr(index);
    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence).
     * 默认从0开始进行迭代
     * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @see #listIterator(int)
     */
    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * <p>The returned iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     * 这个迭代器是快速失败的
     * @return an iterator over the elements in this list in proper sequence
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * An optimized version of AbstractList.Itr 最优版本迭代器
     */
    private class Itr implements Iterator<E> {
        int cursor;       // index of next element to return    游标位置（指向下一个元素）
        int lastRet = -1; // index of last element returned; -1 if no such  （返回最后操作元素索引， -1表示删除或添加、初始化的时候）
        int expectedModCount = modCount; //结构化修改次数

        public boolean hasNext() {
            return cursor != size;
        } // 从头到尾遍历，判断是否等于最大大小

        @SuppressWarnings("unchecked")
        public E next() {
            checkForComodification(); //是否并发修改了
            int i = cursor; // i获取游标的位置
            if (i >= size) //游标超出当前元素最大位置，抛出没有这个元素的异常（NoSuchElementException)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData; // 获取这个元素的数组内容
            if (i >= elementData.length) // 如果i大于最大元素将会抛出并发修改问题（这个瞬间有线程修改这个数组）
                throw new ConcurrentModificationException();
            cursor = i + 1; //游标移动下一个位置
            return (E) elementData[lastRet = i]; //获取到该元素，同时设置lastRet为该索引
        }

        public void remove() {
            if (lastRet < 0) // 判断当前是否有元素需要做进一步操作
                throw new IllegalStateException();
            checkForComodification();//检查并发修改问题

            try {
                ArrayList.this.remove(lastRet);//移除某个元素 （这里就是分装一个壳，具体操作还是通过ArrayList去完成）
                cursor = lastRet; // 游标位置需要重新设置
                lastRet = -1; //这个恢复为-1
                expectedModCount = modCount; //
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        /**
         * 这个函数编程，consumer就是定义一下执行什么操作 x-> {}
         * @param consumer
         */
        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);// 判空
            final int size = ArrayList.this.size; //获取数组大小
            int i = cursor; //获取游标的位置,也就是说并不是从0开始的
            if (i >= size) { // 如果大于size表示超出范围
                return;
            }
            final Object[] elementData = ArrayList.this.elementData; // 获取数组元素
            if (i >= elementData.length) { // 说明有线程修改了
                throw new ConcurrentModificationException();
            }
            while (i != size && modCount == expectedModCount) {//这可能出现并发修改，然后就终止操作
                consumer.accept((E) elementData[i++]);
            }
            // update once at end of iteration to reduce heap write traffic
            cursor = i; //设置游标的位置
            lastRet = i - 1; //设置当前lastRet元素
            checkForComodification(); //检查并发抛出异常
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    /**
     * An optimized version of AbstractList.ListItr
     * 最优版本
     * previous() 和 next() 依次调用，返回同一元素
     */
    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            super();
            cursor = index; // 传入索引
        }

        public boolean hasPrevious() {
            return cursor != 0;
        } // 没有在最开始位置

        public int nextIndex() {
            return cursor;
        } //一个游标的位置

        public int previousIndex() {
            return cursor - 1;
        } //上一个游标的位置

        @SuppressWarnings("unchecked")
        public E previous() { //获取上个元素
            checkForComodification();//检查并发修改
            int i = cursor - 1; //获取上一次元素， 此时这个cursor指向上上个元素
            if (i < 0) //
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;//获取元素
            if (i >= elementData.length) //如果长度超出，说明被其他线程修改了
                throw new ConcurrentModificationException();
            cursor = i;
            return (E) elementData[lastRet = i];//返回元素
        }

        public void set(E e) { //设置元素
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();//检查并发修改

            try {
                ArrayList.this.set(lastRet, e); //修改当前位置的元素
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(E e) { //添加元素
            checkForComodification(); //检查并发

            try {
                int i = cursor; //游标位置
                ArrayList.this.add(i, e); // 当前位置添加元素
                cursor = i + 1; //游标移动下一个位置
                lastRet = -1; //复位
                expectedModCount = modCount; //复位
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Returns a view of the portion of this list between the specified
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.  (If
     * {@code fromIndex} and {@code toIndex} are equal, the returned list is
     * empty.)  The returned list is backed by this list, so non-structural
     * changes in the returned list are reflected in this list, and vice-versa.
     * The returned list supports all of the optional list operations.
     *
     * <p>This method eliminates【消除】 the need for explicit range operations (of
     * the sort that commonly exist for arrays).  Any operation that expects
     * a list can be used as a range operation by passing a subList view
     * instead of a whole list.  For example, the following idiom
     * removes a range of elements from a list:
     * <pre>
     *      list.subList(from, to).clear();
     * </pre>
     * Similar idioms may be constructed for {@link #indexOf(Object)} and
     * {@link #lastIndexOf(Object)}, and all of the algorithms in the
     * {@link Collections} class can be applied to a subList.
     *
     * <p>The semantics of the list returned by this method become undefined if
     * the backing list (i.e., this list) is <i>structurally modified</i> in
     * any way other than via the returned list.  (Structural modifications are
     * those that change the size of this list, or otherwise perturb 【不安】it in such
     * a fashion that iterations in progress may yield incorrect results.)
     * 返回这个列表的视图（子列表），不能并发进行修改
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public List<E> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size); //判断索引是否越界
        return new SubList(this, 0, fromIndex, toIndex);
    }

    static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                               ") > toIndex(" + toIndex + ")");
    }

    private class SubList extends AbstractList<E> implements RandomAccess {
        private final AbstractList<E> parent; //当前对象
        private final int parentOffset; //当前对象数组索引
        private final int offset; //要取索引开始位置
        int size; //

        SubList(AbstractList<E> parent,
                int offset, int fromIndex, int toIndex) {
            this.parent = parent;//当前对象
            this.parentOffset = fromIndex;//开始位置
            this.offset = offset + fromIndex;//总偏移量
            this.size = toIndex - fromIndex; // 元素大小
            this.modCount = ArrayList.this.modCount; //并发判断 记录初始化
        }

        public E set(int index, E e) {
            rangeCheck(index); //判断 索引越界
            checkForComodification();// 检测并发
            E oldValue = ArrayList.this.elementData(offset + index); //获取旧的值
            ArrayList.this.elementData[offset + index] = e;//设置新的值
            return oldValue;
        }

        public E get(int index) {
            rangeCheck(index);//判断索引是否越界
            checkForComodification(); //检查并发
            return ArrayList.this.elementData(offset + index);//获取对应位置的元素
        }

        public int size() { //获取元素大小
            checkForComodification(); //检查并发
            return this.size;
        }

        public void add(int index, E e) {
            rangeCheckForAdd(index);//检查添加索引是否越界
            checkForComodification();//检查并发
            parent.add(parentOffset + index, e); // 添加元素
            this.modCount = parent.modCount; //将ArrayList的mod同步到当前对象
            this.size++;
        }

        public E remove(int index) {//移除元素
            rangeCheck(index);
            checkForComodification();
            E result = parent.remove(parentOffset + index); // 移除元素
            this.modCount = parent.modCount;
            this.size--;
            return result;
        }

        protected void removeRange(int fromIndex, int toIndex) {//移除范围
            checkForComodification();
            parent.removeRange(parentOffset + fromIndex,
                               parentOffset + toIndex);//本质上修改ArrayList
            this.modCount = parent.modCount;
            this.size -= toIndex - fromIndex; //显示元素大小
        }

        public boolean addAll(Collection<? extends E> c) {
            return addAll(this.size, c);//添加元素
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            rangeCheckForAdd(index);
            int cSize = c.size();
            if (cSize==0)
                return false;

            checkForComodification();
            parent.addAll(parentOffset + index, c); //添加元素
            this.modCount = parent.modCount;
            this.size += cSize;
            return true;
        }

        public Iterator<E> iterator() {
            return listIterator();
        }

        public ListIterator<E> listIterator(final int index) { //list迭代
            checkForComodification();
            rangeCheckForAdd(index);
            final int offset = this.offset;//偏移量

            return new ListIterator<E>() {
                int cursor = index;   //遍历位置
                int lastRet = -1;
                int expectedModCount = ArrayList.this.modCount;

                public boolean hasNext() {
                    return cursor != SubList.this.size; //最后位置
                }

                @SuppressWarnings("unchecked")
                public E next() {
                    checkForComodification();
                    int i = cursor; //游标位置
                    if (i >= SubList.this.size) // 判断是否在最后位置
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData; //数组元素
                    if (offset + i >= elementData.length) //检查是否并发修改
                        throw new ConcurrentModificationException();
                    cursor = i + 1; // 游标+1
                    return (E) elementData[offset + (lastRet = i)]; //获取元素
                }

                public boolean hasPrevious() {
                    return cursor != 0;
                } //是否

                @SuppressWarnings("unchecked")
                public E previous() { //上一次
                    checkForComodification();
                    int i = cursor - 1; //
                    if (i < 0)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData; //获取元素
                    if (offset + i >= elementData.length) // 检查是否并发修改
                        throw new ConcurrentModificationException();
                    cursor = i;// 设置游标
                    return (E) elementData[offset + (lastRet = i)];
                }

                @SuppressWarnings("unchecked")
                public void forEachRemaining(Consumer<? super E> consumer) {
                    Objects.requireNonNull(consumer);
                    final int size = SubList.this.size; // 当前子list表大小
                    int i = cursor; //游标位置
                    if (i >= size) {//大于子list最大位置
                        return;
                    }
                    final Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length) { //检查并发
                        throw new ConcurrentModificationException();
                    }
                    while (i != size && modCount == expectedModCount) { //检查并发
                        consumer.accept((E) elementData[offset + (i++)]);
                    }
                    // update once at end of iteration to reduce heap write traffic
                    lastRet = cursor = i; // 设置游标位置
                    checkForComodification(); //检查并发（防止上面并发发生）
                }

                public int nextIndex() {
                    return cursor;
                } //下一个索引位置

                public int previousIndex() {
                    return cursor - 1;
                } // 上一个索引位置

                public void remove() { //移除元素
                    if (lastRet < 0) //如果小于0，表示
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        SubList.this.remove(lastRet); // 移除该元素
                        cursor = lastRet; //设置游标
                        lastRet = -1; //复位
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void set(E e) {
                    if (lastRet < 0) //设置元素
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        ArrayList.this.set(offset + lastRet, e); //设置元素
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void add(E e) { //增加元素
                    checkForComodification();

                    try {
                        int i = cursor;
                        SubList.this.add(i, e);
                        cursor = i + 1;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                final void checkForComodification() {
                    if (expectedModCount != ArrayList.this.modCount)
                        throw new ConcurrentModificationException();
                }
            };
        }

        public List<E> subList(int fromIndex, int toIndex) { // 子List
            subListRangeCheck(fromIndex, toIndex, size); //检查索引越界
            return new SubList(this, offset, fromIndex, toIndex);
        }

        private void rangeCheck(int index) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private void rangeCheckForAdd(int index) {
            if (index < 0 || index > this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private String outOfBoundsMsg(int index) {
            return "Index: "+index+", Size: "+this.size;
        }

        private void checkForComodification() {
            if (ArrayList.this.modCount != this.modCount)
                throw new ConcurrentModificationException();
        }

        public Spliterator<E> spliterator() { // 一分为二迭代
            checkForComodification();
            return new ArrayListSpliterator<E>(ArrayList.this, offset,
                                               offset + this.size, this.modCount);
        }
    }

    /**
     * jdk 1.8
     * @param action
     */
    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        final int expectedModCount = modCount;
        @SuppressWarnings("unchecked")
        final E[] elementData = (E[]) this.elementData;// 数组元素
        final int size = this.size; //大小
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            action.accept(elementData[i]);
        }
        if (modCount != expectedModCount) {  //是否出现并发修改
            throw new ConcurrentModificationException();
        }
    }

    /**
     * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
     * and <em>fail-fast</em> {@link Spliterator} over the elements in this
     * list.
     *  创建一个延迟绑定和快速失败的迭代器
     * <p>The {@code Spliterator} reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, and {@link Spliterator#ORDERED}.
     * Overriding implementations should document the reporting of additional
     * characteristic values.
     *
     * @return a {@code Spliterator} over the elements in this list
     * @since 1.8
     */
    @Override
    public Spliterator<E> spliterator() {
        return new ArrayListSpliterator<>(this, 0, -1, 0);
    }

    /** Index-based split-by-two, lazily initialized Spliterator */    //这个将List一分为二
    static final class ArrayListSpliterator<E> implements Spliterator<E> {

        /*
         * If ArrayLists were immutable, or structurally immutable (no
         * adds, removes, etc), we could implement their spliterators
         * with Arrays.spliterator. Instead we detect as much
         * interference during traversal as practical without  //如果ArrayList是不变的类的话可以使用 Arrays.spliterator,减少检查并发而降低性能
         * sacrificing much performance. We rely primarily on
         * modCounts. These are not guaranteed to detect concurrency
         * violations, and are sometimes overly conservative【保守】 about
         * within-thread interference【干扰】, but detect enough problems to // 但是这种检查不能完全保证，有时候感觉检查并发是比较保守做法，但是这种做法却在实践中是有意义的
         * be worthwhile in practice. To carry this out, we (1) lazily
         * initialize fence and expectedModCount until the latest
         * point that we need to commit to the state we are checking// 为了实现这个目标，我们进行懒加载初始化fence和expectedModeCount,直到提交到这个状态才去检查
         * against; thus improving precision.  (This doesn't apply to
         * SubLists, that create spliterators with current non-lazy//但是这个方法并没有在SubList进行实施，因为当前没有懒加载的值
         * values).  (2) We perform only a single
         * ConcurrentModificationException check at the end of forEach //我们统一在最后进行一次并发检查
         * (the most performance-sensitive method). When using forEach
         * (as opposed to iterators), we can normally only detect
         * interference after actions, not before. Further//一般我之后在操作之后进行检查,你要先犯罪之后才能抓你
         * CME-triggering checks apply to all other possible //
         * violations of assumptions for example null or too-small
         * elementData array given its size(), that could only have
         * occurred due to interference.  This allows the inner loop
         * of forEach to run without any further checks, and //这里允许内部遍历时候不进行更深层检查
         * simplifies lambda-resolution. While this does entail 【牵扯】 a
         * number of checks, note that in the common case of
         * list.stream().forEach(a), no checks or other computation //list.stream().forEach(a) 不会检测
         * occur anywhere other than inside forEach itself.  The other
         * less-often-used methods cannot take advantage of most of
         * these streamlinings.
         */

        private final ArrayList<E> list;  //定义一个final的ArrayList
        private int index; // current index, modified on advance/split  当前索引
        private int fence; // -1 until used; then one past last index  访问到最后的元素，（边界）
        private int expectedModCount; // initialized when fence set  初始化修改结构次数

        /** Create new spliterator covering the given  range */
        ArrayListSpliterator(ArrayList<E> list, int origin, int fence,
                             int expectedModCount) {
            this.list = list; // OK if null unless traversed【遍历】 直到为null
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        /**
         * 返回元素大小
         * @return
         */
        private int getFence() { // initialize fence to size on first use 第一次使用的时候初始化边界到元素大小
            int hi; // (a specialized variant appears in method forEach) //一个局部变量用于遍历使用
            ArrayList<E> lst;
            if ((hi = fence) < 0) {// 表示初始化时候时候
                if ((lst = list) == null)     //List为null，表示没有元素
                    hi = fence = 0; //那么fence就是0
                else {
                    expectedModCount = lst.modCount; // 初始化modCount计数器
                    hi = fence = lst.size; //fence等于元素的大小
                }
            }
            return hi;
        }

        /**
         * 尝试将list一分为二，获取前半部分, 他只是改变索引
         * @return
         */
        public ArrayListSpliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1; // 相当于mid = (当前索引+size）/2
            return (lo >= mid) ? null : // divide range in half unless too small  //表示不足以分为两份
                new ArrayListSpliterator<E>(list, lo, index = mid,
                                            expectedModCount); //这里它取的前半部分， 同时他索引变成中间位置
        }

        /**
         * 这里与forEachRemaining不同地方就是这里有返回值，操作是否成功
         * @param action
         * @return
         */
        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null) //定一下操作
                throw new NullPointerException(); //抛出异常
            int hi = getFence(), i = index; //获取边界和当前索引位置
            if (i < hi) {//当前索引位置小于元素大小(边界更合适一点
                index = i + 1; //索引总是指向下一个
                @SuppressWarnings("unchecked") E e = (E)list.elementData[i];
                action.accept(e);
                if (list.modCount != expectedModCount) //并发性检查
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // hoist【起重机】 accesses and checks from loop
            ArrayList<E> lst; Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((lst = list) != null && (a = lst.elementData) != null) {//lst是当前List， a为当前数组元素（它是确定当前是有元素的，否则抛出异常）
                if ((hi = fence) < 0) { //这是初始化时候才会小于0
                    mc = lst.modCount;
                    hi = lst.size;
                }
                else
                    mc = expectedModCount; //定义expectedModCount 防止并发修改
                if ((i = index) >= 0 && (index = hi) <= a.length) { //其实从当前元素开始遍历，这里index变成size
                    for (; i < hi; ++i) {//遍历这个list
                        @SuppressWarnings("unchecked") E e = (E) a[i];
                        action.accept(e);
                    }
                    if (lst.modCount == mc) //如果相等，说中途没有线程进行结构修改
                        return;
                }
            }
            throw new ConcurrentModificationException();
        }

        public long estimateSize() { //获取剩余没有遍历的元素
            return (long) (getFence() - index);
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED; //描述iterator特性， 有序，大小，子list
        }
    }

    /**
     * 移除元素
     * @param filter
     * @return
     */
    @Override
    public boolean removeIf(Predicate<? super E> filter) { //移除元素
        Objects.requireNonNull(filter); //判断是否为空
        // figure out which elements are to be removed
        // any exception thrown from the filter predicate at this stage
        // will leave the collection unmodified    计算那个元素将会被移除，如果当前集合是不能修改将会抛出异常
        int removeCount = 0; //移除数量
        final BitSet removeSet = new BitSet(size); //移除set 标记用的
        final int expectedModCount = modCount; //并发检测的计数器
        final int size = this.size; //当前数据的大小
        for (int i=0; modCount == expectedModCount && i < size; i++) {//如果并发修改之后终止遍历
            @SuppressWarnings("unchecked")
            final E element = (E) elementData[i];
            if (filter.test(element)) {// 判断当前元素是否满足要求
                removeSet.set(i); //将要移除索引放入BitSet中
                removeCount++; //移除计数
            }
        }
        if (modCount != expectedModCount) { //判断是否并发修改了，如果修改了终止操作
            throw new ConcurrentModificationException();
        }

        // shift surviving elements left over the spaces left by removed elements 移动存在元素，然后移除后面元素和清除空间
        final boolean anyToRemove = removeCount > 0;
        if (anyToRemove) { //有元素需要移除
            final int newSize = size - removeCount; //新List长度
            for (int i=0, j=0; (i < size) && (j < newSize); i++, j++) {
                i = removeSet.nextClearBit(i); //获取下一个没有被标记清除元素
                elementData[j] = elementData[i];// 将改元素复制到新的数组中
            }
            for (int k=newSize; k < size; k++) {// 并把后面的元素进行置null
                elementData[k] = null;  // Let gc do its work
            }
            this.size = newSize;//新元素大小
            if (modCount != expectedModCount) { //检查并发修改问题
                throw new ConcurrentModificationException();
            }
            modCount++;//作为整的修改一次
        }

        return anyToRemove; //返回产生移除
    }

    /**
     * 替换需要替换的元素， 二元操作符
     * @param operator
     */
    @Override
    @SuppressWarnings("unchecked")
    public void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator); //判断对象是否为null
        final int expectedModCount = modCount; //初始化计数器
        final int size = this.size; //当前list的大小
        for (int i=0; modCount == expectedModCount && i < size; i++) { // 并发修改之后终止操作
            elementData[i] = operator.apply((E) elementData[i]); // 进行操作
        }
        if (modCount != expectedModCount) { //检查并发问题
            throw new ConcurrentModificationException();
        }
        modCount++;//计数增加+1
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c) { //排序
        final int expectedModCount = modCount; //并发检查
        Arrays.sort((E[]) elementData, 0, size, c);//直接用Arrays.sort排序
        if (modCount != expectedModCount) { //检查并发问题
            throw new ConcurrentModificationException();
        }
        modCount++;
    }
}
