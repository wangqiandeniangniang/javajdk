package com.example.javajdk.srcjdk.java.util.arrayList;

import java.io.*;
import java.util.ArrayList;
import java.util.*;

/**
 * @ClassName ArrayListTest
 * @Description ArrayList测试
 * @Author chen.liang
 * @Date 2018/12/6 11:45
 * @Version 1.0
 **/
public class ArrayListTest {

    private static Collection<Object> list = Arrays.asList("1211","1212132","2131314");
    public static void main(String[] args) throws Exception{
//        testArrayListContruct();
//        testDefaultList();
//        testShallowCopy();
//        testToArray();
//        testSerializable();
//        testtrySplit();
//        testRemoveIf();
//        testReplaceAll();
        testSort();
    }


    /**
     * ArrayList(Collection<? extends E> c)
     * 测试不是返回Object[]的元素将将会转成Object[]返回
     *
     */

    public static void  testArrayListContruct() {

        Object[] array = Arrays.asList("A").toArray();
        System.out.println(array.getClass());
//        array[0] = new Object(); // cause ArrayStoreException
        ArrayList ddd = new ArrayList<>(list);
        System.out.println(Arrays.toString(ddd.toArray()));
    }

    /**
     * 测试创建List,长度声明了，但是没有初始化，会抛出异常
     */

    public static void testDefaultList() {
        ArrayList<Object> objects = new ArrayList<>(55);
        System.out.println(objects.contains(null));
        System.out.println(objects.get(1));

    }

    /**
     * 测试ArrayList浅拷贝 ,浅拷贝会影响到原来对象, 其实拷贝是栈内存的数据，拷贝在堆内存的本身对象
     */
    public static void testShallowCopy() {
        ArrayList<Person> list = new ArrayList<>(3);
        ArrayList arrayList = new ArrayList();
        arrayList.add(new Person("小王", 32));
        list.add(new Person("小明", 20,arrayList));
        list.add(new Person("小王", 32,arrayList));
        list.add(new Person("小吴", 21,arrayList));
        System.out.println(Arrays.toString(list.toArray()));
        ArrayList<Person>  clone = (ArrayList<Person>) list.clone();
        list.get(0).setName("喜是是是");
        System.out.println(Arrays.toString(clone.toArray()));
        System.out.println(list.get(0) == clone.get(0));
        System.out.println(list.size() );
        System.out.println(clone.size() );
        System.out.println(clone);
        System.out.println(list);

    }

    /**
     * 测试ArrayList方法 toArray方法，的元素修改是会影响到原来的List, 数组对象复制不会改变元素不会拷贝，只会索引拷贝
     */
    public static void testToArray() {
        ArrayList<Person> list = new ArrayList<>(3);
        ArrayList arrayList = new ArrayList();
        arrayList.add(new Person("小王", 32));
        list.add(new Person("小明", 20,arrayList));
        list.add(new Person("小王", 32,arrayList));
        list.add(new Person("小吴", 21,arrayList));
        Object[] objects = list.toArray();
       Person people = (Person) objects[0];
       people.setName("仙男");
        System.out.println(Arrays.toString(objects));
        System.out.println(Arrays.toString(list.toArray()));
        
        Person [] people1 = new Person[list.size()];
        Person [] people2 = new Person[list.size()];
        people1 = list.toArray(people1);
        people1[0].setName("meiiiiiii");
         System.arraycopy(people1,1,people2,0,people1.length-2);
        people2[0].setName("hhhhhh");

        Person [] people3 = new Person[3];

        for (int i = 0; i < people1.length; i++) {
            people3[i] = people1[i];
        }
        people3[0].setName("ssssss");

    }

    /**
     * 测试ArrayList 序列化
     */
    public static void testSerializable() throws IOException, ClassNotFoundException {
        ArrayList<Person> list = new ArrayList<>(3);
        ArrayList arrayList = new ArrayList();
        arrayList.add(new Person("小王", 32));
        list.add(new Person("小明", 20,arrayList));
        list.add(new Person("小王", 32,arrayList));
        list.add(new Person("小吴", 21,arrayList));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
        outputStream.writeObject(list);

        ListIterator<Person> personListIterator = list.listIterator(1);
        Person next = personListIterator.next();
        Person previous = personListIterator.previous();
        //反序列化
        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

        ArrayList<Person> o = (ArrayList<Person>) inputStream.readObject();
        System.out.println(Arrays.toString(o.toArray()));

    }


