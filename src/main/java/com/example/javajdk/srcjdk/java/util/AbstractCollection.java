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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * This class provides a skeletal(骨骼) implementation of the <tt>Collection</tt>
 * interface, to minimize the effort required to implement this interface. <p>
 * 这个类提供实现Collection接口最小的工作量的一个骨架。
 *
 * To implement an unmodifiable【无法修改的】 collection, the programmer needs only to
 * extend this class and provide implementations for the <tt>iterator</tt> and
 * <tt>size</tt> methods.  (The iterator returned by the <tt>iterator</tt>
 * method must implement <tt>hasNext</tt> and <tt>next</tt>.)<p>
 * 为了实现一个无法修改的集合类，程序员只需扩展这个类，然后提供实现iterator、size的方法（
 * iterator必须实现hasNext和next方法
 * ）
 *
 * To implement a modifiable collection, the programmer must additionally
 * override this class's <tt>add</tt> method (which otherwise throws an
 * <tt>UnsupportedOperationException</tt>), and the iterator returned by the
 * <tt>iterator</tt> method must additionally implement its <tt>remove</tt>
 * method.<p>
 * 为了实现一个可以修改的集合，程序必须重写这个类add的方法，实现remove方法
 *
 * The programmer should generally provide a void (no argument) and
 * <tt>Collection</tt> constructor, as per the recommendation in the
 * <tt>Collection</tt> interface specification.<p>
 * 程序员应该生成提供一个无参数构造函数
 *
 * The documentation for each non-abstract method in this class describes its
 * implementation in detail.  Each of these methods may be overridden if
 * the collection being implemented admits a more efficient implementation.<p>
 *  如果你有更好实现方法可以重写这里实现
 *
 * This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 *  这个类提供实现Collection接口最小的工作量的一个骨架。 为了实现一个无法修改的集合类，程序员只需扩展这个类，然后提供实现iterator、size的方法（
 * iterator必须实现hasNext和next方法），为了实现一个可以修改的集合，程序必须重写这个类add的方法，实现remove方法，程序员应该生成提供一个无参数构造函数，
 * 如果你有更好实现方法可以重写这里实现
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see Collection
 * @since 1.2
 */

public abstract class AbstractCollection<E> implements Collection<E> {
    /**
     * Sole constructor.  (For invocation by subclass constructors, typically
     * implicit.)
     * 子类调用，无参数构造方法
     */
    protected AbstractCollection() {
    }

    // Query Operations

    /**
     * Returns an iterator over the elements contained in this collection.
     * 返回一个可以遍历这个集合的迭代器
     *
     * @return an iterator over the elements contained in this collection
     */
    public abstract Iterator<E> iterator();

