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

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 * This class provides a skeletal implementation of the <tt>Set</tt>
 * interface to minimize the effort required to implement this
 * interface. <p>
 * 这个set抽象类型最简单的实现
 *
 * The process of implementing a set by extending this class is identical
 * to that of implementing a Collection by extending AbstractCollection,
 * except that all of the methods and constructors in subclasses of this
 * class must obey the additional constraints imposed by the <tt>Set</tt>
 * interface (for instance, the add method must not permit addition of
 * multiple instances of an object to a set).<p>
 *  他扩展了AbstractCollection
 *
 * Note that this class does not override any of the implementations from
 * the <tt>AbstractCollection</tt> class.  It merely adds implementations
 * for <tt>equals</tt> and <tt>hashCode</tt>.<p>
 *
 * 这个类没有重写AbstractCollection类的任何的方法，只是实现了equals和hashCode方法
 * This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @param <E> the type of elements maintained by this set
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see Collection
 * @see AbstractCollection
 * @see Set
 * @since 1.2
 */

public abstract class AbstractSet<E> extends AbstractCollection<E> implements Set<E> {
    /**
     * Sole constructor.  (For invocation by subclass constructors, typically
     * implicit.)
     */
    protected AbstractSet() { //无参数的构造方法
    }

    // Comparison and hashing

    /**
     * Compares the specified object with this set for equality.  Returns
     * <tt>true</tt> if the given object is also a set, the two sets have
     * the same size, and every member of the given set is contained in
     * this set.  This ensures that the <tt>equals</tt> method works
     * properly across different implementations of the <tt>Set</tt>
     * interface.<p>
     *
     * 判断两个SET集合是否相等，长度、和set的子集，每个元素都相等
     * This implementation first checks if the specified object is this
     * set; if so it returns <tt>true</tt>.  Then, it checks if the
     * specified object is a set whose size is identical to the size of
     * this set; if not, it returns false.  If so, it returns
     * <tt>containsAll((Collection) o)</tt>.
     *
     * @param o object to be compared for equality with this set
     * @return <tt>true</tt> if the specified object is equal to this set
     */
    public boolean equals(Object o) {
        if (o == this)   //判断是否本对象
            return true;

        if (!(o instanceof Set)) //是否set子集
            return false;
        Collection<?> c = (Collection<?>) o;
        if (c.size() != size())  //判断大小
            return false;
        try {
            return containsAll(c); //所有元素是否相等
        } catch (ClassCastException unused)   {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
    }

    /**
     * Returns the hash code value for this set.  The hash code of a set is
     * defined to be the sum of the hash codes of the elements in the set,
     * where the hash code of a <tt>null</tt> element is defined to be zero.
     * This ensures that <tt>s1.equals(s2)</tt> implies that
     * <tt>s1.hashCode()==s2.hashCode()</tt> for any two sets <tt>s1</tt>
     * and <tt>s2</tt>, as required by the general contract of
     * {@link Object#hashCode}.
     *
     * 如果两个对象equals相等，那么hashCode应该相等， 如果set为null 那么hashcode为0
     * <p>This implementation iterates over the set, calling the
     * <tt>hashCode</tt> method on each element in the set, and adding up
     * the results.
     *
     * @return the hash code value for this set
     * @see Object#equals(Object)
     * @see Set#equals(Object)
     */
    public int hashCode() {
        int h = 0;
        Iterator<E> i = iterator();
        while (i.hasNext()) {
            E obj = i.next();
            if (obj != null)
                h += obj.hashCode();//累加
        }
        return h;
    }

    /**
     * Removes from this set all of its elements that are contained in the
     * specified collection (optional operation).  If the specified
     * collection is also a set, this operation effectively modifies this
     * set so that its value is the <i>asymmetric 【不对称的】set difference</i> of
     * the two sets.
     *
     *
     * <p>This implementation determines【确定】 which is the smaller of this set
     * and the specified collection, by invoking the <tt>size</tt>
     * method on each.  If this set has fewer elements, then the
     * implementation iterates over this set, checking each element
     * returned by the iterator in turn to see if it is contained in
     * the specified collection.  If it is so contained, it is removed
     * from this set with the iterator's <tt>remove</tt> method.  If
     * the specified collection has fewer elements, then the
     * implementation iterates over the specified collection, removing
     * from this set each element returned by the iterator, using this
     * set's <tt>remove</tt> method.
     *
     * <p>Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by the
     * <tt>iterator</tt> method does not implement the <tt>remove</tt> method.
     * 必须实现remove方法， 否则抛出UnsupportedException
     * @param  c collection containing elements to be removed from this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>removeAll</tt> operation
     *         is not supported by this set
     * @throws ClassCastException if the class of an element of this set
     *         is incompatible with the specified collection
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this set contains a null element and the
     *         specified collection does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>),
     *         or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;

        if (size() > c.size()) {
            for (Iterator<?> i = c.iterator(); i.hasNext(); )
                modified |= remove(i.next());
        } else {
            for (Iterator<?> i = iterator(); i.hasNext(); ) {
                if (c.contains(i.next())) {
                    i.remove();
                    modified = true;
                }
            }
        }
        return modified;
    }

}
