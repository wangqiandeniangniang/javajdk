package com.example.javajdk.srcjdk.java.util.test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @ClassName AbstractListTest
 * @Description TODO
 * @Author chen.liang
 * @Date 2018/11/26 17:43
 * @Version 1.0
 **/
public class AbstractListTest {
    public static void main(String[] args) {
//        getArrayExpand(Integer.MAX_VALUE/3 * 2 );
//        getArrayReflect();
//        getIndexOf();
//        gequal();
//        hashdoe();
        listCode();
    }
    /**
     * 获取cap + (cap >> 1) + 1 的值
     */
    public static void getArrayExpand(int cap) {
        System.out.println("cap >> 1 : " + (cap >> 1));
        cap = cap + (cap >> 1) + 1;
        System.out.println("cap = " + cap);
    }

    public static void getArrayReflect() {
        String[] dd = (String[]) Array.newInstance(String.class, 10);
        dd[0] = "12123";
        System.out.println(Arrays.toString(dd));
    }

    public static void getIndexOf(){
        List<String> list = new ArrayList<>();
        list.add("8");
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        int i = list.indexOf("8");
    }


    /**
     * 不同类型集合判断是否相等 （顺序要一样）
     */
    public static void gequal(){
        List<String> list = new ArrayList<>();
        List<String> list1 = new LinkedList<>();
        list.add("8");
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list1.add("8");
        list1.add("1");
        list1.add("2");
        list1.add("3");
        list1.add("4");
        System.out.println(list.equals(list1));
        int i = list.indexOf("8");
    }


    /**
     * 元素个数相同，hashcode是否一致
     */
    public static void hashdoe(){
        List<String> list = new ArrayList<>();
        List<String> list1 = new LinkedList<>();
        list.add("8");
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list1.add("8");
        list1.add("1");
        list1.add("2");
        list1.add("3");
        list1.add("4");
        System.out.println(list.hashCode());
        System.out.println(list1.hashCode());
    }

    /**
     * 修改子list会影响到原来的list
     */
    public static void listCode(){
        List<String> list = new ArrayList<>();
        list.add("8");
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        List<String> list1 = list.subList(1, list.size());
        list1.set(1, "0");
        System.out.println(list.toString());
        System.out.println(list1.toString());
    }
}
