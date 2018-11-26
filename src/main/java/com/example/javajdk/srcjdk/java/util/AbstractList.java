/*
 * Copyright (c) 1997, 2012, Oracle and/or its affiliates. All rights reserved.
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

/**
 * This class provides a skeletal implementation of the {@link List}
 * interface to minimize the effort required to implement this interface
 * backed by a "random access" data store (such as an array).  For sequential
 * access data (such as a linked list), {@link AbstractSequentialList} should
 * be used in preference to this class.
 * 实现List简单的抽象类
 *
 * <p>To implement an unmodifiable list, the programmer needs only to extend
 * this class and provide implementations for the {@link #get(int)} and
 * {@link List#size() size()} methods.
 * 实现一个不变的类只需要继承该类和实现get(int) 和 size()方法
 * <p>To implement a modifiable list, the programmer must additionally
 * override the {@link #set(int, Object) set(int, E)} method (which otherwise
 * throws an {@code UnsupportedOperationException}).  If the list is
 * variable-size the programmer must additionally override the
 * {@link #add(int, Object) add(int, E)} and {@link #remove(int)} methods.
 * 实现一个可变的方法 需要添加额外set(int, Object) set(int, E)的方法，如果长度可变需要重写add(int, Object),add(int ,E)和是remove方法
 *
 * <p>The programmer should generally provide a void (no argument) and collection
 * constructor, as per the recommendation in the {@link Collection} interface
 * specification.
 * 提供空的构造的方法
 *
 * <p>Unlike the other abstract collection implementations, the programmer does
 * <i>not</i> have to provide an iterator implementation; the iterator and
 * list iterator are implemented by this class, on top of the "random access"
 * methods:
 * {@link #get(int)},
 * {@link #set(int, Object) set(int, E)},
 * {@link #add(int, Object) add(int, E)} and
 * {@link #remove(int)}.
 * 程序不需要实现这个iterator方法
 * <p>The documentation for each non-abstract method in this class describes its
 * implementation in detail.  Each of these methods may be overridden if the
 * collection being implemented admits a more efficient implementation.
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * 实现List简单的抽象类
 * 实现一个不变的类只需要继承该类和实现get(int) 和 size()方法
 * 实现一个可变的方法 需要添加额外set(int, Object) set(int, E)的方法，如果长度可变需要重写add(int, Object),add(int ,E)和是remove方法
 * 提供空的构造的方法
 * 程序不需要实现这个iterator方法 (迭代器这个都实现了）
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @since 1.2
 */