    /**
     * 测试ArrayList trySplit获取前半部分的list
     */
    public static void testtrySplit()   {
        ArrayList<Person> list = new ArrayList<>(3);
        ArrayList arrayList = new ArrayList();
        arrayList.add(new Person("小王", 32));
        list.add(new Person("小明", 20,arrayList));
        list.add(new Person("小王", 32,arrayList));
        list.add(new Person("小吴", 21,arrayList));
        list.add(new Person("苏苏", 21,arrayList));
        Spliterator<Person> personSpliterator = list.spliterator().trySplit();
        personSpliterator.forEachRemaining(x-> System.out.println(x.toString())); //小明、小王
        boolean advance = personSpliterator.tryAdvance(x -> System.out.println(x.toString())); //上面已经遍历完了，所以这里没有输出
        Spliterator<Person> spliterator = list.subList(list.size() / 2, list.size()).spliterator().trySplit();
        spliterator.forEachRemaining(x-> System.out.println(x.toString())); //小吴
        System.out.println(personSpliterator.estimateSize());  // 0


    }
    //removeIf

    /**
     * 测试ArrayList removeIf 移除元素
     */
    public static void testRemoveIf()  {
        ArrayList<Person> list = new ArrayList<>(3);
        ArrayList arrayList = new ArrayList();
        arrayList.add(new Person("小王", 32));
        list.add(new Person("小明", 20,arrayList));
        list.add(new Person("小王", 32,arrayList));
        list.add(new Person("小吴", 21,arrayList));
        list.add(new Person("苏苏", 21,arrayList));
       /*list.removeIf(x -> {
          return x.getAge() > 20;
       });*/
       list.removeIf(x -> x.getAge() > 20); //底层采用BitSet进行标记
        System.out.println(Arrays.toString(list.toArray()));

    }

    //replaceAll
    /**
     * 测试ArrayList replaceAll 替换元素
     */
    public static void testReplaceAll()  {
        ArrayList<Person> list = new ArrayList<>(3);
        ArrayList arrayList = new ArrayList();
        arrayList.add(new Person("小王", 32));
        list.add(new Person("小明", 20,arrayList));
        list.add(new Person("小王", 32,arrayList));
        list.add(new Person("小吴", 21,arrayList));
        list.add(new Person("苏苏", 21,arrayList));
        list.replaceAll(x -> {
            if (x.getName().startsWith("小")) {
                x.getName().replace("小", "大");
            }
            return  x;
        });
        System.out.println(Arrays.toString(list.toArray()));

    }


    /**
     * 测试ArrayList sort 排序元素
     */
    public static void testSort()  {
        ArrayList<Person> list = new ArrayList<>(3);
        ArrayList arrayList = new ArrayList();
        arrayList.add(new Person("小王", 32));
        list.add(new Person("小明", 20,arrayList));
        list.add(new Person("小王", 32,arrayList));
        list.add(new Person("小吴", 21,arrayList));
        list.add(new Person("苏苏", 21,arrayList));
        list.sort(new Comparator<Person>() {
            @Override
            public int compare(Person o1, Person o2) { //年龄排序
                if (o1==o2||o1.getAge()==o2.getAge()) {
                    return 0;
                } else if (o1.getAge() > o2.getAge()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        list.sort((o1, o2) ->{//年龄排序
            if (o1==o2||o1.getAge()==o2.getAge()) {
                return 0;
            } else if (o1.getAge() > o2.getAge()) {
                return 1;
            } else {
                return -1;
            }
        });
        System.out.println(Arrays.toString(list.toArray()));

    }
    static class Person implements Serializable  {
       private String name;
       private Integer age;

       private List<Person> persons;
        public Person(String name, Integer age , List<Person> persons) {
            this.name = name;
            this.age = age;
            this.persons=persons;
        }

        public Person(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }

        public List<Person> getPersons() {
            return persons;
        }

        public void setPersons(List<Person> persons) {
            this.persons = persons;
        }

    }
}
