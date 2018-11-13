package com.example.javajdk;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @ClassName SerializableObject
 * @Description TODO
 * @Author chen.liang
 * @Date 2018/11/13 16:33
 * @Version 1.0
 **/
public class SerializableObject implements Serializable {

    private String str0;
    private transient  String str1;
    private static String str2 = "aaaaa";

    public SerializableObject(String str0, String str1) {
        this.str0 = str0;
        this.str1 = str1;
    }

    public String getStr0() {
        return str0;
    }

    public String getStr1() {
        return str1;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        System.out.println("自己控制序列化的过程");
        stream.defaultWriteObject();
        stream.writeInt(str1.length());
        for (int i = 0; i < str1.length(); i++) {
            stream.writeChar(str1.charAt(i));
        }
    }

    private void readObject(ObjectInputStream inputStream) throws Exception {
        System.out.println("自己控制反序列化的过程");
        inputStream.defaultReadObject();
        int length = inputStream.readInt();
        char[] cs = new char[length];
        for (int i = 0; i < length; i++) {
            cs[i] = inputStream.readChar();
        }
        str1 = new String(cs, 0, length);
    }
}
