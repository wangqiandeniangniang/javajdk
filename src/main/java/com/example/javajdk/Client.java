package com.example.javajdk;

import java.io.*;

/**
 * @ClassName Client
 * @Description TODO
 * @Author chen.liang
 * @Date 2018/11/13 16:55
 * @Version 1.0
 **/
public class Client {
    public static void main(String[] args) throws Exception {
        File file = new File("s.txt");
        System.out.println( file.getAbsolutePath());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
        outputStream.writeObject(new SerializableObject("AAAAA", "BBBBB"));
        outputStream.close();


        InputStream is = new FileInputStream(file);
        ObjectInputStream inputStream = new ObjectInputStream(is);
        SerializableObject o = (SerializableObject)inputStream.readObject();
        System.out.println("str0=" + o.getStr0());
        System.out.println("str1=" + o.getStr1());
        inputStream.close();
    }
}
