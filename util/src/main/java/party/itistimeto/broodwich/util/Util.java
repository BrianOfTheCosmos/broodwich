package party.itistimeto.broodwich.util;

import java.io.*;
import java.lang.reflect.Field;

public class Util {
    public static Object getField(Object instance, String fieldName) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(instance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setField(Object instance, String fieldName, Object newValue) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, newValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static byte[] serialize(Object obj) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            new ObjectOutputStream(bos).writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }

    public static Object deserialize(byte[] bytes) {
        try {
            return new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] concatByteArr(byte[] a, byte[] b) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            bos.write(a);
            bos.write(b);
            return bos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public static String classToResource(Class clazz) {
        return clazz.getName().replace('.', '/') + ".class";
    }

    public static String getResourceText(String resourceName) throws IOException {
        return new String(getResourceBytes(resourceName));
    }

    public static byte[] getResourceBytes(String resourceName) throws IOException {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName).readAllBytes();
    }
}
