package com.example.javajdk;

import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static com.sun.xml.internal.fastinfoset.util.ValueArray.MAXIMUM_CAPACITY;

/**
 * @ClassName MapLearn
 * @Description HashMap学习
 * @Author chen.liang
 * @Date 2018/11/13 10:41
 * @Version 1.0
 *https://blog.csdn.net/qpzkobe/article/details/79533237
 *
 * 1、instanceof
 * x instanceof Comparable    (继承链上实现的接口)
 * 2、getClass()
 * c= x.getClass()  getClass()返回只是运行时类型
 * 3、 getGenericInterfaces()
 * ts = c.getGenericInterfaces()   返回直接实现的接口
 * 4、getGenericSuperclass() 和 getSuperclass()
 *
 * getSuperclass(): 返回的是直接父类的类型，不包括泛型参数
 * getGenericSuperclass 包括泛型参数在内的直接父类
 *
 **/

public class MapLearn {

    public static void main(String[] args) {
        System.out.println(comparableClassFor(new A()));    // null,A does not implement Comparable.
        System.out.println(comparableClassFor(new B()));    // null,B implements Comparable, compare to Object.
        System.out.println(comparableClassFor(new C()));    // class Demo$C,C implements Comparable, compare to itself.
        System.out.println(comparableClassFor(new D()));    // null,D implements Comparable, compare to its sub type.
        System.out.println(comparableClassFor(new F()));    // null,F is C's sub type.
        System.out.println(C.class.getGenericSuperclass());

    }

    static class A{}

    static class B implements Comparable<Object> {

        @Override
        public int compareTo(Object o) {
            return 0;
        }
    }

    static class C implements Comparable<C> {
        @Override
        public int compareTo(C o) {
            return 0;
        }
    }

    static class D implements Comparable<E> {

        @Override
        public int compareTo(E o) {
            return 0;
        }
    }

    static class E{}

    static  class F extends C{}