    /**
     * 集合大小
     * @return
     */
    public abstract int size();

    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns <tt>size() == 0</tt>.
     * size为0表示空集合
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation iterates over the elements in the collection,
     * checking each element in turn for equality with the specified element.
     *
     * 迭代集合中的元素，然后判断是否与传入的对象相等
     * null 对象相等
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean contains(Object o) {
        Iterator<E> it = iterator();
        if (o==null) {
            while (it.hasNext())
                if (it.next()==null)
                    return true;
        } else {
            while (it.hasNext())
                if (o.equals(it.next()))
                    return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns an array containing all the elements
     * returned by this collection's iterator, in the same order, stored in
     * consecutive【连续的】 elements of the array, starting with index {@code 0}.
     * The length of the returned array is equal to the number of elements
     * returned by the iterator, even if the size of this collection changes
     * during iteration, as might happen if the collection permits
     * concurrent modification during iteration.  The {@code size} method is
     * called only as an optimization hint; the correct result is returned
     * even if the iterator returns a different number of elements.
     *
     * 将元素变成数组形式，有可能出现并发修改的情况
     * <p>This method is equivalent to:
     *
     *  <pre> {@code
     * List<E> list = new ArrayList<E>(size());
     * for (E e : this)
     *     list.add(e);
     * return list.toArray();
     * }</pre>
     */
    public Object[] toArray() {
        // Estimate size of array; be prepared to see more or fewer elements
        Object[] r = new Object[size()];  //创建一个数组对象
        Iterator<E> it = iterator();   //获取迭代器
        for (int i = 0; i < r.length; i++) {   // 迭代器元素可能小于集合元素大小，具体看iterator的实现（也可能多于这个集合元素的大小）
            if (! it.hasNext()) // fewer elements than expected
                return Arrays.copyOf(r, i); //
            r[i] = it.next();
        }
        return it.hasNext() ? finishToArray(r, it) : r; //超出的元素继续迭代
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns an array containing all the elements
     * returned by this collection's iterator in the same order, stored in
     * consecutive elements of the array, starting with index {@code 0}.
     * If the number of elements returned by the iterator is too large to
     * fit into the specified array, then the elements are returned in a
     * newly allocated array with length equal to the number of elements
     * returned by the iterator, even if the size of this collection
     * changes during iteration, as might happen if the collection permits
     * concurrent modification during iteration.  The {@code size} method is
     * called only as an optimization hint; the correct result is returned
     * even if the iterator returns a different number of elements.
     * 将元素变成数组形式，有可能出现并发修改的情况
     * <p>This method is equivalent to:
     *
     *  <pre> {@code
     * List<E> list = new ArrayList<E>(size());
     * for (E e : this)
     *     list.add(e);
     * return list.toArray(a);
     * }</pre>
     *
     * @throws ArrayStoreException  {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        // Estimate size of array; be prepared to see more or fewer elements
        int size = size();
        T[] r = a.length >= size ? a :
                  (T[])java.lang.reflect.Array
                  .newInstance(a.getClass().getComponentType(), size); //利用反射创建数组 ，传入数组是有大小大于size()大小就不需要创建数组 了
        Iterator<E> it = iterator();

        for (int i = 0; i < r.length; i++) { // 遍历数组长度（集合长度或者传入数组的长度）
            if (! it.hasNext()) { // fewer elements than expected  元素小于数组本身分配长度
                if (a == r) {//表示传入数组（不是内部反射创建的数组）
                    r[i] = null; // null-terminate //作为结束标志
                } else if (a.length < i) { // 传入的数组小于i（表示传入的数组容量不够）
                    return Arrays.copyOf(r, i); // 反射创建的数组缩容
                } else { //表示传入数组长度够用，将反射创建的元素的拷贝到a数组中
                    System.arraycopy(r, 0, a, 0, i);
                    if (a.length > i) { //如果a的长度还有大于i， 设置结束符
                        a[i] = null;
                    }
                }
                return a;
            }
            r[i] = (T)it.next();
        }
        // more elements than expected
        return it.hasNext() ? finishToArray(r, it) : r;  //遍历数组的元素多余size（）函数返回的值
    }

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * Reallocates the array being used within toArray when the iterator
     * returned more elements than expected, and finishes filling it from
     * the iterator.
     * 重新分配空间来完成多余集合元素大小的size元素
     * @param r the array, replete（充满的） with previously stored elements 前面的数组已经填满了
     * @param it the in-progress iterator over this collection （集合迭代器）
     * @return array containing the elements in the given array, plus any
     *         further elements returned by the iterator, trimmed to size 返回所有迭代的元素最后去除多余数组的空间
     */
    @SuppressWarnings("unchecked")
    private static <T> T[] finishToArray(T[] r, Iterator<?> it) {
        int i = r.length;   // 获取数组的长度  i 等于当前r的长度
        while (it.hasNext()) { //直到取完所有元素
            int cap = r.length;  // cap 等于当前数组的长度
            if (i == cap) { // 如果 i== cap  需要扩容
                int newCap = cap + (cap >> 1) + 1; //扩容 cap+1/2cap + 1)
                // overflow-conscious code  //溢出危险
                if (newCap - MAX_ARRAY_SIZE > 0)  // 判断是否大于数组长度最大值
                    newCap = hugeCapacity(cap + 1);  // 为啥加1？(最后一次扩展不会溢出，刚刚加上1就会溢出)
                r = Arrays.copyOf(r, newCap); // 数组元素扩容
            }
            r[i++] = (T)it.next(); //获取下一个元素
        }
        // trim if overallocated
        return (i == r.length) ? r : Arrays.copyOf(r, i);   //去掉多余数组的空间
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError
                ("Required array size too large");
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

    // Modification Operations

    /**
     * {@inheritDoc}
     *
     * <p>This implementation always throws an
     * <tt>UnsupportedOperationException</tt>.
     * 不支持添加
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IllegalStateException         {@inheritDoc}
     */
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation iterates over the collection looking for the
     * specified element.  If it finds the element, it removes the element
     * from the collection using the iterator's remove method.
     *
     * <p>Note that this implementation throws an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by this
     * collection's iterator method does not implement the <tt>remove</tt>
     * method and this collection contains the specified object.
     *
     * 如果没有实现iterator的remove的方法，将会抛出UnsupportedOperationException
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     */
    public boolean remove(Object o) {
        Iterator<E> it = iterator();
        if (o==null) {
            while (it.hasNext()) {
                if (it.next()==null) {
                    it.remove();
                    return true;
                }
            }
        } else {
            while (it.hasNext()) {
                if (o.equals(it.next())) {
                    it.remove();
                    return true;
                }
            }
        }
        return false;
    }


    // Bulk Operations

    /**
     * {@inheritDoc}
     *
     * <p>This implementation iterates over the specified collection,
     * checking each element returned by the iterator in turn to see
     * if it's contained in this collection.  If all elements are so
     * contained <tt>true</tt> is returned, otherwise <tt>false</tt>.
     *  判断集合元素是否存在当前集合中
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @see #contains(Object)
     */
    public boolean containsAll(Collection<?> c) {
        for (Object e : c)
            if (!contains(e))
                return false;
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation iterates over the specified collection, and adds
     * each object returned by the iterator to this collection, in turn.
     *
     * <p>Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> unless <tt>add</tt> is
     * overridden (assuming the specified collection is non-empty).
     * 必须实现add方法
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IllegalStateException         {@inheritDoc}
     *
     * @see #add(Object)
     */
    public boolean addAll(Collection<? extends E> c) {
        boolean modified = false;
        for (E e : c)
            if (add(e))
                modified = true;
        return modified;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation iterates over this collection, checking each
     * element returned by the iterator in turn to see if it's contained
     * in the specified collection.  If it's so contained, it's removed from
     * this collection with the iterator's <tt>remove</tt> method.
     *
     * <p>Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by the
     * <tt>iterator</tt> method does not implement the <tt>remove</tt> method
     * and this collection contains one or more elements in common with the
     * specified collection.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);  //判断对象是否为空
        boolean modified = false;
        Iterator<?> it = iterator();
        while (it.hasNext()) { //只要一个修改成功，modified就成功
            if (c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation iterates over this collection, checking each
     * element returned by the iterator in turn to see if it's contained
     * in the specified collection.  If it's not so contained, it's removed
     * from this collection with the iterator's <tt>remove</tt> method.
     *
     * <p>Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by the
     * <tt>iterator</tt> method does not implement the <tt>remove</tt> method
     * and this collection contains one or more elements not present in the
     * specified collection.
     * 这个方法与 removeAll()相反
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation iterates over this collection, removing each
     * element using the <tt>Iterator.remove</tt> operation.  Most
     * implementations will probably choose to override this method for
     * efficiency.
     *
     * <p>Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by this
     * collection's <tt>iterator</tt> method does not implement the
     * <tt>remove</tt> method and this collection is non-empty.
     * 清除所有元素
     * @throws UnsupportedOperationException {@inheritDoc}
     */
    public void clear() {
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }


    //  String conversion

    /**
     * Returns a string representation of this collection.  The string
     * representation consists of a list of the collection's elements in the
     * order they are returned by its iterator, enclosed in square brackets
     * (<tt>"[]"</tt>).  Adjacent elements are separated by the characters
     * <tt>", "</tt> (comma and space).  Elements are converted to strings as
     * by {@link String#valueOf(Object)}.
     * 空 []     [元素1，元素2，元素3]
     * @return a string representation of this collection
     */
    public String toString() {
        Iterator<E> it = iterator();
        if (! it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            E e = it.next();
            sb.append(e == this ? "(this Collection)" : e);
            if (! it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }

}
