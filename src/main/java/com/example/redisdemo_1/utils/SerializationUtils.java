package com.example.redisdemo_1.utils;

import lombok.Getter;
import lombok.Setter;

import java.io.*;

/**
 * 序列化工具类
 */
public class SerializationUtils {

    /**
     * 序列化
     * 将对象序列化到一个文件中
     *
     * @param obj
     * @throws IOException
     */
    public static String serialize(Object obj) throws IOException {
        try (
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(byteOut)
        ) {
            oos.writeObject(obj);
            byte[] bytes = byteOut.toByteArray();
            return bytes.toString();
        }
    }

    /**
     * 反序列化
     *
     * @param src
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object deserialize(String src) throws IOException, ClassNotFoundException {
        byte[] bytes = src.getBytes();
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return ois.readObject();
        }
    }

    public static void main(String[] args) {
//        try {
//            System.out.println(SerializationUtils.serialize(new User("zhangsan", 18, "123456")));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        try {
            System.out.println(SerializationUtils.deserialize("[B@61064425"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

class User implements Serializable {

    //被 transient 修饰的字段（如示例中的 password）不会被序列化，反序列化后会恢复为默认值（如 null、0）。
    //序列化版本号
    @Getter
    private transient long serializeVersionUID = 1L;
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private int age;
    @Setter
    @Getter
    private transient String password;

    //构造方法，getter,setter
    public User(String name, int age, String password) {
        this.name = name;
        this.age = age;
        this.password = password;
    }


    @Override
    public String toString() {
        return "User{" +
                "serializeVersionUID=" + serializeVersionUID +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", password='" + password + '\'' +
                '}';
    }
}