public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E> {
    /**
     * Sole constructor.  (For invocation by subclass constructors, typically
     * implicit.)
     * 被子类调用的无参数的构造方法
     */
    protected AbstractList() {
    }

    /**
     * Appends the specified element to the end of this list (optional
     * operation).
     * 将元素添加元素的后面
     * <p>Lists that support this operation may place limitations on what
     * elements may be added to this list.  In particular, some
     * lists will refuse to add null elements, and others will impose【强加】
     * restrictions on the type of elements that may be added.  List
     * classes should clearly specify in their documentation any restrictions
     * on what elements may be added.
     *
     * <p>This implementation calls {@code add(size(), e)}.
     *
     * <p>Note that this implementation throws an
     * {@code UnsupportedOperationException} unless
     * {@link #add(int, Object) add(int, E)} is overridden.
     *  需要重写 add() 方法
     * @param e element to be appended to this list
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws UnsupportedOperationException if the {@code add} operation
     *         is not supported by this list
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this list
     * @throws NullPointerException if the specified element is null and this
     *         list does not permit null elements
     * @throws IllegalArgumentException if some property of this element
     *         prevents it from being added to this list
     */
    public boolean add(E e) {
        add(size(), e);
        return true;
    }

    /**
     * {@inheritDoc}
     * 数组越界
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    abstract public E get(int index);

    /**
     * {@inheritDoc}
     *
     * <p>This implementation always throws an
     * {@code UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */
    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation always throws an
     * {@code UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation always throws an
     * {@code UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */
    public E remove(int index) {
        throw new UnsupportedOperationException();
    }


    // Search Operations

    /**
     * {@inheritDoc}
     *
     * <p>This implementation first gets a list iterator (with
     * {@code listIterator()}).  Then, it iterates over the list until the
     * specified element is found or the end of the list is reached.
     *  实现 ListIterator()方法
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public int indexOf(Object o) {
        ListIterator<E> it = listIterator(); //获取迭代器
        if (o==null) {  // 如果对象为空
            while (it.hasNext())
                if (it.next()==null)
                    return it.previousIndex(); //上一个索引
        } else {
            while (it.hasNext())
                if (o.equals(it.next()))
                    return it.previousIndex();
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation first gets a list iterator that points to the end
     * of the list (with {@code listIterator(size())}).  Then, it iterates
     * backwards over the list until the specified element is found, or the
     * beginning of the list is reached.
     *
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public int lastIndexOf(Object o) {
        ListIterator<E> it = listIterator(size()); //从后面往前迭代
        if (o==null) {
            while (it.hasPrevious())
                if (it.previous()==null)
                    return it.nextIndex();//网
        } else {
            while (it.hasPrevious())
                if (o.equals(it.previous()))
                    return it.nextIndex();
        }
        return -1;
    }


    // Bulk Operations

    /**
     * Removes all of the elements from this list (optional operation).
     * The list will be empty after this call returns.
     *
     * <p>This implementation calls {@code removeRange(0, size())}.
     *
     * <p>Note that this implementation throws an
     * {@code UnsupportedOperationException} unless {@code remove(int
     * index)} or {@code removeRange(int fromIndex, int toIndex)} is
     * overridden.
     *  清除元素
     * @throws UnsupportedOperationException if the {@code clear} operation
     *         is not supported by this list
     */
    public void clear() {
        removeRange(0, size());
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation gets an iterator over the specified collection
     * and iterates over it, inserting the elements obtained from the
     * iterator into this list at the appropriate position, one at a time,
     * using {@code add(int, E)}.
     * Many implementations will override this method for efficiency.
     * 这个方法需要重写 add(int , Object) add(int, E)
     * <p>Note that this implementation throws an
     * {@code UnsupportedOperationException} unless
     * {@link #add(int, Object) add(int, E)} is overridden.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index); //添加位置极端情况过滤
        boolean modified = false;
        for (E e : c) {
            add(index++, e); //添加元素
            modified = true;
        }
        return modified;
    }


    // Iterators

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     * 返回合适迭代器
     * <p>This implementation returns a straightforward implementation of the
     * iterator interface, relying on the backing list's {@code size()},
     * {@code get(int)}, and {@code remove(int)} methods.
     *  直接是iterator 直接实现接口， size(), get(int), remove(int)
     *
     * <p>Note that the iterator returned by this method will throw an
     * {@link UnsupportedOperationException} in response to its
     * {@code remove} method unless the list's {@code remove(int)} method is
     * overridden.
     * 注意这个迭代器返回这个方法将会抛出一个，除非重写remove
     *
     * <p>This implementation can be made to throw runtime exceptions in the
     * face of concurrent modification, as described in the specification
     * for the (protected) {@link #modCount} field.
     * 如果并发修改将会抛出， modCount
     *
     * @return an iterator over the elements in this list in proper sequence
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns {@code listIterator(0)}.
     * 返回ListeIterator迭代器 （从0开始）
     *
     * @see #listIterator(int)
     */
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns a straightforward implementation of the
     * {@code ListIterator} interface that extends the implementation of the
     * {@code Iterator} interface returned by the {@code iterator()} method.
     * The {@code ListIterator} implementation relies on the backing list's
     * {@code get(int)}, {@code set(int, E)}, {@code add(int, E)}
     * and {@code remove(int)} methods.
     * 这个迭代器方法需要依赖 get(int)/set(int, E)/ add(int, E)的方法
     *
     * <p>Note that the list iterator returned by this implementation will
     * throw an {@link UnsupportedOperationException} in response to its
     * {@code remove}, {@code set} and {@code add} methods unless the
     * list's {@code remove(int)}, {@code set(int, E)}, and
     * {@code add(int, E)} methods are overridden.
     * list 迭代器必须要是实现 remove(int), set(int, E), add(int E)
     *
     * <p>This implementation can be made to throw runtime exceptions in the
     * face of concurrent modification, as described in the specification for
     * the (protected) {@link #modCount} field.
     * 如果并发修改将会抛出异常， 需要通过modCount控制
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     *
     *
     * ===================================================
     * 这个迭代器方法需要依赖 get(int)/set(int, E)/ add(int, E)的方法
     * list 迭代器必须要是实现 remove(int), set(int, E), add(int E)
     * 如果并发修改将会抛出异常， 需要通过modCount控制
     *
     */
    public ListIterator<E> listIterator(final int index) {
        rangeCheckForAdd(index); //检查迭代边界

        return new ListItr(index); //创建私有的内部类
    }

    /**
     * Itr私有类
     */
    private class Itr implements Iterator<E> {  //它实现了Iterator方法
        /**
         * Index of element to be returned by subsequent call to next.
         * 游标，下个元素
         */
        int cursor = 0;

        /**
         * Index of element returned by most recent call to next or
         * previous.  Reset to -1 if this element is deleted by a call
         * to remove.
         * 返回最近调用索引， -1表示该元素被删除
         */
        int lastRet = -1;

        /**
         * 希望并发修改数
         * The modCount value that the iterator believes that the backing
         * List should have.  If this expectation is violated, the iterator
         * has detected concurrent modification.
         */
        int expectedModCount = modCount;

        public boolean hasNext() { //判断是否有下一个，只要判断游标是否等于size()(这里游标会不会大于 size呢？）
            return cursor != size();
        }

        public E next() { // 下一个元素
            checkForComodification(); //检查是否并发修改
            try {
                int i = cursor; //设置游标为i
                E next = get(i); //设置下个元素
                lastRet = i;   // 设置返回值
                cursor = i + 1; //游标+1
                return next;
            } catch (IndexOutOfBoundsException e) { //越界了
                checkForComodification(); //检查是否是线程多了
                throw new NoSuchElementException(); //如果不是就是没有这个元素
            }
        }

        public void remove() { //移除元素
            if (lastRet < 0) // 说明元素被移除了参数状态不对
                throw new IllegalStateException();
            checkForComodification(); //检查并发修改情况

            try {
                AbstractList.this.remove(lastRet); // 移除最近的元素
                if (lastRet < cursor) //如果游标大于当前最近元素的索引
                    cursor--; //游标减一
                lastRet = -1; // 设置最近元素为-1表示已经删除了
                expectedModCount = modCount; //expectModCount复位成modCOunt
            } catch (IndexOutOfBoundsException e) { // 如果数组越界表示并发修改异常
                throw new ConcurrentModificationException();
            }
        }

        final void checkForComodification() {//检查并发 expectedModCount的区别？
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    private class ListItr extends Itr implements ListIterator<E> { // ListItr实现了ListIterator迭代器
        ListItr(int index) { //  初始化游标的位置
            cursor = index;
        }

        public boolean hasPrevious() {
            return cursor != 0;
        } //表示不是第一个（是否有上一个元素）

        public E previous() { //获取先前的元素
            checkForComodification(); //检查并发问题
            try {
                int i = cursor - 1; //向前移动游标
                E previous = get(i); //获取前面的元素
                lastRet = cursor = i; //这个游标和最近值的索引相同
                return previous;
            } catch (IndexOutOfBoundsException e) { //索引越界， 是否为并发修改的情况
                checkForComodification();
                throw new NoSuchElementException(); //如果不是就是改元素不存在
            }
        }

        public int nextIndex() {
            return cursor;
        } //获取下个索引 ，也就是当前游标的位置

        public int previousIndex() {
            return cursor-1;
        }//获取先前的索引

        public void set(E e) { //设置值
            if (lastRet < 0) // 如果lastRet小于0，表示删除了，
                throw new IllegalStateException(); //状态不对
            checkForComodification(); //检查并发修改情况

            try {
                AbstractList.this.set(lastRet, e); //在当前元素设置值
                expectedModCount = modCount; //这个值什么修改呢？
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(E e) { //添加元素
            checkForComodification(); //检查修改情况

            try {
                int i = cursor; //设置游标的位置为
                AbstractList.this.add(i, e); //在游标位置添加元素
                lastRet = -1; //表示当前元素被修改了
                cursor = i + 1; //设置游标为+1
                expectedModCount = modCount; //复位（其实他一直在保护lastRet如果不小于0，他被其他线程改变了）
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns a list that subclasses
     * {@code AbstractList}.  The subclass stores, in private fields, the
     * offset of the subList within the backing list, the size of the subList
     * (which can change over its lifetime), and the expected
     * {@code modCount} value of the backing list.  There are two variants
     * of the subclass, one of which implements {@code RandomAccess}.
     * If this list implements {@code RandomAccess} the returned list will
     * be an instance of the subclass that implements {@code RandomAccess}.
     *
     * 返回一个AbstractList的子类, 有两种类型一种是实现RandomAccess类
     * <p>The subclass's {@code set(int, E)}, {@code get(int)},
     * {@code add(int, E)}, {@code remove(int)}, {@code addAll(int,
     * Collection)} and {@code removeRange(int, int)} methods all
     * delegate to the corresponding methods on the backing abstract list,
     * after bounds-checking the index and adjusting for the offset.  The
     * {@code addAll(Collection c)} method merely returns {@code addAll(size,
     * c)}.
     * 子类一些方法都是委派AbstractList去执行
     *
     * <p>The {@code listIterator(int)} method returns a "wrapper object"
     * over a list iterator on the backing list, which is created with the
     * corresponding method on the backing list.  The {@code iterator} method
     * merely returns {@code listIterator()}, and the {@code size} method
     * merely returns the subclass's {@code size} field.
     *
     * <p>All methods first check to see if the actual {@code modCount} of
     * the backing list is equal to its expected value, and throw a
     * {@code ConcurrentModificationException} if it is not.
     *
     * @throws IndexOutOfBoundsException if an endpoint index value is out of range
     *         {@code (fromIndex < 0 || toIndex > size)}
     * @throws IllegalArgumentException if the endpoint indices are out of order
     *         {@code (fromIndex > toIndex)}
     */
    public List<E> subList(int fromIndex, int toIndex) {
        return (this instanceof RandomAccess ?  //是否实现RandomAcess创建不同子List
                new RandomAccessSubList<>(this, fromIndex, toIndex) :
                new SubList<>(this, fromIndex, toIndex));
    }

    // Comparison and hashing

    /**
     * Compares the specified object with this list for equality.  Returns
     * {@code true} if and only if the specified object is also a list, both
     * lists have the same size, and all corresponding pairs of elements in
     * the two lists are <i>equal</i>.  (Two elements {@code e1} and
     * {@code e2} are <i>equal</i> if {@code (e1==null ? e2==null :
     * e1.equals(e2))}.)  In other words, two lists are defined to be
     * equal if they contain the same elements in the same order.<p>
     * 相等两个list集合必须是有相同的元素和顺序
     *
     * This implementation first checks if the specified object is this
     * list. If so, it returns {@code true}; if not, it checks if the
     * specified object is a list. If not, it returns {@code false}; if so,
     * it iterates over both lists, comparing corresponding pairs of elements.
     * If any comparison returns {@code false}, this method returns
     * {@code false}.  If either iterator runs out of elements before the
     * other it returns {@code false} (as the lists are of unequal length);
     * otherwise it returns {@code true} when the iterations complete.
     *
     * @param o the object to be compared for equality with this list
     * @return {@code true} if the specified object is equal to this list
     */
    public boolean equals(Object o) {
        if (o == this) //判断是不是本对象
            return true;
        if (!(o instanceof List)) //判断是否是list (只要List子类就是可以的）
            return false;

        ListIterator<E> e1 = listIterator(); //获取本对象list迭代器
        ListIterator<?> e2 = ((List<?>) o).listIterator(); // 获取传入对象迭代器
        while (e1.hasNext() && e2.hasNext()) { //如果他们顺序是否有下一个（也就是保证顺序一致）
            E o1 = e1.next();
            Object o2 = e2.next();
            if (!(o1==null ? o2==null : o1.equals(o2))) //只要一个不满足要求就是返回false
                return false;
        }
        return !(e1.hasNext() || e2.hasNext()); //表示两个集合的元素的个数不相等
    }

    /**
     * Returns the hash code value for this list.
     *
     * <p>This implementation uses exactly the code that is used to define the
     * list hash function in the documentation for the {@link List#hashCode}
     * method.
     *
     * @return the hash code value for this list
     */
    public int hashCode() { //实现hash的方法
        int hashCode = 1;
        for (E e : this)
            hashCode = 31*hashCode + (e==null ? 0 : e.hashCode());  //相同长度元素list集合的hashCode是相同的
        return hashCode;
    }

    /**
     * Removes from this list all of the elements whose index is between
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     * Shifts any succeeding elements to the left (reduces their index).
     * This call shortens the list by {@code (toIndex - fromIndex)} elements.
     * (If {@code toIndex==fromIndex}, this operation has no effect.)
     *
     * [开始索引，借宿索引)  前包后不包
     * <p>This method is called by the {@code clear} operation on this list
     * and its subLists.  Overriding this method to take advantage of
     * the internals of the list implementation can <i>substantially</i>
     * improve the performance of the {@code clear} operation on this list
     * and its subLists.
     *
     * <p>This implementation gets a list iterator positioned before
     * {@code fromIndex}, and repeatedly calls {@code ListIterator.next}
     * followed by {@code ListIterator.remove} until the entire range has
     * been removed.  <b>Note: if {@code ListIterator.remove} requires linear
     * time, this implementation requires quadratic time.</b>
     * ListIterator.remove需要线性时间，而这个方法需要二次方时间
     * @param fromIndex index of first element to be removed
     * @param toIndex index after last element to be removed
     */
    protected void removeRange(int fromIndex, int toIndex) {
        ListIterator<E> it = listIterator(fromIndex);
        for (int i=0, n=toIndex-fromIndex; i<n; i++) {
            it.next();
            it.remove();
        }
    }

    /**
     * The number of times this list has been <i>structurally modified</i>.
     * Structural modifications are those that change the size of the
     * list, or otherwise perturb【烦恼） it in such a fashion that iterations in
     * progress may yield incorrect results.
     * 结构上修改
     *
     * <p>This field is used by the iterator and list iterator implementation
     * returned by the {@code iterator} and {@code listIterator} methods.
     * If the value of this field changes unexpectedly, the iterator (or list
     * iterator) will throw a {@code ConcurrentModificationException} in
     * response to the {@code next}, {@code remove}, {@code previous},
     * {@code set} or {@code add} operations.  This provides
     * <i>fail-fast</i> behavior, rather than non-deterministic behavior in
     * the face of concurrent modification during iteration.
     *
     * <p><b>Use of this field by subclasses is optional.</b> If a subclass
     * wishes to provide fail-fast iterators (and list iterators), then it
     * merely has to increment this field in its {@code add(int, E)} and
     * {@code remove(int)} methods (and any other methods that it overrides
     * that result in structural modifications to the list).  A single call to
     * {@code add(int, E)} or {@code remove(int)} must add no more than
     * one to this field, or the iterators (and list iterators) will throw
     * bogus【伪造】 {@code ConcurrentModificationExceptions}.  If an implementation
     * does not wish to provide fail-fast iterators, this field may be
     * ignored.
     */
    protected transient int modCount = 0;

    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > size())
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size();
    } //数组越界
}

class SubList<E> extends AbstractList<E> { //子list类
    private final AbstractList<E> l;  //定义最终类
    private final int offset; //定义偏移
    private int size; //定义大小

    SubList(AbstractList<E> list, int fromIndex, int toIndex) {
        if (fromIndex < 0) //开始索引 小于0
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > list.size())//结束索引大于集合最大长度
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex) //开始索引大于结束索引
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                               ") > toIndex(" + toIndex + ")");
        l = list; //赋值给成员变量
        offset = fromIndex; //offset 赋值给开始索引
        size = toIndex - fromIndex;
        this.modCount = l.modCount;//防止并发修改
    }

    public E set(int index, E element) { //重写set方法
        rangeCheck(index);  //检查索引是否越界
        checkForComodification(); //检查是否并发问题
        return l.set(index+offset, element); //设置索引+偏移量
    }

    public E get(int index) { //获取索引的值
        rangeCheck(index); //检查索引是否越界
        checkForComodification(); //检查是否并发问题
        return l.get(index+offset);// 获取索引的值
    }

    public int size() {
        checkForComodification();//检查并发问题
        return size;//返回子list的长度
    }

    public void add(int index, E element) {// 添加元素
        rangeCheckForAdd(index); // 检查数据越界
        checkForComodification(); //检查并发问题
        l.add(index+offset, element); //
        this.modCount = l.modCount;  //复位
        size++; //长度增加
    }

    public E remove(int index) {
        rangeCheck(index);
        checkForComodification();
        E result = l.remove(index+offset);
        this.modCount = l.modCount; //移除也需要复位
        size--;
        return result;
    }

    protected void removeRange(int fromIndex, int toIndex) {
        checkForComodification();
        l.removeRange(fromIndex+offset, toIndex+offset); //删除元素
        this.modCount = l.modCount;
        size -= (toIndex-fromIndex);//长度也要减去
    }

    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    } //添加元素

    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);
        int cSize = c.size(); //需要添加元素大小
        if (cSize==0) //如果为0直接返回false
            return false;

        checkForComodification(); //检查并发修改问题
        l.addAll(offset+index, c); //添加元素
        this.modCount = l.modCount; //复位
        size += cSize; //list长度增加
        return true;
    }

    public Iterator<E> iterator() {
        return listIterator();
    } //获取迭代器

    public ListIterator<E> listIterator(final int index) {//
        checkForComodification();//检查并发修改问题
        rangeCheckForAdd(index); //索引是否越界

        return new ListIterator<E>() {
            private final ListIterator<E> i = l.listIterator(index+offset); //初始化迭代器

            public boolean hasNext() {
                return nextIndex() < size;
            } //判断是否有下一个

            public E next() { //获取下一个
                if (hasNext())
                    return i.next();
                else
                    throw new NoSuchElementException();
            }

            public boolean hasPrevious() {
                return previousIndex() >= 0;
            } //是否有上一个

            public E previous() {  //获取上一次元素
                if (hasPrevious())
                    return i.previous();
                else
                    throw new NoSuchElementException();
            }

            public int nextIndex() {
                return i.nextIndex() - offset;
            } //下一个索引位置

            public int previousIndex() {
                return i.previousIndex() - offset;
            } //先前索引位置

            public void remove() { //移除元素
                i.remove();
                SubList.this.modCount = l.modCount;//复位
                size--; //长度减一
            }

            public void set(E e) {
                i.set(e);
            } //设置大小

            public void add(E e) {
                i.add(e);
                SubList.this.modCount = l.modCount; //变更大小都是需要复位
                size++;
            }
        };
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return new SubList<>(this, fromIndex, toIndex);
    }

    private void rangeCheck(int index) { //判断是否越界
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void rangeCheckForAdd(int index) { //判断索引是否越界
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }//异常

    private void checkForComodification() {//判断是否并发
        if (this.modCount != l.modCount)
            throw new ConcurrentModificationException();
    }
}

class RandomAccessSubList<E> extends SubList<E> implements RandomAccess {
    RandomAccessSubList(AbstractList<E> list, int fromIndex, int toIndex) {
        super(list, fromIndex, toIndex);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return new RandomAccessSubList<>(this, fromIndex, toIndex);
    }
}
