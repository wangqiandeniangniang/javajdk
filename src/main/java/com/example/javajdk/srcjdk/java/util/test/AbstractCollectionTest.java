package com.example.javajdk.srcjdk.java.util.test;

/**
 * @ClassName AbstractCollectionTest
 * @Description TODO
 * @Author chen.liang
 * @Date 2018/11/22 16:14
 * @Version 1.0
 **/
public class AbstractCollectionTest {


    public static void main(String[] args) {
        getArrayExpand(1);
    }
    /**
     * 获取cap + (cap >> 1) + 1 的值
     */
    private static void getArrayExpand(int cap) {
        cap = cap + (cap >> 1) + 1;
        System.out.println("cap = " + cap);
    }
}
