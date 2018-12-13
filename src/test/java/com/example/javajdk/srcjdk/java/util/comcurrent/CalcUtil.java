package com.example.javajdk.srcjdk.java.util.comcurrent;

import java.math.BigInteger;

/**
 * @ClassName CalcUtil
 * @Description 计算工具类
 * @Author chen.liang
 * @Date 2018/12/12 20:34
 * @Version 1.0
 **/
public class CalcUtil {

    public static BigInteger calculateFactorial(BigInteger input) {
        BigInteger factorial = BigInteger.ONE;
        for (BigInteger i = BigInteger.ONE; i.compareTo(input) <= 0; i = i.add(BigInteger.ONE)) {
            factorial = factorial.multiply(i);
        }
        return factorial;
    }
}