    /**
     * Returns x's Class if it is of the form "class C implements
     * Comparable<C>", else null.
     */
    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) { //判断是否实现了Comparable接口
            Class<?> c; Type[] ts, as; Type t; ParameterizedType p;
            if ((c = x.getClass()) == String.class) // bypass checks
                return c;//如果是String类型，直接返回String.class
            if ((ts = c.getGenericInterfaces()) != null) { //判断是否有直接实现的接口
                for (int i = 0; i < ts.length; ++i) { //遍历直接实现的接口
                    if (((t = ts[i]) instanceof ParameterizedType) && //该接口实现了泛型
                            ((p = (ParameterizedType)t).getRawType() == //获取接口不带参数部分的
                                    Comparable.class) && //该类型是为Comparable
                            (as = p.getActualTypeArguments()) != null &&  //获取泛型参数数组
                            as.length == 1 && as[0] == c) // 只有一个参数，且改实现类型是该类型
                        return c;
                }
            }
        }
        return null;
    }

    /**
     * 测试ParameterizedType 是 Type 接口的子接口，表示参数的类型，即实现了泛型参数的类型
     *
     */
    //==============================================ParameterizedType=========================
    class Grand{}
    class Super<A,B> extends Grand{}
    class Child extends Super<String, String >{}
    class Child2<A, B> extends Super<A,B>{}
    class Child3<A,B> extends Super{}

    private void testChild() {
        Grand child1 = new Child();
        Grand child2_1 = new Child2();
        Grand child2_2 = new Child2<String, String>();
        Child2<String , String> child2_3= new Child2<>();
        Child3<String, String> child3 = new Child3<>();
        System.out.println(child1 instanceof ParameterizedType);  //false
        System.out.println(child2_1 instanceof ParameterizedType);//false
        System.out.println(child2_2 instanceof ParameterizedType);//false
        System.out.println(child2_3 instanceof ParameterizedType);//false
        System.out.println(child1.getClass().getGenericSuperclass() instanceof  ParameterizedType);// true
        System.out.println(child2_1.getClass().getGenericSuperclass() instanceof  ParameterizedType);// true
        System.out.println(child3.getClass().getGenericSuperclass() instanceof  ParameterizedType);// true
        //System.out.println(child1.getClass() instanceof  ParameterizedType);// comply ERROR
    }


    interface IG<X,Y>{}
    interface IA<X,Y>{}
    interface IB extends IG{}
    interface  IC <X> {}
    interface ID<X> {}
    class Grand2<X> implements IA<String, String>, IB,IC<X>,ID{}

    private void testParameterizedType() {
        Grand2 grand2 = new Grand2();
        Type[] types = grand2.getClass().getGenericInterfaces();
        if (types != null) {
            for (Type type : types) {
                System.out.println(type.getTypeName() + " " + (type instanceof  ParameterizedType)  );
                //Output result:
                //IA<java.lang.String, java.lang.Integer> true
                //IB false
                //IC <X> true
                //ID false
            }
        }
    }

    /**
     * getRawType()    返回声明了这个类型的类或接口，也就是去掉了泛型参数部分的类型对象
     * get
     * （p= (ParameterizedType)t）.getRawType()
     */

    private void testRawType() {
        Grand2 grand2 = new Grand2();
        Type[] types = grand2.getClass().getGenericInterfaces();
        if (types != null) {
            for (Type type : types) {
                if (type instanceof ParameterizedType) {
                    System.out.println(((ParameterizedType)type).getRawType());
                    //Output result;
                    //interface test.IA
                    //interface test.IC

                    System.out.println(type.getTypeName());
                    Type[] arguments = ((ParameterizedType) type).getActualTypeArguments();
                    if (arguments != null) {
                        for (Type argument : arguments) {
                            System.out.println(argument.getTypeName());
                            //Output result:
                            //IA<java.lang.String, java.lang.Integer>
                            // java.lang.String
                            // java.lang.Integer
                            //IC<X>
                            //X
                        }
                    }
                }
            }
        }
    }
    //==============================================ParameterizedType=========================

    /**
     * getOwnerType() ：  如果改类型是一个内部接口/类，返回它的外部类接口，如果该类型不是内部接口/类，返回null
     *
     */

    class Outer<X>{
        class Inner<Y> {

        }
        class Child<Y> extends Inner<Y>{}
    }

    private void testOwnerType() {
        Outer<String> outer = new Outer<>();
        Outer<String>.Child<Integer> child = outer.new Child<Integer>();
        Type type = child.getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            System.out.println(((ParameterizedType)type).getOwnerType()); // Outer<X>
        }
    }


    @Test
    public void testTableSizeFor() {
        int i = tableSizeFor(9);
        System.out.println(i);
    }

    /**
     * 返回两倍容量， 当然是 1 2 4 8 16 32 64   （超过就扩容）
     * @param cap
     * @return
     */
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /**
     * 测试HashMap(Map<? extends K, ? extends V> m)
     */
    @Test
    public void  mapConstructor() {
        Map<String, String> map = new HashMap<>(8);
        map.put("111", "22222");
        map.put("222", "22222");
        map.put("3333", "22222");
        HashMap<String, String> h = new HashMap<>(map);
        int size = h.size();
        System.out.println(size);
    }
/*
    final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
        int s = m.size(); //获取map的大小
        if (s > 0) {  //map有值进行处理
            if (table == null) { //  红黑树表为空
                float ft = ((float)s / loadFactor) + 1.0F;  //当前大小是否满足扩容需求，为了设置一个阈值，加一是为下次触发扩容的大小
                int t = ((ft < (float)MAXIMUM_CAPACITY) ?//t最大不超过MAXINUM_CAPACITY
                        (int)ft : MAXIMUM_CAPACITY);
                if (t > threshold)  //如果大于当前阈值需要进行扩容
                    threshold = tableSizeFor(t);
            }
            else if (s > threshold)  //红黑树table不为空，并且大小超过当前阈值，需要重新扩容和变换红黑树
                resize();
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) { //将m的原来的键值对放入当前map中（重新计算hash值）
                K key = e.getKey();
                V value = e.getValue();
                putVal(hash(key), key, value, false, evict);
            }
        }
    }
    */

    /**
     *
     //获取对应key的value， 也可能返回null，返回null 有两种情况一种是本身就是没有这个key，第二种，这个key对应的value就是null ， 如果是区分某个key 是否存在
     //一般用contain(key)更好
    public V get(Object key) {
        HashMap.Node<K,V> e;  // map对应key-value 是node节点
        return (e = getNode(hash(key), key)) == null ? null : e.value;  //获取对应的节点然后获取值
    }
     *
     *
     */
}
